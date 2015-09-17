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
import net.rptools.maptool.client.ui.macrobuttons.buttons.MacroButtonPrefs;
import net.rptools.maptool.model.MacroButtonProperties;

public class GlobalPanel extends AbstractMacroPanel {
	public GlobalPanel() {
		super();
		setPanelClass("GlobalPanel");
		addMouseListener(this);
		init();
	}

	private void init() {
		List<MacroButtonProperties> properties = MacroButtonPrefs.getButtonProperties();
		addArea(properties, "");
	}

	public void reset() {
		clear();
		init();
	}

	public static void deleteButtonGroup(String macroGroup) {
		AbstractButtonGroup.clearHotkeys(MapTool.getFrame().getGlobalPanel(), macroGroup);
		for (MacroButtonProperties nextProp : MacroButtonPrefs.getButtonProperties()) {
			if (macroGroup.equals(nextProp.getGroup())) {
				MacroButtonPrefs.delete(nextProp);
			}
		}
		MapTool.getFrame().getGlobalPanel().reset();
	}

	public static void clearPanel() {
		MacroButtonPrefs.deletePanel();
		AbstractMacroPanel.clearHotkeys(MapTool.getFrame().getGlobalPanel());
		MapTool.getFrame().getGlobalPanel().reset();
	}
}
