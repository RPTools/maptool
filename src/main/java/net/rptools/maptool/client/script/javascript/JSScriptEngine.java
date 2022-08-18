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
 * <http://www.gnu.org/licenses/> and specifically the Affero license
 * text at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.client.script.javascript;

import com.oracle.truffle.js.scriptengine.*;
import java.util.*;
import java.util.List;
import javax.script.*;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.MacroJavaScriptBridge;
import net.rptools.maptool.client.script.javascript.api.*;
import net.rptools.maptool.language.I18N;
import net.rptools.parser.ParserException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.graalvm.polyglot.*;
import org.graalvm.polyglot.HostAccess.*;
import org.reflections.Reflections;

public class JSScriptEngine {

  private static Context.Builder cbuilder;
  private static final JSScriptEngine jsScriptEngine = new JSScriptEngine();
  private static final Logger log = LogManager.getLogger(JSScriptEngine.class);
  private static final Map<String, JSContext> contexts = new HashMap<String, JSContext>();
  private static final Map<String, JSContext> addOnContexts = new HashMap<String, JSContext>();
  private static final Stack<JSContext> contextStack = new Stack<>();

  public static JSContext getCurrentContext() {
    return contextStack.peek();
  }

  public static boolean inTrustedContext() {
    if (JSScriptEngine.contextStack.empty()) {
      return false;
    }
    return JSScriptEngine.contextStack.peek().trusted();
  }

  private void registerAPIObject(Value bindings, MapToolJSAPIInterface apiObj) {
    MapToolJSAPIDefinition def = apiObj.getClass().getAnnotation(MapToolJSAPIDefinition.class);
    bindings.putMember(def.javaScriptVariableName(), apiObj);
  }

  private JSScriptEngine() {
    HostAccess.Builder habuilder = HostAccess.newBuilder();
    habuilder.allowAccessAnnotatedBy(HostAccess.Export.class);
    habuilder.allowArrayAccess(true);
    habuilder.allowListAccess(true);
    habuilder.allowMapAccess(true);
    habuilder.targetTypeMapping(
        Value.class, Object.class, (v) -> v.hasArrayElements(), (v) -> v.as(List.class));

    HostAccess access = habuilder.build();

    cbuilder = Context.newBuilder("js");
    cbuilder.allowHostAccess(access);
    cbuilder.option("js.ecmascript-version", "2021");
  }

  public static JSContext registerContext(String name, boolean trusted, boolean makeTrusted)
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
    JSContext c = new JSContext(makeTrusted, jsScriptEngine.makeContext(), name);
    contexts.put(name, c);
    return c;
  }

  public static void removeContext(String name, boolean trusted) throws ParserException {
    if (trusted) {
      contexts.remove(name);
      return;
    }
    JSContext c = contexts.get(name);
    if (c == null || c.trusted()) {
      throw new ParserException(I18N.getText("macro.function.general.noPermJS", name));
    }
    contexts.remove(name);
    return;
  }

  public static void resetContexts() {
    JSMacro.clear();
    contexts.clear();
    addOnContexts.clear();
  }

  public static JSContext registerAddOnContext(String name) {
    JSContext c = new JSContext(true, jsScriptEngine.makeContext(), name);
    addOnContexts.put(name, c);
    return c;
  }

  public static void removeAddOnContext(String name) {
    addOnContexts.remove(name);
  }

  public static boolean hasContext(String name) {
    return contexts.containsKey(name);
  }

  public static boolean hasAddOnContext(String name) {
    return addOnContexts.containsKey(name);
  }

  public static Set<JSContext> getContexts() {
    return new HashSet<>(contexts.values());
  }

  public Context makeContext() {
    Context context = cbuilder.build();
    Value bindings = context.getBindings("js");

    Reflections reflections = new Reflections("net.rptools.maptool.client.script.javascript.api");
    Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(MapToolJSAPIDefinition.class);

    for (Class<?> apiClass : annotated) {
      try {
        if (MapToolJSAPIInterface.class.isAssignableFrom(apiClass)) {
          registerAPIObject(bindings, (MapToolJSAPIInterface) apiClass.newInstance());
        } else {
          log.error("Could not add API object " + apiClass.getName() + " (missing interface)");
        }
      } catch (Exception e) {
        log.error("Could not add API object " + apiClass.getName(), e);
      }
    }
    return context;
  }

  public static JSScriptEngine getJSScriptEngine() {
    return jsScriptEngine;
  }

  public Value evalScript(String contextName, String script)
      throws ScriptException, ParserException {
    return evalScript(contextName, script, MapTool.getParser().isMacroTrusted());
  }

  public Value evalScript(String contextName, String script, boolean trusted)
      throws ScriptException, ParserException {
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
      throws ScriptException, ParserException {
    JSContext jc = addOnContexts.get(contextName);
    return evalScript(jc, script, trusted);
  }

  public Value evalScript(JSContext context, String script, boolean trusted)
      throws ScriptException, ParserException {

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

  public Value evalAnonymous(String script) throws ScriptException {

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
