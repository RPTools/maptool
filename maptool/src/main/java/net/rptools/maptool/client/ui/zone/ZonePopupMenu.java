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

package net.rptools.maptool.client.ui.zone;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.Zone;

public class ZonePopupMenu extends JPopupMenu {

	private Zone zone;

	public ZonePopupMenu(Zone zone) {
		super("Zone");

		this.zone = zone;

		Action action = null;
		if (zone.isVisible()) {
			action = new AbstractAction() {
				{
					putValue(NAME, "Hide from players");
				}

				public void actionPerformed(ActionEvent e) {
					ZonePopupMenu.this.zone.setVisible(false);
					MapTool.serverCommand().setZoneVisibility(ZonePopupMenu.this.zone.getId(), false);
					MapTool.getFrame().getZoneMiniMapPanel().flush();
					MapTool.getFrame().refresh();
				}
			};
		} else {
			action = new AbstractAction() {
				{
					putValue(NAME, "Show to players");
				}

				public void actionPerformed(ActionEvent e) {

					ZonePopupMenu.this.zone.setVisible(true);
					MapTool.serverCommand().setZoneVisibility(ZonePopupMenu.this.zone.getId(), true);
					MapTool.getFrame().getZoneMiniMapPanel().flush();
					MapTool.getFrame().refresh();
				}
			};
		}
		add(new JMenuItem(action));
	}

}
