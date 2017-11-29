package net.rptools.maptool.client.lua.misc;

import java.util.HashSet;
import java.util.Set;

import net.rptools.common.expression.Result;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolLineParser;
import net.rptools.maptool.client.MapToolLineParser.OptionType;
import net.rptools.maptool.client.MapToolLineParser.Output;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.lua.LuaConverters;
import net.rptools.parser.ParserException;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

public class PrintRoll extends VarArgFunction {

	private MapToolVariableResolver resolver;
	boolean newContext = false;
	private Globals globals;
	private OptionType option;
	public PrintRoll(MapToolVariableResolver resolver, boolean newContext, MapToolLineParser.OptionType output, Globals globals) {
		this.resolver = resolver;
		this.newContext = newContext;
		this.globals = globals;
		this.option = output;
	}
	@Override
	public Varargs invoke(Varargs args) {
		MapToolVariableResolver macroResolver = resolver;
		String macro = args.checkjstring(1);
		if (newContext) {
			macroResolver = new MapToolVariableResolver(resolver.getTokenInContext());
		}
		StringBuilder sb = new StringBuilder();
		try {
			String text = null;
			String output_text = "";
			Output output;
			if (MapTool.useToolTipsForUnformatedRolls()) {
				output = Output.TOOLTIP;
			} else {
				output = Output.EXPANDED;
			}
			Set<String> outputOpts = new HashSet<String>();
			switch (option) {

			///////////////////////////////////////////////////
			// OUTPUT FORMAT OPTIONS
			///////////////////////////////////////////////////
			case HIDDEN:
				output = Output.NONE;
				break;
			case RESULT:
				output = Output.RESULT;
				break;
			case EXPANDED:
				output = Output.EXPANDED;
				break;
			case UNFORMATTED:
				output = Output.UNFORMATTED;
				outputOpts.add("u");
				break;
			case TOOLTIP:
				// T(display_text)
				output = Output.TOOLTIP;
				text = args.isnil(2) ? null : args.arg(2).checkjstring();
				break;
			case GM:
				outputOpts.add("g");
				break;
			case SELF:
				outputOpts.add("s");
				break;
			case WHISPER:
				outputOpts.add("w");
				for (int i = 2; i <= args.narg(); i++) {
					LuaValue arg = args.arg(i);
					if (arg.istable()) {
						for (LuaValue name : LuaConverters.arrayIterate(arg.checktable())) {
							outputOpts.add("w:" + name.tojstring().toLowerCase());
						}
					} else
						outputOpts.add("w:" + arg.tojstring().toLowerCase());
				}
				break;
			case GMTT:
				outputOpts.add("gt");
				break;
			case SELFTT:
				outputOpts.add("st");
				break;
			default:
				return NONE;
			}
			
			Result result = MapTool.getParser().parseExpression(macroResolver, macroResolver.getTokenInContext(), macro);
			
			switch (output) {
			case NONE:
				break;
			case RESULT:
				output_text = result != null ? result.getValue().toString() : "";
				if (!MapTool.getParser().isMacroTrusted()) {
					output_text = output_text.replaceAll("\u00AB|\u00BB|&#171;|&#187;|&laquo;|&raquo;|\036|\037", "");
				}
				if (outputOpts.isEmpty()) {
					globals.STDOUT.print(output_text);
				} else {
					outputOpts.add("r");
					globals.STDOUT.print(MapTool.getParser().rollString(outputOpts, output_text));
				}

				break;
			case TOOLTIP:
				String tooltip = macro + " = ";
				output_text = null;
				tooltip += result.getDetailExpression();
				if (text == null) {
					output_text = result.getValue().toString();
				} else {
					if (!result.getDetailExpression().equals(result.getValue().toString())) {
						tooltip += " = " + result.getValue();
					}
					resolver.setVariable("roll.result", result.getValue());
					output_text = MapTool.getParser().parseExpression(macroResolver, macroResolver.getTokenInContext(), text).getValue().toString();
				}
				tooltip = tooltip.replaceAll("'", "&#39;");
				globals.STDOUT.print(output_text != null ? MapTool.getParser().rollString(outputOpts, tooltip, output_text) : "");
				break;
			case EXPANDED:
				if (result.getDetailExpression().equals(result.getValue().toString())) {
					sb.append(result.getDetailExpression());
				} else {
					sb.append(result.getDetailExpression()).append(" = ").append(result.getValue());
				}
				globals.STDOUT.print(MapTool.getParser().rollString(outputOpts, macro + " = " + sb.toString()));
				break;
			case UNFORMATTED:
				if (result.getDetailExpression().equals(result.getValue().toString())) {
					sb.append(result.getDetailExpression());
				} else {
					sb.append(result.getDetailExpression()).append(" = ").append(result.getValue());
				}
				output_text = macro + " = " + sb.toString();

				// Escape quotes so that the result can be used in a title attribute
				output_text = output_text.replaceAll("'", "&#39;");
				output_text = output_text.replaceAll("\"", "&#34;");

				globals.STDOUT.print(MapTool.getParser().rollString(outputOpts, output_text));
			} // end of switch(output) statement
		} catch (ParserException e) {
			throw new LuaError(e);
		}
		return NONE;
	}	
}
