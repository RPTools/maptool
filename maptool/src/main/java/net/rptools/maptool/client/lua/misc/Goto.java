package net.rptools.maptool.client.lua.misc;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.lua.MapToolToken;
import net.rptools.maptool.model.ZonePoint;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;

public class Goto extends TwoArgFunction {

	@Override
	public LuaValue call(LuaValue arg1, LuaValue arg2) {
		if (arg1 instanceof MapToolToken) {
			MapTool.getFrame().getCurrentZoneRenderer().centerOn(new ZonePoint(((MapToolToken) arg1).getToken().getX(), ((MapToolToken) arg1).getToken().getY()));
		} else {
			MapTool.getFrame().getCurrentZoneRenderer().centerOn(new ZonePoint(arg1.checkint(), arg2.checkint()));
		}
		return NIL;
	}

}
