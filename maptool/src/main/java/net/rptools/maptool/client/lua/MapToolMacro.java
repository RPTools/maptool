/**
 * 
 */
package net.rptools.maptool.client.lua;

import net.rptools.maptool.client.MapToolMacroContext;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.lua.misc.Abort;
import net.rptools.maptool.client.lua.misc.Arg;
import net.rptools.maptool.client.lua.misc.Exec;
import net.rptools.maptool.client.lua.misc.IsTrusted;
import net.rptools.maptool.client.lua.misc.Link;
import net.rptools.maptool.client.lua.misc.MacroCall;
import net.rptools.maptool.model.Token;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableModifiers;

import org.apache.commons.lang.ObjectUtils;
import org.luaj.vm2.Globals;
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
	private Token tokenInContext;
	private Globals globals;
	private MapToolMacroContext context;

	public MapToolMacro(MapToolVariableResolver resolver, Token tokenInContext, Globals globals, MapToolMacroContext context) {
		Object args = null;
		this.globals = globals;
		this.tokenInContext = tokenInContext;
		this.context = context;
		try {
			args = resolver.getVariable("macro.args", VariableModifiers.None);
		} catch (ParserException e) {
		}
		super.rawset(LuaValue.valueOf("args"), args == null ? LuaValue.NIL : LuaConverters.fromJson(ObjectUtils.toString(args)));
		super.rawset(LuaValue.valueOf("abort"), new Abort());
		super.rawset(LuaValue.valueOf("arg"), new Arg(resolver));
		super.rawset(LuaValue.valueOf("link"), new Link(true));
		super.rawset(LuaValue.valueOf("linkText"), new Link(false));
		super.rawset(LuaValue.valueOf("execLink"), new ExecLink());
		super.rawset(LuaValue.valueOf("exec"), new Exec(resolver, true, true, true));
		super.rawset(LuaValue.valueOf("eval"), new Exec(resolver, false, true, true));
		super.rawset(LuaValue.valueOf("execUntrusted"), new Exec(resolver, true, false, true));
		super.rawset(LuaValue.valueOf("evalUntrusted"), new Exec(resolver, false, false, true));
		super.rawset(LuaValue.valueOf("execRaw"), new Exec(resolver, true, true, false));
		super.rawset(LuaValue.valueOf("evalRaw"), new Exec(resolver, false, true, false));
		super.rawset(LuaValue.valueOf("execUntrustedRaw"), new Exec(resolver, true, false, false));
		super.rawset(LuaValue.valueOf("evalUntrustedRaw"), new Exec(resolver, false, false, false));
		super.rawset(LuaValue.valueOf("call"), new MacroCall(resolver, this.tokenInContext, null, true));
		super.rawset(LuaValue.valueOf("run"), new MacroCall(resolver, this.tokenInContext, this.globals, true));
		super.rawset(LuaValue.valueOf("callRaw"), new MacroCall(resolver, this.tokenInContext, null, false));
		super.rawset(LuaValue.valueOf("runRaw"), new MacroCall(resolver, this.tokenInContext, this.globals, false));
		super.rawset(LuaValue.valueOf("isTrusted"), new IsTrusted());
		super.rawset(LuaValue.valueOf("name"), valOf(this.context != null ? this.context.getName() : "Chat"));
		super.rawset(LuaValue.valueOf("location"), valOf(this.context != null ? this.context.getSouce() : "Chat"));
		super.rawset(LuaValue.valueOf("buttonIndex"), valueOf(this.context != null ? this.context.getMacroButtonIndex() : -1));
		this.resolver = resolver;
	}

	private LuaValue valOf(String text) {
		if (text == null) return null;
		return valueOf(text);
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
