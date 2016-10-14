/**
 * 
 */
package net.rptools.maptool.client.lua.misc;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.LookupTable;
import net.rptools.maptool.model.LookupTable.LookupEntry;
import net.rptools.parser.ParserException;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;

/**
 * @author Maluku
 *
 */
public class TableRollImage extends TwoArgFunction {
	LookupTable lookupTable;
	public TableRollImage(LookupTable table) {
		this.lookupTable = table;
	}
	@Override
	public LuaValue call(LuaValue roll, LuaValue size) {
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
			if (result.getImageId() == null) {
				throw new ParserException(I18N.getText("macro.function.LookupTableFunctions.noImage", "table.image", lookupTable.getName()));
			}
			
			StringBuilder assetId = new StringBuilder("asset://");
			assetId.append(result.getImageId().toString());
			if (!size.isnil()) {
				int i = Math.max(size.checkint(), 1); // Constrain to a minimum of 1
				assetId.append("-");
				assetId.append(i);
			}
			return LuaValue.valueOf(assetId.toString());
		} catch (ParserException e) {
			throw new LuaError(e);
		}
	}
	
}
