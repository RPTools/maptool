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

package net.rptools.maptool.client.tool;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.util.Set;

import javax.swing.*;

import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ScreenPoint;
import net.rptools.maptool.client.ui.MapToolFrame;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.util.GraphicsUtil;

/**
 * @author trevor
 */
public class ToolHelper {

	private static AbstractAction deleteTokenAction = new AbstractAction() {
		@Override
		public void actionPerformed(ActionEvent e) {
			ZoneRenderer renderer = (ZoneRenderer) e.getSource();

			// Check to see if this is the required action
			if (!MapTool.confirmTokenDelete()) {
				return;
			}
			boolean unhideImpersonated = false;
			boolean unhideSelected = false;
			if (renderer.getSelectedTokenSet().size() > 10) {
				if (MapTool.getFrame().getFrame(MapToolFrame.MTFrame.IMPERSONATED).isHidden() == false) {
					unhideImpersonated = true;
					MapTool.getFrame().getDockingManager().hideFrame(MapToolFrame.MTFrame.IMPERSONATED.name());
				}
				if (MapTool.getFrame().getFrame(MapToolFrame.MTFrame.SELECTION).isHidden() == false) {
					unhideSelected = true;
					MapTool.getFrame().getDockingManager().hideFrame(MapToolFrame.MTFrame.SELECTION.name());
				}
			}
			Set<GUID> selectedTokenSet = renderer.getSelectedTokenSet();

			for (GUID tokenGUID : selectedTokenSet) {
				Token token = renderer.getZone().getToken(tokenGUID);

				if (AppUtil.playerOwns(token)) {
					renderer.getZone().removeToken(tokenGUID);
					MapTool.serverCommand().removeToken(renderer.getZone().getId(), tokenGUID);
				}
			}
			if (unhideImpersonated) {
				MapTool.getFrame().getDockingManager().showFrame(MapToolFrame.MTFrame.IMPERSONATED.name());
			}

			if (unhideSelected) {
				MapTool.getFrame().getDockingManager().showFrame(MapToolFrame.MTFrame.SELECTION.name());
			}
		}
	};

	public static void drawDiamondMeasurement(ZoneRenderer renderer, Graphics2D g, Shape diamond) {
		double[] north = null;
		double[] west = null;
		double[] east = null;
		PathIterator path = diamond.getPathIterator(getPaintTransform(renderer));
		while (!path.isDone()) {
			double[] coords = new double[2];
			int segType = path.currentSegment(coords);
			if (segType != PathIterator.SEG_CLOSE) {
				if (north == null)
					north = coords;
				if (west == null)
					west = coords;
				if (east == null)
					east = coords;
				if (coords[1] < north[1])
					north = coords;
				if (coords[0] < west[0])
					west = coords;
				if (coords[0] > east[0])
					east = coords;
			}
			path.next();
		}
		// Measure
		int nx = (int) north[0];
		int ny = (int) north[1];
		int ex = (int) east[0];
		int ey = (int) east[1];
		int wx = (int) west[0];
		int wy = (int) west[1];
		if (g != null) {
			g.setColor(Color.white);
			g.setStroke(new BasicStroke(3));
			g.drawLine(nx, ny - 20, nx, ny - 10);
			g.drawLine(nx, ny - 15, ex, ey - 15);
			g.drawLine(ex, ey - 20, ex, ey - 10);
			g.drawLine(nx, ny - 15, wx, wy - 15);
			g.drawLine(wx, wy - 20, wx, wy - 10);
			g.setColor(Color.black);
			g.setStroke(new BasicStroke(1));
			g.drawLine(nx, ny - 20, nx, ny - 10);
			g.drawLine(nx, ny - 15, ex, ey - 15);
			g.drawLine(ex, ey - 20, ex, ey - 10);
			g.drawLine(nx, ny - 15, wx, wy - 15);
			g.drawLine(wx, wy - 20, wx, wy - 10);
			//g.setPaintMode();
			String displayString = String.format("%1.1f", isometricDistance(renderer, new ScreenPoint(nx, ny), new ScreenPoint(ex, ey)));
			GraphicsUtil.drawBoxedString(g, displayString, nx + 25, ny - 25);
			displayString = String.format("%1.1f", isometricDistance(renderer, new ScreenPoint(nx, ny), new ScreenPoint(wx, wy)));
			GraphicsUtil.drawBoxedString(g, displayString, nx - 25, ny - 25);
		}
	}

	public static void drawBoxedMeasurement(ZoneRenderer renderer, Graphics2D g, ScreenPoint startPoint, ScreenPoint endPoint) {
		if (!MapTool.getFrame().isPaintDrawingMeasurement())
			return;

		// Calculations
		int left = (int) Math.min(startPoint.x, endPoint.x);
		int top = (int) Math.min(startPoint.y, endPoint.y);
		int right = (int) Math.max(startPoint.x, endPoint.x);
		int bottom = (int) Math.max(startPoint.y, endPoint.y);

		// outline
		g.setColor(Color.white);
		g.setStroke(new BasicStroke(3));
		// HORIZONTAL Measure
		g.drawLine(left, top - 15, right, top - 15);
		g.drawLine(left, top - 20, left, top - 10);
		g.drawLine(right, top - 20, right, top - 10);
		// VETICAL Measure
		g.drawLine(right + 15, top, right + 15, bottom);
		g.drawLine(right + 10, top, right + 20, top);
		g.drawLine(right + 10, bottom, right + 20, bottom);
		// inner line
		g.setColor(Color.black);
		g.setStroke(new BasicStroke(1));
		// HORIZONTAL Measure
		g.drawLine(left, top - 15, right, top - 15);
		g.drawLine(left, top - 20, left, top - 10);
		g.drawLine(right, top - 20, right, top - 10);
		// VETICAL Measure
		g.drawLine(right + 15, top, right + 15, bottom);
		g.drawLine(right + 10, top, right + 20, top);
		g.drawLine(right + 10, bottom, right + 20, bottom);

		// Horizontal number
		String displayString = String.format("%1.1f", euclideanDistance(renderer, new ScreenPoint(left, top), new ScreenPoint(right, top)));
		GraphicsUtil.drawBoxedString(g, displayString, left + (right - left) / 2, top - 15);

		// Verical number
		displayString = String.format("%1.1f", euclideanDistance(renderer, new ScreenPoint(right, top), new ScreenPoint(right, bottom)));
		GraphicsUtil.drawBoxedString(g, displayString, right + 18, bottom + (top - bottom) / 2);

	}

	public static void drawMeasurement(ZoneRenderer renderer, Graphics2D g, ScreenPoint startPoint, ScreenPoint endPoint) {
		if (!MapTool.getFrame().isPaintDrawingMeasurement())
			return;

		boolean dirLeft = startPoint.x > endPoint.x;
		boolean dirUp = startPoint.y < endPoint.y;

		String displayString = String.format("%1.1f", euclideanDistance(renderer, startPoint, endPoint));

		GraphicsUtil.drawBoxedString(g, displayString,
				(int) endPoint.x + (dirLeft ? -15 : 10),
				(int) endPoint.y + (dirUp ? 15 : -15),
				dirLeft ? SwingUtilities.LEFT : SwingUtilities.RIGHT);
	}

	/**
	 * Draw a measurement on the passed graphics object.
	 * 
	 * @param g
	 *            Draw the measurement here.
	 * @param distance
	 *            The size of the measurement in feet
	 * @param x
	 *            The x location of the measurement
	 * @param y
	 *            The y location of the measurement
	 */
	public static void drawMeasurement(Graphics2D g, int distance, int x, int y) {
		if (!MapTool.getFrame().isPaintDrawingMeasurement())
			return;
		String radius = Integer.toString(distance);
		GraphicsUtil.drawBoxedString(g, radius, x, y);
	}

	private static double euclideanDistance(ZoneRenderer renderer, ScreenPoint p1, ScreenPoint p2) {
		double a = p2.x - p1.x;
		double b = p2.y - p1.y;

		return Math.sqrt(a * a + b * b) * renderer.getZone().getUnitsPerCell() / renderer.getScaledGridSize();
	}

	private static double isometricDistance(ZoneRenderer renderer, ScreenPoint p1, ScreenPoint p2) {
		double b = p2.y - p1.y;
		//return b;
		return 2 * b * renderer.getZone().getUnitsPerCell() / renderer.getScaledGridSize();
	}

	protected static AffineTransform getPaintTransform(ZoneRenderer renderer) {
		AffineTransform transform = new AffineTransform();
		transform.translate(renderer.getViewOffsetX(), renderer.getViewOffsetY());
		transform.scale(renderer.getScale(), renderer.getScale());
		return transform;
	}

	protected static AbstractAction getDeleteTokenAction() {
		return deleteTokenAction;
	}
}
