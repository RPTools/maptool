package net.rptools.maptool.client.lua.misc;

import java.util.ArrayList;
import java.util.List;

import net.rptools.maptool.client.lua.LuaConverters;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

public class Encode extends VarArgFunction {

	@Override
	public LuaValue invoke(Varargs args) {
		LuaValue arg1 = args.arg(1); 
		LuaValue arg2 = args.arg(2);
		List<String> vals = new ArrayList<String>();
		for (int i = 3; !args.arg(i).isnil(); i++) {
			vals.add(args.arg(i).checkjstring());
		}
		return LuaValue.valueOf(LuaConverters.encode(arg1.checkjstring(), arg2.isboolean() ? arg2.toboolean() : true, vals.toArray(new String[0])));
	}

}
