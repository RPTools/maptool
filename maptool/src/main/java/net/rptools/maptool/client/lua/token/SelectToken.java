/**
 * 
 */
package net.rptools.maptool.client.lua.token;

import java.util.ArrayList;
import java.util.List;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.lua.MapToolToken;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Zone;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;
import static net.rptools.maptool.client.lua.LuaConverters.iterate;

/**
 * @author Maluku
 *
 */
public class SelectToken extends VarArgFunction {
	private boolean deselect = false;
	private GUID guid;
	public SelectToken(boolean deselect) {
		this(deselect, null);
	}
	public SelectToken(boolean deselect, GUID guid) {
		this.deselect = deselect;
		this.guid = guid;
	}
	private List<GUID> getID(LuaValue val, Zone zone) {
		List<GUID> result = new ArrayList<GUID>();
		if (val instanceof MapToolToken) {
			result.add(((MapToolToken) val).getToken().getId());
		} else if (val.istable()) {
			for (LuaValue o: iterate(val.checktable())) {
				result.addAll(getID(o, zone));
			}
		} else {
			result.add(zone.resolveToken(val.tojstring().trim()).getId());
		}
		return result;
	}
	@Override
	public Varargs invoke(Varargs args) {
		ZoneRenderer zr = MapTool.getFrame().getCurrentZoneRenderer();
		if (guid != null) {
			if (deselect) zr.deselectToken(guid);
			else zr.selectToken(guid);
			return LuaValue.NIL;
		}
		int count = args.narg();
		Zone zone = zr.getZone();
		for (int i = 1; i <= count; i++) {
			LuaValue val = args.arg(i);
			if (deselect) {
				for (GUID gid: getID(val, zone)) {
					zr.deselectToken(gid);
				}
			}
			else zr.selectTokens(getID(val, zone));
		}
		return LuaValue.NIL;
	}
}
