package net.rptools.maptool.client.lua.token;

import net.rptools.maptool.client.lua.MapToolToken;
import net.rptools.maptool.language.I18N;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;

public class HasMacro extends OneArgFunction {
	private MapToolToken token;
	public HasMacro(MapToolToken token) {
		this.token = token;
	}
	
	@Override
	public LuaValue call(LuaValue arg) {
		if (!token.isSelfOrTrusted()) {
			throw new LuaError(I18N.getText("macro.function.general.noPerm", "hasMacro"));
		}
		return LuaValue.valueOf(token.getToken().getMacroNames(false).contains(arg.checkjstring()));
	}

}
