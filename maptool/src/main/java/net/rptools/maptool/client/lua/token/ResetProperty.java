/**
 * 
 */
package net.rptools.maptool.client.lua.token;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.lua.MapToolToken;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Zone;
import net.rptools.parser.ParserException;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;

/**
 * @author Maluku
 *
 */
public class ResetProperty extends ZeroArgFunction {
	private MapToolToken token;
	private String property;
	public ResetProperty(MapToolToken token, String property) {
		this.token = token;
		this.property = property;
	}

	/* (non-Javadoc)
	 * @see org.luaj.vm2.lib.ZeroArgFunction#call()
	 */
	@Override
	public LuaValue call() {
		if (!token.isSelfOrTrusted()) {
			throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", "token.getProperty"))); 
		}
		Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
		token.getToken().resetProperty(property);
		MapTool.serverCommand().putToken(zone.getId(), token.getToken());
		zone.putToken(token.getToken());
		return LuaValue.NIL;
	}

}
