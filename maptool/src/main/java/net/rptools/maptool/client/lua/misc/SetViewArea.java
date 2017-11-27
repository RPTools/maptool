package net.rptools.maptool.client.lua.misc;

import java.awt.Rectangle;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.Grid;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

public class SetViewArea extends VarArgFunction {
	@Override
	public Varargs invoke(Varargs args) {
		int x1 = 0;
		int y1 = 0;
		int x2 = 0;
		int y2 = 0;
		boolean pixels = true;
		boolean enforce = false;
		x1 = args.checkint(0);
		y1 = args.checkint(1);
		x2 = args.checkint(2);
		y2 = args.checkint(3);
		if (args.isvalue(4))
			pixels = args.checkboolean(4);
		if (args.isvalue(5))
			enforce = args.checkboolean(5);
		// If x & y not in pixels, use grid cell coordinates and convert to pixels
		if (!pixels) {
			Grid mapGrid = MapTool.getFrame().getCurrentZoneRenderer().getZone().getGrid();
			Rectangle fromBounds = mapGrid.getBounds(new CellPoint(x1, y1));
			x1 = fromBounds.x;
			y1 = fromBounds.y;
			Rectangle toBounds = mapGrid.getBounds(new CellPoint(x2, y2));
			x2 = toBounds.x + toBounds.width;
			y2 = toBounds.y + toBounds.height;
		}
		// enforceView command uses point at centre of screen
		int width = x2 - x1;
		int height = y2 - y1;
		int centreX = x1 + (width / 2);
		int centreY = y1 + (height / 2);
		MapTool.getFrame().getCurrentZoneRenderer().enforceView(centreX, centreY, 1, width, height);
		// if requested, set all players to map and match view
		if (enforce && MapTool.getParser().isMacroTrusted()) {
			MapTool.serverCommand().enforceZone(MapTool.getFrame().getCurrentZoneRenderer().getZone().getId());
			MapTool.getFrame().getCurrentZoneRenderer().forcePlayersView();
		}
		return LuaValue.NONE;
	}
}
