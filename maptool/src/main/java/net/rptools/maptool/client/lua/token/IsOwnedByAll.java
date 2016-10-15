/**
 * 
 */
package net.rptools.maptool.client.lua.token;

import net.rptools.maptool.client.lua.MapToolToken;
import net.rptools.maptool.language.I18N;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;

/**
 * @author Maluku
 *
 */
public class IsOwnedByAll extends ZeroArgFunction {
	MapToolToken token;

	public IsOwnedByAll(MapToolToken token) {
		this.token = token;
	}

	/* (non-Javadoc)
	 * @see org.luaj.vm2.lib.ZeroArgFunction#call()
	 */
	@Override
	public LuaValue call() {
		if (!token.isSelfOrTrusted()) {
			throw new LuaError(I18N.getText("macro.function.general.noPerm", "isOwnedByAll"));
		}
		return valueOf(token.getToken().isOwnedByAll());
	}

}
