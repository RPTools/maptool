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

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TabPopupListener extends MouseAdapter {

	private JComponent component;
	private int index;

	public TabPopupListener(JComponent component, int index) {
		this.component = component;
		this.index = index;
	}

	public void mousePressed(MouseEvent e) {
		if (SwingUtilities.isRightMouseButton(e)) {
			new TabPopupMenu(component, index).show(component, e.getX(), e.getY());
		} else {
			//System.out.println("Tab index: " + ((JTabbedPane) component).indexAtLocation(e.getX(), e.getY()));
		}
	}
}
