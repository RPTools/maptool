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

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Area;
import java.io.IOException;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.drawing.Drawable;
import net.rptools.maptool.model.drawing.LineSegment;
import net.rptools.maptool.model.drawing.Pen;
import net.rptools.maptool.model.drawing.ShapeDrawable;

/**
 * Tool for drawing freehand lines.
 */
public class FreehandExposeTool extends FreehandTool implements MouseMotionListener {
	private static final long serialVersionUID = 3258132466219627316L;

	public FreehandExposeTool() {
		try {
			setIcon(new ImageIcon(ImageIO.read(getClass().getClassLoader().getResourceAsStream("net/rptools/maptool/client/image/tool/fog-blue-free.png"))));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	@Override
	public String getTooltip() {
		return "tool.freehandexpose.tooltip";
	}

	@Override
	public String getInstructions() {
		return "tool.freehandexpose.instructions";
	}

	@Override
	public boolean isAvailable() {
		return MapTool.getPlayer().isGM();
	}

	@Override
	protected Pen getPen() {
		Pen pen = super.getPen();
		pen.setThickness(1);
		return pen;
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
	protected boolean isBackgroundFill(MouseEvent e) {
		// Expose tools are implied to be filled
		return false;
	}

	@Override
	protected void stopLine(MouseEvent e) {
		LineSegment line = getLine();

		if (line == null)
			return; // Escape has been pressed
		addPoint(e);
		completeDrawable(renderer.getZone().getId(), getPen(), line);
		resetTool();
	}

	@Override
	protected void completeDrawable(GUID zoneId, Pen pen, Drawable drawable) {
		if (!MapTool.getPlayer().isGM()) {
			MapTool.showError("msg.error.fogexpose");
			MapTool.getFrame().refresh();
			return;
		}
		Zone zone = MapTool.getCampaign().getZone(zoneId);

		Area area = null;
		if (drawable instanceof LineSegment) {
			area = new Area(getPolygon((LineSegment) drawable));
		}
		if (drawable instanceof ShapeDrawable) {
			area = new Area(((ShapeDrawable) drawable).getShape());
		}
		Set<GUID> selectedToks = MapTool.getFrame().getCurrentZoneRenderer().getSelectedTokenSet();
		if (pen.isEraser()) {
			zone.hideArea(area, selectedToks);
			MapTool.serverCommand().hideFoW(zone.getId(), area, selectedToks);
		} else {
			zone.exposeArea(area, selectedToks);
			MapTool.serverCommand().exposeFoW(zone.getId(), area, selectedToks);
		}
		MapTool.getFrame().refresh();
	}
}
