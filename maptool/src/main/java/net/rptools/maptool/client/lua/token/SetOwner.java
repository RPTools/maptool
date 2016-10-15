/**
 * 
 */
package net.rptools.maptool.client.lua.token;

import java.util.ArrayList;
import java.util.List;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.lua.MapToolToken;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.util.StringUtil;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;

/**
 * @author Maluku
 *
 */
public class SetOwner extends OneArgFunction {
	MapToolToken token;

	public SetOwner(MapToolToken token) {
		this.token = token;
	}

	/* (non-Javadoc)
	 * @see org.luaj.vm2.lib.ZeroArgFunction#call()
	 */
	@Override
	public LuaValue call(LuaValue owners) {
		if (!token.isSelfOrTrusted()) {
			throw new LuaError(I18N.getText("macro.function.general.noPerm", "setOwner"));
		}
		boolean trusted = MapTool.getParser().isMacroTrusted();
		String myself = MapTool.getPlayer().getName();
		token.getToken().clearAllOwners();
		List<String> newowners = new ArrayList<String>();

		if (owners.isstring() && !StringUtil.isEmpty(owners.tojstring())) {
			newowners.add(owners.tojstring());
		} else {
			LuaTable table = owners.checktable();
			for (Varargs n = table.next(LuaValue.NIL); !n.arg1().isnil(); n = table
					.next(n.arg1())) {
				newowners.add(n.checkjstring(2));
			}
		}
		if (newowners.isEmpty()) {
			// Do nothing when trusted, since all ownership should be turned off for an empty string used in such a macro.
		} else {
			for (String owner : newowners) {
				token.getToken().addOwner(owner);
			}
		}
		if (!trusted)
			token.getToken().addOwner(myself); // If not trusted we must have been in the owner list -- keep us there.
		Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
		MapTool.serverCommand().putToken(zone.getId(), token.getToken());
		zone.putToken(token.getToken());
		return NIL;
	}

}
