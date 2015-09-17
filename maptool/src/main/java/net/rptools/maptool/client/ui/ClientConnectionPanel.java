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

package net.rptools.maptool.client.ui;

import java.awt.event.MouseListener;

import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;

import net.rptools.lib.swing.PopupListener;
import net.rptools.maptool.client.AppActions;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.PlayerListModel;

/**
 * Implements the contents of the Window -> Connections status panel.
 * Previously this class only displayed a list of connected clients, but it is
 * being extended to include other information as well:
 * <ul>
 * <li>current map name,
 * <li>viewing range of current map (as a rectangle of grid coordinates),
 * <li>whether a macro is running (?),
 * <li>IP address (for ping/traceroute tests?)
 * <li>others?
 * </ul>
 */
public class ClientConnectionPanel extends JList {
	public ClientConnectionPanel() {
		setModel(new PlayerListModel(MapTool.getPlayerList()));
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		//		setCellRenderer(new DefaultListCellRenderer());

		addMouseListener(createPopupListener());
	}

	private MouseListener createPopupListener() {
		PopupListener listener = new PopupListener(createPopupMenu());
		return listener;
	}

	private JPopupMenu createPopupMenu() {
		JPopupMenu menu = new JPopupMenu();
		menu.add(new JMenuItem(AppActions.BOOT_CONNECTED_PLAYER));
		return menu;
	}
}
