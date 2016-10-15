/**
 * 
 */
package net.rptools.maptool.client.lua.misc;

import net.rptools.maptool.client.lua.LuaConverters;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;

/**
 * @author Maluku
 *
 */
public class FromJson extends OneArgFunction {

	/* (non-Javadoc)
	 * @see org.luaj.vm2.lib.OneArgFunction#call(org.luaj.vm2.LuaValue)
	 */
	@Override
	public LuaValue call(LuaValue arg) {
		return LuaConverters.fromJson(arg.checkstring().tojstring());
	}

}
