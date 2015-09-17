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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Path<T extends AbstractPoint> {
	private final List<T> cellList = new LinkedList<T>();
	private final List<T> waypointList = new LinkedList<T>();

	public void addPathCell(T point) {
		cellList.add(point);
	}

	public void addAllPathCells(List<T> cells) {
		cellList.addAll(cells);
	}

	public List<T> getCellPath() {
		return Collections.unmodifiableList(cellList);
	}

	public void replaceLastPoint(T point) {
		cellList.remove(cellList.size() - 1);
		cellList.add(point);
	}

	public void addWayPoint(T point) {
		waypointList.add(point);
	}

	public boolean isWaypoint(T point) {
		return waypointList.contains(point);
	}

	public T getLastWaypoint() {
		if (waypointList.isEmpty())
			return null;
		return waypointList.get(waypointList.size() - 1);
	}

	/**
	 * Returns the last waypoint if there is one, or the last T point if there is not.
	 * 
	 * @return a non-<code>null</code> location
	 */
	public T getLastJunctionPoint() {
		T temp = getLastWaypoint();
		return temp != null ? temp : cellList.get(cellList.size() - 1);
	}

	public Path<T> derive(int cellOffsetX, int cellOffsetY) {
		Path<T> path = new Path<T>();
		for (T cp : cellList) {
			T np = (T) cp.clone();
			np.x -= cellOffsetX;
			np.y -= cellOffsetY;
			path.addPathCell(np);
		}
		for (T cp : waypointList) {
			T np = (T) cp.clone();
			np.x -= cellOffsetX;
			np.y -= cellOffsetY;
			path.addWayPoint(np);
		}
		return path;
	}
}
