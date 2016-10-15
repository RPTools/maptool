/**
 * 
 */
package net.rptools.maptool.client.lua.misc;

import net.rptools.maptool.client.functions.AbortFunction.AbortFunctionException;
import net.rptools.maptool.language.I18N;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;

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
