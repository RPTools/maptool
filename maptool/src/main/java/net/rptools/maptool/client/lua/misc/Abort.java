/**
 * 
 */
package net.rptools.maptool.client.lua.misc;

import java.math.BigDecimal;

import net.rptools.maptool.client.functions.AbortFunction.AbortFunctionException;
import net.rptools.maptool.language.I18N;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.BaseLib;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.VarArgFunction;

/**
 * @author Maluku
 *
 */
public class Abort extends OneArgFunction {

	@Override
	public LuaValue call(LuaValue arg) {
		if (arg.isnil() || (arg.isnumber() && arg.toint() == 0) || (arg.isboolean() && !arg.toboolean())) {
			throw new LuaError(new AbortFunctionException(I18N.getText("macro.function.abortFunction.message", "Abort()")));
		}
		return arg;
	}
	
}
