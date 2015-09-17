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

package net.rptools.maptool.client.ui;

import java.awt.Rectangle;

import net.rptools.maptool.model.Token;

public class TokenLocation {

	private Rectangle bounds;
	private Token token;

	public TokenLocation(Rectangle bounds, Token token) {
		this.bounds = bounds;
		this.token = token;
	}

	public Rectangle getBounds() {
		return bounds;
	}

	public Token getToken() {
		return token;
	}
}
