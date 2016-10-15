/**
 * 
 */
package net.rptools.maptool.client.lua.misc;

import net.rptools.maptool.client.MapTool;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;

/**
 * @author Maluku
 *
 */
public class IsTrusted extends ZeroArgFunction {
	@Override
	public LuaValue call() {
		return LuaValue.valueOf(MapTool.getParser().isMacroTrusted());
	}
}
