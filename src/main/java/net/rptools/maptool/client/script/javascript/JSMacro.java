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

import com.google.gson.*;
import java.util.*;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.functions.*;
import net.rptools.maptool.client.script.javascript.api.*;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;
import org.graalvm.polyglot.*;

public class JSMacro extends AbstractFunction {
  private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
  private static JSMacro instance = new JSMacro();
  private static HashMap<String, JSAPIRegisteredMacro> macros = new HashMap<>();

  public static void registerMacro(String name, JSAPIRegisteredMacro macro) {
    macros.put(name, macro);
  }

  public static JSMacro getInstance() {
    return instance;
  }

  public static void clear() {
    macros.clear();
  }

  private JSMacro() {
    super(0, UNLIMITED_PARAMETERS);
  }

  public static boolean isFunctionDefined(String functionName) {
    return macros.containsKey(functionName);
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> args)
      throws ParserException {
    MapToolVariableResolver variableResolver = (MapToolVariableResolver) resolver;
    Object[] aargs = args.toArray(new Object[0]);
    JSAPIRegisteredMacro macro = macros.get(functionName);
    Object ret = JSScriptEngine.getJSScriptEngine().applyFunction(macro, aargs);
    if (ret != null) {
      if (ret instanceof Value val) {
        return MacroJavaScriptBridge.getInstance().ValueToMTScriptType(val, new ArrayList());
      }
      Object r = MacroJavaScriptBridge.getInstance().HostObjectToMTScriptType(ret, new ArrayList());
      if (r instanceof List || r instanceof AbstractMap) {
        return gson.toJson(r);
      }
      return r;
    }
    return "";
  }
}
