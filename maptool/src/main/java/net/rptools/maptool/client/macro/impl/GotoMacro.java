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

package net.rptools.maptool.client.macro.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolMacroContext;
import net.rptools.maptool.client.macro.Macro;
import net.rptools.maptool.client.macro.MacroContext;
import net.rptools.maptool.client.macro.MacroDefinition;
import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.ZonePoint;

@MacroDefinition(
		name = "goto",
		aliases = { "g" },
		description = "goto.description")
public class GotoMacro implements Macro {
	private static Pattern COORD_PATTERN = Pattern.compile("(-?\\d+)\\s*,?\\s*(-?\\d+)");

	public void execute(MacroContext context, String parameter, MapToolMacroContext executionContext) {
		Matcher m = COORD_PATTERN.matcher(parameter.trim());
		if (m.matches()) {
			// goto coordinate locations
			int x = Integer.parseInt(m.group(1));
			int y = Integer.parseInt(m.group(2));

			MapTool.getFrame().getCurrentZoneRenderer().centerOn(new CellPoint(x, y));
		} else {
			// goto token location
			Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
			Token token = zone.getTokenByName(parameter);

			if (!MapTool.getPlayer().isGM() && !zone.isTokenVisible(token)) {
				return;
			}
			if (token != null) {
				int x = token.getX();
				int y = token.getY();
				MapTool.getFrame().getCurrentZoneRenderer().centerOn(new ZonePoint(x, y));
			}
		}
	}
}
