/**
 * 
 */
package net.rptools.maptool.client.lua.token;

import java.util.ArrayList;
import java.util.List;

import net.rptools.maptool.client.functions.TokenLightFunctions;
import net.rptools.maptool.client.lua.MapToolToken;
import net.rptools.maptool.language.I18N;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;

/**
 * @author Maluku
 *
 */
public class HasLights extends TwoArgFunction {
	MapToolToken token;
	public HasLights(MapToolToken token) {
		this.token = token;
	}
	/* (non-Javadoc)
	 * @see org.luaj.vm2.lib.TwoArgFunction#call(org.luaj.vm2.LuaValue, org.luaj.vm2.LuaValue)
	 */
	@Override
	public LuaValue call(LuaValue cat, LuaValue name) {
		if (!token.isSelfOrTrusted()) {
			throw new LuaError(I18N.getText("macro.function.general.noPerm", "hasLights"));
		}
		List<Object> params = new ArrayList<Object>();
		if (cat.isstring()) {
			params.add(cat.tojstring());
			if (name.isstring()) {
				params.add(name.checkjstring());
			}
		}
		return LuaValue.valueOf(TokenLightFunctions.hasLightSource(token.getToken(), params));
	}

}
