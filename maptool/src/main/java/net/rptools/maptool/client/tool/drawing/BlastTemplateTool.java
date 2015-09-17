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
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import net.rptools.maptool.client.ScreenPoint;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.ZonePoint;
import net.rptools.maptool.model.drawing.AbstractTemplate;
import net.rptools.maptool.model.drawing.BlastTemplate;
import net.rptools.maptool.model.drawing.RadiusTemplate;
import net.rptools.maptool.model.drawing.AbstractTemplate.Direction;

/**
 * Draws a square blast template next to a base cell.
 * 
 * @author Jay
 */
public class BlastTemplateTool extends BurstTemplateTool {

	/*---------------------------------------------------------------------------------------------
	 * Constructors
	 *-------------------------------------------------------------------------------------------*/

	/**
	 * Set the icon for the base tool.
	 */
	public BlastTemplateTool() {
		try {
			setIcon(new ImageIcon(ImageIO.read(getClass().getClassLoader().getResourceAsStream(
					"net/rptools/maptool/client/image/tool/temp-blue-square.png"))));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} // endtry
	}

	/*---------------------------------------------------------------------------------------------
	 * Overridden RadiusTemplateTool methods
	 *-------------------------------------------------------------------------------------------*/

	/**
	 * @see net.rptools.maptool.client.tool.drawing.BurstTemplateTool#createBaseTemplate()
	 */
	@Override
	protected AbstractTemplate createBaseTemplate() {
		return new BlastTemplate();
	}

	/**
	 * @see net.rptools.maptool.client.ui.Tool#getTooltip()
	 */
	@Override
	public String getTooltip() {
		return "tool.blasttemplate.tooltip";
	}

	/**
	 * @see net.rptools.maptool.client.ui.Tool#getInstructions()
	 */
	@Override
	public String getInstructions() {
		return "tool.blasttemplate.instructions";
	}

	/**
	 * @see net.rptools.maptool.client.tool.drawing.RadiusTemplateTool#setRadiusFromAnchor(java.awt.event.MouseEvent)
	 */
	@Override
	protected void setRadiusFromAnchor(MouseEvent e) {
		// Determine mouse cell position relative to base cell and then pass to blast template
		CellPoint workingCell = renderer.getZone().getGrid().convert(getCellAtMouse(e));
		CellPoint vertexCell = renderer.getZone().getGrid().convert(template.getVertex());
		((BlastTemplate) template).setControlCellRelative(workingCell.x - vertexCell.x, workingCell.y - vertexCell.y);
	}
}
