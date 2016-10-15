/**
 * 
 */
package net.rptools.maptool.client.lua.token;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.lua.MapToolToken;
import net.rptools.maptool.language.I18N;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;

/**
 * @author Maluku
 *
 */
public class ClearLights extends ZeroArgFunction {
	MapToolToken token;

	public ClearLights(MapToolToken token) {
		this.token = token;
	}

	/* (non-Javadoc)
	 * @see org.luaj.vm2.lib.ZeroArgFunction#call()
	 */
	@Override
	public LuaValue call() {
		if (!token.isSelfOrTrusted()) {
			throw new LuaError(I18N.getText("macro.function.general.noPerm", "clearLights"));
		}
		token.getToken().clearLightSources();
		MapTool.serverCommand().putToken(MapTool.getFrame().getCurrentZoneRenderer().getZone().getId(), token.getToken());
		MapTool.getFrame().updateTokenTree();
		return LuaValue.NIL;
	}

}
