package net.rptools.maptool.model;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

public class IsometricHexGrid extends HexGridVertical {
	/**
	 * This class is vastly incomplete so currently disabled
	 */
	
	// This constructor is necessary for deserialisation
	public IsometricHexGrid() {
		super();
	}
	
	public IsometricHexGrid(boolean faceEdges, boolean faceVertices) {
		super(faceEdges, faceVertices);
	}

	@Override
	public void setSize(int size) {
		super.setSize(size);

		setMinorRadius( (double) size / 2 );
		setEdgeLength( (double) size );
		setEdgeProjection( (double) size / 2 );
	}

	@Override
	public boolean isIsometric() {
		return true;
	}

	@Override
	public Area getShapedArea(ShapeType shape, Token token, double range, double arcAngle, int offsetAngle) {
		if (shape == null) {
			shape = ShapeType.CIRCLE;
		}
		int visionDistance = getZone().getTokenVisionInPixels();
		double visionRange = (range == 0) ? visionDistance : range * getSize() / getZone().getUnitsPerCell();
		Area visibleArea = new Area();
		switch (shape) {
		case CIRCLE:
			visionRange = (float) Math.sin(Math.toRadians(45)) * visionRange;
			visibleArea = new Area(new Ellipse2D.Double(-visionRange * 2 * 0.707, -visionRange, visionRange * 4 * 0.707, visionRange * 2));
			break;
		case SQUARE:
			visibleArea = new Area(new Rectangle2D.Double(-visionRange * 2 * 0.707, -visionRange, visionRange * 4 * 0.707, visionRange * 2));
			break;
		case CONE:
			if (token.getFacing() == null) {
				token.setFacing(0);
			}
			// Rotate the vision range by 45 degrees for isometric view
			visionRange = (float) Math.sin(Math.toRadians(45)) * visionRange;
			// Get the cone, use degreesFromIso to convert the facing from isometric to plan 
			Area tempvisibleArea = new Area(new Arc2D.Double(-visionRange * 2, -visionRange, visionRange * 4, visionRange * 2, token.getFacing() - (arcAngle / 2.0)
					+ (offsetAngle * 1.0), arcAngle, Arc2D.PIE));
			// Get the cell footprint
			Rectangle footprint = token.getFootprint(getZone().getGrid()).getBounds(getZone().getGrid());
			footprint.x = -footprint.width / 2;
			footprint.y = -footprint.height / 2;
			// convert the cell footprint to an area
			Area cellShape = getZone().getGrid().createCellShape(footprint.height);
			// convert the area to isometric view
			AffineTransform mtx = new AffineTransform();
			mtx.translate(-footprint.width / 2, -footprint.height / 2);
			cellShape.transform(mtx);
			// join cell footprint and cone to create viewable area
			visibleArea.add(cellShape);
			visibleArea.add(tempvisibleArea);
			break;
		default:
			visibleArea = new Area(new Ellipse2D.Double(-visionRange, -visionRange, visionRange * 2, visionRange * 2));
			break;
		}
		return visibleArea;
	}

}
