/**
 * 
 */
package net.rptools.maptool.client.lua.misc;

import net.rptools.maptool.client.lua.LuaConverters;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ThreeArgFunction;

/**
 * @author Maluku
 *
 */
public class FromStr extends ThreeArgFunction {
	@Override
	public LuaValue call(LuaValue val, LuaValue listSep, LuaValue propSep) {
		return LuaConverters.fromStr(val.checkstring().tojstring(), listSep.isstring() || listSep.isnumber() ? listSep.tojstring() : ",", propSep.isstring() || propSep.isnumber() ? propSep.tojstring() : ";");
	}

}
