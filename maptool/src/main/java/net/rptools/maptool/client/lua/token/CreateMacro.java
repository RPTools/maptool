/**
 * 
 */
package net.rptools.maptool.client.lua.token;

import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.lua.LuaConverters;
import net.rptools.maptool.client.lua.Macro;
import net.rptools.maptool.client.lua.MapToolToken;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.MacroButtonProperties;
import net.rptools.maptool.model.Token;
import net.rptools.parser.ParserException;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ThreeArgFunction;

/**
 * @author Maluku
 *
 */
public class CreateMacro extends ThreeArgFunction {
	MapToolToken token;
	private MapToolVariableResolver resolver;

	public CreateMacro(MapToolToken token, MapToolVariableResolver resolver) {
		this.token = token;
		this.resolver = resolver;
	}

	/* (non-Javadoc)
	 * @see org.luaj.vm2.lib.ZeroArgFunction#call()
	 */
	@Override
	public LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3) {
		if (!token.isSelfOrTrusted()) {
			throw new LuaError(I18N.getText("macro.function.general.noPerm", "bringToFront"));
		}
		Token token = this.token.getToken();
		String label = null;
		String command = null;
		LuaTable props = null;
		if (arg1.isstring()) {
			label = arg1.checkjstring();
			if (arg2.isstring()) {
				command = arg2.checkjstring();
				if (arg3.istable()) {
					props = arg3.checktable();
				} else {
					props = new LuaTable();
				}
			} else {
				props = arg2.checktable();
				if (props.rawget("command").isnil()) {
					throw new LuaError(new ParserException("createMacro(): Missing command."));
				} else {
					command = props.rawget("command").checkjstring();
				}
			}
		} else {
			props = arg1.checktable();
			if (props.rawget("command").isnil()) {
				throw new LuaError(new ParserException("createMacro(): Missing command."));
			} else {
				command = props.rawget("command").checkjstring();
			}
			if (props.rawget("label").isnil()) {
				throw new LuaError(new ParserException("createMacro(): Missing label."));
			} else {
				label = props.rawget("label").checkjstring();
			}
		}
		int index = token.getMacroNextIndex();
		MacroButtonProperties mbp = new MacroButtonProperties(index);
		mbp.setCommand(command);
		mbp.setLabel(label);
		mbp.setSaveLocation("Token");
		mbp.setTokenId(token);
		mbp.setApplyToTokens(false);
		for (LuaValue prop : LuaConverters.keyIterate(props)) {
			if (!prop.checkjstring().equals("index") && !prop.checkjstring().equals("command") && !prop.checkjstring().equals("label")) {
				Macro.setProp(mbp, prop, props.get(prop));
			}
		}
		mbp.save();
		return new Macro(this.token, index, resolver);
	}

}
