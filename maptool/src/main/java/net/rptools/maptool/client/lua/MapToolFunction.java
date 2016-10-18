/**
 * 
 */
package net.rptools.maptool.client.lua;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolLineParser;
import net.rptools.maptool.client.MapToolMacroContext;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.functions.UserDefinedMacroFunctions;
import net.rptools.maptool.client.functions.UserDefinedMacroFunctions.FunctionDefinition;
import net.rptools.maptool.client.ui.macrobuttons.buttons.MacroButtonPrefs;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.MacroButtonProperties;
import net.rptools.maptool.model.Token;
import net.rptools.parser.ParserException;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

/**
 * @author Maluku
 *
 */

public class MapToolFunction extends VarArgFunction {
	private String function;
	private MapToolVariableResolver resolver;
	public MapToolFunction(String function, MapToolVariableResolver resolver) {
		this.function = function;
		this.resolver = resolver;
	}

	@Override
	public Varargs invoke(Varargs args) {
		FunctionDefinition funcDef = UserDefinedMacroFunctions.getInstance().getUserDefinedFunctions().get(function);
		if (funcDef != null) {
			return runMacro(resolver, resolver.getTokenInContext(), funcDef.getMacroName(), funcDef.isIgnoreOutput(), funcDef.isNewVariableContext(), args, true);
		}
		return NONE;
	}
	
	public static Varargs runMacro(MapToolVariableResolver resolver,
			Token tokenInContext, String macro, boolean ignoreOutput, boolean newVariableContext, Varargs args, boolean stripComments) {
		try {
			MapToolMacroContext macroContext;
			String macroBody = null;
			String[] macroParts = macro.split("@", 2);
			String macroLocation;

			String macroName = macroParts[0];
			if (macroParts.length == 1) {
				macroLocation = null;
			} else {
				macroLocation = macroParts[1];
			}
			// For convenience to macro authors, no error on a blank macro name
			if (macroName.equalsIgnoreCase(""))
				return NONE;

			// IF the macro is a  @this, then we get the location of the current macro and use that.
			if (macroLocation != null && macroLocation.equalsIgnoreCase("this")) {
				macroLocation = MapTool.getParser().getMacroSource();
				if (macroLocation.equals(MapToolLineParser.CHAT_INPUT) || macroLocation.toLowerCase().startsWith("token:")) {
					macroLocation = "TOKEN";
				}
			}
			if (macroLocation == null || macroLocation.length() == 0 || macroLocation.equals(MapToolLineParser.CHAT_INPUT)) {
				// Unqualified names are not allowed.
				throw new ParserException(I18N.getText("lineParser.invalidMacroLoc", macroName));
			} else if (macroLocation.equalsIgnoreCase("TOKEN")) {
				macroContext = new MapToolMacroContext(macroName, "token", MapTool.getPlayer().isGM());
				// Search token for the macro
				if (tokenInContext != null) {
					MacroButtonProperties buttonProps = tokenInContext.getMacro(macroName, false);
					if (buttonProps == null) {
						throw new ParserException(I18N.getText("lineParser.atTokenNotFound", macroName));
					}
					macroBody = buttonProps.getCommand();
				}
			} else if (macroLocation.equalsIgnoreCase("CAMPAIGN")) {
				MacroButtonProperties mbp = null;
				for (MacroButtonProperties m : MapTool.getCampaign().getMacroButtonPropertiesArray()) {
					if (m.getLabel().equals(macroName)) {
						mbp = m;
						break;
					}
				}
				if (mbp == null) {
					throw new ParserException(I18N.getText("lineParser.unknownCampaignMacro", macroName));
				}
				macroBody = mbp.getCommand();
				macroContext = new MapToolMacroContext(macroName, "campaign", !mbp.getAllowPlayerEdits());
			} else if (macroLocation.equalsIgnoreCase("GLOBAL")) {
				macroContext = new MapToolMacroContext(macroName, "global", MapTool.getPlayer().isGM());
				MacroButtonProperties mbp = null;
				for (MacroButtonProperties m : MacroButtonPrefs.getButtonProperties()) {
					if (m.getLabel().equals(macroName)) {
						mbp = m;
						break;
					}
				}
				if (mbp == null) {
					throw new ParserException(I18N.getText("lineParser.unknownGlobalMacro", macroName));
				}
				macroBody = mbp.getCommand();
			} else { // Search for a token called macroLocation (must start with "Lib:")
				macroBody = MapTool.getParser().getTokenLibMacro(macroName, macroLocation);
				Token token = MapTool.getParser().getTokenMacroLib(macroLocation);

				if (macroBody == null || token == null) {
					throw new ParserException(I18N.getText("lineParser.unknownMacro", macroName));
				}
				boolean secure = MapTool.getParser().isSecure(macroName, token);
				macroContext = new MapToolMacroContext(macroName, macroLocation, secure);
			}
			// Error if macro not found
			if (macroBody == null) {
				throw new ParserException(I18N.getText("lineParser.unknownMacro", macroName));
			}
			MapToolVariableResolver macroResolver;
			if (newVariableContext) {
				macroResolver = new MapToolVariableResolver(tokenInContext);
			} else {
				macroResolver = resolver;
			}
			
			Varargs res = Macro.runMacro(macroResolver, tokenInContext, macroContext, macroBody, args);
			String output = res.arg(2).toString();
			if (ignoreOutput) {
				return varargsOf(res.arg(1), valueOf(""), res.arg(3));
			}
			if (stripComments) {
				String stripOutput = output.replaceAll("(?s)<!--.*?-->", ""); // Strip comments
				return varargsOf(res.arg(1), valueOf(stripOutput), res.arg(3));
			}
			return varargsOf(res.arg(1), valueOf(output), res.arg(3));
			
		} catch (ParserException e) {
			throw new LuaError(e);
		}
	}

	public String tojstring() {
		return "User defined function "+ function;
	}

	@Override
	public LuaValue tostring() {
		return LuaValue.valueOf(tojstring());
	}

	@Override
	public LuaString checkstring() {
		return LuaValue.valueOf(tojstring());
	}

	@Override
	public String toString() {
		return tojstring();
	}
	
	
}
