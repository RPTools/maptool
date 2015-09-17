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

package net.rptools.maptool.model;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import net.rptools.maptool.client.tool.PointerTool;

public class MovementKey extends AbstractAction {
	private static final long serialVersionUID = -4103031698708914986L;
	private final double dx, dy;
	private final PointerTool tool; // I'd like to store this in the Grid, but then it has to be final :(

	public MovementKey(PointerTool callback, double x, double y) {
		tool = callback;
		dx = x;
		dy = y;
	}

	@Override
	public String toString() {
		return "[" + dx + "," + dy + "]";
	}

	public void actionPerformed(ActionEvent e) {
		tool.handleKeyMove(dx, dy);
	}
}