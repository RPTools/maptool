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

package net.rptools.maptool.client.ui.zone;

import java.awt.geom.Area;

import net.rptools.maptool.model.LightSource;
import net.rptools.maptool.model.drawing.DrawablePaint;

public class DrawableLight {

	private DrawablePaint paint;
	private Area area;
	private LightSource.Type type;

	public DrawableLight(LightSource.Type type, DrawablePaint paint, Area area) {
		super();
		this.paint = paint;
		this.area = area;
		this.type = type;
	}

	public DrawablePaint getPaint() {
		return paint;
	}

	public Area getArea() {
		return area;
	}

	public LightSource.Type getType() {
		return type;
	}

	@Override
	public String toString() {
		return "DrawableLight[" + area.getBounds() + ", " + paint.getClass().getName() + "]";
	}

}
