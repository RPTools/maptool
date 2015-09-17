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

package net.rptools.maptool.client.ui.token;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import net.rptools.maptool.model.Token;

/**
 * Paints a single reduced alpha color over the token.
 * 
 * @author jgorrell
 * @version $Revision: 5945 $ $Date: 2013-06-03 04:35:50 +0930 (Mon, 03 Jun 2013) $ $Author: azhrei_fje $
 */
public class ShadedTokenOverlay extends BooleanTokenOverlay {

	/*---------------------------------------------------------------------------------------------
	 * Instance Variables
	 *-------------------------------------------------------------------------------------------*/

	/**
	 * The color that is painted over the token.
	 */
	private Color color;

	/*---------------------------------------------------------------------------------------------
	 * Constructors
	 *-------------------------------------------------------------------------------------------*/

	/**
	 * Default constructor needed for XML encoding/decoding
	 */
	public ShadedTokenOverlay() {
		this(BooleanTokenOverlay.DEFAULT_STATE_NAME, Color.RED);
	}

	/**
	 * Create the new token overlay
	 * 
	 * @param aName Name of the new overlay.
	 * @param aColor The color that is painted over the token. If the
	 * alpha is 100%, it will be reduced to 25%.
	 */
	public ShadedTokenOverlay(String aName, Color aColor) {
		super(aName);
		assert aColor != null : "A color is required but null was passed.";
		color = aColor;
		setOpacity(25);
	}

	/*---------------------------------------------------------------------------------------------
	 * TokenOverlay Abstract Method Implementations
	 *-------------------------------------------------------------------------------------------*/

	/**
	 * @see net.rptools.maptool.client.ui.token.BooleanTokenOverlay#paintOverlay(java.awt.Graphics2D, net.rptools.maptool.model.Token, Rectangle)
	 */
	@Override
	public void paintOverlay(Graphics2D g, Token aToken, Rectangle bounds) {
		Color temp = g.getColor();
		g.setColor(color);
		Composite tempComposite = g.getComposite();
		if (getOpacity() != 100)
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) getOpacity() / 100));
		g.fill(bounds);
		g.setColor(temp);
		g.setComposite(tempComposite);
	}

	/**
	 * @see net.rptools.maptool.client.ui.token.BooleanTokenOverlay#clone()
	 */
	@Override
	public Object clone() {
		BooleanTokenOverlay overlay = new ShadedTokenOverlay(getName(), getColor());
		overlay.setOrder(getOrder());
		overlay.setGroup(getGroup());
		overlay.setMouseover(isMouseover());
		overlay.setOpacity(getOpacity());
		overlay.setShowGM(isShowGM());
		overlay.setShowOwner(isShowOwner());
		overlay.setShowOthers(isShowOthers());
		return overlay;
	}

	/**
	 * Get the color for this ShadedTokenOverlay.
	 *
	 * @return Returns the current value of color.
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Set the value of color for this ShadedTokenOverlay.
	 *
	 * @param aColor The color to set.
	 */
	public void setColor(Color aColor) {
		color = aColor;
	}
}
