/*
 * This software copyright by various authors including the RPTools.net
 * development team, and licensed under the LGPL Version 3 or, at your option,
 * any later version.
 *
 * Portions of this software were originally covered under the Apache Software
 * License, Version 1.1 or Version 2.0.
 *
 * See the file LICENSE elsewhere in this distribution for license details.
 */

package net.rptools.maptool.client.functions;

import java.math.BigDecimal;
import java.util.List;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.LookupTable;
import net.rptools.maptool.model.LookupTable.LookupEntry;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;
import net.sf.json.JSONArray;

public class LookupTableFunction extends AbstractFunction {

	public LookupTableFunction() {
		super(0, 3, "tbl", "table", "tblImage", "tableImage", "getTableNames");
	}

	/** The singleton instance. */
	private final static LookupTableFunction instance = new LookupTableFunction();

	/**
	 * Gets the instance of TableLookup.
	 * @return the TableLookup.
	 */
	public static LookupTableFunction getInstance() {
		return instance;
	}

	@Override
	public Object childEvaluate(Parser parser, String function, List<Object> params) throws ParserException {
		
		if ("getTableNames".equalsIgnoreCase(function)) {
			String delim = ",";
			if (params.size() > 0) {
				delim = params.get(0).toString();
			}
			if ("json".equalsIgnoreCase(delim))
				return JSONArray.fromObject(MapTool.getCampaign().getLookupTableMap().keySet());
			String results = "";
			for (LookupTable lt :MapTool.getCampaign().getLookupTableMap().values()) {
				results=results+(results!=""?delim:"")+lt.getName();
			}
			return results;
		} else {

			String name = params.get(0).toString();

			String roll = null;
			if (params.size() > 1) {
				roll = params.get(1).toString().length() == 0 ? null : params.get(1).toString();
			}

			LookupTable lookupTable = MapTool.getCampaign().getLookupTableMap().get(name);
			if (!MapTool.getPlayer().isGM() && !lookupTable.getAllowLookup()) {
				if (lookupTable.getVisible()) {
					throw new ParserException(function + "(): " + I18N.getText("msg.error.tableUnknown") + name);
				} else {
					throw new ParserException(function + "(): " + I18N.getText("msg.error.tableAccessProhibited") + ": " + name);
				}
			}
			if (lookupTable == null) {
				throw new ParserException(I18N.getText("macro.function.LookupTableFunctions.unknownTable", function, name));
			}

			LookupEntry result = lookupTable.getLookup(roll);

			if (function.equals("table") || function.equals("tbl")) {
				String val = result.getValue();
				try {
					BigDecimal bival = new BigDecimal(val);
					return bival;
				} catch (NumberFormatException nfe) {
					return val;
				}
			} else { // We want the image URI

				if (result.getImageId() == null) {
					throw new ParserException(I18N.getText("macro.function.LookupTableFunctions.noImage", function, name));
				}

				BigDecimal size = null;
				if (params.size() > 2) {
					if (params.get(2) instanceof BigDecimal) {
						size = (BigDecimal) params.get(2);
					} else {
						throw new ParserException(I18N.getText("macro.function.LookupTableFunctions.invalidSize", function));
					}
				}

				StringBuilder assetId = new StringBuilder("asset://");
				assetId.append(result.getImageId().toString());
				if (size != null) {
					int i = Math.max(size.intValue(), 1); // Constrain to a minimum of 1
					assetId.append("-");
					assetId.append(i);
				}
				return assetId.toString();
			}
		}
	}
}
