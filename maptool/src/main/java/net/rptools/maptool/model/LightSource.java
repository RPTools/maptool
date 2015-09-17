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

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.rptools.lib.FileUtil;

import org.apache.commons.lang.math.NumberUtils;

public class LightSource implements Comparable<LightSource> {
	public enum Type {
		NORMAL, AURA
	}

	private List<Light> lightList;
	private String name;
	private GUID id;
	private Type type;
	private ShapeType shapeType;

	public LightSource() {
		// for serialization
	}

	public LightSource(String name) {
		id = new GUID();
		this.name = name;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof LightSource)) {
			return false;
		}
		return ((LightSource) obj).id.equals(id);
	}

	public double getMaxRange() {
		double range = 0;
		for (Light light : getLightList()) {
			range = Math.max(range, light.getRadius());
		}
		return range;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	public void setId(GUID id) {
		this.id = id;
	}

	public GUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void add(Light source) {
		getLightList().add(source);
	}

	public void remove(Light source) {
		getLightList().remove(source);
	}

	public List<Light> getLightList() {
		if (lightList == null) {
			lightList = new LinkedList<Light>();
		}
		return lightList;
	}

	public Type getType() {
		return type != null ? type : Type.NORMAL;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public void setShapeType(ShapeType type) {
		this.shapeType = type;
	}

	public ShapeType getShapeType() {
		return shapeType != null ? shapeType : ShapeType.CIRCLE;
	}

	/**
	 * Area for a single light, subtracting any previous lights
	 */
	public Area getArea(Token token, Zone zone, Direction position, Light light) {
		Area area = light.getArea(token, zone);
		// TODO: This seems horribly inefficient
		// Subtract out the lights that are previously defined
		for (int i = getLightList().indexOf(light) - 1; i >= 0; i--) {
			Light lessLight = getLightList().get(i);
			area.subtract(getArea(token, zone, position, lessLight.getArea(token, zone)));
		}
		return getArea(token, zone, position, area);
	}

	/**
	 * Area for all lights combined
	 */
	public Area getArea(Token token, Zone zone, Direction position) {
		Area area = new Area();
		for (Light light : getLightList()) {
			area.add(light.getArea(token, zone));
		}
		return getArea(token, zone, position, area);
	}

	private Area getArea(Token token, Zone zone, Direction position, Area area) {
		Grid grid = zone.getGrid();
		Rectangle footprintBounds = token.getFootprint(grid).getBounds(grid, grid.convert(new ZonePoint(token.getX(), token.getY())));

		int tx = 0;
		int ty = 0;
		switch (position) {
		case NW:
			tx -= footprintBounds.width / 2;
			ty -= footprintBounds.height / 2;
			break;
		case N:
			ty -= footprintBounds.height / 2;
			break;
		case NE:
			tx += footprintBounds.width / 2;
			ty -= footprintBounds.height / 2;
			break;
		case W:
			tx -= footprintBounds.width / 2;
			break;
		case CENTER:
			break;
		case E:
			tx += footprintBounds.width / 2;
			break;
		case SW:
			tx -= footprintBounds.width / 2;
			ty += footprintBounds.height / 2;
			break;
		case S:
			ty += footprintBounds.height / 2;
			break;
		case SE:
			tx += footprintBounds.width / 2;
			ty += footprintBounds.height / 2;
			break;
		}
		area.transform(AffineTransform.getTranslateInstance(tx, ty));
		return area;
	}

	public void render(Graphics2D g, Token token, Grid grid) {
	}

	@SuppressWarnings("unchecked")
	public static Map<String, List<LightSource>> getDefaultLightSources() throws IOException {
		Object defaultLights = FileUtil.objFromResource("net/rptools/maptool/model/defaultLightSourcesMap.xml");
		return (Map<String, List<LightSource>>) defaultLights;
	}

	@Override
	public String toString() {
		return name;
	}

	private Object readResolve() {
		if (type == null) {
			type = Type.NORMAL;
		}
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(LightSource o) {
		if (o != this) {
			Integer nameLong = NumberUtils.toInt(name, Integer.MIN_VALUE);
			Integer onameLong = NumberUtils.toInt(o.name, Integer.MIN_VALUE);
			if (nameLong != Integer.MIN_VALUE && onameLong != Integer.MIN_VALUE)
				return nameLong - onameLong;
			return name.compareTo(o.name);
		}
		return 0;
	}
}
