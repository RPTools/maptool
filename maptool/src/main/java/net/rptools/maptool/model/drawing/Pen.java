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

import java.awt.Color;

/**
 * The color and thickness to draw a {@link Drawable}with. Also used to erase
 * by drawing {@link Drawable}s with a Pen whose {@link #setEraser}is true.
 */
public class Pen {
	public static final int MODE_SOLID = 0;
	public static final int MODE_TRANSPARENT = 1;

	public static final Pen DEFAULT = new Pen(new DrawableColorPaint(Color.black), 3.0f);

	private int foregroundMode = MODE_SOLID;
	private DrawablePaint paint;

	private int backgroundMode = MODE_SOLID;
	private DrawablePaint backgroundPaint;

	private float thickness;
	private boolean eraser;
	private float opacity = 1;

	// ***** Legacy support, these supports drawables from 1.1
	private int color;
	private int backgroundColor;

	public Pen() {
	}

	public Pen(DrawablePaint paint, float thickness) {
		this(paint, thickness, false);
	}

	public Pen(DrawablePaint paint, float thickness, boolean eraser) {
		this.paint = paint;
		this.thickness = thickness;
		this.eraser = eraser;
	}

	public Pen(Pen copy) {
		this.paint = copy.paint;
		this.foregroundMode = copy.foregroundMode;
		this.backgroundPaint = copy.backgroundPaint;
		this.backgroundMode = copy.backgroundMode;
		this.thickness = copy.thickness;
		this.eraser = copy.eraser;
		this.opacity = copy.opacity;
	}

	public DrawablePaint getPaint() {
		return paint;
	}

	public void setPaint(DrawablePaint paint) {
		this.paint = paint;
	}

	public DrawablePaint getBackgroundPaint() {
		return backgroundPaint;
	}

	public void setBackgroundPaint(DrawablePaint paint) {
		this.backgroundPaint = paint;
	}

	public boolean isEraser() {
		return eraser;
	}

	public void setEraser(boolean eraser) {
		this.eraser = eraser;
	}

	public float getThickness() {
		return thickness;
	}

	public void setThickness(float thickness) {
		this.thickness = thickness;
	}

	public int getBackgroundMode() {
		return backgroundMode;
	}

	public void setBackgroundMode(int backgroundMode) {
		this.backgroundMode = backgroundMode;
	}

	public int getForegroundMode() {
		return foregroundMode;
	}

	public void setForegroundMode(int foregroundMode) {
		this.foregroundMode = foregroundMode;
	}

	public float getOpacity() {
		return opacity;
	}

	public void setOpacity(float opacity) {
		this.opacity = opacity;
	}

	// ***** Legacy support, these supports drawables from 1.1
	// Note the lack of mutators
	public int getColor() {
		return color;
	}

	public int getBackgroundColor() {
		return backgroundColor;
	}
}
