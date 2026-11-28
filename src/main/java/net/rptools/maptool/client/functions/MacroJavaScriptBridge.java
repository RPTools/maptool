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
package net.rptools.maptool.client.functions;

import com.google.gson.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.script.javascript.JSArray;
import net.rptools.maptool.client.script.javascript.JSObject;
import net.rptools.maptool.client.script.javascript.JSScriptEngine;
import net.rptools.maptool.client.script.javascript.JSScriptEngine.ContextType;
import net.rptools.maptool.client.script.javascript.JSScriptEngine.JSContext;
import net.rptools.maptool.client.script.javascript.api.MapToolJSAPIInterface;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.library.*;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.*;
import org.graalvm.polyglot.*;

public class MacroJavaScriptBridge extends AbstractFunction implements DefinesSpecialVariables {
  private static final String NOT_ENOUGH_PARAM =
      "Function '%s' requires at least %d parameters; %d were provided.";
  private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  private static final MacroJavaScriptBridge instance = new MacroJavaScriptBridge();

  private MapToolVariableResolver variableResolver;

  private Stack<List<Object>> callingArgsStack = new Stack<>();

  private MacroJavaScriptBridge() {
    super(
        0,
        UNLIMITED_PARAMETERS,
        "js.eval",
        "js.evalNS",
        "js.evalURI",
        "js.removeNS",
        "js.createNS",
        "js.listNS");
  }

  public static MacroJavaScriptBridge getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> args)
      throws ParserException {
    variableResolver = (MapToolVariableResolver) resolver;

    if ("js.listNS".equalsIgnoreCase(functionName)) {
      FunctionUtil.checkNumberParam(functionName, args, 0, 0);

      JsonArray array = new JsonArray();
      JSScriptEngine.getInstance().getContexts(ContextType.Regular).stream()
          .sorted(Comparator.comparing(JSContext::name))
          .forEach(
              c -> {
                JsonObject obj = new JsonObject();
                obj.addProperty("name", c.name());
                obj.addProperty("trusted", c.trusted());
                array.add(obj);
              });
      return array;
    } else if ("js.removeNS".equalsIgnoreCase(functionName)) {
      FunctionUtil.checkNumberParam(functionName, args, 1, 1);

      var contextName = args.get(0).toString();
      var jsEngine = JSScriptEngine.getInstance();
      var context = jsEngine.getContext(contextName, ContextType.Regular).orElse(null);
      if (context != null) {
        if (!MapTool.getParser().isMacroTrusted() && context.trusted()) {
          throw new ParserException(
              I18N.getText("macro.function.general.noPermJS", context.name()));
        }
        context.remove();
      }
      return "removed";
    } else if ("js.createNS".equalsIgnoreCase(functionName)) {
      FunctionUtil.checkNumberParam(functionName, args, 1, 2);

      var contextName = args.get(0).toString();
      var makeTrusted =
          args.size() > 1
              ? !BigDecimal.ZERO.equals(args.get(1))
              : MapTool.getParser().isMacroTrusted();

      var trusted = MapTool.getParser().isMacroTrusted();
      if (!trusted && makeTrusted) {
        throw new ParserException("Cannot make a trusted JS context from an untrusted context");
      }

      var jsEngine = JSScriptEngine.getInstance();
      if (trusted) {
        // In trusted contexts, allow overwriting existing namespaces.
        jsEngine.putContext(contextName, ContextType.Regular, true);
      } else {
        var newContext = jsEngine.createContextIfAbsent(contextName, ContextType.Regular, false);
        if (newContext == null) {
          throw new ParserException("Context " + contextName + " already exists");
        }
      }

      return "created";
    } else {
      /* js.evalURI(), js.evalNS(), js.eval() */
      var trusted = MapTool.getParser().isMacroTrusted();
      var engine = JSScriptEngine.getInstance();

      JSContext context;
      String script;
      List<Object> rest;
      if ("js.evalURI".equalsIgnoreCase(functionName)) {
        FunctionUtil.checkNumberParam(functionName, args, 2, UNLIMITED_PARAMETERS);

        var contextName = args.get(0).toString();
        var urlString = args.get(1).toString();
        rest = args.subList(2, args.size());

        context = engine.ensureContext(contextName, ContextType.Regular, trusted);

        // Look up the script in the named library.
        URL url;
        try {
          url = new URL(urlString);
        } catch (MalformedURLException e) {
          throw new ParserException(I18N.getText("macro.function.html5.invalidURI", urlString));
        }

        Optional<Library> library;
        try {
          library = new LibraryManager().getLibrary(url).get();
          if (library.isPresent()) {
            script = library.get().readAsString(url).get();
          } else {
            throw new ParserException(
                I18N.getText("macro.function.jsevalURI.invalidURI", urlString));
          }
        } catch (ExecutionException | InterruptedException | IOException | CompletionException e) {
          if (e.getCause() instanceof LibraryNotValidException lnve) {
            throw new ParserException(lnve.getMessage());
          } else if (e.getCause() != null) {
            throw new ParserException(e.getCause());
          }
          throw new ParserException(e);
        }
      } else if ("js.evalNS".equalsIgnoreCase(functionName)) {
        FunctionUtil.checkNumberParam(functionName, args, 2, UNLIMITED_PARAMETERS);

        var contextName = args.get(0).toString();
        script = args.get(1).toString();
        rest = args.subList(2, args.size());

        context = engine.ensureContext(contextName, ContextType.Regular, trusted);
      } else {
        /* js.eval() */
        FunctionUtil.checkNumberParam(functionName, args, 1, UNLIMITED_PARAMETERS);

        script = args.get(0).toString();
        rest = args.subList(1, args.size());

        context = engine.ensureContext("<anonymous>", ContextType.Anonymous, trusted);
      }

      List<Object> scriptArgs = new ArrayList<>();
      for (var arg : rest) {
        scriptArgs.add(HostObjectToJavaScriptType(arg));
      }

      if (!trusted && context.trusted()) {
        throw new ParserException(I18N.getText("macro.function.general.noPermJS", context.name()));
      }

      callingArgsStack.push(scriptArgs);
      try {
        return JavaScriptToMTScriptType(context.eval(script));
      } catch (PolyglotException e) {
        throw toParserException(e);
      } finally {
        callingArgsStack.pop();
      }
    }
  }

  /**
   * Converts an error raised while running a script into the exception reported to the macro.
   *
   * <p>An error thrown by Java code the script called -- an MTScript function failing, say -- is
   * reported as that exception. An error in the script itself, such as a {@code ReferenceError}, a
   * {@code TypeError} or a {@code throw}, has no Java exception behind it, and is reported with the
   * script's own message and the line it happened on. {@link PolyglotException#asHostException()}
   * throws for those, which used to replace the script's message with a bare {@code
   * PolyglotEngineException} and left nothing to go on.
   *
   * @param e the error from the script engine
   * @return the exception to throw
   */
  static ParserException toParserException(PolyglotException e) {
    if (e.isHostException()) {
      Throwable host = e.asHostException();
      return host instanceof ParserException pe ? pe : new ParserException(host);
    }
    StringBuilder message = new StringBuilder(String.valueOf(e.getMessage()));
    SourceSection location = e.getSourceLocation();
    if (location != null && location.isAvailable()) {
      message.append(" (line ").append(location.getStartLine()).append(')');
    }
    return new ParserException(message.toString());
  }

  public Object HostObjectToJavaScriptType(Object obj) {
    if (obj instanceof JsonElement jObj) {
      if (jObj.isJsonArray()) {
        ArrayList newList = new ArrayList();
        for (JsonElement element : jObj.getAsJsonArray()) {
          newList.add(HostObjectToJavaScriptType(element));
        }
        return new JSArray(newList);
      }
      if (jObj.isJsonObject()) {
        HashMap<String, Object> newObj = new HashMap<String, Object>();
        for (Map.Entry<String, JsonElement> element : jObj.getAsJsonObject().entrySet()) {
          Object val = HostObjectToJavaScriptType(element.getValue());
          newObj.put(element.getKey(), val);
        }
        return new JSObject(newObj);
      }
      if (jObj.isJsonNull()) {
        return null;
      }
      if (jObj.isJsonPrimitive()) {
        JsonPrimitive jPrim = jObj.getAsJsonPrimitive();
        if (jPrim.isBoolean()) {
          return jPrim.getAsBoolean();
        }
        if (jPrim.isNumber()) {
          return jPrim.getAsDouble();
        }
        if (jPrim.isString()) {
          return jPrim.getAsString();
        }
      }
    } else if (obj instanceof BigDecimal) {
      BigDecimal bd = (BigDecimal) obj;
      try {
        return Long.valueOf(bd.longValueExact());
      } catch (ArithmeticException e) {
        return Double.valueOf(bd.doubleValue());
      }
    }
    return obj;
  }

  public Object HostObjectToMTScriptType(Object obj, ArrayList seen) {
    if (obj instanceof Integer i) {
      return BigDecimal.valueOf(i);
    }

    if (obj instanceof Double d) {
      return BigDecimal.valueOf(d);
    }

    if (obj instanceof MapToolJSAPIInterface maptoolWrapper) {
      return maptoolWrapper.serializeToString();
    }
    if (obj.getClass().isArray()) {
      obj = Arrays.asList(obj);
    }
    if (obj instanceof List list) {
      ArrayList outList = new ArrayList();
      for (Object li : list) {
        if (li instanceof Value val) {
          outList.add(ValueToMTScriptType(val, seen));
        } else {
          outList.add(HostObjectToMTScriptType(li, seen));
        }
      }
      return outList;
    }
    if (obj instanceof AbstractMap) {
      AbstractMap amap = (AbstractMap) obj;
      HashMap<Object, Object> outMap = new HashMap<>();
      for (Object key : amap.keySet()) {
        Object value = amap.get(key);
        if (key instanceof Value) {
          Value val = (Value) key;
          key = ValueToMTScriptType(val, seen);
        } else {
          key = HostObjectToMTScriptType(key, seen);
        }
        if (value instanceof Value val) {
          value = ValueToMTScriptType(val, seen);
        } else {
          value = HostObjectToMTScriptType(value, seen);
        }
        outMap.put(key, value);
      }
      return outMap;
    }
    return obj;
  }

  public Object JavaScriptArrayToMTScriptType(Value val, ArrayList seen) {
    ArrayList<Object> array = new ArrayList<>();
    long size = val.getArraySize();
    for (long i = 0; i < size; i++) {
      Value value = val.getArrayElement(i);
      Object valueElement = ValueToMTScriptType(value, seen);
      array.add(valueElement);
    }
    return array;
  }

  public Object JavaScriptObjectToMTScriptType(Value val, ArrayList seen) {
    HashMap<String, Object> obj = new HashMap<>();
    for (String key : val.getMemberKeys()) {
      Value value = val.getMember(key);
      Object valueElement = ValueToMTScriptType(value, seen);
      obj.put(key, valueElement);
    }
    return obj;
  }

  public Object ValueToMTScriptType(Value val, ArrayList seen) {
    if (val.isNull()) {
      return "";
    }
    if (val.isString()) {
      return val.asString();
    }
    if (val.isNumber()) {
      if (val.fitsInLong()) {
        return BigDecimal.valueOf(val.asLong());
      }
      return BigDecimal.valueOf(val.asDouble());
    }
    if (seen.contains(val.as(Object.class))) {
      return "[[...]]";
    }
    seen.add(val.as(Object.class));

    if (val.isHostObject()) {
      Object r = HostObjectToMTScriptType(val.as(Object.class), seen);
      seen.remove(seen.size() - 1);
      return r;
    }

    if (val.hasArrayElements()) {
      Object r = JavaScriptArrayToMTScriptType(val, seen);
      seen.remove(seen.size() - 1);
      return r;
    }
    if (val.hasMembers()) {
      Object r = JavaScriptObjectToMTScriptType(val, seen);
      seen.remove(seen.size() - 1);
      return r;
    }
    seen.remove(seen.size() - 1);
    return val.as(Object.class).toString();
  }

  public Object JavaScriptToMTScriptType(Value value) {
    ArrayList<Object> seen = new ArrayList<>();
    Object r = ValueToMTScriptType(value, seen);
    if (r instanceof List || r instanceof AbstractMap) {
      return gson.toJson(r);
    }
    return r;
  }

  public Object getMTScriptVariable(String name) throws ParserException {
    return variableResolver.getVariable(name);
  }

  public void setMTScriptVariable(String name, Value value) throws ParserException {
    variableResolver.setVariable(name, JavaScriptToMTScriptType(value));
  }

  public Token getTokenInContext() {
    return variableResolver.getTokenInContext();
  }

  public MapToolVariableResolver getVariableResolver() {
    return variableResolver;
  }

  public List<Object> getCallingArgs() {
    if (callingArgsStack.empty()) {
      return new ArrayList<>();
    } else {
      return callingArgsStack.peek();
    }
  }

  @Override
  public String[] getSpecialVariables() {
    return new String[] {"macro.catchAssert"};
  }
}
