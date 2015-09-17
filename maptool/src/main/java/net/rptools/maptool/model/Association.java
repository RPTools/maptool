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

public class Association<E, T> {

	private E lhs;
	private T rhs;

	public Association(E lhs, T rhs) {
		this.lhs = lhs;
		this.rhs = rhs;
	}

	public void setLeft(E value) {
		lhs = value;
	}

	public void setRight(T value) {
		rhs = value;
	}

	public E getLeft() {
		return lhs;
	}

	public T getRight() {
		return rhs;
	}
}
