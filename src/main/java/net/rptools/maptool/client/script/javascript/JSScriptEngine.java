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

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.rptools.maptool.client.functions.MacroJavaScriptBridge;
import net.rptools.maptool.client.script.javascript.api.*;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;
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
  private final Map<String, JSAPIRegisteredMacro> macros = new HashMap<>();
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

  public JSAPIRegisteredMacro registerMacro(String name, Function<Object, Object> callable) {
    var newMacro = new JSAPIRegisteredMacro(name, callable, getCurrentContext());
    macros.put(name, newMacro);
    return newMacro;
  }

  public Optional<JSAPIRegisteredMacro> getMacro(String name) {
    return Optional.ofNullable(macros.get(name));
  }

  private @Nullable Map<String, JSContext> getContextMap(ContextType type) {
    return switch (type) {
      case Anonymous -> null;
      case Regular -> contexts;
      case AddOn -> addOnContexts;
    };
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

  public void resetContexts() {
    macros.clear();
    contexts.clear();
    addOnContexts.clear();
  }

  public Optional<JSContext> getContext(String name, ContextType type) {
    var contextMap = getContextMap(type);
    if (contextMap == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(contextMap.get(name));
  }

  /**
   * Creates a new context if none already exists for the name and type.
   *
   * <p>If a context with the given name and type already exists, no new context is created and
   * {@code null} is returned. If you just need to make sure a context exists, and don't need to
   * handle the case of name collision, call {@link #ensureContext(String, ContextType, boolean)}.
   * Or if you need to create a new context regardless of whether one already exists, call {@link
   * #putContext(String, ContextType, boolean)}.
   *
   * @param name The name of the new context.
   * @param type The type of the next context.
   * @param makeTrusted Whether the new context should be trusted.
   * @return The newly created context, or {@code null} if one already exists.
   */
  public @Nullable JSContext createContextIfAbsent(
      @Nonnull String name, ContextType type, boolean makeTrusted) {
    var contextMap = getContextMap(type);
    if (contextMap == null) {
      // Anonymous. Don't mess around with checks, just make the thing.
      return new JSContext(name, makeTrusted, type, makeContext());
    }

    if (contextMap.containsKey(name)) {
      return null;
    }
    var context = new JSContext(name, makeTrusted, type, makeContext());
    contextMap.put(name, context);
    return context;
  }

  /**
   * Ensures that a context exists with the given name and type.
   *
   * @param name The name of the context.
   * @param type The type of the context.
   * @param makeNewContextTrusted If a new context is created, whether it should be trusted.
   * @return The existing context matching {@code name} and {@code type} if there is one, otherwise
   *     a new context.
   */
  public @Nonnull JSContext ensureContext(
      @Nonnull String name, ContextType type, boolean makeNewContextTrusted) {
    var contextMap = getContextMap(type);
    if (contextMap == null) {
      // Anonymous. Always a new one.
      return new JSContext(name, makeNewContextTrusted, type, makeContext());
    }

    return contextMap.computeIfAbsent(
        name, key -> new JSContext(key, makeNewContextTrusted, type, makeContext()));
  }

  /**
   * Creates a new context with the given name and type.
   *
   * <p>If a context already exists for {@code name} and {@code type}, it is removed and a new one
   * is inserted instead. If you just need to make sure a context exists, call {@link
   * #ensureContext(String, ContextType, boolean)} to avoid removing the existing context.
   *
   * @param name The name of the new context.
   * @param type The type of the new context.
   * @param makeTrusted Whether the new context should be trusted.
   * @return The newly created context.
   */
  public @Nonnull JSContext putContext(String name, ContextType type, boolean makeTrusted) {
    var context = new JSContext(name, makeTrusted, type, makeContext());
    var contextMap = getContextMap(type);
    if (contextMap != null) {
      contextMap.put(name, context);
    }
    return context;
  }

  public Collection<JSContext> getContexts(ContextType type) {
    var contextMap = getContextMap(type);
    if (contextMap == null) {
      return List.of();
    }
    return List.copyOf(contextMap.values());
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

  public enum ContextType {
    Anonymous,
    Regular,
    AddOn
  }

  public final class JSContext {
    private final String name;
    private final boolean trusted;
    private final ContextType type;
    private final Context context;

    private JSContext(String name, boolean trusted, ContextType type, Context context) {
      this.name = name;
      this.trusted = trusted;
      this.type = type;
      this.context = context;
    }

    public String name() {
      return name;
    }

    public boolean trusted() {
      return trusted;
    }

    public Context context() {
      return context;
    }

    public Value eval(String script) {
      if (type == ContextType.Anonymous) {
        script = "(function() { var args = MTScript.getMTScriptCallingArgs(); " + script + "})();";
      }

      contextStack.push(this);
      try {
        return context.eval("js", script);
      } finally {
        contextStack.pop();
      }
    }

    public void remove() {
      Map<String, JSContext> contextMap = getContextMap(type);
      if (contextMap == null) {
        // Not stored in the engine, so nothing to do.
        return;
      }

      // Don't assume this instance is still the one associated with its name.
      // Instead, check that it is.
      // Null return signals removal.
      contextMap.compute(name, (key, value) -> this == value ? null : value);
    }
  }

  public final class JSAPIRegisteredMacro extends AbstractFunction {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final String name;
    private final Function<Object, Object> callable;
    private final JSContext context;

    public JSAPIRegisteredMacro(
        String macroName, Function<Object, Object> callable, JSContext context) {
      this.name = macroName;
      this.callable = callable;
      this.context = context;
    }

    private Object apply(List<Object> args) {
      contextStack.push(context);
      List<Object> modifiedArgs =
          Lists.transform(
              args, arg -> MacroJavaScriptBridge.getInstance().HostObjectToJavaScriptType(arg));
      try {
        return callable.apply(modifiedArgs);
      } finally {
        contextStack.pop();
      }
    }

    @Override
    public Object childEvaluate(
        Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
        throws ParserException {
      var ret = apply(parameters);
      if (ret == null) {
        return "";
      }

      var bridge = MacroJavaScriptBridge.getInstance();

      if (ret instanceof Value val) {
        return bridge.ValueToMTScriptType(val, new ArrayList<>());
      }

      Object r = bridge.HostObjectToMTScriptType(ret, new ArrayList<>());
      if (r instanceof List || r instanceof AbstractMap) {
        return gson.toJson(r);
      }
      return r;
    }
  }
}
