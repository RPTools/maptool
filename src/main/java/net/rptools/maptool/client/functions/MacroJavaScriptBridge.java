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
import java.math.BigDecimal;
import java.util.*;
import javax.script.ScriptException;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.script.javascript.JSScriptEngine;
import net.rptools.maptool.client.script.javascript.api.MapToolJSAPIInterface;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Token;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;
import org.graalvm.polyglot.*;

public class MacroJavaScriptBridge extends AbstractFunction {
  private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  private static final MacroJavaScriptBridge instance = new MacroJavaScriptBridge();

  private MapToolVariableResolver variableResolver;

  private Stack<List<Object>> callingArgsStack = new Stack<>();

  private MacroJavaScriptBridge() {
    super(1, UNLIMITED_PARAMETERS, "js.eval", "js.evala");
  }

  public static MacroJavaScriptBridge getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> args)
      throws ParserException {
    variableResolver = (MapToolVariableResolver) resolver;
    if ("js.eval".equals(functionName)) {
      if (!MapTool.getParser().isMacroTrusted()) {
        throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));
      }
      String script = args.get(0).toString();
      List<Object> scriptArgs = new ArrayList<>();

      if (args.size() > 1) {
        for (int i = 1; i < args.size(); i++) {
          scriptArgs.add(args.get(i));
        }
      }

      callingArgsStack.push(scriptArgs);
      try {
        return JavaScriptToMTScriptType(JSScriptEngine.getJSScriptEngine().evalAnonymous(script));
      } catch (ScriptException e) {
        throw new ParserException(e);
      } finally {
        callingArgsStack.pop();
      }
    }

    throw new ParserException(I18N.getText("macro.function.general.unknownFunction", functionName));
  }

  public Object HostObjectToMTScriptType(Object obj, ArrayList seen) {

    if (obj instanceof MapToolJSAPIInterface) {
      MapToolJSAPIInterface maptoolWrapper = (MapToolJSAPIInterface) obj;
      return maptoolWrapper.serializeToString();
    }
    if (obj.getClass().isArray()) {
      obj = Arrays.asList(obj);
    }
    if (obj instanceof List) {
      List list = (List) obj;
      ArrayList outList = new ArrayList();
      for (Object li : list) {
        if (li instanceof Value) {
          Value val = (Value) li;
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
        if (value instanceof Value) {
          Value val = (Value) value;
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
}
