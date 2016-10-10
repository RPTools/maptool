/**
 * 
 */
package net.rptools.maptool.client.lua.token;

import net.rptools.maptool.client.functions.TokenPropertyFunctions;
import net.rptools.maptool.client.lua.MapToolToken;
import net.rptools.maptool.language.I18N;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;

/**
 * @author Maluku
 *
 */
public class MatchingProperties extends TwoArgFunction {
	MapToolToken token;
	public MatchingProperties(MapToolToken token) {
		this.token = token;
	}
	/* (non-Javadoc)
	 * @see org.luaj.vm2.lib.OneArgFunction#call(org.luaj.vm2.LuaValue)
	 */
	@Override
	public LuaValue call(LuaValue pattern, LuaValue raw) {
		if (!token.isSelfOrTrusted()) {
			throw new LuaError(I18N.getText("macro.function.general.noPerm", "getMatchingProperties"));
		}
		LuaTable result = new LuaTable();
		
		String pat = ".*";
		if (pattern.isstring()) {
			pat = pattern.tojstring();
		}
		boolean r = false;
		if (raw.isboolean()) {
			r = raw.checkboolean();
		}
		for (String prop: TokenPropertyFunctions.getPropertyNames(token.getToken(), pat, r)) {
			result.insert(0, LuaValue.valueOf(prop));
		}
		return result;
	}

}
