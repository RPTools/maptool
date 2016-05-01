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
		name = "gm",
		aliases = { "togm" },
		description = "togm.description")
public class ToGMMacro extends AbstractRollMacro {
	public void execute(MacroContext context, String macro, MapToolMacroContext executionContext) {
		StringBuilder sb = new StringBuilder();

		if (executionContext != null && MapTool.getParser().isMacroPathTrusted() && !MapTool.getPlayer().isGM()) {
			sb.append("<span class='trustedPrefix' ").append("title='").append(executionContext.getName());
			sb.append("@").append(executionContext.getSource()).append("'>");
			sb.append(I18N.getText("togm.saysToGM", MapTool.getPlayer().getName())).append("</span> ").append(macro);
		} else {
			sb.append(I18N.getText("togm.saysToGM", MapTool.getPlayer().getName())).append(" ").append(macro);
		}
		MapTool.addMessage(new TextMessage(TextMessage.Channel.GM, null, MapTool.getPlayer().getName(), sb.toString(), context.getTransformationHistory()));
		MapTool.addMessage(new TextMessage(TextMessage.Channel.ME, null, MapTool.getPlayer().getName(), I18N.getText("togm.self", macro), context.getTransformationHistory()));
	}
}
