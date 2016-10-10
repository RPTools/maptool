/**
 * 
 */
package net.rptools.maptool.client.lua;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;

/**
 * @author Maluku
 *
 */
public class ToJson extends OneArgFunction {

	/* (non-Javadoc)
	 * @see org.luaj.vm2.lib.OneArgFunction#call(org.luaj.vm2.LuaValue)
	 */
	@Override
	public LuaValue call(LuaValue arg) {
		return LuaValue.valueOf(LuaConverters.toJson(arg).toString());
	}

}
