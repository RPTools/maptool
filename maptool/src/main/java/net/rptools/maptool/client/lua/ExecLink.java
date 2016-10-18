package net.rptools.maptool.client.lua;

import net.rptools.maptool.client.functions.MacroLinkFunction;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;

public class ExecLink extends TwoArgFunction {

	@Override
	public LuaValue call(LuaValue arg1, LuaValue arg2) {
		MacroLinkFunction.execLink(arg1.checkjstring(), arg2.isboolean() ? arg2.toboolean() : false);
		return NIL;
	}

}
