/**
 * 
 */
package net.rptools.maptool.client.lua.token;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Area;

import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.TokenSightFunctions.TokenLocations;
import net.rptools.maptool.client.lua.LuaConverters;
import net.rptools.maptool.client.lua.MapToolToken;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.model.Grid;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.ZonePoint;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;

/**
 * @author Maluku
 *
 */
public class CanSee extends OneArgFunction {

	private MapToolToken sourceToken;
	public CanSee(MapToolToken source) {
		sourceToken = source;
	}

	/* (non-Javadoc)
	 * @see org.luaj.vm2.lib.OneArgFunction#call(org.luaj.vm2.LuaValue)
	 */
	@Override
	public LuaValue call(LuaValue targetVal) {
		LuaTable result = new LuaTable();
		Token source = sourceToken.getToken();
		ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
		if (!source.getHasSight()) {
			return result;
		}
		Area tokensVisibleArea = renderer.getZoneView().getVisibleArea(source);
		if (tokensVisibleArea == null) {
			return result;
		}
		try {
			Token target = LuaConverters.getToken(targetVal);
			if (!target.isVisible() || (target.isVisibleOnlyToOwner() && !AppUtil.playerOwns(target))) {
				return result;
			}
			Zone zone = renderer.getZone();
			Grid grid = zone.getGrid();
	
			Rectangle bounds = target.getFootprint(grid).getBounds(grid, grid.convert(new ZonePoint(target.getX(), target.getY())));
			if (!target.isSnapToGrid())
				bounds = target.getBounds(zone);
	
			int x = (int) bounds.getX();
			int y = (int) bounds.getY();
			int w = (int) bounds.getWidth();
			int h = (int) bounds.getHeight();
			int halfX = x + (w) / 2;
			int halfY = y + (h) / 2;
			if (tokensVisibleArea.intersects(bounds)) {
				if (tokensVisibleArea.contains(new Point(x, y))) {
					result.insert(0, LuaValue.valueOf(TokenLocations.TOP_LEFT.toString()));
				}
				if (tokensVisibleArea.contains(new Point(x, y + h))) {
					result.insert(0, LuaValue.valueOf(TokenLocations.BOTTOM_LEFT.toString()));
				}
				if (tokensVisibleArea.contains(new Point(x + w, y))) {
					result.insert(0, LuaValue.valueOf(TokenLocations.TOP_RIGHT.toString()));
				}
				if (tokensVisibleArea.contains(new Point(x + w, y + h))) {
					result.insert(0, LuaValue.valueOf(TokenLocations.BOTTOM_RIGHT.toString()));
				}
				if (tokensVisibleArea.contains(new Point(halfX, halfY))) {
					result.insert(0, LuaValue.valueOf(TokenLocations.CENTER.toString()));
				}
			}
			return result;
		} catch (LuaError e) {
			return result;
		}
	}

}
