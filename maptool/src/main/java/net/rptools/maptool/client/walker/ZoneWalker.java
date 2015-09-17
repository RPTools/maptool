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

import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.Path;

public interface ZoneWalker {
	public void setWaypoints(CellPoint... points);

	public void addWaypoints(CellPoint... point);

	public CellPoint replaceLastWaypoint(CellPoint point);

	public boolean isWaypoint(CellPoint point);

	public int getDistance();

	public Path<CellPoint> getPath();

	public CellPoint getLastPoint();

	/**
	 * Remove an existing waypoint. Nothing is removed if the passed point is not a waypoint.
	 * 
	 * @param point
	 *            The point to be removed
	 * @return The value <code>true</code> is returned if the point is removed.
	 */
	boolean removeWaypoint(CellPoint point);

	/**
	 * Toggle the existence of a way point. A waypoint is added if the passed point is not on an existing waypoint or a
	 * waypoint is removed if it is on an existing point.
	 * 
	 * @param point
	 *            Point being toggled
	 * @return The value <code>true</code> if a waypoint was added, <code>false</code> if one was removed.
	 */
	boolean toggleWaypoint(CellPoint point);
}
