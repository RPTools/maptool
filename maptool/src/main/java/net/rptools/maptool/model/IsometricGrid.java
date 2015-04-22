package net.rptools.maptool.model;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.KeyStroke;

import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.AppState;
import net.rptools.maptool.client.tool.PointerTool;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;

public class IsometricGrid extends Grid {
	private static final int[] ALL_ANGLES = new int[] { -135, -90, -45, 0, 45, 90, 135, 180 };
	private static int[] FACING_ANGLES;
	
	public IsometricGrid() {
		super();
		if (FACING_ANGLES == null) {
			boolean faceEdges = AppPreferences.getFaceEdge();
			boolean faceVertices = AppPreferences.getFaceVertex();
			setFacings(faceEdges, faceVertices);
		}
	}
	
	private static final GridCapabilities GRID_CAPABILITIES = new GridCapabilities() {
		public boolean isPathingSupported() {
			return true;
		}

		public boolean isSnapToGridSupported() {
			return true;
		}

		public boolean isPathLineSupported() {
			return true;
		}

		public boolean isSecondDimensionAdjustmentSupported() {
			return false;
		}

		public boolean isCoordinatesSupported() {
			return true;
		}
	};
	
	public IsometricGrid(boolean faceEdges, boolean faceVertices) {
		setFacings(faceEdges, faceVertices);
	}

	@Override
	public List<TokenFootprint> getFootprints() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CellPoint convert(ZonePoint zp) {
		double calcX = (zp.x - getOffsetX()) / (float) getSize();
		double calcY = (zp.y - getOffsetY()) / (float) getSize();

		boolean exactCalcX = (zp.x - getOffsetX()) % getSize() == 0;
		boolean exactCalcY = (zp.y - getOffsetY()) % getSize() == 0;

		int newX = (int) (zp.x < 0 && !exactCalcX ? calcX - 1 : calcX);
		int newY = (int) (zp.y < 0 && !exactCalcY ? calcY - 1 : calcY);

		System.out.format("%d - %d = %s\n", zp.x, zp.y, isoX);
		//System.out.format("%d / %d => %f, %f => %d, %d\n", zp.x, getSize(), calcX, calcY, newX, newY);
		return new CellPoint(newX, newY);
	}

	@Override
	public ZonePoint convert(CellPoint cp) {
		return new ZonePoint((cp.x * getSize() + getOffsetX()), (cp.y * getSize() + getOffsetY()));
	}

	@Override
	public GridCapabilities getCapabilities() {
		return GRID_CAPABILITIES;
	}

	@Override
	protected Area createCellShape(int size) {
		Rectangle r = new Rectangle(0, 0, size, size);
	    AffineTransform tx = new AffineTransform();
	    tx.rotate(0.25);
	    Shape diamond = tx.createTransformedShape(r);
		return new Area(diamond);
	}

	@Override
	public Rectangle getBounds(CellPoint cp) {
		return new Rectangle(cp.x * getSize(), cp.y * getSize(), getSize(), getSize());
	}

	@Override
	public void installMovementKeys(PointerTool callback,
			Map<KeyStroke, Action> actionMap) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void uninstallMovementKeys(Map<KeyStroke, Action> actionMap) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setFacings(boolean faceEdges, boolean faceVertices) {
		if (faceEdges && faceVertices) {
			FACING_ANGLES = ALL_ANGLES;
		} else if (!faceEdges && faceVertices) {
			FACING_ANGLES = new int[] { -90, 0, 90, 180 };
		} else if (faceEdges && !faceVertices) {
			FACING_ANGLES = new int[] { -120, -30, 30, 120 };
		} else {
			FACING_ANGLES = new int[] { 90 };
		}
	}
	
	@Override
	public void draw(ZoneRenderer renderer, Graphics2D g, Rectangle bounds) {
		double scale = renderer.getScale();
		double gridSize = getSize() * scale;
		double isoHeight = getSize() * scale;
		double isoWidth = getSize() * 2 * scale;

		g.setColor(new Color(getZone().getGridColor()));

		int offX = (int) (renderer.getViewOffsetX() % isoWidth + getOffsetX() * scale);
		int offY = (int) (renderer.getViewOffsetY() % gridSize + getOffsetY() * scale);

		int startCol = (int) ((int) (bounds.x / isoWidth) * isoWidth);
		int startRow = (int) ((int) (bounds.y / gridSize) * gridSize);

		for (double row = startRow; row < bounds.y + bounds.height + gridSize; row += gridSize) {
			for (double col = startCol; col < bounds.x + bounds.width + isoWidth; col += isoWidth) {
				g.fillOval((int) (col + offX - AppState.getGridSize()), (int) (row + offY - AppState.getGridSize()), AppState.getGridSize()*2, AppState.getGridSize()*2);
			}
		}

		for (double row = startRow-(isoHeight/2); row < bounds.y + bounds.height + gridSize; row += gridSize) {
			for (double col = startCol-(isoWidth/2); col < bounds.x + bounds.width + isoWidth; col += isoWidth) {
				g.fillOval((int) (col + offX - AppState.getGridSize()), (int) (row + offY - AppState.getGridSize()), AppState.getGridSize()*2, AppState.getGridSize()*2);
			}
		}
	}
}
