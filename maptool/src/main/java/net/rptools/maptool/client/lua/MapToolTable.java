/**
 * 
 */
package net.rptools.maptool.client.lua;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.lua.misc.TableRoll;
import net.rptools.maptool.client.lua.misc.TableRollImage;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.LookupTable;
import net.rptools.parser.ParserException;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

/**
 * @author Maluku
 *
 */

public class MapToolTable extends LuaTable {
	private final LookupTable table;
	public MapToolTable(LookupTable lookupTable) {
		this.table = lookupTable;
		super.rawset(valueOf("name"), LuaValue.valueOf(lookupTable.getName()));
		super.rawset(valueOf("visible"), LuaValue.valueOf(Boolean.TRUE.equals(lookupTable.getVisible())));
		super.rawset(valueOf("entries"), NIL); 
		super.rawset(valueOf("diceroll"), NIL);
		super.rawset(valueOf("image"), NIL);
		super.rawset(valueOf("roll"), NIL);
		super.rawset(valueOf("thumbnail"), table.getTableImage() != null ? valueOf("asset://" + table.getTableImage()) : NIL);
		super.rawset(valueOf("access"), NIL);
	}
	public LuaValue setmetatable(LuaValue metatable) { return error("table is read-only"); }
	public void set(int key, LuaValue value) { error("table is read-only"); }
	public void rawset(int key, LuaValue value) { error("table is read-only"); }
	public void rawset(LuaValue key, LuaValue value) { 
		if (key.isstring()) {
			if (key.checkjstring().equals("name")) {
				//TODO 1.4.0.1
			} else if (key.checkjstring().equals("visible")) {
				//TODO 1.4.0.1
			}		
		}
		error("table is read-only"); 
	}
	public LuaValue remove(int pos) { return error("table is read-only"); }
	
	@Override
	public LuaValue rawget(LuaValue key) {
		if (key.isstring()) {
			if (key.checkjstring().equals("name")) {
				return LuaValue.valueOf(table.getName());
			} else if (key.checkjstring().equals("visible")) {
				return LuaValue.valueOf(Boolean.TRUE.equals(table.getVisible()));
			} else if (key.checkjstring().equals("diceroll")) {
				if (!MapTool.getParser().isMacroTrusted()) {
					throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", "table.getRoll"))); 
				}
				return LuaValue.valueOf(table.getRoll());
			} else if (key.checkjstring().equals("access")) {
				if (!MapTool.getParser().isMacroTrusted()) {
					throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", "table.getAccess"))); 
				}
				return LuaValue.valueOf(Boolean.TRUE.equals(table.getAllowLookup()));
			} else if (key.checkjstring().equals("thumbnail")) {
				if (table.getTableImage() != null) {
					return LuaValue.valueOf("asset://" + table.getTableImage());
				}
				return NIL;
			} else if (key.checkjstring().equals("roll")) {
				return new TableRoll(table);
			} else if (key.checkjstring().equals("image")) {
				return new TableRollImage(table);
			}
		}
		return NIL;
	}
	
	@Override
	public LuaValue call() {
		return call(NIL);
	}
	
	@Override
	public LuaValue call(LuaValue arg) {
		return new TableRoll(table).call();
	}
	
	@Override
	public Varargs invoke() {
		return call();
	}
	
	@Override
	public Varargs invoke(Varargs args) {
		return call(args.arg(1));
	}
	
	public String tojstring() {
		return "Table " + table.getName();
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
	public LookupTable getTable() {
		return table;
	}
	
}



