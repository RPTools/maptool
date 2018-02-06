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

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import net.rptools.maptool.client.AppStyle;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.drawing.Drawable;
import net.rptools.maptool.model.drawing.DrawableColorPaint;
import net.rptools.maptool.model.drawing.LineSegment;
import net.rptools.maptool.model.drawing.Pen;
import net.rptools.maptool.model.drawing.ShapeDrawable;

/**
 * Tool for drawing freehand lines.
 */
public class PolygonTopologyTool extends LineTool implements MouseMotionListener {
	private static final long serialVersionUID = 3258132466219627316L;

	public PolygonTopologyTool() {
		try {
			setIcon(new ImageIcon(ImageIO.read(getClass().getClassLoader().getResourceAsStream("net/rptools/maptool/client/image/tool/top-blue-poly.png"))));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	@Override
	// Override abstracttool to prevent color palette from
	// showing up
	protected void attachTo(ZoneRenderer renderer) {
		super.attachTo(renderer);
		// Hide the drawable color palette
		MapTool.getFrame().hideControlPanel();
	}

	@Override
	public boolean isAvailable() {
		return MapTool.getPlayer().isGM();
	}

	@Override
	protected boolean drawMeasurement() {
		return false;
	}

	@Override
	public String getTooltip() {
		return "tool.polytopo.tooltip";
	}

	@Override
	public String getInstructions() {
		return "tool.poly.instructions";
	}

	@Override
	protected boolean isBackgroundFill(MouseEvent e) {
		return true;
	}

	@Override
	protected void completeDrawable(GUID zoneGUID, Pen pen, Drawable drawable) {
		Area area = new Area();

		if (drawable instanceof LineSegment) {
			LineSegment line = (LineSegment) drawable;
			BasicStroke stroke = new BasicStroke(pen.getThickness(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);

			Path2D path = new Path2D.Double();
			Point lastPoint = null;

			for (Point point : line.getPoints()) {
				if (path.getCurrentPoint() == null) {
					path.moveTo(point.x, point.y);
				} else if (!point.equals(lastPoint)) {
					path.lineTo(point.x, point.y);
					lastPoint = point;
				}
			}

			area.add(new Area(stroke.createStrokedShape(path)));
		} else {
			area = new Area(((ShapeDrawable) drawable).getShape());
		}
		if (pen.isEraser()) {
			renderer.getZone().removeTopology(area);
			MapTool.serverCommand().removeTopology(renderer.getZone().getId(), area);
		} else {
			renderer.getZone().addTopology(area);
			MapTool.serverCommand().addTopology(renderer.getZone().getId(), area);
		}
		renderer.repaint();
	}

	@Override
	protected Pen getPen() {
		Pen pen = new Pen(MapTool.getFrame().getPen());
		pen.setEraser(isEraser());
		pen.setForegroundMode(Pen.MODE_TRANSPARENT);
		pen.setBackgroundMode(Pen.MODE_SOLID);
		pen.setThickness(1.0f);
		pen.setPaint(new DrawableColorPaint(isEraser() ? AppStyle.topologyRemoveColor : AppStyle.topologyAddColor));
		return pen;
	}

	@Override
	protected Polygon getPolygon(LineSegment line) {
		Polygon polygon = new Polygon();
		for (Point point : line.getPoints()) {
			polygon.addPoint(point.x, point.y);
		}
		return polygon;
	}

	@Override
	public void paintOverlay(ZoneRenderer renderer, Graphics2D g) {
		if (MapTool.getPlayer().isGM()) {
			Zone zone = renderer.getZone();
			Area topology = zone.getTopology();

			Graphics2D g2 = (Graphics2D) g.create();
			g2.translate(renderer.getViewOffsetX(), renderer.getViewOffsetY());
			g2.scale(renderer.getScale(), renderer.getScale());

			g2.setColor(AppStyle.tokenTopologyColor);
			g2.fill(getTokenTopology());

			g2.setColor(AppStyle.topologyColor);
			g2.fill(topology);

			g2.dispose();
		}
		super.paintOverlay(renderer, g);
	}
}
