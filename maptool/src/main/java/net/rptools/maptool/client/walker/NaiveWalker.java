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

package net.rptools.maptool.client.walker;

import java.util.ArrayList;
import java.util.List;

import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.Zone;

public class NaiveWalker extends AbstractZoneWalker {
	public NaiveWalker(Zone zone) {
		super(zone);
	}

	private int distance;

	@Override
	protected List<CellPoint> calculatePath(CellPoint start, CellPoint end) {
		List<CellPoint> list = new ArrayList<CellPoint>();

		int x = start.x;
		int y = start.y;

		int count = 0;
		while (true && count < 100) {
			list.add(new CellPoint(x, y));

			if (x == end.x && y == end.y) {
				break;
			}
			if (x < end.x)
				x++;
			if (x > end.x)
				x--;
			if (y < end.y)
				y++;
			if (y > end.y)
				y--;

			count++;
		}
		distance = (list.size() - 1) * 5;
		return list;
	}

	public int getDistance() {
		return distance;
	}
}
