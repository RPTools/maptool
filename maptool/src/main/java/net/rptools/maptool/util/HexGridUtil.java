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

package net.rptools.maptool.util;

import java.awt.Dimension;
import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.HexGrid;
import net.rptools.maptool.model.HexGridHorizontal;

/**
 * Provides methods to handle hexgrid issues that don't exist with a square grid.
 * @author Tylere
 */
public class HexGridUtil {
	/** 
	 * Convert to u-v coordinates where the v-axis points
	 * along the direction of edge to edge hexes
	 */
	private static int[] toUVCoords(CellPoint cp, HexGrid grid) {
		int cpU, cpV;
		if (grid instanceof HexGridHorizontal) {
			cpU = cp.y;
			cpV = cp.x;
		} else {
			cpU = cp.x;
			cpV = cp.y;
		}
		return new int[] { cpU, cpV };
	}

	/**
	 * Convert from u-v coords to grid coords
	 * @return the point in grid-space
	 */
	private static CellPoint fromUVCoords(int u, int v, HexGrid grid) {
		CellPoint cp = new CellPoint(u, v);
		if (grid instanceof HexGridHorizontal) {
			cp.x = v;
			cp.y = u;
		}
		return cp;
	}

	public static CellPoint getWaypoint(HexGrid grid, CellPoint cp, int width, int height) {
		if (width == height) {
			int[] cpUV = toUVCoords(cp, grid);
			return fromUVCoords(cpUV[0], cpUV[1] + (int) ((width - 1) / 2), grid);
		}
		return cp;
	}
}
