/**
 * 
 */
package net.rptools.maptool.client.lua.token;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.FindTokenFunctions;
import net.rptools.maptool.client.functions.TokenLocationFunctions;
import net.rptools.maptool.client.lua.MapToolToken;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Token;
import net.rptools.parser.ParserException;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

/**
 * @author Maluku
 *
 */
public class Distance extends VarArgFunction {
	MapToolToken token;

	public Distance(MapToolToken token) {
		this.token = token;
	}

	@Override
	public Varargs invoke(Varargs args) {
		try {
			if (!token.isSelfOrTrusted()) {
				throw new LuaError(I18N.getText("macro.function.general.noPerm", "getDistance"));
			}
			if (args.arg(1).isint() && args.arg(2).isint()) {
				return valueOf(TokenLocationFunctions.getDistance(token.getToken(), args.arg(1).toint(), args.arg(2).toint(), args.isnil(3) || args.checkboolean(3), args.isnil(4) ? null : args.checkjstring(4)));
			} else if (args.arg(1) instanceof MapToolToken) {
				MapToolToken t = (MapToolToken) args.arg(1);
				if (t.visibleToMe() || MapTool.getParser().isMacroTrusted()) {
					return valueOf(TokenLocationFunctions.getDistance(token.getToken(), t.getToken(), args.isnil(2) || args.checkboolean(2), args.isnil(3) ? null : args.checkjstring(3)));
				}
				throw new LuaError(I18N.getText("macro.function.general.noPerm", "getDistance"));
			}
			if (!MapTool.getParser().isMacroTrusted()) {
				throw new LuaError(I18N.getText("macro.function.general.noPerm", "getDistance"));
			}
			Token t = FindTokenFunctions.findToken(args.checkjstring(1), null);
			return valueOf(TokenLocationFunctions.getDistance(token.getToken(), t, args.isnil(2) || args.checkboolean(2), args.isnil(3) ? null : args.checkjstring(3)));
		} catch (ParserException e) {
			throw new LuaError(e);
		}
	}
}
