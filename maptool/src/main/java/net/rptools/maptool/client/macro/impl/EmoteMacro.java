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
import net.rptools.maptool.model.TextMessage;

@MacroDefinition(
		name = "emote",
		aliases = { "me" },
		description = "emote.description")
public class EmoteMacro extends AbstractMacro {
	public void execute(MacroContext context, String macro, MapToolMacroContext executionContext) {
		macro = processText(macro);
		StringBuilder sb = new StringBuilder();

		// Prevent spoofing
		sb.append("* ");
		sb.append("<span style='color:green'>");
		sb.append(MapTool.getFrame().getCommandPanel().getIdentity());
		sb.append(" ");

		sb.append(macro);
		sb.append("</span>");
		MapTool.addMessage(TextMessage.say(context.getTransformationHistory(), sb.toString()));
	}
}
