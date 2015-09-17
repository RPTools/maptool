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
import java.awt.geom.Ellipse2D;

/**
 * An oval.
 */
public class Oval extends Rectangle {
	/**
	 * @param x
	 * @param y
	 */
	public Oval(int x, int y, int width, int height) {
		super(x, y, width, height);
	}

	@Override
	protected void draw(Graphics2D g) {
		int minX = Math.min(startPoint.x, endPoint.x);
		int minY = Math.min(startPoint.y, endPoint.y);

		int width = Math.abs(startPoint.x - endPoint.x);
		int height = Math.abs(startPoint.y - endPoint.y);

		g.drawOval(minX, minY, width, height);
	}

	@Override
	protected void drawBackground(Graphics2D g) {
		int minX = Math.min(startPoint.x, endPoint.x);
		int minY = Math.min(startPoint.y, endPoint.y);

		int width = Math.abs(startPoint.x - endPoint.x);
		int height = Math.abs(startPoint.y - endPoint.y);

		g.fillOval(minX, minY, width, height);
	}

	@Override
	public Area getArea() {
		java.awt.Rectangle r = getBounds();
		return new Area(new Ellipse2D.Double(r.x, r.y, r.width, r.height));
	}
}
