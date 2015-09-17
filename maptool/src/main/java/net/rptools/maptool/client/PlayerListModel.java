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

package net.rptools.maptool.client;

import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractListModel;

import net.rptools.maptool.model.ObservableList;
import net.rptools.maptool.model.Player;

public class PlayerListModel extends AbstractListModel implements Observer {

	private ObservableList<Player> playerList;

	public PlayerListModel(ObservableList<Player> playerList) {
		this.playerList = playerList;

		// TODO: Figure out how to clean this up when no longer in use
		// for now it doesn't matter, but, it's bad design
		playerList.addObserver(this);
	}

	public Object getElementAt(int index) {
		return playerList.get(index);
	}

	public int getSize() {
		return playerList.size();
	}

	////
	// OBSERVER
	public void update(Observable o, Object arg) {
		fireContentsChanged(this, 0, playerList.size());
	}
}