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

import java.awt.Graphics2D;
import java.awt.Rectangle;

import net.rptools.maptool.client.functions.AbstractTokenAccessorFunction;
import net.rptools.maptool.model.Token;

/**
 * An overlay that may be applied to a token to show state.
 * 
 * @author jgorrell
 * @version $Revision: 5945 $ $Date: 2013-06-03 04:35:50 +0930 (Mon, 03 Jun 2013) $ $Author: azhrei_fje $
 */
public abstract class BooleanTokenOverlay extends AbstractTokenOverlay {

	/*---------------------------------------------------------------------------------------------
	 * Constructors
	 *-------------------------------------------------------------------------------------------*/

	/**
	 * Create an overlay with the passed name.
	 * 
	 * @param aName Name of the new overlay.
	 */
	protected BooleanTokenOverlay(String aName) {
		super(aName);
	}

	/*---------------------------------------------------------------------------------------------
	 * AbstractTokenOverlay Methods
	 *-------------------------------------------------------------------------------------------*/

	/**
	 * @see net.rptools.maptool.client.ui.token.AbstractTokenOverlay#paintOverlay(java.awt.Graphics2D, net.rptools.maptool.model.Token, java.awt.Rectangle, java.lang.Object)
	 */
	@Override
	public void paintOverlay(Graphics2D g, Token token, Rectangle bounds, Object value) {
		if (AbstractTokenAccessorFunction.getBooleanValue(value))
			paintOverlay(g, token, bounds);
	}

	/*---------------------------------------------------------------------------------------------
	 * Abstract Methods
	 *-------------------------------------------------------------------------------------------*/

	/**
	 * Paint the overlay for the passed token.
	 * 
	 * @param g Graphics used to paint. It is already translated so that 0,0 is
	 * the upper left corner of the token. It is also clipped so that the overlay can not
	 * draw out of the token's bounding box.
	 * @param token The token being painted.
	 * @param bounds The bounds of the actual token. This will be different than the clip
	 * since the clip also has to take into account the edge of the window. If you draw 
	 * based on the clip it will be off for partial token painting.
	 */
	public abstract void paintOverlay(Graphics2D g, Token token, Rectangle bounds);
}
