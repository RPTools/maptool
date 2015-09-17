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

public class AStarHorizHexEuclideanWalker extends AbstractAStarHexEuclideanWalker {
	public AStarHorizHexEuclideanWalker(Zone zone) {
		super(zone);
		initNeighborMaps();
	}

	// @formatter:off
	@Override
	protected void initNeighborMaps() {
		oddNeighborMap = new int[][] 
	      { { 0, -1, 1 },	{ 0, 0, 0 },		{ 1, -1, 1 }, 
			{ -1, 0, 1 },						{ 1, 0, 1 }, 
			{ 0, 1, 1 },		{ 0, 0, 0 },		{ 1, 1, 1 } };
		
		evenNeighborMap = new int[][] 
  	      { { -1, -1, 1 },	{ 0, 0, 0 },		{ 0, -1, 1 }, 
			{ -1, 0, 1 },						{ 1, 0, 1 }, 
			{ -1, 1, 1 },	{ 0, 0, 0 },		{ 0, 1, 1 } };
	}
	// @formatter:on

	@Override
	protected int[][] getNeighborMap(int x, int y) {
		return y % 2 == 0 ? evenNeighborMap : oddNeighborMap;
	}
}
