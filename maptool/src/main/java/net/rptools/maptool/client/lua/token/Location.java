/**
 * 
 */
package net.rptools.maptool.client.lua.token;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ReadOnlyLuaTable;
import net.rptools.maptool.client.functions.TokenLightFunctions;
import net.rptools.maptool.client.lua.MapToolToken;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.ZonePoint;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;

/**
 * @author Maluku
 *
 */
public class Location extends OneArgFunction {
	MapToolToken token;

	public Location(MapToolToken token) {
		this.token = token;
	}

	/* (non-Javadoc)
	 * @see org.luaj.vm2.lib.TwoArgFunction#call(org.luaj.vm2.LuaValue, org.luaj.vm2.LuaValue)
	 */
	@Override
	public LuaValue call(LuaValue useDistancePerCell) {
		if (!token.isSelfOrTrusted()) {
			throw new LuaError(I18N.getText("macro.function.general.noPerm", "location"));
		}
		LuaTable loc = new LuaTable();
		if (!useDistancePerCell.isboolean() || useDistancePerCell.checkboolean()) {
			Rectangle tokenBounds = token.getToken().getBounds(MapTool.getFrame().getCurrentZoneRenderer().getZone());
			loc.rawset("x", tokenBounds.x);
			loc.rawset("y", tokenBounds.y);
			loc.rawset("z", token.getToken().getZOrder());
		} else {
			Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
			CellPoint cellPoint = zone.getGrid().convert(new ZonePoint(token.getToken().getX(), token.getToken().getY()));

			loc.rawset("x", cellPoint.x);
			loc.rawset("y", cellPoint.y);
			loc.rawset("z", token.getToken().getZOrder());
		}
		return new ReadOnlyLuaTable(loc);
	}

}
