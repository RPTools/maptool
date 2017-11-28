/**
 * 
 */
package net.rptools.maptool.client.lua.token;

import java.awt.geom.Area;

import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.lua.MapToolToken;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;

/**
 * @author Maluku
 *
 */
public class IsVisible extends TwoArgFunction {

	private MapToolToken sourceToken;

	public IsVisible(MapToolToken source) {
		sourceToken = source;
	}

	/* (non-Javadoc)
	 * @see org.luaj.vm2.lib.OneArgFunction#call(org.luaj.vm2.LuaValue)
	 */
	@Override
	public LuaValue call(LuaValue x, LuaValue y) {
		ZoneRenderer zr = MapTool.getFrame().getCurrentZoneRenderer();
		if (sourceToken.getToken().isVisibleOnlyToOwner() && !AppUtil.playerOwns(sourceToken.getToken())) {
			return FALSE;
		}
		Area visArea = zr.getZoneView().getVisibleArea(sourceToken.getToken());
		if (visArea == null) {
			return FALSE;
		}

		return valueOf(visArea.contains(x.checklong(), y.checklong()));
	}

}
