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

package net.rptools.maptool.client.ui.zone;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.List;

import net.rptools.maptool.model.drawing.DrawnElement;

/**
 */
public interface DrawableRenderer {

	public void renderDrawables(Graphics g, List<DrawnElement> drawableList, Rectangle viewport, double scale);

	public void flush();
}
