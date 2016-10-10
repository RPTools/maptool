/**
 * 
 */
package net.rptools.maptool.client.lua.token;

import net.rptools.maptool.client.lua.MapToolToken;
import net.rptools.maptool.language.I18N;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;

/**
 * @author Maluku
 *
 */
public class GetOwners extends ZeroArgFunction {
	MapToolToken token;
	public GetOwners(MapToolToken token) {
		this.token = token;
	}
	/* (non-Javadoc)
	 * @see org.luaj.vm2.lib.ZeroArgFunction#call()
	 */
	@Override
	public LuaValue call() {
		if (!token.isSelfOrTrusted()) {
			throw new LuaError(I18N.getText("macro.function.general.noPerm", "getOwners"));
		}
		LuaTable owners = new LuaTable();
		for (String owner: token.getToken().getOwners()) {
			owners.insert(0, LuaValue.valueOf(owner));
		}
		return owners;
	}

}
