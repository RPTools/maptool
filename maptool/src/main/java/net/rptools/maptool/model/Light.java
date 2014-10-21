/*
 *  This software copyright by various authors including the RPTools.net
 *  development team, and licensed under the LGPL Version 3 or, at your
 *  option, any later version.
 *
 *  Portions of this software were originally covered under the Apache
 *  Software License, Version 1.1 or Version 2.0.
 *
 *  See the file LICENSE elsewhere in this distribution for license details.
 */

package net.rptools.maptool.model;

import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import net.rptools.maptool.model.drawing.DrawablePaint;

public class Light {
	private DrawablePaint paint;
	private double facingOffset;
	private double radius;
	private double arcAngle;
	private ShapeType shape;
	private boolean isGM;
	private boolean ownerOnly;

	public Light() {
		// For serialization
	}

	public Light(ShapeType shape, double facingOffset, double radius, double arcAngle, DrawablePaint paint) {
		this.facingOffset = facingOffset;
		this.shape = shape;
		this.radius = radius;
		this.arcAngle = arcAngle;
		this.paint = paint;
		this.isGM = false;
		this.ownerOnly = false;

		if (arcAngle == 0) {
			this.arcAngle = 90;
		}
	}

	public Light(ShapeType shape, double facingOffset, double radius, double arcAngle, DrawablePaint paint, boolean isGM, boolean owner) {
		this.facingOffset = facingOffset;
		this.shape = shape;
		this.radius = radius;
		this.arcAngle = arcAngle;
		this.paint = paint;
		this.isGM = isGM;
		this.ownerOnly = owner;
		if (arcAngle == 0) {
			this.arcAngle = 90;
		}
	}

	public DrawablePaint getPaint() {
		return paint;
	}

	public void setPaint(DrawablePaint paint) {
		this.paint = paint;
	}

	public double getFacingOffset() {
		return facingOffset;
	}

	public void setFacingOffset(double facingOffset) {
		this.facingOffset = facingOffset;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public double getArcAngle() {
		return arcAngle;
	}

	public void setArcAngle(double arcAngle) {
		this.arcAngle = arcAngle;
	}

	public ShapeType getShape() {
		return shape;
	}

	public void setShape(ShapeType shape) {
		this.shape = shape;
	}

	public Area getArea(Token token, Zone zone) {
		double size = radius / zone.getUnitsPerCell() * zone.getGrid().getSize();
		if (shape == null) {
			shape = ShapeType.CIRCLE;
		}
		switch (shape) {
		case SQUARE:
			return new Area(new Rectangle2D.Double(-size, -size, size * 2, size * 2));

		case CONE:
			// Be sure we can always at least see our feet
//			Lee: decoupling from dependence on grid and just use token-centric values. 
//			Area footprint = new Area(token.getFootprint(zone.getGrid()).getBounds(zone.getGrid()));
			double magnitude = (2.5 * token.getFootprint(zone.getGrid()).getScale()) / zone.getUnitsPerCell() * zone.getGrid().getSize();
			Area footprint = new Area(new Ellipse2D.Double(-magnitude, -magnitude, magnitude * 2, magnitude * 2));
//			footprint.transform(AffineTransform.getTranslateInstance(-footprint.getBounds().getWidth() / 2.0, -footprint.getBounds().getHeight() / 2.0));

			Area area = new Area(new Arc2D.Double(-size, -size, size * 2, size * 2, 360.0 - (arcAngle / 2.0), arcAngle, Arc2D.PIE));
			if (token.getFacing() != null) {
				area = area.createTransformedArea(AffineTransform.getRotateInstance(-Math.toRadians(token.getFacing())));
			}
			area.add(footprint);
			return area;

		default:
		case CIRCLE:
			return new Area(new Ellipse2D.Double(-size, -size, size * 2, size * 2));
		}
	}

	public void setGM(boolean b) {
		isGM = b;
	}

	public boolean isGM() {
		return isGM;
	}

	public boolean isOwnerOnly() {
		return ownerOnly;
	}

	public void setOwnerOnly(boolean owner) {
		ownerOnly = owner;
	}
}
