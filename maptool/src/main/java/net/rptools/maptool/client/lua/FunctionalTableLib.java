package net.rptools.maptool.client.lua;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.LibFunction;
import org.luaj.vm2.lib.TableLib;

public class FunctionalTableLib extends TableLib {
	public LuaValue call(LuaValue modname, LuaValue env) {
		LuaValue result = super.call(modname, env);
		LuaTable table = env.get("table").checktable();
		table.set("map", new map());
		table.set("reduce", new reduce());
		table.set("length", new length());
		return result;
	}
	
	static class TableLibFunction extends LibFunction {
		public LuaValue call() {
			return argerror(1, "table expected, got no value");
		}
	}
	
	static class map extends TableLibFunction {
		public LuaValue call(LuaValue list) {
			return argerror(2, "function expected, got no value");
		}
		public LuaValue call(LuaValue list, LuaValue func) {
			LuaTable result = new LuaTable();
			func.checkfunction();
			for (LuaValue val: LuaConverters.arrayIterate(list.checktable())) {
				result.insert(0, func.call(val));
			}
			return result;
		}
	}

	static class reduce extends TableLibFunction {
		public LuaValue call(LuaValue list) {
			return argerror(2, "function expected, got no value");
		}
		public LuaValue call(LuaValue list, LuaValue func) {
			LuaValue result = NIL;
			func.checkfunction();
			for (LuaValue val: LuaConverters.arrayIterate(list.checktable())) {
				result= func.call(result, val);
			}
			return result;
		}
	}
	static class length extends TableLibFunction {
		public LuaValue call(LuaValue list) {
			LuaTable l = list.checktable();
			int count = 0;
			Varargs next = l.next(LuaValue.NIL);
			while (!next.isnil(1)) {
				count++;
				next = l.next(next.arg1());
			}
			return LuaValue.valueOf(count);
		}
	}
	
}
