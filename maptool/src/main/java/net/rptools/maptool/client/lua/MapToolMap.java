/**
 * 
 */
package net.rptools.maptool.client.lua;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.lua.token.CopyToken;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Zone;
import net.rptools.parser.ParserException;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

/**
 * @author Maluku
 *
 */

public class MapToolMap extends LuaTable implements IRepresent {
	private final Zone zone;
	private MapToolVariableResolver resolver;

	public MapToolMap(Zone zone, MapToolVariableResolver resolver) {
		this.zone = zone;
		this.resolver = resolver;
		super.rawset(valueOf("name"), LuaValue.valueOf(zone.getName()));
		super.rawset(valueOf("visible"), LuaValue.valueOf(zone.isVisible()));
	}

	public LuaValue setmetatable(LuaValue metatable) {
		return error("table is read-only");
	}

	public void set(int key, LuaValue value) {
		error("table is read-only");
	}

	public void rawset(int key, LuaValue value) {
		error("table is read-only");
	}

	public void rawset(LuaValue key, LuaValue value) {
		if (key.isstring()) {
			if (key.checkjstring().equals("name")) {
				if (!MapTool.getParser().isMacroTrusted()) {
					throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", "maps.setMapName")));
				}
				String name = value.checkjstring();
				zone.setName(name);
				MapTool.serverCommand().renameZone(zone.getId(), name);
				return;
			} else if (key.checkjstring().equals("visible")) {
				if (!MapTool.getParser().isMacroTrusted()) {
					throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", "maps.setMapVisible")));
				}
				zone.setVisible(value.checkboolean());
				MapTool.serverCommand().setZoneVisibility(zone.getId(), value.checkboolean());
				MapTool.getFrame().getZoneMiniMapPanel().flush();
				MapTool.getFrame().refresh();
				return;
			}
		}
		error("table is read-only, except for name and visible");
	}

	public LuaValue remove(int pos) {
		return error("table is read-only");
	}

	@Override
	public LuaValue rawget(LuaValue key) {
		if (key.isstring()) {
			if (key.checkjstring().equals("name")) {
				return LuaValue.valueOf(zone.getName());
			} else if (key.checkjstring().equals("visible")) {
				return LuaValue.valueOf(zone.isVisible());
			} else if (key.checkjstring().equals("copyToken")) {
				return new CopyToken(resolver, this);
			}
		}
		return NIL;
	}

	public String tojstring() {
		return "Map " + zone.getName();
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

	public Zone getZone() {
		return zone;
	}

	@Override
	public Object export() {
		return zone.getName();
	}

}
