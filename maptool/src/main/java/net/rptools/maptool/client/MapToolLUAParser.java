package net.rptools.maptool.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import net.rptools.maptool.client.lua.FromJson;
import net.rptools.maptool.client.lua.LuaConverters;
import net.rptools.maptool.client.lua.MapToolBaseLib;
import net.rptools.maptool.client.lua.MapToolGlobals;
import net.rptools.maptool.client.lua.MapToolMacro;
import net.rptools.maptool.client.lua.MapToolToken;
import net.rptools.maptool.client.lua.Print;
import net.rptools.maptool.client.lua.Println;
import net.rptools.maptool.client.lua.ToJson;
import net.rptools.maptool.client.lua.TokensLib;
import net.rptools.maptool.client.lua.token.CopyToken;
import net.rptools.maptool.client.lua.token.SelectToken;
import net.rptools.maptool.model.Token;
import net.rptools.parser.ParserException;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LoadState;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.BaseLib;
import org.luaj.vm2.lib.Bit32Lib;
import org.luaj.vm2.lib.PackageLib;
import org.luaj.vm2.lib.StringLib;
import org.luaj.vm2.lib.TableLib;
import org.luaj.vm2.lib.jse.JseBaseLib;
import org.luaj.vm2.lib.jse.JseMathLib;

/**
 * 
 */

/**
 * @author Maluku
 *
 */
public class MapToolLUAParser {
	public static final String LUA_HEADER = "--{abort(0)} LUA--";
	static Globals globals;
	public MapToolLUAParser() {
		globals = new Globals();
		globals.load(new JseBaseLib());
		globals.load(new PackageLib());
		globals.load(new StringLib());
		globals.load(new JseMathLib());
		LoadState.install(globals);
		LuaC.install(globals);
		LuaString.s_metatable = new ReadOnlyLuaTable(LuaString.s_metatable);
	}
	public String parseLine(MapToolVariableResolver res, Token tokenInContext, String line, MapToolMacroContext context) throws ParserException {
		
		BaseLib base = new MapToolBaseLib(res, tokenInContext, context);
		Globals user_globals = new MapToolGlobals(res);
		user_globals.load(base);
		user_globals.load(new PackageLib());
		user_globals.load(new Bit32Lib());
		user_globals.load(new TableLib());
		user_globals.load(new StringLib());
		user_globals.load(new JseMathLib());
		user_globals.load(new JseMathLib());
		user_globals.load(new TokensLib());
		TokensLib.resolver = res;
		user_globals.set("print", new Print(base, user_globals));
		user_globals.set("println", new Println(base, user_globals));
		user_globals.set("fromJSON", new FromJson());
		user_globals.set("toJSON", new ToJson());
		user_globals.set("token", new MapToolToken(tokenInContext, true));
		user_globals.set("copyToken", new CopyToken(res));
		user_globals.set("selectTokens", new SelectToken(false));
		user_globals.set("deselectTokens", new SelectToken(true));
		user_globals.set("tokenProperties", LuaValue.NIL);
		user_globals.set("macro", new MapToolMacro(res));
		
		
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		user_globals.STDOUT = new PrintStream(bo);
		try {
			LuaValue chunk = globals.load(new ByteArrayInputStream(line.getBytes()) , "main", "t", user_globals);
			LuaValue macroReturn = chunk.call();
			if (macroReturn.isnoneornil(1)) {
				res.setVariable("macro.return", null);
			} else {
				res.setVariable("macro.return", LuaConverters.toJson(macroReturn));
			}
			
			if (macroReturn instanceof LuaFunction) {
				
				//TODO
	//			Varargs args = LuaValue.varargsOf(null)
	//			macroReturn = chunk.invoke(args);
			}
		} catch (LuaError e) {
			if (e.getCause() instanceof ParserException) {
				throw (ParserException)e.getCause();
			}
			else {
				throw new ParserException(e);
			}
		}
		try {
			bo.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return bo.toString();
	}
}
