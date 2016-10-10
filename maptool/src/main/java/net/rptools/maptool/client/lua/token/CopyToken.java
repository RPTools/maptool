/**
 * 
 */
package net.rptools.maptool.client.lua.token;

import java.util.ArrayList;
import java.util.List;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.functions.TokenCopyDeleteFunctions;
import net.rptools.maptool.client.lua.MapToolToken;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Token;
import net.rptools.parser.ParserException;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

import static net.rptools.maptool.client.lua.LuaConverters.toObj;
import static net.rptools.maptool.client.lua.LuaConverters.toJson;
/**
 * @author Maluku
 *
 */
public class CopyToken extends VarArgFunction {
	private MapToolVariableResolver resolver;
	public CopyToken(MapToolVariableResolver resolver) {
		this.resolver = resolver;
	}
	@Override
	public Varargs invoke(Varargs args) {
		if (!MapTool.getParser().isMacroTrusted()) {
			throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", "copyToken")));
		}
		List<Object> arg = new ArrayList<Object>();
		if (!args.isnil(1)) {
			if (args.arg1() instanceof MapToolToken || args.arg1().isstring()) {
				arg.add(args.arg1().tojstring());
			}
			else {
				arg.add(toObj(args.arg1()));
			}
			if (args.isnumber(2)) {
				arg.add(toObj(args.checknumber(2)));
				if (args.isstring(3)) {
					arg.add(toObj(args.checkstring(3)));
					if (args.istable(4)) arg.add(toJson(args.checktable(4)));
					else if (args.isstring(4)) arg.add(toObj(args.checkstring(3)));
				}
			}
		}
		try {
			@SuppressWarnings("unchecked")
			List<Token> tokens = (List<Token>) TokenCopyDeleteFunctions.copyTokens(resolver, arg, true);
			LuaTable result = new LuaTable();
			for (Token t: tokens) {
				result.insert(0, new MapToolToken(t));
			}
			return result;
		} catch (ParserException e) {
			throw new LuaError(e);
		}
	}
	

}
