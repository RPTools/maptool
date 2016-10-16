/**
 * 
 */
package net.rptools.maptool.client.lua;

import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.functions.UserDefinedMacroFunctions;

import org.apache.commons.lang.ObjectUtils;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

/**
 * @author Maluku
 *
 */

public class MapToolFunctions extends LuaTable {
	private MapToolVariableResolver resolver;

	public MapToolFunctions(MapToolVariableResolver resolver) {
		this.resolver = resolver;
	}

	public LuaValue setmetatable(LuaValue metatable) {
		return error("table is read-only");
	}

	public void set(int key, LuaValue value) {
		error("table is read-only, use defineFunction()");
	}

	public void rawset(int key, LuaValue value) {
		error("table is read-only, use defineFunction()");
	}

	public void rawset(LuaValue key, LuaValue value) {
		error("table is read-only, use defineFunction()");
	}

	public LuaValue remove(int pos) {
		return error("table is read-only");
	}

	@Override
	public LuaValue rawget(LuaValue key) {
		if (UserDefinedMacroFunctions.getInstance().getUserDefinedFunctions().containsKey(key.checkjstring())) {
			return new MapToolFunction(key.tojstring(), resolver);
		}
		return NIL;
	}

	public String tojstring() {
		return "User defined Functions";
	}

	@Override
	public LuaValue tostring() {
		return LuaValue.valueOf(tojstring());
	}

	@Override
	public LuaString checkstring() {
		return LuaValue.valueOf(tojstring());
	}

	@Override
	public String toString() {
		return tojstring();
	}

	@Override
	public int length() {
		return UserDefinedMacroFunctions.getInstance().getUserDefinedFunctions().size();
	}

	@Override
	public Varargs next(LuaValue key) {
		boolean found = false;
		String name = null;
		if (key.isnil()) {
			found = true;
		} else {
			name = key.checkjstring();
		}
		for (String s: UserDefinedMacroFunctions.getInstance().getUserDefinedFunctions().keySet()) {
			if (s == null) {
				continue;
			}
			if (found && !ObjectUtils.equals(s, name)) {
				return varargsOf(valueOf(s), new MapToolFunction(s, resolver));
			}
			if (!found && name.equals(s)) {
				found = true;
			}
		}
		return NIL;
	}
}
