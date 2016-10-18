package net.rptools.maptool.client.lua.misc;

import java.math.BigDecimal;

import net.rptools.common.expression.Result;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.lua.LuaConverters;
import net.rptools.parser.ParserException;
import net.sf.json.JSONArray;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

public class Eval extends VarArgFunction {

	private MapToolVariableResolver resolver;
	boolean newContext = false;
	public Eval(MapToolVariableResolver resolver, boolean newContext) {
		this.resolver = resolver;
		this.newContext = newContext;
	}
	@Override
	public Varargs invoke(Varargs args) {
		MapToolVariableResolver macroResolver = resolver;
		String macro = args.checkjstring(1);
		if (newContext) {
			macroResolver = new MapToolVariableResolver(resolver.getTokenInContext());
		}
		JSONArray arr = new JSONArray();
		try {
			macroResolver.setVariable("macro.args.num", BigDecimal.valueOf(args.narg() - 1));
			for (int i = 2; i <= args.narg(); i++) {
				Object res = LuaConverters.toJson(args.arg(i));
				macroResolver.setVariable("macro.args." + (i - 2), res);
				arr.add(res);
			}
			macroResolver.setVariable("macro.args", arr);
			macroResolver.setVariable("macro.return", "");
			Result r = MapTool.getParser().parseExpression(macroResolver, macroResolver.getTokenInContext(), macro);
			return varargsOf(LuaConverters.fromJson(r.getValue()), LuaConverters.fromObj(r.getValue()));
		} catch (ParserException e) {
			throw new LuaError(e);
		}
	}	
}
