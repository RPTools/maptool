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

package net.rptools.maptool.model;

public abstract class AbstractPoint implements Cloneable {

	public int x;
	public int y;

	public AbstractPoint(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void translate(int dx, int dy) {
		x += dx;
		y += dy;
	}

	public boolean equals(Object o) {
		if (!(o instanceof AbstractPoint))
			return false;
		AbstractPoint p = (AbstractPoint) o;

		return p.x == x && p.y == y;
	}

	public int hashCode() {
		return new String(x + "-" + y).hashCode();
	}

	public String toString() {
		return "[" + x + "," + y + "]";
	}

	public AbstractPoint clone() {
		try {
			return (AbstractPoint) super.clone();
		} catch (CloneNotSupportedException e) {
			// this shouldn't happen, since we are Cloneable
			throw new InternalError();
		}
	}
}
