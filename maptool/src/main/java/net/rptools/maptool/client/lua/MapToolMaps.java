/**
 * 
 */
package net.rptools.maptool.client.lua;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.model.Zone;

import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

/**
 * @author Maluku
 *
 */

public class MapToolMaps extends LuaTable {
	private MapToolVariableResolver resolver;
	public MapToolMaps(MapToolVariableResolver resolver) {
		this.resolver = resolver;
		super.rawset(valueOf("current"), NIL);
		super.rawset(valueOf("all"), new MapToolMapList(false, resolver));
		super.rawset(valueOf("visible"), new MapToolMapList(true, resolver));
	}
	public LuaValue setmetatable(LuaValue metatable) { return error("table is read-only"); }
	public void set(int key, LuaValue value) { error("table is read-only"); }
	public void rawset(int key, LuaValue value) { error("table is read-only"); }
	public void rawset(LuaValue key, LuaValue value) { 
		if (key.isstring()) {
			if (key.checkjstring().equals("current")) {
				if (value instanceof MapToolMap) {
					Zone zone = ((MapToolMap) value).getZone();
					for (ZoneRenderer zr : MapTool.getFrame().getZoneRenderers()) {
						if (zone.getId().equals(zr.getZone().getId())) {
							MapTool.getFrame().setCurrentZoneRenderer(zr);
							return;
						}
					}
				} else{
					error("not a map");
				}
			} 
		}
		error("table is read-only, except for current"); 
	}
	public LuaValue remove(int pos) { return error("table is read-only"); }
	
	@Override
	public LuaValue rawget(LuaValue key) {
		if (key.isstring()) {
			if (key.checkjstring().equals("current")) {
				return new MapToolMap(MapTool.getFrame().getCurrentZoneRenderer().getZone(), resolver);
			}
		}
		return super.rawget(key);
	}
	
	public String tojstring() {
		return "Maps";
	}
	
	@Override
	public LuaValue tostring() {
		return LuaValue.valueOf(tojstring());
	}
	@Override
	public LuaString checkstring() {
		return LuaValue.valueOf(tojstring());
	}
	@Override
	public String toString() {
		return tojstring();
	}
}

