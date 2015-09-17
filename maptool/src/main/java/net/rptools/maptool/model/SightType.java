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

import java.awt.geom.Area;

public class SightType {
	private String name;
	private double multiplier;
	private LightSource personalLightSource;
	private ShapeType shape;
	private int arc = 0;
	private float distance = 0;
	private int offset = 0;

	public int getOffset() {
		return this.offset;
	}

	public void setOffset(int offset2) {
		this.offset = offset2;
	}

	public float getDistance() {
		return this.distance;
	}

	public void setDistance(float range) {
		this.distance = range;
	}

	public ShapeType getShape() {
		return shape != null ? shape : ShapeType.CIRCLE;
	}

	public void setShape(ShapeType shape) {
		this.shape = shape;
	}

	public SightType() {
		// For serialization
	}

	public SightType(String name, double multiplier, LightSource personalLightSource) {
		this(name, multiplier, personalLightSource, ShapeType.CIRCLE);
	}

	public SightType(String name, double multiplier, LightSource personalLightSource, ShapeType shape) {
		this.name = name;
		this.multiplier = multiplier;
		this.personalLightSource = personalLightSource;
		this.shape = shape;
	}

	public SightType(String name, double multiplier, LightSource personalLightSource, ShapeType shape, int arc) {
		this.name = name;
		this.multiplier = multiplier;
		this.personalLightSource = personalLightSource;
		this.shape = shape;
		this.arc = arc;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getMultiplier() {
		return multiplier;
	}

	public void setMultiplier(double multiplier) {
		this.multiplier = multiplier;
	}

	public boolean hasPersonalLightSource() {
		return personalLightSource != null;
	}

	public LightSource getPersonalLightSource() {
		return personalLightSource;
	}

	public void setPersonalLightSource(LightSource personalLightSource) {
		this.personalLightSource = personalLightSource;
	}

	public void setArc(int arc) {
		this.arc = arc;
	}

	public int getArc() {
		return arc;
	}

	public Area getVisionShape(Token token, Zone zone) {
		return zone.getGrid().getShapedArea(getShape(), token, getDistance(), getArc(), getOffset());
	}
}
