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

public interface GridCapabilities {
	/**
	 * Whether the parent grid type supports snap-to-grid. Some may not, such as the Gridless grid type.
	 * 
	 * @return
	 */
	public boolean isSnapToGridSupported();

	/**
	 * Whether the parent grid type supports automatic pathing from point A to point B. Usually true except for the
	 * Gridless grid type.
	 * 
	 * @return
	 */
	public boolean isPathingSupported();

	/**
	 * Whether ...
	 * 
	 * @return
	 */
	public boolean isPathLineSupported();

	/**
	 * Whether the parent grid supports the concept of coordinates to be placed on the grid. Generally this requires a
	 * grid type that has some notion of "cell size", which means Gridless need not apply. ;-)
	 * 
	 * @return
	 */
	public boolean isCoordinatesSupported();

	/**
	 * The secondary dimension should be linked to changes in the primary dimension but the primary dimension is
	 * independent of the secondary.
	 */
	public boolean isSecondDimensionAdjustmentSupported();
}
