/**
 * 
 */
package net.rptools.maptool.client.lua;

import java.io.InputStream;

import net.rptools.maptool.client.MapToolMacroContext;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.model.Token;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.BaseLib;

/**
 * @author Maluku
 *
 */
public class MapToolBaseLib extends BaseLib {
	Globals globals;

	public MapToolBaseLib(MapToolVariableResolver res, Token tokenInContext, MapToolMacroContext context) {
	}

	@Override
	public LuaValue call(LuaValue modname, LuaValue env) {
		super.call(modname, env);
		globals = env.checkglobals();
		globals.STDIN = System.in;
		return env;
	}

	@Override
	public InputStream findResource(String macroname) {
		throw new RuntimeException("File not found");
	}
}
