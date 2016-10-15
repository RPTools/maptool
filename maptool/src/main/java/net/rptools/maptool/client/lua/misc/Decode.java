package net.rptools.maptool.client.lua.misc;

import net.rptools.maptool.client.lua.LuaConverters;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;

public class Decode extends TwoArgFunction {

	@Override
	public LuaValue call(LuaValue arg1, LuaValue arg2) {
		return LuaValue.valueOf(LuaConverters.decode(arg1.checkjstring(), arg2.isboolean() ? arg2.toboolean() : true));
	}

}
