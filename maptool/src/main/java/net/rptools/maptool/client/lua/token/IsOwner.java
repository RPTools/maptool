/**
 * 
 */
package net.rptools.maptool.client.lua.token;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.lua.MapToolToken;
import net.rptools.maptool.language.I18N;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;

/**
 * @author Maluku
 *
 */
public class IsOwner extends OneArgFunction {
	MapToolToken token;

	public IsOwner(MapToolToken token) {
		this.token = token;
	}

	/* (non-Javadoc)
	 * @see org.luaj.vm2.lib.ZeroArgFunction#call()
	 */
	@Override
	public LuaValue call(LuaValue owner) {
		if (!token.isSelfOrTrusted()) {
			throw new LuaError(I18N.getText("macro.function.general.noPerm", "isOwner"));
		}
		String name = MapTool.getPlayer().getName();
		if (!owner.isnil()) {
			name = owner.checkjstring();
		}
		return valueOf(token.getToken().isOwner(name));
	}

}
