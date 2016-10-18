package net.rptools.maptool.client.lua.misc;

import java.util.IdentityHashMap;
import java.util.Map;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolMacroContext;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.lua.Macro;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

public class Exec extends VarArgFunction {

	private MapToolVariableResolver resolver;
	private Map<LuaValue, Varargs> seen = new IdentityHashMap<LuaValue, Varargs>();
	boolean newContext = false;
	public Exec(MapToolVariableResolver resolver, boolean newContext) {
		this.resolver = resolver;
		this.newContext = newContext;
	}
	@Override
	public Varargs invoke(Varargs args) {
		seen.clear();
		Varargs v = exec(args.arg(1), args.subargs(2));
		seen.clear();
		return v;
	}

	private Varargs exec(LuaValue input, Varargs args) {
		if (seen.containsKey(input)) {
			return seen.get(input);
		}
		seen.put(input, NIL);
		if (input.istable()) {
			LuaTable result = new LuaTable();
			LuaTable output = new LuaTable();
			LuaTable raw = new LuaTable();
			LuaTable table = input.checktable();
			for (Varargs v = table.next(NIL); !v.isnil(1); v = table.next(v.arg1())) {
				LuaValue key = v.arg1();
				Varargs res = exec(v.arg(2), args);
				result.rawset(key, res.arg(1));
				output.rawset(key, res.arg(2));
				raw.rawset(key, res.arg(3));
			}
			Varargs v = varargsOf(result, output, raw);
			seen.put(input, v);
			return v;
		} else if (input.isstring()) {
			MapToolMacroContext context = new MapToolMacroContext("<dynamic>", MapTool.getParser().getContext().getSouce(), true);
			MapToolVariableResolver res = resolver;
			if (newContext) {
				res = new MapToolVariableResolver(resolver.getTokenInContext());
			}
			Varargs v = Macro.runMacro(res, res.getTokenInContext(), context, input.toString(), args);
			seen.put(input, v);
			return v;
		} else {
			Varargs v = varargsOf(NIL, input, NIL);
			seen.put(input, v);
			return v;
		}
		
	}
	
}
