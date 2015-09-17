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

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolMacroContext;
import net.rptools.maptool.client.macro.Macro;
import net.rptools.maptool.client.macro.MacroContext;
import net.rptools.maptool.client.macro.MacroDefinition;
import net.rptools.maptool.language.I18N;

@MacroDefinition(
		name = "undefined",
		description = "Undefined macro.",
		hidden = true)
public class UndefinedMacro implements Macro {

	public void execute(MacroContext context, String macro, MapToolMacroContext executionContext) {
		MapTool.addLocalMessage(I18N.getText("undefinedmacro.unknownCommand", macro));
	}

}
