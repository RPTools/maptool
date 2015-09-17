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

package net.rptools.maptool.client.walker.astar;

import net.rptools.maptool.model.CellPoint;

public class AStarCellPoint extends CellPoint {
	AStarCellPoint parent;
	double hScore;
	double gScore;

	public AStarCellPoint() {
		super(0, 0);
	}

	public AStarCellPoint(int x, int y) {
		super(x, y);
	}

	public AStarCellPoint(CellPoint p) {
		super(p.x, p.y);
	}

	public double cost() {
		return hScore + gScore;
	}
}
