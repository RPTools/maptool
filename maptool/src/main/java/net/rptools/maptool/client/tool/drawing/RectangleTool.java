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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import net.rptools.maptool.client.ScreenPoint;
import net.rptools.maptool.client.tool.ToolHelper;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.model.ZonePoint;
import net.rptools.maptool.model.drawing.DrawableColorPaint;
import net.rptools.maptool.model.drawing.Pen;
import net.rptools.maptool.model.drawing.ShapeDrawable;

/**
 * @author drice
 * 
 */
public class RectangleTool extends AbstractDrawingTool implements MouseMotionListener {
	private static final long serialVersionUID = 3258413928311830323L;

	protected Rectangle rectangle;
	protected ZonePoint originPoint;

	public RectangleTool() {
		try {
			setIcon(new ImageIcon(ImageIO.read(getClass().getClassLoader().getResourceAsStream("net/rptools/maptool/client/image/tool/draw-blue-box.png"))));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	@Override
	public String getInstructions() {
		return "tool.rect.instructions";
	}

	@Override
	public String getTooltip() {
		return "tool.rect.tooltip";
	}

	@Override
	public void paintOverlay(ZoneRenderer renderer, Graphics2D g) {
		if (rectangle != null) {
			Pen pen = getPen();
			if (pen.isEraser()) {
				pen = new Pen(pen);
				pen.setEraser(false);
				pen.setPaint(new DrawableColorPaint(Color.white));
				pen.setBackgroundPaint(new DrawableColorPaint(Color.white));
			}
			paintTransformed(g, renderer, new ShapeDrawable(rectangle, false), pen);
			ToolHelper.drawBoxedMeasurement(renderer, g, ScreenPoint.fromZonePoint(renderer, rectangle.x, rectangle.y),
					ScreenPoint.fromZonePoint(renderer, rectangle.x + rectangle.width, rectangle.y + rectangle.height));
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		ZonePoint zp = getPoint(e);
		if (SwingUtilities.isLeftMouseButton(e)) {
			if (rectangle == null) {
				originPoint = zp;
				rectangle = createRect(originPoint, originPoint);
			} else {
				rectangle = createRect(originPoint, zp);

				if (rectangle.width == 0 || rectangle.height == 0) {
					rectangle = null;
					renderer.repaint();
					return;
				}
				// Draw Rectangle with initial point as Center
				if (e.isAltDown()) {
					if (zp.x > originPoint.x)
						rectangle.x -= rectangle.width;

					if (zp.y > originPoint.y)
						rectangle.y -= rectangle.height;

					rectangle.width *= 2;
					rectangle.height *= 2;
				}
				//				System.out.println("Adding Rectangle to zone: " + rectangle);
				completeDrawable(renderer.getZone().getId(), getPen(), new ShapeDrawable(rectangle, false));
				rectangle = null;
			}
			setIsEraser(isEraser(e));
		}
		super.mousePressed(e);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (rectangle == null) {
			super.mouseDragged(e);
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		super.mouseMoved(e);

		if (rectangle != null) {
			ZonePoint p = getPoint(e);
			rectangle = createRect(originPoint, p);

			// Draw Rectangle with initial point as Center
			if (e.isAltDown()) {
				if (p.x > originPoint.x)
					rectangle.x -= rectangle.width;

				if (p.y > originPoint.y)
					rectangle.y -= rectangle.height;

				rectangle.width *= 2;
				rectangle.height *= 2;
			}
			renderer.repaint();
		}
	}

	/**
	 * Stop drawing a rectangle and repaint the zone.
	 */
	@Override
	public void resetTool() {
		if (rectangle != null) {
			rectangle = null;
			renderer.repaint();
		} else {
			super.resetTool();
		}
	}
}
