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

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;

import javax.swing.CellRendererPane;

import net.rptools.maptool.client.swing.TwoToneTextPane;
import net.rptools.maptool.client.tool.drawing.DrawnTextTool;

/**
 * @author jgorrell
 * @version $Revision: 5945 $ $Date: 2013-06-03 04:35:50 +0930 (Mon, 03 Jun 2013) $ $Author: azhrei_fje $
 */
public class DrawnLabel extends AbstractDrawing {

	/**
	 * The bounds of the display rectangle
	 */
	private Rectangle bounds = new Rectangle();

	/**
	 * Text being painted.
	 */
	private String text;

	/**
	 * The font used to paint the text.
	 */
	private String font;

	/**
	 * The pane used to render the text
	 */
	private transient CellRendererPane renderer;

	/**
	 * The text pane used to paint the text.
	 */
	private transient TwoToneTextPane textPane;

	/**
	 * Create a new drawn label.
	 * 
	 * @param theText Text to be drawn
	 * @param theBounds The bounds containing the text.
	 * @param aFont The font used to draw the text as a string that can
	 * be passed to {@link Font#decode(java.lang.String)}.
	 */
	public DrawnLabel(String theText, Rectangle theBounds, String aFont) {
		text = theText;
		bounds = theBounds;
		font = aFont;
	}

	/**
	 * @see net.rptools.maptool.model.drawing.Drawable#draw(java.awt.Graphics2D, net.rptools.maptool.model.drawing.Pen)
	 */
	public void draw(Graphics2D aG) {
		if (renderer == null) {
			renderer = new CellRendererPane();
			textPane = DrawnTextTool.createTextPane(bounds, null, font);
			textPane.setText(text);
		}
		renderer.paintComponent(aG, textPane, null, bounds);
	}

	@Override
	protected void drawBackground(Graphics2D g) {
	}

	/**
	 * @see net.rptools.maptool.model.drawing.Drawable#getBounds()
	 */
	public Rectangle getBounds() {
		return bounds;
	}

	public Area getArea() {
		// TODO Auto-generated method stub
		return null;
	}
}
