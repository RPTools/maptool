package net.rptools.maptool.client.lua.misc;

import net.rptools.maptool.client.functions.MacroLinkFunction;
import net.rptools.maptool.client.lua.LuaConverters;
import net.rptools.maptool.language.I18N;
import net.rptools.parser.ParserException;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

public class Link extends VarArgFunction {
	private boolean formatted;
	public Link(boolean formatted) {
		this.formatted = formatted;
	}

	@Override
	public Varargs invoke(Varargs args) {
		String linkText = "";
		if (formatted) {
			args.argcheck(args.isstring(1) || args.isnumber(1), 1, I18N.getText("macro.function.macroLink.missingName", "macroLink"));
			linkText = args.checkjstring(1);
			args = args.subargs(2);
		}
		String macroName = args.checkjstring(1);
		String linkArgs = args.isnil(3) ? "" : LuaConverters.toJson(args.arg(3)).toString();
		String linkWho = args.isnil(2) ? "none" : args.checkjstring(2);
		String linkTarget = args.isnil(4) ? "Impersonated" : LuaConverters.toString(args.arg(4));

		StringBuilder sb = new StringBuilder();

		if (formatted) {
			sb.append("<a href='");
		}
		try {
			sb.append(MacroLinkFunction.createMacroText(macroName, linkWho, linkTarget, linkArgs));
		} catch (ParserException e) {
			throw new LuaError(e);
		}
		if (formatted) {
			sb.append("'>").append(linkText).append("</a>");
		}
		return valueOf(sb.toString());
	}
}
