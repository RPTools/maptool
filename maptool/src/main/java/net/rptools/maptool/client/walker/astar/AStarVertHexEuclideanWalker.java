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

import net.rptools.maptool.model.Zone;

public class AStarVertHexEuclideanWalker extends AbstractAStarHexEuclideanWalker {

	public AStarVertHexEuclideanWalker(Zone zone) {
		super(zone);
		initNeighborMaps();
	}

	@Override
	protected void initNeighborMaps() {
		oddNeighborMap = new int[][] { { -1, 0, 1 }, { 0, -1, 1 }, { 1, 0, 1 },
				{ 0, 0, 0 }, { 0, 0, 0 },
				{ -1, 1, 1 }, { 0, 1, 1 }, { 1, 1, 1 } };

		evenNeighborMap = new int[][] { { -1, -1, 1 }, { 0, -1, 1 }, { 1, -1, 1 },
				{ 0, 0, 0 }, { 0, 0, 0 },
				{ -1, 0, 1 }, { 0, 1, 1 }, { 1, 0, 1 } };
	}

	@Override
	protected int[][] getNeighborMap(int x, int y) {

		return x % 2 == 0 ? evenNeighborMap : oddNeighborMap;
	}

}
