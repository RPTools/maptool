/**
 * 
 */
package net.rptools.maptool.client.lua;

import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.lua.misc.Abort;
import net.rptools.maptool.client.lua.misc.Arg;
import net.rptools.maptool.client.lua.misc.ArgCount;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableModifiers;

import org.apache.commons.lang.ObjectUtils;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

/**
 * @author Maluku
 *
 */

public class MapToolMacro extends LuaTable {
	private MapToolVariableResolver resolver;

	public MapToolMacro(MapToolVariableResolver resolver) {
		Object args = null;
		try {
			args = resolver.getVariable("macro.args", VariableModifiers.None);
		} catch (ParserException e) {
		}
		super.rawset(LuaValue.valueOf("args"), args == null ? LuaValue.NIL : LuaConverters.fromJson(ObjectUtils.toString(args)));
		super.rawset(LuaValue.valueOf("abort"), new Abort());
		super.rawset(LuaValue.valueOf("arg"), new Arg(resolver));
		super.rawset(LuaValue.valueOf("argCount"), new ArgCount(resolver));
		this.resolver = resolver;
	}

	public LuaValue setmetatable(LuaValue metatable) {
		return error("table is read-only");
	}

	public void set(int key, LuaValue value) {
		error("table is read-only");
	}

	public void rawset(int key, LuaValue value) {
		error("table is read-only");
	}

	public void rawset(LuaValue key, LuaValue value) {
		if (key.isstring()) {
			if (key.checkjstring().equals("return")) {
				Object val;
				if (value.isnil()) {
					val = null;
				} else {
					val = LuaConverters.toJson(value);
				}
				try {
					resolver.setVariable("macro.return", VariableModifiers.None, val);
				} catch (ParserException e) {
					throw new LuaError(e);
				}
				return;
			}
		}
		error("table is read-only, except for return");
	}

	public LuaValue remove(int pos) {
		return error("table is read-only");
	}

	public String tojstring() {
		return "Macro Properties";
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
}
