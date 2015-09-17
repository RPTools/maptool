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

import java.awt.geom.Area;

public class ExposedAreaMetaData {
	private Area exposedAreaHistory;

	public ExposedAreaMetaData() {
		exposedAreaHistory = new Area();
	}

	public ExposedAreaMetaData(Area area) {
		exposedAreaHistory = new Area(area);
	}

	public Area getExposedAreaHistory() {
		//		if (exposedAreaHistory == null) {
		//			exposedAreaHistory = new Area();
		//		}
		return exposedAreaHistory;
	}

	public void addToExposedAreaHistory(Area newArea) {
		if (newArea != null && !newArea.isEmpty()) {
			exposedAreaHistory.add(newArea);
		}
	}

	public void removeExposedAreaHistory(Area newArea) {
		if (newArea != null && !newArea.isEmpty()) {
			exposedAreaHistory.subtract(newArea);
		}
	}

	public void clearExposedAreaHistory() {
		exposedAreaHistory = new Area();
	}
}
