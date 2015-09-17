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

package net.rptools.maptool.model.vision;

import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Vision;
import net.rptools.maptool.model.Zone;

public class RoundVision extends Vision {
	public RoundVision() {
	}

	public RoundVision(int distance) {
		setDistance(distance);
	}

	@Override
	public Anchor getAnchor() {
		return Vision.Anchor.CENTER;
	}

	@Override
	protected Area createArea(Zone zone, Token token) {
		int size = getDistance() * getZonePointsPerCell(zone) * 2;
		int half = size / 2;
		Area area = new Area(new Ellipse2D.Double(-half, -half, size, size));

		return area;
	}

	@Override
	public String toString() {
		return "Round";
	}
}
