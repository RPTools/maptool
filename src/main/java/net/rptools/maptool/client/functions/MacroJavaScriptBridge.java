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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.util.*;
import javax.script.ScriptException;
import jdk.nashorn.api.scripting.JSObject;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.functions.json.JSONMacroFunctions;
import net.rptools.maptool.client.script.javascript.JSScriptEngine;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Token;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;

public class MacroJavaScriptBridge extends AbstractFunction {

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
  public Object childEvaluate(Parser parser, String functionName, List<Object> args)
      throws ParserException {
    variableResolver = (MapToolVariableResolver) parser.getVariableResolver();
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

  public Object JavaScriptToMTScriptType(Object val) throws ParserException {
    if (val == null) {
      // MTScript doesnt have a null, only empty string
      return "";
    } else if (val instanceof Integer) {
      return BigDecimal.valueOf(((Integer) val).intValue());
    } else if (val instanceof Long) {
      return BigDecimal.valueOf(((Long) val).longValue());
    } else if (val instanceof Double) {
      return BigDecimal.valueOf(((Double) val).doubleValue());
    } else if (val instanceof JSObject) {
      JSObject jsObject = (JSObject) val;
      if (jsObject.isArray()) {
        JsonArray arr = new JsonArray();
        for (Object o : jsObject.values()) {
          arr.add(0);
        }
        return arr;
      } else {
        JsonObject obj = new JsonObject();
        for (String key : jsObject.keySet()) {
          obj.add(key, JSONMacroFunctions.getInstance().asJsonElement(jsObject.getMember(key)));
        }
        return obj;
      }
    } else {
      return val;
    }
  }

  public Object getMTScriptVariable(String name) throws ParserException {
    return variableResolver.getVariable(name);
  }

  public void setMTScriptVariable(String name, Object value) throws ParserException {
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
