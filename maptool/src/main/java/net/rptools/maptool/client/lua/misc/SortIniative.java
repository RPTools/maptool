package net.rptools.maptool.client.lua.misc;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.InitiativeList;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;

public class SortIniative extends ZeroArgFunction {

	@Override
	public LuaValue call() {
		InitiativeList list = MapTool.getFrame().getCurrentZoneRenderer().getZone().getInitiativeList();
		list.sort();
		return valueOf(list.getSize());
	}

}
