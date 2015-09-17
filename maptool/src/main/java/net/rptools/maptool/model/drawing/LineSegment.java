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

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;

/**
 * @author drice
 */
public class LineSegment extends AbstractDrawing {
	private final List<Point> points = new ArrayList<Point>();
	private Float width;
	private transient int lastPointCount = -1;
	private transient Rectangle cachedBounds;
	private transient Area area;

	public LineSegment(float width) {
		this.width = width;
	}

	/**
	 * Manipulate the points by calling {@link #getPoints} and then adding {@link Point} objects to the returned
	 * {@link List}.
	 */
	public List<Point> getPoints() {
		// This is really, really ugly, but we need to flush the area on any change to the shape
		// and typically the reason for calling this method is to change the list
		area = null;
		return points;
	}

	public Area getArea() {
		if (area == null) {
			area = createLineArea();
		}
		return area;
	}

	private Area createLineArea() {
		GeneralPath gp = null;
		for (Point point : points) {
			if (gp == null) {
				gp = new GeneralPath();
				gp.moveTo(point.x, point.y);
				continue;
			}
			gp.lineTo(point.x, point.y);
		}
		BasicStroke stroke = new BasicStroke(width != null ? width : 2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		return new Area(stroke.createStrokedShape(gp));
	}

	@Override
	protected void draw(Graphics2D g) {
		if (width == null) {
			// Handle legacy values
			area = null; // reset, build with new value
			width = ((BasicStroke) g.getStroke()).getLineWidth();
		}
		Area area = getArea();
		g.fill(area);
	}

	@Override
	protected void drawBackground(Graphics2D g) {
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.rptools.maptool.model.drawing.Drawable#getBounds()
	 */
	public Rectangle getBounds() {
		if (lastPointCount == points.size()) {
			return cachedBounds;
		}
		Rectangle bounds = new Rectangle(points.get(0));
		for (Point point : points) {
			bounds.add(point);
		}

		// Special casing
		if (bounds.width < 1) {
			bounds.width = 1;
		}
		if (bounds.height < 1) {
			bounds.height = 1;
		}
		cachedBounds = bounds;
		lastPointCount = points.size();
		return bounds;
	}
}
