/**
 * 
 */
package net.rptools.maptool.client.lua.misc;

import java.math.BigDecimal;

import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableModifiers;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;

/**
 * @author Maluku
 *
 */
public class ArgCount extends ZeroArgFunction {
	MapToolVariableResolver resolver;

	public ArgCount(MapToolVariableResolver resolver) {
		this.resolver = resolver;
	}

	@Override
	public LuaValue call() {
		Object numArgs = 0;
		try {
			numArgs = resolver.getVariable("macro.args.num", VariableModifiers.None);
			if (numArgs instanceof BigDecimal) {
				return LuaValue.valueOf(((BigDecimal) numArgs).intValue());
			}
			return LuaValue.valueOf(0);
		} catch (ParserException e) {
			throw new LuaError(e);
		}
	}

}
