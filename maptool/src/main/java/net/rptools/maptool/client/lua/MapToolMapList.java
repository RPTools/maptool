/**
 * 
 */
package net.rptools.maptool.client.lua;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.parser.ParserException;

import org.apache.commons.lang.ObjectUtils;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

/**
 * @author Maluku
 *
 */

public class MapToolMapList extends LuaTable {
	private boolean visible = true;
	public MapToolMapList(boolean visibleOnly) {
		visible = visibleOnly;
	}
	public LuaValue setmetatable(LuaValue metatable) { return error("table is read-only"); }
	public void set(int key, LuaValue value) { error("table is read-only"); }
	public void rawset(int key, LuaValue value) { error("table is read-only"); }
	public void rawset(LuaValue key, LuaValue value) { 
		error("table is read-only, except for value"); 
	}
	public LuaValue remove(int pos) { return error("table is read-only"); }
	
	@Override
	public LuaValue rawget(LuaValue key) {
		if (!visible && !MapTool.getParser().isMacroTrusted()) {
			throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", "maps.getAllMapNames"))); 
		}
		if (key.isstring()) {
			for (ZoneRenderer zr : MapTool.getFrame().getZoneRenderers()){
				if (zr != null && zr.getZone() != null && key.tojstring().equals(zr.getZone().getName())) {
					if (!visible || zr.getZone().isVisible()) {
						return new MapToolMap(zr.getZone());
					} else {
						return NIL;
					}
				}
			}
		}
		return NIL;
	}
	
	public String tojstring() {
		return visible ? "Visible Maps" : "Maps";
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
	
	@Override
	public int length() {
		if (!visible && !MapTool.getParser().isMacroTrusted()) {
			throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", "maps.getAllMapNames"))); 
		}
		if (visible) {
			int count = 0;
			for (ZoneRenderer zr : MapTool.getFrame().getZoneRenderers()){
				if (zr.isVisible()) count++;
			}
			return count;
		}
		return MapTool.getFrame().getZoneRenderers().size();
	}
	
	@Override
	public Varargs next(LuaValue key) {
		if (!visible && !MapTool.getParser().isMacroTrusted()) {
			throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", "maps.getAllMapNames"))); 
		}
		boolean found = false;
		String name=null;
		if (key.isnil()) {
			found = true;
		}
		else {
			name = key.checkjstring();
		}
		for (ZoneRenderer zr : MapTool.getFrame().getZoneRenderers()) {
			if (zr == null || zr.getZone() == null) {
				continue;
			}
			if (visible && !zr.getZone().isVisible()) {
				continue;
			} 
			if (found && !ObjectUtils.equals(name, zr.getZone().getName())) {
				return varargsOf(valueOf(zr.getZone().getName()), new MapToolMap(zr.getZone()));
			}
			if (!found && name.equals(zr.getZone().getName())) {
				found = true;
			}
		}
		return NIL;
	}
}
