/**
 * 
 */
package net.rptools.maptool.client.lua.token;

import java.util.HashSet;
import java.util.Set;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.lua.MapToolToken;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Zone;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;

/**
 * @author Maluku
 *
 */
public class SendToBack extends ZeroArgFunction {
	MapToolToken token;
	public SendToBack(MapToolToken token) {
		this.token = token;
	}

	/* (non-Javadoc)
	 * @see org.luaj.vm2.lib.ZeroArgFunction#call()
	 */
	@Override
	public LuaValue call() {
		if (!token.isSelfOrTrusted()) {
			throw new LuaError(I18N.getText("macro.function.general.noPerm", "sendToBack"));
		}
		Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
		Set<GUID> tokens = new HashSet<GUID>();
		tokens.add(token.getToken().getId());
		MapTool.serverCommand().sendTokensToBack(zone.getId(), tokens);
		zone.putToken(token.getToken());
		return LuaValue.NIL;
	}

}
