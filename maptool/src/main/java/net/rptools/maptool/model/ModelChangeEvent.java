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

public class ModelChangeEvent {
	public Object model;
	public Object eventType;
	public Object arg;

	public ModelChangeEvent(Object model, Object eventType) {
		this(model, eventType, null);
	}

	public ModelChangeEvent(Object model, Object eventType, Object arg) {
		this.model = model;
		this.eventType = eventType;
		this.arg = arg;
	}

	public Object getModel() {
		return model;
	}

	public Object getArg() {
		return arg;
	}

	public Object getEvent() {
		return eventType;
	}

	@Override
	public String toString() {
		return "ModelChangeEvent: " + model + " - " + eventType + " - " + arg;
	}
}
