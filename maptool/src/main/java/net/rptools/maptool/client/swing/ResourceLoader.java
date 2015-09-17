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

package net.rptools.maptool.client.swing;

import java.awt.Rectangle;
import java.util.StringTokenizer;

// This should really be in rplib
public class ResourceLoader {

	/**
	 * Rectangles are in the form x, y, width, height
	 */
	public static Rectangle loadRectangle(String rectString) {

		StringTokenizer strtok = new StringTokenizer(rectString, ",");
		if (strtok.countTokens() != 4) {
			throw new IllegalArgumentException("Could not load rectangle: '" + rectString + "', must be in the form x, y, w, h");
		}

		int x = Integer.parseInt(strtok.nextToken().trim());
		int y = Integer.parseInt(strtok.nextToken().trim());
		int w = Integer.parseInt(strtok.nextToken().trim());
		int h = Integer.parseInt(strtok.nextToken().trim());

		return new Rectangle(x, y, w, h);
	}
}
