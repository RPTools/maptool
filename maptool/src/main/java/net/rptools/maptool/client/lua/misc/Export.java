/**
 * 
 */
package net.rptools.maptool.client.lua.misc;

import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.lua.LuaConverters;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableModifiers;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;

/**
 * @author Maluku
 *
 */
public class Export extends TwoArgFunction {
	private final MapToolVariableResolver resolver;
	public Export(MapToolVariableResolver resolver) {
		this.resolver = resolver;
	}
	@Override
	public LuaValue call(LuaValue name, LuaValue value) {
		Object v = LuaConverters.toJson(value).toString();
		try {
			resolver.setVariable(name.checkjstring(), VariableModifiers.None, v);
		} catch (ParserException e) {
			throw new LuaError(e);
		}
		return name;
	}

}
