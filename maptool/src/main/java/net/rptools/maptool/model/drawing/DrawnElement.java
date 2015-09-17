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

import java.io.Serializable;

/**
 */
public class DrawnElement {

	private Drawable drawable;
	private Pen pen;

	public DrawnElement(Drawable drawable, Pen pen) {
		this.drawable = drawable;
		this.pen = pen;
	}

	public Drawable getDrawable() {
		return drawable;
	}

	public Pen getPen() {
		return pen;
	}
}
