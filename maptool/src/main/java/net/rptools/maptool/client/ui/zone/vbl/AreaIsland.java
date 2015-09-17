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

package net.rptools.maptool.client.ui.zone.vbl;

import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Set;

public class AreaIsland implements AreaContainer {

	private AreaMeta meta;
	private Set<AreaOcean> oceanSet = new HashSet<AreaOcean>();

	public AreaIsland(AreaMeta meta) {
		this.meta = meta;
	}

	public Set<VisibleAreaSegment> getVisibleAreaSegments(Point2D origin) {

		return meta.getVisibleAreas(origin);
	}

	public AreaOcean getDeepestOceanAt(Point2D point) {

		if (!meta.area.contains(point)) {
			return null;
		}

		for (AreaOcean ocean : oceanSet) {
			AreaOcean deepOcean = ocean.getDeepestOceanAt(point);
			if (deepOcean != null) {
				return deepOcean;
			}
		}

		// If we don't have an ocean that contains the point then 
		// the point is not technically in an ocean
		return null;
	}

	public Set<AreaOcean> getOceans() {
		return new HashSet<AreaOcean>(oceanSet);
	}

	public void addOcean(AreaOcean ocean) {
		oceanSet.add(ocean);
	}

	////
	// AREA CONTAINER
	public Area getBounds() {
		return meta.area;
	}
}
