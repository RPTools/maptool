/**
 * 
 */
package net.rptools.maptool.client.lua.misc;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.IsTrustedFunction;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;

/**
 * @author Maluku
 *
 */
public class IsGM extends OneArgFunction {
	@Override
	public LuaValue call(LuaValue arg) {
		if (arg.isnil())
			return LuaValue.valueOf(MapTool.getPlayer().isGM());
		else {
			return LuaValue.valueOf(IsTrustedFunction.getGMs().contains(arg.checkjstring()));
		}
	}

}
