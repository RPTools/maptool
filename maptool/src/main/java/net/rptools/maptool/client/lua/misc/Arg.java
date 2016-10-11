/**
 * 
 */
package net.rptools.maptool.client.lua.misc;

import java.math.BigDecimal;

import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.lua.LuaConverters;
import net.rptools.maptool.language.I18N;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableModifiers;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;

/**
 * @author Maluku
 *
 */
public class Arg extends OneArgFunction {
	MapToolVariableResolver resolver;
	public Arg(MapToolVariableResolver resolver) {
		this.resolver = resolver;
	}
	@Override
	public LuaValue call(LuaValue arg) {
		Object numArgs = 0;
		try {
			numArgs = resolver.getVariable("macro.args.num", VariableModifiers.None);
			int argCount = 0;
			if (numArgs instanceof BigDecimal) {
				argCount = ((BigDecimal) numArgs).intValue();
			}
			int argNo = arg.checkint();
			if (argCount == 0 && argNo == 0) {
				return LuaConverters.fromJson(resolver.getVariable("macro.args"));
			}
	
			if (argNo < 0 || argNo >= argCount) {
				throw new LuaError(new ParserException(I18N.getText("macro.function.args.outOfRange", "arg", argNo, argCount - 1)));
			}
	
			return LuaConverters.fromJson(resolver.getVariable("macro.args." + argNo));
		} catch (ParserException e) {
			throw new LuaError(e);
		}
	}
	
}
