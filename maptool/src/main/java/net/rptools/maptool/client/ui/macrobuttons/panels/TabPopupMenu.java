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

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import java.awt.event.ActionEvent;

public class TabPopupMenu extends JPopupMenu {

	//private final JComponent parent;
	private int index;

	//TODO: replace index with Tab.TABNAME.index
	public TabPopupMenu(JComponent parent, int index) {
		//this.parent = parent;
		this.index = index;
		add(new AddNewButtonAction());
	}

	private class AddNewButtonAction extends AbstractAction {
		public AddNewButtonAction() {
			putValue(Action.NAME, "New Tab");
		}

		public void actionPerformed(ActionEvent event) {
		}
	}
}
