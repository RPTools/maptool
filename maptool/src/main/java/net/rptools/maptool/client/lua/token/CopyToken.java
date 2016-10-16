/**
 * 
 */
package net.rptools.maptool.client.lua.token;

import static net.rptools.maptool.client.functions.TokenCopyDeleteFunctions.setTokenValues;
import static net.rptools.maptool.client.lua.LuaConverters.toObj;

import java.util.ArrayList;
import java.util.List;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.functions.JSONMacroFunctions;
import net.rptools.maptool.client.lua.LuaConverters;
import net.rptools.maptool.client.lua.MapToolMap;
import net.rptools.maptool.client.lua.MapToolToken;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.parser.ParserException;
import net.sf.json.JSONObject;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

/**
 * @author Maluku
 *
 */
public class CopyToken extends VarArgFunction {
	private MapToolVariableResolver resolver;
	private MapToolMap map;

	public CopyToken(MapToolVariableResolver resolver, MapToolMap map) {
		this.resolver = resolver;
		this.map = map;
	}

	@Override
	public Varargs invoke(Varargs args) {
		if (!MapTool.getParser().isMacroTrusted()) {
			throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", "map.copyToken")));
		}

		Token token = null;
		int numberCopies = 1;
		Zone zone = map.getZone();
		JSONObject newVals = null;
		LuaValue tokarg = args.arg1();
		if (tokarg instanceof MapToolToken) {
			token = ((MapToolToken) tokarg).getToken();
		} else {
			String tokenName = tokarg.checkjstring();
			token = zone.resolveToken(tokenName);
			if (token == null) {
				throw new LuaError(new ParserException(I18N.getText("macro.function.general.unknownTokenOnMap", "map.copyToken", tokenName, zone.getName())));
			}
		}
		if (args.isnumber(2)) {
			numberCopies = args.checkint(2);
			if (args.istable(3)) {
				Object o = LuaConverters.toJson(args.checktable(3));
				if (!(o instanceof JSONObject)) {
					throw new LuaError(new ParserException(I18N.getText("macro.function.general.argumentTypeO", "map.copyToken", 4)));
				}
				newVals = (JSONObject) o;
			} else if (!args.isnil(3)) {
				Object o = JSONMacroFunctions.asJSON(toObj(args.arg(3)));
				if (!(o instanceof JSONObject)) {
					throw new LuaError(new ParserException(I18N.getText("macro.function.general.argumentTypeO", "map.copyToken", 4)));
				}
				newVals = (JSONObject) o;
			}
		}
		List<Token> resultTokens = new ArrayList<Token>(numberCopies);
		List<Token> allTokens = zone.getTokens();
		for (int i = 0; i < numberCopies; i++) {
			Token t = new Token(token);
			if (allTokens != null) {
				for (Token tok : allTokens) {
					GUID tea = tok.getExposedAreaGUID();
					if (tea != null && tea.equals(t.getExposedAreaGUID())) {
						t.setExposedAreaGUID(new GUID());
					}
				}
			}
			try {
				setTokenValues(t, newVals, zone, resolver);
			} catch (ParserException e) {
				throw new LuaError(e);
			}
			zone.putToken(t);

			MapTool.serverCommand().putToken(zone.getId(), t);
			resultTokens.add(t);
		}
		MapTool.getFrame().getCurrentZoneRenderer().flushLight();
		if (numberCopies == 1) {
			return new MapToolToken(resultTokens.get(0), resolver);
		} else {
			LuaTable result = new LuaTable();
			for (Token t : resultTokens) {
				result.insert(0, new MapToolToken(t, resolver));
			}
			return result;
		}
		//		List<Object> arg = new ArrayList<Object>();
		//		if (!args.isnil(1)) {
		//			if (args.arg1() instanceof MapToolToken || args.arg1().isstring()) {
		//				arg.add(args.arg1().tojstring());
		//			}
		//			else {
		//				arg.add(toObj(args.arg1()));
		//			}
		//			if (args.isnumber(2)) {
		//				arg.add(toObj(args.checknumber(2)));
		//				if (args.isstring(3)) {
		//					arg.add(toObj(args.checkstring(3)));
		//					if (args.istable(4)) arg.add(toJson(args.checktable(4)));
		//					else if (args.isstring(4)) arg.add(toObj(args.checkstring(3)));
		//				}
		//			}
		//		}
		//		try {
		//			@SuppressWarnings("unchecked")
		//			List<Token> tokens = (List<Token>) TokenCopyDeleteFunctions.copyTokens(resolver, arg, true);
		//			LuaTable result = new LuaTable();
		//			for (Token t: tokens) {
		//				result.insert(0, new MapToolToken(t));
		//			}
		//			return result;
		//		} catch (ParserException e) {
		//			throw new LuaError(e);
		//		}

	}

}
