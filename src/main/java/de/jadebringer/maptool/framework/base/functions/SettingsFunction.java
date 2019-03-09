/*
 * This software Copyright by the RPTools.net development team, and licensed under the Affero GPL Version 3 or, at your option, any later version.
 *
 * MapTool Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License * along with this source Code. If not, please visit <http://www.gnu.org/licenses/> and specifically the Affero license text
 * at <http://www.gnu.org/licenses/agpl.html>.
 */
package de.jadebringer.maptool.framework.base.functions;

import java.util.List;

import de.jadebringer.maptool.framework.FunctionCaller;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.TokenPropertyFunctions;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Token;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;

/**
 * 
 * @author oliver.szymanski
 */
public class SettingsFunction extends AbstractFunction {
	public SettingsFunction() {
		super(0, 3, "setSetting", "getSetting", "deleteSetting", "listSettings");
	}

	private final static SettingsFunction instance = new SettingsFunction();

	public static SettingsFunction getInstance() {
		return instance;
	}

	@Override
	public Object childEvaluate(Parser parser, String functionName, List<Object> parameters) throws ParserException {

		if (!MapTool.getParser().isMacroTrusted()) {
			throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));
		}
		
		if ("setSetting".equals(functionName)) {
			String key = FunctionCaller.getParam(parameters, 0);
			Object value = FunctionCaller.getParam(parameters, 1);
			String tokenName = FunctionCaller.getParam(parameters, 2,"Lib:JadebringerSettings");
			setSetting(parser, key, value, tokenName);
			return "";
		} else if ("getSetting".equals(functionName)) {
			String key = FunctionCaller.getParam(parameters, 0);
			Object defaultValue = FunctionCaller.getParam(parameters, 1);
			String tokenName = FunctionCaller.getParam(parameters, 2,"Lib:JadebringerSettings");
			return getSetting(parser, key, defaultValue, tokenName);
		} else if ("deleteSetting".equals(functionName)) {
			String key = FunctionCaller.getParam(parameters, 0);
			String tokenName = FunctionCaller.getParam(parameters, 1,"Lib:JadebringerSettings");
			deleteSetting(parser, key, tokenName);
			return "";
		} else if ("listSettings".equals(functionName)) {
			String tokenName = FunctionCaller.getParam(parameters, 0,"Lib:JadebringerSettings");
			return listSettings(parser, tokenName);
		}
	
		return "";
	}

	public void setSetting(Parser parser, String key, Object value, String tokenName) throws ParserException {
		TokenPropertyFunctions tpFunc = TokenPropertyFunctions.getInstance();
		
		List<Object> parameters = FunctionCaller.toObjectList(
				key, value, tokenName);
		tpFunc.evaluate(parser, "setLibProperty", parameters);
	}
	
	public void deleteSetting(Parser parser, String key, String tokenName) throws ParserException {
		TokenPropertyFunctions tpFunc = TokenPropertyFunctions.getInstance();
		
		List<Object> parameters = FunctionCaller.toObjectList(
				key, tokenName);
		tpFunc.evaluate(parser, "resetProperty", parameters);
	}
	
	public Object getSetting(Parser parser, String key, Object defaultValue, String tokenName) throws ParserException {
		TokenPropertyFunctions tpFunc = TokenPropertyFunctions.getInstance();
		
		List<Object> parameters = FunctionCaller.toObjectList(
				key, tokenName);
		Object result = tpFunc.evaluate(parser, "getLibProperty", parameters);
		
		if ("".equals(result) && defaultValue != null) {
			result = defaultValue;
		}
		
		return result;
	}
	
	public Object listSettings(Parser parser, String tokenName) throws ParserException {
		TokenPropertyFunctions tpFunc = TokenPropertyFunctions.getInstance();
		
		List<Object> parameters = FunctionCaller.toObjectList(
				".*", tokenName);
		String names = (String)tpFunc.evaluate(parser, "getMatchingLibProperties", parameters);
		
		Token token = MapTool.getParser().getTokenMacroLib(tokenName);
		//JSONObject result = new JSONObject();
		StringBuilder sb = new StringBuilder();
		for(String key : names.split(",")) {
			//Object value = getSetting(parser, key, null, tokenName);
			Object value = token.getProperty(key);		
			//result.put(key, value);
			sb.append(key).append(" = ").append(value).append("<br>");
		}
				
		return sb.toString();
	}
}
