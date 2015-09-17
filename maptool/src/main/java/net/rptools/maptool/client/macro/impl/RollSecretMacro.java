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
		name = "rollsecret",
		aliases = { "rsec" },
		description = "rollsecret.description")
public class RollSecretMacro extends AbstractRollMacro {
	public void execute(MacroContext context, String macro, MapToolMacroContext executionContext) {
		String result = roll(macro);
		if (result != null) {
			if (!MapTool.getPlayer().isGM()) {
				MapTool.addMessage(new TextMessage(TextMessage.Channel.GM, null, MapTool.getPlayer().getName(), "* " +
						I18N.getText("rollsecret.gm.string", MapTool.getPlayer().getName(), result), context.getTransformationHistory()));
				MapTool.addMessage(new TextMessage(TextMessage.Channel.ME, null, MapTool.getPlayer().getName(),
						I18N.getText("rollsecret.self.string"), context.getTransformationHistory()));
			} else {
				MapTool.addMessage(new TextMessage(TextMessage.Channel.GM, null, MapTool.getPlayer().getName(), "* " +
						I18N.getText("rollsecret.gmself.string", result), context.getTransformationHistory()));
			}
		}
	}
}
