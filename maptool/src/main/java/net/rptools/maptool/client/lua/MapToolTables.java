/**
 * 
 */
package net.rptools.maptool.client.lua;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.LookupTable;
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

public class MapToolTables extends LuaTable {
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
		error("table is read-only");
	}

	public LuaValue remove(int pos) {
		return error("table is read-only");
	}

	@Override
	public LuaValue rawget(LuaValue key) {
		if (key.isstring()) {
			if (MapTool.getCampaign().getLookupTableMap().containsKey(key.tojstring())) {
				LookupTable lookupTable = MapTool.getCampaign().getLookupTableMap().get(key.tojstring());
				if (Boolean.TRUE.equals(lookupTable.getVisible())) {
					return new MapToolTable(lookupTable);
				} else if (!MapTool.getPlayer().isGM()) {
					throw new LuaError(new ParserException(I18N.getText("msg.error.tableUnknown")));
				}
			}
		}
		return NIL;
	}

	public String tojstring() {
		return "Tables";
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
		if (!MapTool.getPlayer().isGM()) {
			int count = 0;
			for (LookupTable tb : MapTool.getCampaign().getLookupTableMap().values()) {
				if (Boolean.TRUE.equals(tb.getVisible()))
					count++;
			}
			return count;
		}
		return MapTool.getCampaign().getLookupTableMap().size();
	}

	@Override
	public Varargs next(LuaValue key) {
		boolean found = false;
		String name = null;
		if (key.isnil()) {
			found = true;
		} else {
			name = key.checkjstring();
		}
		for (java.util.Map.Entry<String, LookupTable> e : MapTool.getCampaign().getLookupTableMap().entrySet()) {
			if (!MapTool.getPlayer().isGM() && !Boolean.TRUE.equals(e.getValue().getVisible())) {
				continue;
			}
			if (found && !ObjectUtils.equals(name, e.getKey())) {
				return varargsOf(valueOf(e.getKey()), new MapToolTable(e.getValue()));
			}
			if (!found && name.equals(e.getKey())) {
				found = true;
			}
		}
		return NIL;
	}
}
