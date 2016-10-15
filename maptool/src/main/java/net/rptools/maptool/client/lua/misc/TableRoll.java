/**
 * 
 */
package net.rptools.maptool.client.lua.misc;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.lua.LuaConverters;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.LookupTable;
import net.rptools.maptool.model.LookupTable.LookupEntry;
import net.rptools.parser.ParserException;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;

/**
 * @author Maluku
 *
 */
public class TableRoll extends OneArgFunction {
	LookupTable lookupTable;

	public TableRoll(LookupTable table) {
		this.lookupTable = table;
	}

	@Override
	public LuaValue call(LuaValue roll) {
		try {
			if (!MapTool.getPlayer().isGM() && !lookupTable.getAllowLookup()) {
				if (lookupTable.getVisible()) {
					throw new ParserException("table.roll(): " + I18N.getText("msg.error.tableUnknown") + lookupTable.getName());
				} else {
					throw new ParserException("table.roll(): " + I18N.getText("msg.error.tableAccessProhibited") + ": " + lookupTable.getName());
				}
			}
			if (lookupTable == null) {
				throw new ParserException(I18N.getText("macro.function.LookupTableFunctions.unknownTable", "table.roll", lookupTable.getName()));
			}
			LookupEntry result = lookupTable.getLookup(roll.isnil() ? null : roll.checkjstring());
			return LuaConverters.fromObj(result.getValue());
		} catch (ParserException e) {
			throw new LuaError(e);
		}
	}

}
