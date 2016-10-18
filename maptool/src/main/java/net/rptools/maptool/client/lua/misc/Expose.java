package net.rptools.maptool.client.lua.misc;

import static net.rptools.maptool.client.functions.FogOfWarFunctions.getTokenSelectedSet;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.lua.MapToolMap;
import net.rptools.maptool.client.ui.zone.FogUtil;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ZeroArgFunction;

public class Expose extends ZeroArgFunction {

	private MapToolMap map;
	private boolean onlySelected;

	public Expose(boolean onlySelected, MapToolMap mapToolMap) {
		this.onlySelected = onlySelected;
		this.map = mapToolMap;
	}

	@Override
	public LuaValue call() {
		ZoneRenderer zr = findRenderer(map);
		if (onlySelected) {
			
			FogUtil.exposeVisibleArea(zr, getTokenSelectedSet(zr));
		} else {
			FogUtil.exposePCArea(zr);
		}
		return NONE;
		
	}
	
	private ZoneRenderer findRenderer(MapToolMap map) {
		if(MapTool.getFrame().getCurrentZoneRenderer().getZone().getId().equals(map.getZone().getId())) {
			return MapTool.getFrame().getCurrentZoneRenderer();
		}
		for (ZoneRenderer zr : MapTool.getFrame().getZoneRenderers()) {
			if (zr.getZone().getId().equals(map.getZone().getId())) {
				return zr;
			}
		}
		return MapTool.getFrame().getCurrentZoneRenderer();
	}

}
