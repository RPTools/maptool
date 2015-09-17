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
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Area;

/**
 * An rectangle
 */
public class Rectangle extends AbstractDrawing {
	protected Point startPoint;
	protected Point endPoint;
	private transient java.awt.Rectangle bounds;

	public Rectangle(int startX, int startY, int endX, int endY) {
		startPoint = new Point(startX, startY);
		endPoint = new Point(endX, endY);
	}

	public Area getArea() {
		return new Area(getBounds());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.rptools.maptool.model.drawing.Drawable#getBounds()
	 */
	public java.awt.Rectangle getBounds() {
		if (bounds == null) {
			int x = Math.min(startPoint.x, endPoint.x);
			int y = Math.min(startPoint.y, endPoint.y);
			int width = Math.abs(endPoint.x - startPoint.x);
			int height = Math.abs(endPoint.y - startPoint.y);

			bounds = new java.awt.Rectangle(x, y, width, height);
		}
		return bounds;
	}

	public Point getStartPoint() {
		return startPoint;
	}

	public Point getEndPoint() {
		return endPoint;
	}

	@Override
	protected void draw(Graphics2D g) {
		int minX = Math.min(startPoint.x, endPoint.x);
		int minY = Math.min(startPoint.y, endPoint.y);

		int width = Math.abs(startPoint.x - endPoint.x);
		int height = Math.abs(startPoint.y - endPoint.y);

		Object oldAA = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		g.drawRect(minX, minY, width, height);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAA);
	}

	@Override
	protected void drawBackground(Graphics2D g) {
		int minX = Math.min(startPoint.x, endPoint.x);
		int minY = Math.min(startPoint.y, endPoint.y);

		int width = Math.abs(startPoint.x - endPoint.x);
		int height = Math.abs(startPoint.y - endPoint.y);

		Object oldAA = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		g.fillRect(minX, minY, width, height);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAA);
	}
}
