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

import java.awt.Color;

public class Label {
	private final GUID id;
	private String label;
	private int x, y;
	private boolean showBackground;
	private int foregroundColor;

	public Label() {
		this("");
	}

	public Label(String label) {
		this(label, 0, 0);
	}

	public Label(String label, int x, int y) {
		id = new GUID();
		this.label = label;
		this.x = x;
		this.y = y;
		showBackground = true;
	}

	public Label(Label label) {
		this(label.label, label.x, label.y);
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public GUID getId() {
		return id;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public boolean isShowBackground() {
		return showBackground;
	}

	public void setShowBackground(boolean showBackground) {
		this.showBackground = showBackground;
	}

	public Color getForegroundColor() {
		return new Color(foregroundColor);
	}

	public void setForegroundColor(Color foregroundColor) {
		this.foregroundColor = foregroundColor.getRGB();
	}
}
