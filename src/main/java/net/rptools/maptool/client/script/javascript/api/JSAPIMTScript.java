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
package net.rptools.maptool.client.script.javascript.api;

import java.util.List;
import net.rptools.maptool.client.functions.AbortFunction;
import net.rptools.maptool.client.functions.AssertFunction;
import net.rptools.maptool.client.functions.EvalMacroFunctions;
import net.rptools.maptool.client.functions.MacroJavaScriptBridge;
import net.rptools.maptool.language.I18N;
import net.rptools.parser.ParserException;

@MapToolJSAPIDefinition(javaScriptVariableName = "MTScript")
/** Class used to provide an API to interact with MapTool custom scripting language. */
public class JSAPIMTScript implements MapToolJSAPIInterface {

  public Object getVariable(String name) throws ParserException {
    return MacroJavaScriptBridge.getInstance().getMTScriptVariable(name);
  }

  public void setVariable(String name, Object value) throws ParserException {
    MacroJavaScriptBridge.getInstance().setMTScriptVariable(name, value);
  }

  public void raiseError(String msg) throws ParserException {
    throw new ParserException(msg);
  }

  public void abort() throws ParserException {
    throw new AbortFunction.AbortFunctionException(
        I18N.getText("macro.function.abortFunction.message", "MTScript.abort()"));
  }

  public void mtsAssert(boolean check, String message)
      throws AssertFunction.AssertFunctionException {
    if (!check) {
      throw new AssertFunction.AssertFunctionException(message);
    }
  }

  public Object execMacro(String macro) throws ParserException {
    return EvalMacroFunctions.getInstance()
        .execMacro(MacroJavaScriptBridge.getInstance().getTokenInContext(), macro);
  }

  public Object evalMacro(String macro) throws ParserException {
    return EvalMacroFunctions.getInstance()
        .evalMacro(
            MacroJavaScriptBridge.getInstance().getVariableResolver(),
            MacroJavaScriptBridge.getInstance().getTokenInContext(),
            macro);
  }

  public List<Object> getMTScriptCallingArgs() {
    return MacroJavaScriptBridge.getInstance().getCallingArgs();
  }
}
