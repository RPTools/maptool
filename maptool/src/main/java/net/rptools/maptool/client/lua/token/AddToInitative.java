/**
 * 
 */
package net.rptools.maptool.client.lua.token;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.TokenInitFunction;
import net.rptools.maptool.client.lua.MapToolToken;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.InitiativeList;
import net.rptools.maptool.model.InitiativeList.TokenInitiative;
import net.rptools.maptool.model.Token;
import net.rptools.parser.ParserException;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;

/**
 * @author Maluku
 *
 */
public class AddToInitative extends TwoArgFunction {
	MapToolToken token;

	public AddToInitative(MapToolToken token) {
		this.token = token;
	}

	/* (non-Javadoc)
	 * @see org.luaj.vm2.lib.ZeroArgFunction#call()
	 */
	@Override
	public LuaValue call(LuaValue duplicates, LuaValue value) {

		InitiativeList list = MapTool.getFrame().getCurrentZoneRenderer().getZone().getInitiativeList();
		Token token = this.token.getToken();
		if (!MapTool.getParser().isMacroTrusted()) {
			if (!MapTool.getFrame().getInitiativePanel().hasOwnerPermission(token)) {
				String message = I18N.getText("macro.function.initiative.gmOnly", "addToInitiative");
				if (MapTool.getFrame().getInitiativePanel().isOwnerPermissions())
					message = I18N.getText("macro.function.initiative.gmOrOwner", "addToInitiative");
				throw new LuaError(new ParserException(message));
			} // endif
		}
		boolean allowDuplicates = false;
		if (!duplicates.isnil()) {
			allowDuplicates = duplicates.checkboolean();
		}
		String state = null;
		if (!value.isnil()) {
			state = value.checkjstring();
		}
		TokenInitiative ti = null;
		if (allowDuplicates || list.indexOf(token).isEmpty()) {
			ti = list.insertToken(-1, token);
			if (state != null)
				ti.setState(state);
		} else {
			try {
				TokenInitFunction.getInstance().setTokenValue(token, state);
			} catch (ParserException e) {
				throw new LuaError(e);
			}
		}
		return ti != null ? LuaValue.TRUE : LuaValue.FALSE;
	}

}
