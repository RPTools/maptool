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

import java.awt.Point;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

public class Scale implements Serializable {

	private final double oneToOneScale = 1; // Let this be configurable at some point
	private double scale = oneToOneScale;
	private final double scaleIncrement = .075;

	private int zoomLevel = 0;

	public static final String PROPERTY_SCALE = "scale";
	public static final String PROPERTY_OFFSET = "offset";

	private transient PropertyChangeSupport propertyChangeSupport;

	private int offsetX;
	private int offsetY;

	private final int width;
	private final int height;

	private boolean initialized;

	// LEGACY for 1.3b31 and earlier
	transient private int scaleIndex; // 'transient' prevents serialization; prep for 1.4

	public Scale() {
		this(0, 0);
	}

	public Scale(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public Scale(Scale copy) {
		this.width = copy.width;
		this.height = copy.height;
		this.offsetX = copy.offsetX;
		this.offsetY = copy.offsetY;
		this.zoomLevel = copy.zoomLevel;
		this.initialized = copy.initialized;
		this.scale = copy.scale;
		//     this.oneToOneScale = copy.oneToOneScale;
		//     this.scaleIncrement = copy.scaleIncrement;
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		getPropertyChangeSupport().addPropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
		getPropertyChangeSupport().addPropertyChangeListener(property, listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		getPropertyChangeSupport().removePropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(String property, PropertyChangeListener listener) {
		getPropertyChangeSupport().removePropertyChangeListener(property, listener);
	}

	public int getOffsetX() {
		return offsetX;
	}

	public int getOffsetY() {
		return offsetY;
	}

	public void setOffset(int x, int y) {

		int oldX = offsetX;
		int oldY = offsetY;

		offsetX = x;
		offsetY = y;

		getPropertyChangeSupport().firePropertyChange(PROPERTY_OFFSET, new Point(oldX, oldY), new Point(offsetX, offsetY));
	}

	public double getScale() {
		return scale;
	}

	public void setScale(double scale) {
		if (scale <= 0.0) {
			return;
		}

		// Determine zoomLevel appropriate for given scale
		zoomLevel = (int) Math.round(Math.log(scale / oneToOneScale) / Math.log(1 + scaleIncrement));

		setScaleNoZoomLevel(scale);
	}

	private void setScaleNoZoomLevel(double scale) {
		double oldScale = this.scale;
		this.scale = scale;

		getPropertyChangeSupport().firePropertyChange(PROPERTY_SCALE, oldScale, scale);
	}

	public double getOneToOneScale() {
		return oneToOneScale;
	}

	public double reset() {
		double oldScale = this.scale;
		scale = oneToOneScale;
		zoomLevel = 0;

		getPropertyChangeSupport().firePropertyChange(PROPERTY_SCALE, oldScale, scale);
		return oldScale;
	}

	public double scaleUp() {
		zoomLevel++;
		setScaleNoZoomLevel(oneToOneScale * Math.pow(1 + scaleIncrement, zoomLevel));
		return scale;
	}

	public double scaleDown() {
		zoomLevel--;
		setScaleNoZoomLevel(oneToOneScale * Math.pow(1 + scaleIncrement, zoomLevel));
		return scale;
	}

	public void zoomReset(int x, int y) {
		zoomTo(x, y, reset());
	}

	public void zoomIn(int x, int y) {
		double oldScale = scale;
		scaleUp();
		zoomTo(x, y, oldScale);
	}

	public void zoomOut(int x, int y) {
		double oldScale = scale;
		scaleDown();
		zoomTo(x, y, oldScale);
	}

	public void zoomScale(int x, int y, double scale) {
		double oldScale = this.scale;
		setScale(scale);
		zoomTo(x, y, oldScale);
	}

	public boolean isInitialized() {
		return initialized;
	}

	private PropertyChangeSupport getPropertyChangeSupport() {
		if (propertyChangeSupport == null) {
			propertyChangeSupport = new PropertyChangeSupport(this);
		}
		return propertyChangeSupport;
	}

	/**
	 * Fit the image into the given space by finding the zoom level
	 * that allows the image to fit.  Then center the image.
	 * @param width
	 * @param height
	 * @return true if this call did something, false if the init has already been called
	 */
	public boolean initialize(int width, int height) {

		if (initialized) {
			return false;
		}

		centerIn(width, height);

		initialized = true;
		return true;
	}

	public void centerIn(int width, int height) {

		int currWidth = (int) (this.width * getScale());
		int currHeight = (int) (this.height * getScale());

		int x = (width - currWidth) / 2;
		int y = (height - currHeight) / 2;

		setOffset(x, y);
	}

	private void zoomTo(int x, int y, double oldScale) {

		// Keep the current pixel centered
		x -= offsetX;
		y -= offsetY;

		// Rounding reduces drift in offset from repeated zooming
		int newX = (int) Math.round((x * scale) / oldScale);
		int newY = (int) Math.round((y * scale) / oldScale);

		offsetX = offsetX - (newX - x);
		offsetY = offsetY - (newY - y);
	}

}
