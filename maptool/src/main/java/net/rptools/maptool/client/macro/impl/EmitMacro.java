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
import net.rptools.maptool.client.macro.MacroContext;
import net.rptools.maptool.client.macro.MacroDefinition;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.TextMessage;

@MacroDefinition(
		name = "emit",
		aliases = { "e" },
		description = "emit.description")
public class EmitMacro extends AbstractMacro {
	public void execute(MacroContext context, String macro, MapToolMacroContext executionContext) {
		macro = processText(macro);
		if (!MapTool.getPlayer().isGM()) {
			MapTool.addMessage(TextMessage.me(context.getTransformationHistory(), "<b>" + I18N.getText("slash.mustBeGM", "emit") + "</b>"));
			return;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("<i><b>").append(macro).append("</b></i>");
		MapTool.addMessage(TextMessage.say(context.getTransformationHistory(), sb.toString()));
	}
}
