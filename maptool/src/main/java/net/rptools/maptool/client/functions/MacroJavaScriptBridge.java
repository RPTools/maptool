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
import net.rptools.maptool.client.MapToolVariableResolver;
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

	private MapToolVariableResolver variableResolver;

	private MacroJavaScriptBridge() {
		super(1, 1, "js.eval");
	}

	public static MacroJavaScriptBridge getInstance() {
		return instance;
	}

	@Override
	public Object childEvaluate(Parser parser, String functionName, List<Object> args) throws ParserException {
		variableResolver = (MapToolVariableResolver) parser.getVariableResolver();
		if ("js.eval".equals(functionName)) {
			String script = args.get(0).toString();
			try {
				return JavaScriptToMTScriptType(JSScriptEngine.getJSScriptEngine().evalAnonymous(script));
			} catch (ScriptException e) {
				throw new ParserException(e);
			}
		}

		throw new ParserException(I18N.getText("macro.function.general.unknownFunction", functionName));
	}

	public Object JavaScriptToMTScriptType(Object val) {
		if (val instanceof Integer) {
			return BigDecimal.valueOf(((Integer) val).intValue());
		} else if (val instanceof Long) {
			return BigDecimal.valueOf(((Long) val).longValue());
		} else if (val instanceof Double) {
			return BigDecimal.valueOf(((Double) val).doubleValue());
		} else if (val instanceof JSObject) {
			JSObject jsObject = (JSObject) val;
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
			return val;
		}
	}

	public Object getMTScriptVariable(String name) throws ParserException {
		return variableResolver.getVariable(name);
	}

	public void setMTScriptVariable(String name, Object value) throws ParserException {
		variableResolver.setVariable(name, JavaScriptToMTScriptType(value));
	}

}
