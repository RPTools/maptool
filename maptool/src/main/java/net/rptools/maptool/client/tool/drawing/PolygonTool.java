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

package net.rptools.maptool.client.tool.drawing;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.MouseMotionListener;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.drawing.Drawable;
import net.rptools.maptool.model.drawing.LineSegment;
import net.rptools.maptool.model.drawing.Pen;
import net.rptools.maptool.model.drawing.ShapeDrawable;

/**
 * Tool for drawing freehand lines.
 */
public class PolygonTool extends LineTool implements MouseMotionListener {
	private static final long serialVersionUID = 3258132466219627316L;

	public PolygonTool() {
		try {
			setIcon(new ImageIcon(ImageIO.read(getClass().getClassLoader().getResourceAsStream("net/rptools/maptool/client/image/tool/draw-blue-strtlines.png"))));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	@Override
	public String getTooltip() {
		return "tool.poly.tooltip";
	}

	@Override
	public String getInstructions() {
		return "tool.poly.instructions";
	}

	@Override
	protected void completeDrawable(GUID zoneGUID, Pen pen, Drawable drawable) {
		LineSegment line = (LineSegment) drawable;
		super.completeDrawable(zoneGUID, pen, new ShapeDrawable(getPolygon(line)));
	}

	@Override
	protected Polygon getPolygon(LineSegment line) {
		Polygon polygon = new Polygon();
		for (Point point : line.getPoints()) {
			polygon.addPoint(point.x, point.y);
		}
		return polygon;
	}
}
