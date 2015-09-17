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

package net.rptools.maptool.model.drawing;

import java.awt.Graphics2D;
import java.awt.geom.Area;

import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Zone;

/**
 * @author drice
 */
public interface Drawable {
	public void draw(Graphics2D g, Pen pen);

	public java.awt.Rectangle getBounds();

	public Area getArea();

	public GUID getId();

	public Zone.Layer getLayer();

	public void setLayer(Zone.Layer layer);
}
