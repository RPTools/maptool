/**
 * 
 */
package net.rptools.maptool.client.lua.misc;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.BaseLib;
import org.luaj.vm2.lib.VarArgFunction;

/**
 * @author Maluku
 *
 */
public class Print extends VarArgFunction {
	final BaseLib baselib;
	final Globals globals;

	public Print(BaseLib baselib, Globals globals) {
		this.baselib = baselib;
		this.globals = globals;
	}

	public Varargs invoke(Varargs args) {
		LuaValue tostring = globals.get("tostring");
		for (int i = 1, n = args.narg(); i <= n; i++) {
			LuaString s = tostring.call(args.arg(i)).strvalue();
			globals.STDOUT.print(s.tojstring());
		}
		return NONE;
	}
}
