package net.rptools.maptool.client;

import net.rptools.maptool.client.MapToolMacroContext;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.jse.*;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.model.Token;
import net.rptools.parser.ParserException;

/**
 * 
 */

/**
 * @author Maluku
 *
 */
public class MapToolLUAParser {

	public static final String LUA_HEADER = "--{abort(0)} LUA--";

	public String parseLine(MapToolVariableResolver res, Token tokenInContext, String line, MapToolMacroContext context) throws ParserException {
		return "Foo";
	}
}
