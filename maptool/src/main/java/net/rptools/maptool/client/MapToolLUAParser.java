package net.rptools.maptool.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import net.rptools.maptool.client.lua.ChatLib;
import net.rptools.maptool.client.lua.DiceLib;
import net.rptools.maptool.client.lua.ExtendedStringLib;
import net.rptools.maptool.client.lua.FunctionalTableLib;
import net.rptools.maptool.client.lua.LuaConverters;
import net.rptools.maptool.client.lua.MapToolBaseLib;
import net.rptools.maptool.client.lua.MapToolFunctions;
import net.rptools.maptool.client.lua.MapToolGlobals;
import net.rptools.maptool.client.lua.MapToolIniative;
import net.rptools.maptool.client.lua.MapToolMacro;
import net.rptools.maptool.client.lua.MapToolMaps;
import net.rptools.maptool.client.lua.MapToolTables;
import net.rptools.maptool.client.lua.MapToolToken;
import net.rptools.maptool.client.lua.TokensLib;
import net.rptools.maptool.client.lua.UILib;
import net.rptools.maptool.client.lua.VBLLib;
import net.rptools.maptool.client.lua.misc.Decode;
import net.rptools.maptool.client.lua.misc.DefineFunction;
import net.rptools.maptool.client.lua.misc.Encode;
import net.rptools.maptool.client.lua.misc.Eval;
import net.rptools.maptool.client.lua.misc.Export;
import net.rptools.maptool.client.lua.misc.FromJson;
import net.rptools.maptool.client.lua.misc.FromStr;
import net.rptools.maptool.client.lua.misc.Info;
import net.rptools.maptool.client.lua.misc.IsGM;
import net.rptools.maptool.client.lua.misc.Print;
import net.rptools.maptool.client.lua.misc.Println;
import net.rptools.maptool.client.lua.misc.ToJson;
import net.rptools.maptool.client.lua.misc.ToStr;
import net.rptools.maptool.model.Token;
import net.rptools.parser.ParserException;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LoadState;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Prototype;
import org.luaj.vm2.compiler.DumpState;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.BaseLib;
import org.luaj.vm2.lib.Bit32Lib;
import org.luaj.vm2.lib.PackageLib;
import org.luaj.vm2.lib.StringLib;
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
	public static final String LUA_HEADER = "--{assert(0, \"LUA\")}--";
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
		ByteArrayOutputStream bo;
		try {
			BaseLib base = new MapToolBaseLib(res, tokenInContext, context);
			Globals user_globals = new MapToolGlobals(res);
			user_globals.load(base);
			user_globals.load(new PackageLib());
			user_globals.load(new Bit32Lib());
			user_globals.load(new FunctionalTableLib());
			user_globals.load(new ExtendedStringLib(res));
			user_globals.load(new JseMathLib());
			user_globals.load(new JseMathLib());
			user_globals.load(new TokensLib(res));
			user_globals.load(new UILib());
			user_globals.load(new DiceLib());
			user_globals.load(new VBLLib());
			user_globals.load(new ChatLib(res, user_globals));
			user_globals.set("print", new Print(base, user_globals));
			user_globals.set("println", new Println(base, user_globals));
			user_globals.set("fromJSON", new FromJson());
			user_globals.set("toJSON", new ToJson());
			user_globals.set("fromStr", new FromStr());
			user_globals.set("toStr", new ToStr());
			user_globals.set("encode", new Encode());
			user_globals.set("decode", new Decode());
			user_globals.set("defineFunction", new DefineFunction(res));
			user_globals.set("eval", new Eval(res, false));
			user_globals.set("export", new Export(res));
			user_globals.set("token", new MapToolToken(tokenInContext, true, res));
			user_globals.set("tokenProperties", LuaValue.NIL);
			user_globals.set("macro", new MapToolMacro(res, tokenInContext, globals, context));
			user_globals.set("isGM", new IsGM());
			user_globals.set("maps", new MapToolMaps(res));
			user_globals.set("tables", new MapToolTables());
			user_globals.set("functions", new MapToolFunctions(res));
			user_globals.set("initiative", new MapToolIniative(res));
			user_globals.set("getInfo", new Info());
			user_globals.set("_LUA_HEADER", LUA_HEADER);
	
			bo = new ByteArrayOutputStream();
			user_globals.STDOUT = new PrintStream(bo);
			if (line.startsWith(LUA_HEADER)) {
				line = line.substring(LUA_HEADER.length());
			}
			LuaValue chunk = globals.load(new ByteArrayInputStream(line.getBytes()),
					(context != null ? context.getName() + "@" + context.getSouce() : "Chat") + (tokenInContext != null ? " (" + tokenInContext.getName() + ":" + tokenInContext.getId() + ")" : ""),
					"t", user_globals);
			LuaValue macroReturn = chunk.call();
			if (macroReturn.isnoneornil(1) ) {
//				res.setVariable("macro.return", null);
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
				throw (ParserException) e.getCause();
			} else {
				throw new ParserException(e);
			}
		} catch (Exception e) {
			throw new ParserException("Lua initialization Error: " + e.toString());
			
		}
		try {
			if (bo != null) bo.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bo.toString();
	}
	
	public static InputStream compile(InputStream in, String location, LuaValue user_globals) throws IOException {
		Prototype p = globals.loadPrototype(in, location, "t");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DumpState.dump(p, out, false);
		// We could save dump here and check if location has not been modified to cache the compiled results, but this thing is blazing fast anyway.
		return new ByteArrayInputStream(out.toByteArray());
	}
	
}

