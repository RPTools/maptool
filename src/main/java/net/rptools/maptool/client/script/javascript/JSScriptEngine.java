/*
 * This software Copyright by the RPTools.net development team, and
 * licensed under the Affero GPL Version 3 or, at your option, any later
 * version.
 *
 * MapTool Source Code is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public
 * License * along with this source Code.  If not, please visit
 * <https://www.gnu.org/licenses/> and specifically the Affero license
 * text at <https://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.client.script.javascript;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.MacroJavaScriptBridge;
import net.rptools.maptool.client.script.javascript.api.*;
import net.rptools.maptool.language.I18N;
import net.rptools.parser.ParserException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.graalvm.polyglot.*;
import org.reflections.Reflections;

public class JSScriptEngine {
  private static final Logger log = LogManager.getLogger(JSScriptEngine.class);
  private static final JSScriptEngine jsScriptEngine = new JSScriptEngine();

  public static JSScriptEngine getInstance() {
    return jsScriptEngine;
  }

  private final HostAccess hostAccess;
  private final Map<String, JSContext> contexts = new HashMap<>();
  private final Map<String, JSContext> addOnContexts = new HashMap<>();
  private final Stack<JSContext> contextStack = new Stack<>();

  // region Cached API bindings to avoid redundant reflection.

  private record ApiDefinition(
      Class<? extends MapToolJSAPIInterface> type, MapToolJSAPIDefinition annotation) {}

  private List<ApiDefinition> apiDefinitions = null;

  // endregion

  private JSScriptEngine() {
    HostAccess.Builder habuilder = HostAccess.newBuilder();
    habuilder.allowAccessAnnotatedBy(HostAccess.Export.class);
    habuilder.allowArrayAccess(true);
    habuilder.allowListAccess(true);
    habuilder.allowMapAccess(true);
    habuilder.targetTypeMapping(
        Value.class, Object.class, (v) -> v.hasArrayElements(), (v) -> v.as(List.class));
    hostAccess = habuilder.build();
  }

  public JSContext getCurrentContext() {
    return contextStack.peek();
  }

  public boolean inTrustedContext() {
    if (contextStack.empty()) {
      return false;
    }
    return contextStack.peek().trusted();
  }

  public JSContext registerContext(String name, boolean trusted, boolean makeTrusted)
      throws ParserException {
    if (!trusted) {
      JSContext jc = contexts.get(name);
      if (jc != null) {
        throw new ParserException("Context " + name + " already exists");
      }
    }
    if (!trusted && makeTrusted) {
      throw new ParserException("Cannot make a trusted JS context from an untrusted context");
    }
    JSContext c = new JSContext(makeTrusted, makeContext(), name);
    contexts.put(name, c);
    return c;
  }

  public void removeContext(String name, boolean trusted) throws ParserException {
    if (trusted) {
      contexts.remove(name);
      return;
    }
    JSContext c = contexts.get(name);
    if (c == null || c.trusted()) {
      throw new ParserException(I18N.getText("macro.function.general.noPermJS", name));
    }
    contexts.remove(name);
  }

  public void resetContexts() {
    JSMacro.clear();
    contexts.clear();
    addOnContexts.clear();
  }

  public JSContext registerAddOnContext(String name) {
    JSContext c = new JSContext(true, makeContext(), name);
    addOnContexts.put(name, c);
    return c;
  }

  public void removeAddOnContext(String name) {
    addOnContexts.remove(name);
  }

  public boolean hasContext(String name) {
    return contexts.containsKey(name);
  }

  public boolean hasAddOnContext(String name) {
    return addOnContexts.containsKey(name);
  }

  public Collection<JSContext> getContexts() {
    return List.copyOf(contexts.values());
  }

  private List<ApiDefinition> getApiDefinitions() {
    if (apiDefinitions == null) {
      Reflections reflections = new Reflections("net.rptools.maptool.client.script.javascript.api");
      Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(MapToolJSAPIDefinition.class);

      var definitions = new ArrayList<ApiDefinition>();
      for (Class<?> apiClass : annotated) {
        Class<? extends MapToolJSAPIInterface> type;
        try {
          type = apiClass.asSubclass(MapToolJSAPIInterface.class);
        } catch (ClassCastException e) {
          log.error(
              "Could not add API object {} as it does not implement {}",
              apiClass.getName(),
              MapToolJSAPIInterface.class.getName());
          continue;
        }

        MapToolJSAPIDefinition annotation = type.getAnnotation(MapToolJSAPIDefinition.class);
        if (annotation == null) {
          log.error(
              "Unexpected error: annotation {} not present on {}",
              MapToolJSAPIDefinition.class.getName(),
              type.getName());
        }

        definitions.add(new ApiDefinition(type, annotation));
      }

      // Make sure out list doesn't change in the future.
      apiDefinitions = List.copyOf(definitions);
    }
    return apiDefinitions;
  }

  private Context makeContext() {
    Context context =
        Context.newBuilder("js")
            .allowHostAccess(hostAccess)
            .option("js.ecmascript-version", "2021")
            .build();
    Value bindings = context.getBindings("js");

    var apiDefinitions = getApiDefinitions();
    for (var definition : apiDefinitions) {
      MapToolJSAPIInterface apiObject;
      try {
        apiObject = definition.type().getDeclaredConstructor().newInstance();
      } catch (InstantiationException
          | IllegalAccessException
          | InvocationTargetException
          | NoSuchMethodException e) {
        log.error(
            "Could not instantiate API object of type {} using default constructor",
            definition.type().getName());
        continue;
      }

      try {
        bindings.putMember(definition.annotation().javaScriptVariableName(), apiObject);
      } catch (Exception e) {
        log.error("Could not bind API object of type {}", definition.type().getName(), e);
      }
    }

    return context;
  }

  public Value evalScript(String contextName, String script) throws ParserException {
    return evalScript(contextName, script, MapTool.getParser().isMacroTrusted());
  }

  public Value evalScript(String contextName, String script, boolean trusted)
      throws ParserException {
    if (contextName == null) {
      return evalAnonymous(script);
    }
    JSContext jc = contexts.get(contextName);
    if (jc == null) {
      jc = registerContext(contextName, trusted, trusted);
    }

    return evalScript(jc, script, trusted);
  }

  public Value evalAddOnScript(String contextName, String script, boolean trusted)
      throws ParserException {
    JSContext jc = addOnContexts.get(contextName);
    return evalScript(jc, script, trusted);
  }

  private Value evalScript(JSContext context, String script, boolean trusted)
      throws ParserException {

    if (context.trusted() && !trusted) {
      throw new ParserException(I18N.getText("macro.function.general.noPermJS", context.name()));
    }
    contextStack.push(context);
    try {
      return context.context().eval("js", script);
    } finally {
      contextStack.pop();
    }
  }

  public Object applyFunction(JSAPIRegisteredMacro macro, Object[] args) {
    contextStack.push(macro.context);
    Object[] modifiedArgs = new Object[args.length];
    for (int i = 0; i < args.length; i++) {
      modifiedArgs[i] = MacroJavaScriptBridge.getInstance().HostObjectToJavaScriptType(args[i]);
    }
    try {
      return macro.callable.apply(modifiedArgs);
    } finally {
      contextStack.pop();
    }
  }

  private Value evalAnonymous(String script) {

    StringBuilder wrapped = new StringBuilder();
    wrapped
        .append("(function() { var args = MTScript.getMTScriptCallingArgs(); ")
        .append(script)
        .append("})();");
    Context c = makeContext();
    JSContext jc = new JSContext(MapTool.getParser().isMacroTrusted(), c, "<anonymous>");
    contextStack.push(jc);
    try {
      return makeContext().eval("js", wrapped.toString());
    } finally {
      contextStack.pop();
    }
  }
}
