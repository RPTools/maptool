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

import java.util.Set;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolMacroContext;
import net.rptools.maptool.client.macro.Macro;
import net.rptools.maptool.client.macro.MacroContext;
import net.rptools.maptool.client.macro.MacroDefinition;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Token;

/**
 * Macro to run the speech ID on the given token
 * 
 */
@MacroDefinition(
		name = "tsay",
		aliases = { "ts" },
		description = "tokenspeech.description")
public class RunTokenSpeechMacro implements Macro {
	/**
	 * @see net.rptools.maptool.client.macro.Macro#execute(java.lang.String)
	 */
	public void execute(MacroContext context, String macro, MapToolMacroContext executionContext) {
		Set<GUID> selectedTokenSet = MapTool.getFrame().getCurrentZoneRenderer().getSelectedTokenSet();
		if (selectedTokenSet.size() == 0) {
			MapTool.addLocalMessage(I18N.getText("msg.error.noTokensSelected"));
			return;
		}
		for (GUID tokenId : selectedTokenSet) {
			Token token = MapTool.getFrame().getCurrentZoneRenderer().getZone().getToken(tokenId);
			if (token == null) {
				continue;
			}
			String tmacro = token.getSpeech(macro);
			if (tmacro == null) {
				continue;
			}
			MapTool.getFrame().getCommandPanel().getCommandTextArea().setText("/im " + token.getId() + ": " + tmacro);
			MapTool.getFrame().getCommandPanel().commitCommand();
		}
	}
}
