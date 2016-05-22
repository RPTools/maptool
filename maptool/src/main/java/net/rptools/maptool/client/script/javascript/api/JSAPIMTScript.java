/*
 * This software Copyright by the RPTools.net development team, and licensed
 * under the GPL Version 3 or, at your option, any later version.
 *
 * MapTool 2 Source Code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this source Code. If not, see <http://www.gnu.org/licenses/>
 */
package net.rptools.maptool.client.script.javascript.api;

import net.rptools.maptool.client.functions.AbortFunction;
import net.rptools.maptool.client.functions.AssertFunction;
import net.rptools.maptool.client.functions.EvalMacroFunctions;
import net.rptools.maptool.client.functions.MacroJavaScriptBridge;
import net.rptools.maptool.language.I18N;
import net.rptools.parser.ParserException;

@MapToolJSAPIDefinition(javaScriptVariableName = "MTScript")
/**
 * Class used to provide an API to interact with MapTool custom scripting language.
 */
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
		throw new AbortFunction.AbortFunctionException(I18N.getText("macro.function.abortFunction.message", "MTScript.abort()"));
	}

	public void mtsAssert(boolean check, String message) throws AssertFunction.AssertFunctionException {
		if (!check) {
			throw new AssertFunction.AssertFunctionException(message);
		}
	}

	public Object execMacro(String macro) throws ParserException {
		return EvalMacroFunctions.getInstance().execMacro(MacroJavaScriptBridge.getInstance().getTokenInContext(), macro);
	}

	public Object evalMacro(String macro) throws ParserException {
		return EvalMacroFunctions.getInstance().evalMacro(
				MacroJavaScriptBridge.getInstance().getVariableResolver(),
				MacroJavaScriptBridge.getInstance().getTokenInContext(),
				macro);
	}
}
