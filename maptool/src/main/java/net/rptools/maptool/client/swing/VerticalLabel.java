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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.JLabel;

public class VerticalLabel extends JLabel {
	public final static int ROTATE_RIGHT = 1;

	public final static int DONT_ROTATE = 0;

	public final static int ROTATE_LEFT = -1;

	private int rotation = DONT_ROTATE;

	private boolean painting = false;

	public VerticalLabel() {
		super();
	}

	public VerticalLabel(Icon image, int horizontalAlignment) {
		super(image, horizontalAlignment);
	}

	public VerticalLabel(Icon image) {
		super(image);
	}

	public VerticalLabel(String text, Icon icon, int horizontalAlignment) {
		super(text, icon, horizontalAlignment);
	}

	public VerticalLabel(String text, int horizontalAlignment) {
		super(text, horizontalAlignment);
	}

	public VerticalLabel(String text) {
		super(text);
	}

	public int getRotation() {
		return rotation;
	}

	public void setRotation(int rotation) {
		this.rotation = rotation;
	}

	public boolean isRotated() {
		return rotation != DONT_ROTATE;
	}

	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;

		if (isRotated())
			g2d.rotate(Math.toRadians(90 * rotation));
		if (rotation == ROTATE_RIGHT)
			g2d.translate(0, -this.getWidth());
		else if (rotation == ROTATE_LEFT)
			g2d.translate(-this.getHeight(), 0);
		painting = true;

		super.paintComponent(g2d);

		painting = false;
		if (isRotated())
			g2d.rotate(-Math.toRadians(90 * rotation));
		if (rotation == ROTATE_RIGHT)
			g2d.translate(-this.getWidth(), 0);
		else if (rotation == ROTATE_LEFT)
			g2d.translate(0, -this.getHeight());
	}

	public Insets getInsets(Insets insets) {
		insets = super.getInsets(insets);
		if (painting) {
			if (rotation == ROTATE_LEFT) {
				int temp = insets.bottom;
				insets.bottom = insets.left;
				insets.left = insets.top;
				insets.top = insets.right;
				insets.right = temp;
			} else if (rotation == ROTATE_RIGHT) {
				int temp = insets.bottom;
				insets.bottom = insets.right;
				insets.right = insets.top;
				insets.top = insets.left;
				insets.left = temp;
			}
		}
		return insets;
	}

	public Insets getInsets() {
		Insets insets = super.getInsets();
		if (painting) {
			if (rotation == ROTATE_LEFT) {
				int temp = insets.bottom;
				insets.bottom = insets.left;
				insets.left = insets.top;
				insets.top = insets.right;
				insets.right = temp;
			} else if (rotation == ROTATE_RIGHT) {
				int temp = insets.bottom;
				insets.bottom = insets.right;
				insets.right = insets.top;
				insets.top = insets.left;
				insets.left = temp;
			}
		}
		return insets;
	}

	public int getWidth() {
		if ((painting) && (isRotated()))
			return super.getHeight();
		return super.getWidth();
	}

	public int getHeight() {
		if ((painting) && (isRotated()))
			return super.getWidth();
		return super.getHeight();
	}

	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		if (isRotated()) {
			int width = d.width;
			d.width = d.height;
			d.height = width;
		}
		return d;
	}

	public Dimension getMinimumSize() {
		Dimension d = super.getMinimumSize();
		if (isRotated()) {
			int width = d.width;
			d.width = d.height;
			d.height = width;
		}
		return d;
	}

	public Dimension getMaximumSize() {
		Dimension d = super.getMaximumSize();
		if (isRotated()) {
			int width = d.width;
			d.width = d.height + 10;
			d.height = width + 10;
		}
		return d;
	}

}
