package net.rptools.maptool.client.lua.token;

import net.rptools.maptool.client.functions.TokenMoveFunctions;
import net.rptools.maptool.client.lua.MapToolToken;
import net.rptools.maptool.language.I18N;
import net.rptools.parser.ParserException;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;

public class Movement extends ZeroArgFunction {
	
	MapToolToken token;
	
	public Movement(MapToolToken token) {
		super();
		this.token = token;
	}
	@Override
	public LuaValue call() {
		if (!token.isSelfOrTrusted()) {
			throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", "token.getMovement")));
		}
		try {
			Object res = TokenMoveFunctions.getMovementValue(token.getToken());
			if (res instanceof Integer) {
				return valueOf(((Integer) res).intValue());
			} else if (res instanceof Double) {
				return valueOf(((Double) res).doubleValue());
			}
		} catch (ParserException e) {
			throw new LuaError(e);
		}
		return NIL;
	}

}
