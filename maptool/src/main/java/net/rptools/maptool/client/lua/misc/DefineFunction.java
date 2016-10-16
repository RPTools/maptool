package net.rptools.maptool.client.lua.misc;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.functions.UserDefinedMacroFunctions;
import net.rptools.parser.ParserException;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

public class DefineFunction extends VarArgFunction {
	private MapToolVariableResolver resolver;

	public DefineFunction(MapToolVariableResolver res) {
		this.resolver = res;
	}

	@Override
	public Varargs invoke(Varargs args) {
		try {
			UserDefinedMacroFunctions.getInstance().defineFunction(MapTool.getParser().createParser(resolver, resolver.getTokenInContext() != null).getParser(), args.arg1().checkjstring(), args.arg(2).checkjstring(), args.arg(3).isboolean() ? args.arg(3).toboolean() : false, args.arg(4).isboolean() ? args.arg(4).toboolean() : false);
			return NONE;
		} catch (ParserException e) {
			throw new LuaError(e);
		}
	}
}
