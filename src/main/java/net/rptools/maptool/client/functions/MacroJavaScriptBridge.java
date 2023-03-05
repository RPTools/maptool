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
package net.rptools.maptool.client.functions;

import com.google.gson.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import javax.script.ScriptException;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.script.javascript.JSArray;
import net.rptools.maptool.client.script.javascript.JSContext;
import net.rptools.maptool.client.script.javascript.JSObject;
import net.rptools.maptool.client.script.javascript.JSScriptEngine;
import net.rptools.maptool.client.script.javascript.api.MapToolJSAPIInterface;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.library.*;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.*;
import org.graalvm.polyglot.*;
import org.graalvm.polyglot.proxy.*;

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
    String contextName = null;

    if ("js.listNS".equalsIgnoreCase(functionName)) {
      JsonArray array = new JsonArray();
      JSScriptEngine.getContexts().stream()
          .sorted(Comparator.comparing(JSContext::name))
          .forEach(
              c -> {
                JsonObject obj = new JsonObject();
                obj.addProperty("name", c.name());
                obj.addProperty("trusted", c.trusted());
                array.add(obj);
              });
      return array;
    }

    if ("js.evalNS".equalsIgnoreCase(functionName) || "js.evalURI".equalsIgnoreCase(functionName)) {
      if (args.size() < 2) {
        throw new ParameterException(String.format(NOT_ENOUGH_PARAM, functionName, 2, args.size()));
      }
      contextName = (String) args.remove(0);
    }

    if ("js.removeNS".equalsIgnoreCase(functionName)) {
      if (args.size() < 1) {
        throw new ParameterException(String.format(NOT_ENOUGH_PARAM, functionName, 2, args.size()));
      }
      contextName = (String) args.remove(0);
      JSScriptEngine.removeContext(contextName, MapTool.getParser().isMacroTrusted());
      return "removed";
    }
    if ("js.createNS".equalsIgnoreCase(functionName)) {
      if (args.size() < 1) {
        throw new ParameterException(String.format(NOT_ENOUGH_PARAM, functionName, 2, args.size()));
      }
      contextName = (String) args.remove(0);
      boolean makeTrusted = MapTool.getParser().isMacroTrusted();
      if (args.size() > 0) {
        makeTrusted = ((int) args.remove(0)) > 0;
      }
      JSScriptEngine.registerContext(
          contextName, MapTool.getParser().isMacroTrusted(), makeTrusted);
      return "created";
    }

    String script;
    if ("js.evalURI".equalsIgnoreCase(functionName)) {
      if (args.size() < 1) {
        throw new ParameterException(String.format(NOT_ENOUGH_PARAM, functionName, 2, args.size()));
      }
      URL url;
      try {
        url = new URL(args.get(0).toString());
      } catch (MalformedURLException e) {
        throw new ParserException(e);
      }
      Optional<Library> library;
      try {
        library = new LibraryManager().getLibrary(url).get();
        if (library.isPresent()) {
          script = library.get().readAsString(url).get();
        } else {
          throw new ParserException(
              I18N.getText("macro.function.jsevalURI.invalidURI", args.get(0).toString()));
        }
      } catch (ExecutionException | InterruptedException | IOException | CompletionException e) {
        if (e.getCause() instanceof LibraryNotValidException lnve) {
          throw new ParserException(lnve.getMessage());
        } else if (e.getCause() != null) {
          throw new ParserException(e.getCause());
        }
        throw new ParserException(e);
      }

    } else {
      script = args.get(0).toString();
    }
    List<Object> scriptArgs = new ArrayList<>();
    if (args.size() > 1) {
      for (int i = 1; i < args.size(); i++) {
        scriptArgs.add(HostObjectToJavaScriptType(args.get(i)));
      }
    }

    callingArgsStack.push(scriptArgs);
    try {
      return JavaScriptToMTScriptType(
          JSScriptEngine.getJSScriptEngine().evalScript(contextName, script));
    } catch (PolyglotException e) {
      Throwable je = e.asHostException();
      ParserException pe = (ParserException) je;
      if (pe != null) {
        throw pe;
      }
      throw new ParserException(je);
    } catch (ScriptException e) {
      throw new ParserException(e);
    } finally {
      callingArgsStack.pop();
    }
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
