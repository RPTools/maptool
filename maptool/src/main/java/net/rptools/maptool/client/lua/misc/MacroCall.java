package net.rptools.maptool.client.lua.misc;

import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.lua.MapToolFunction;
import net.rptools.maptool.model.Token;

import org.luaj.vm2.Globals;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

public class MacroCall extends VarArgFunction {

	private MapToolVariableResolver resolver;
	private Token token;
	private Globals globals;

	public MacroCall(MapToolVariableResolver resolver, Token tokenInContext, Globals globals) {
		this.resolver = resolver;
		this.token = tokenInContext;
		this.globals = globals;
	}
	@Override
	public Varargs invoke(Varargs args) {
		String macro = args.checkjstring(1);
		Varargs v = MapToolFunction.runMacro(resolver, token, macro, false, true, args.subargs(2), false);
		if (globals != null) {
			globals.STDOUT.print(v.arg(2).toString());
			return varargsOf(v.arg1(), v.subargs(3));
		}
		return v;
	}

}
