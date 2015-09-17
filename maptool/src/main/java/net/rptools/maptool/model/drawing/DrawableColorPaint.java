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

import java.awt.Color;
import java.awt.Paint;
import java.awt.image.ImageObserver;
import java.io.Serializable;

public class DrawableColorPaint extends DrawablePaint implements Serializable {
	private int color;
	private transient Color colorCache;

	public DrawableColorPaint() {
		// For deserialization
	}

	public DrawableColorPaint(Color color) {
		this.color = color.getRGB();
	}

	public int getColor() {
		return color;
	}

	@Override
	public Paint getPaint(ImageObserver... observers) {
		if (colorCache == null) {
			colorCache = new Color(color);
		}
		return colorCache;
	}

	@Override
	public Paint getPaint(int offsetX, int offsetY, double scale, ImageObserver... observer) {
		return getPaint();
	}
}
