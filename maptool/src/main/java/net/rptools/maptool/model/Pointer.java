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

/**
 * Represents a player pointer on the screen
 */
public class Pointer {

	public enum Type {
		ARROW, SPEECH_BUBBLE, THOUGHT_BUBBLE
	}

	private GUID zoneGUID;
	private int x;
	private int y;
	private double direction; // 
	private String type;

	public Pointer() {
		/* Hessian serializable */}

	public Pointer(Zone zone, int x, int y, double direction, Type type) {
		this.zoneGUID = zone.getId();
		this.x = x;
		this.y = y;
		this.direction = direction;
		this.type = type.name();
	}

	public String toString() {
		return x + "." + y + "-" + direction;
	}

	public Type getType() {
		return type != null ? Type.valueOf(type) : Type.ARROW;
	}

	public GUID getZoneGUID() {
		return zoneGUID;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public double getDirection() {
		return direction;
	}
}
