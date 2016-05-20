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
package net.rptools.maptool.client.functions;

import jdk.nashorn.api.scripting.JSObject;
import net.rptools.maptool.client.script.javascript.JSScriptEngine;
import net.rptools.maptool.language.I18N;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import javax.script.ScriptException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MacroJavaScriptBridge extends AbstractFunction {

	private static final MacroJavaScriptBridge instance = new MacroJavaScriptBridge();

	private MacroJavaScriptBridge() {
		super(1, 1, "js.eval");
	}

	public static MacroJavaScriptBridge getInstance() {
		return instance;
	}

	@Override
	public Object childEvaluate(Parser parser, String functionName, List<Object> args) throws ParserException {
		if ("js.eval".equals(functionName)) {
			String script = args.get(0).toString();
			try {
				Object retval = JSScriptEngine.getJSScriptEngine().evalAnonymous(script);
				if (retval instanceof Integer) {
					return BigDecimal.valueOf(((Integer) retval).intValue());
				} else if (retval instanceof Long) {
					return BigDecimal.valueOf(((Long) retval).longValue());
				} else if (retval instanceof Double) {
					return BigDecimal.valueOf(((Double) retval).doubleValue());
				} else if (retval instanceof JSObject) {
					JSObject jsObject = (JSObject) retval;
					if (jsObject.isArray()) {
						List<Object> arr = new ArrayList<>();
						arr.addAll(jsObject.values());
						return JSONArray.fromObject(arr.toArray());
					} else {
						Map<String, Object> obj = new HashMap<>();
						for (String key : jsObject.keySet()) {
							obj.put(key, jsObject.getMember(key));
						}
						return JSONObject.fromObject(obj);
					}
				} else {
					return retval;
				}
			} catch (ScriptException e) {
				throw new ParserException(e);
			}
		}

		throw new ParserException(I18N.getText("macro.function.general.unknownFunction", functionName));
	}
}
