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

package net.rptools.maptool.client.ui.macrobuttons.panels;

import java.util.ArrayList;
import java.util.List;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.macrobuttons.buttongroups.AbstractButtonGroup;
import net.rptools.maptool.model.MacroButtonProperties;

public class CampaignPanel extends AbstractMacroPanel {

	public CampaignPanel() {
		setPanelClass("CampaignPanel");
		addMouseListener(this);
		init();
	}

	private void init() {
		if (MapTool.getPlayer() == null || MapTool.getPlayer().isGM() || MapTool.getServerPolicy().playersReceiveCampaignMacros()) {
			addArea(MapTool.getCampaign().getMacroButtonPropertiesArray(), "");
		}
	}

	public void reset() {
		clear();
		init();
	}

	public static void deleteButtonGroup(String macroGroup) {
		AbstractButtonGroup.clearHotkeys(MapTool.getFrame().getCampaignPanel(), macroGroup);
		List<MacroButtonProperties> campProps = MapTool.getCampaign().getMacroButtonPropertiesArray();
		List<MacroButtonProperties> startingProps = new ArrayList<MacroButtonProperties>(MapTool.getCampaign().getMacroButtonPropertiesArray());
		campProps.clear();
		for (MacroButtonProperties nextProp : startingProps) {
			if (!macroGroup.equals(nextProp.getGroup())) {
				MapTool.getCampaign().saveMacroButtonProperty(nextProp);
			}
		}
		MapTool.getFrame().getCampaignPanel().reset();
	}

	public static void clearPanel() {
		AbstractMacroPanel.clearHotkeys(MapTool.getFrame().getCampaignPanel());
		MapTool.getCampaign().getMacroButtonPropertiesArray().clear();
		MapTool.getFrame().getCampaignPanel().reset();
	}
}
