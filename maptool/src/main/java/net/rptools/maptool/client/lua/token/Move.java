/**
 * 
 */
package net.rptools.maptool.client.lua.token;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.TokenLocationFunctions;
import net.rptools.maptool.client.lua.MapToolToken;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Zone;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ThreeArgFunction;

/**
 * @author Maluku
 *
 */
public class Move extends ThreeArgFunction {
	MapToolToken token;
	public Move(MapToolToken token) {
		this.token = token;
	}

	/* (non-Javadoc)
	 * @see org.luaj.vm2.lib.ZeroArgFunction#call()
	 */
	@Override
	public LuaValue call(LuaValue x, LuaValue y, LuaValue units) {
		if (!token.isSelfOrTrusted()) {
			throw new LuaError(I18N.getText("macro.function.general.noPerm", "moveToken"));
		}
		Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
		TokenLocationFunctions.moveToken(token.getToken(), x.checkint(), y.checkint(), units.isnil() || units.checkboolean());
		MapTool.serverCommand().putToken(zone.getId(), token.getToken());
		zone.putToken(token.getToken());
		return LuaValue.NIL;
	}
}
