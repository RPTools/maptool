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

import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import net.rptools.maptool.client.AppActions.ClientAction;

/** 
 * This little baby will keep the menu items selected state intact.  Not the most elegant, but works
 */
public class RPCheckBoxMenuItem extends JCheckBoxMenuItem implements MenuListener {

	public RPCheckBoxMenuItem(Action action, JMenu parentMenu) {
		super(action);

		parentMenu.addMenuListener(this);
	}

	public void menuSelected(MenuEvent e) {
		Action action = getAction();
		if (action instanceof ClientAction) {
			setSelected(((ClientAction) action).isSelected());
		}
	}

	public void menuCanceled(MenuEvent e) {
	}

	public void menuDeselected(MenuEvent e) {
	}

}
