/**
 * 
 */
package net.rptools.maptool.client.lua.token;

import net.rptools.maptool.client.lua.MapToolToken;
import net.rptools.maptool.client.lua.TokenProperties;
import net.rptools.maptool.language.I18N;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;

/**
 * @author Maluku
 *
 */
public class MatchingProps extends OneArgFunction {
	MapToolToken token;

	public MatchingProps(MapToolToken token) {
		this.token = token;
	}

	/* (non-Javadoc)
	 * @see org.luaj.vm2.lib.OneArgFunction#call(org.luaj.vm2.LuaValue)
	 */
	@Override
	public LuaValue call(LuaValue pattern) {
		if (!token.isSelfOrTrusted()) {
			throw new LuaError(I18N.getText("macro.function.general.noPerm", "getMatchingProperties"));
		}
		return new TokenProperties(token, pattern.checkjstring());
	}

}
