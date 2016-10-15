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
public class ToStr extends ThreeArgFunction {
	@Override
	public LuaValue call(LuaValue val, LuaValue listSep, LuaValue propSep) {
		return LuaValue.valueOf(LuaConverters.toStr(val, listSep.isstring() || listSep.isnumber() ? listSep.tojstring() : ",", propSep.isstring() || propSep.isnumber() ? propSep.tojstring() : ";"));
	}

}
