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

/**
 * Macro to clear the message panel
 * 
 * @author jgorrell
 * @version $Revision: 5945 $ $Date: 2013-06-03 04:35:50 +0930 (Mon, 03 Jun 2013) $ $Author: azhrei_fje $
 */
@MacroDefinition(
		name = "clear",
		aliases = { "clr", "cls" },
		description = "clear.description")
public class ClearMacro implements Macro {
	/**
	 * @see net.rptools.maptool.client.macro.Macro#execute(java.lang.String)
	 */
	public void execute(MacroContext context, String macro, MapToolMacroContext executionContext) {
		MapTool.getFrame().getCommandPanel().clearMessagePanel();
	}
}
