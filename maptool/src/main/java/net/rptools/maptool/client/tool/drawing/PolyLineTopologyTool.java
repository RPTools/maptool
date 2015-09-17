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
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import net.rptools.maptool.client.AppStyle;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.drawing.DrawableColorPaint;
import net.rptools.maptool.model.drawing.LineSegment;
import net.rptools.maptool.model.drawing.Pen;

/**
 * Tool for drawing freehand lines.
 */
public class PolyLineTopologyTool extends PolygonTopologyTool implements MouseMotionListener {
	private static final long serialVersionUID = 3258132466219627316L;

	public PolyLineTopologyTool() {
		try {
			setIcon(new ImageIcon(ImageIO.read(getClass().getClassLoader().getResourceAsStream("net/rptools/maptool/client/image/tool/top-blue-free.png"))));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	@Override
	public String getTooltip() {
		return "tool.polylinetopo.tooltip";
	}

	@Override
	public String getInstructions() {
		return "tool.poly.instructions";
	}

	protected boolean isBackgroundFill(MouseEvent e) {
		return false;
	}

	protected Pen getPen() {

		Pen pen = new Pen(MapTool.getFrame().getPen());
		pen.setEraser(isEraser());
		pen.setForegroundMode(Pen.MODE_SOLID);
		pen.setBackgroundMode(Pen.MODE_TRANSPARENT);
		pen.setThickness(2.0f);
		pen.setPaint(new DrawableColorPaint(isEraser() ? AppStyle.topologyRemoveColor : AppStyle.topologyAddColor));

		return pen;
	}

	protected Polygon getPolygon(LineSegment line) {
		Polygon polygon = new Polygon();
		for (Point point : line.getPoints()) {
			polygon.addPoint(point.x, point.y);
		}

		return polygon;
	}
}
