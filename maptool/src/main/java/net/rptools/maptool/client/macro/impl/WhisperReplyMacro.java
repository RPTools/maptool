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
		name = "reply",
		aliases = { "rep" },
		description = "whisperreply.description")
public class WhisperReplyMacro extends AbstractMacro {
	public void execute(MacroContext context, String message, MapToolMacroContext executionContext) {
		String playerName = MapTool.getLastWhisperer();
		if (playerName == null) {
			MapTool.addMessage(TextMessage.me(context.getTransformationHistory(), "<b>You have no one to which to reply.</b>"));
		}
		// Validate
		if (!MapTool.isPlayerConnected(playerName)) {
			MapTool.addMessage(TextMessage.me(context.getTransformationHistory(), I18N.getText("msg.error.playerNotConnected", playerName)));
			return;
		}
		if (MapTool.getPlayer().getName().equalsIgnoreCase(playerName)) {
			MapTool.addMessage(TextMessage.me(context.getTransformationHistory(), I18N.getText("whisper.toSelf")));
			return;
		}
		// Send
		MapTool.addMessage(TextMessage.whisper(context.getTransformationHistory(), playerName, "<span class='whisper' style='color:blue'>"
				+ I18N.getText("whisper.string", MapTool.getFrame().getCommandPanel().getIdentity(), message) + "</span>"));
		MapTool.addMessage(TextMessage.me(context.getTransformationHistory(), "<span class='whisper' style='color:blue'>" +
				I18N.getText("whisper.you.string", playerName, message) + "</span>"));
	}
}
