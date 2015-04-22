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
	/**
	*  An attempt at an isometric style map grid where each cell is a diamond
	*  with the sides angled at 30 degrees.  Each cell is twice as wide as high
	*
	*  Grid size is used for cell height.  Therefore size 50 is 100 wide.
	*
	**/
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
		double tile_width_half = getSize();
		double tile_height_half = getSize()/2;
		double isoX = ((zp.x - getOffsetX()) / tile_width_half + (zp.y - getOffsetY()) / tile_height_half) /2;
		double isoY = ((zp.y - getOffsetY()) / tile_height_half -((zp.x - getOffsetX()) / tile_width_half)) /2;
		
		double calcX = (zp.x - getOffsetX()) / (float) getSize();
		double calcY = (zp.y - getOffsetY()) / (float) getSize();

		boolean exactCalcX = (zp.x - getOffsetX()) % getSize() == 0;
		boolean exactCalcY = (zp.y - getOffsetY()) % getSize() == 0;

		int newX = (int) (zp.x < 0 && !exactCalcX ? calcX - 1 : calcX);
		int newY = (int) (zp.y < 0 && !exactCalcY ? calcY - 1 : calcY);

		//System.out.format("%d / %d => %f, %f => %d, %d\n", zp.x, getSize(), calcX, calcY, newX, newY);
		return new CellPoint(newX, newY);
	}

	@Override
	public ZonePoint convert(CellPoint cp) {
		double tile_width_half = getSize();
		double tile_height_half = getSize()/2;
		double mapX = (cp.x - cp.y) * tile_width_half;
		double mapY = (cp.x + cp.y) * tile_height_half;
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
	    tx.rotate(0.5);
	    Shape diamond = tx.createTransformedShape(r);
		return new Area(diamond);
	}

	@Override
	public Rectangle getBounds(CellPoint cp) {
		return new Rectangle(cp.x * getSize(), cp.y * getSize(), getSize() * 2, getSize());
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
			FACING_ANGLES = new int[] { -135, -45, 45, 135 };
		} else {
			FACING_ANGLES = new int[] { 90 };
		}
	}
	
	@Override
	public void draw(ZoneRenderer renderer, Graphics2D g, Rectangle bounds) {
		double scale = renderer.getScale();
		double gridSize = getSize() * scale;

		g.setColor(new Color(getZone().getGridColor()));

		int offX = (int) (renderer.getViewOffsetX() % gridSize + getOffsetX() * scale);
		int offY = (int) (renderer.getViewOffsetY() % gridSize + getOffsetY() * scale);

		int startCol = (int) ((int) (bounds.x / gridSize) * gridSize);
		int startRow = (int) ((int) (bounds.y / gridSize) * gridSize);

		for (double row = startRow; row < bounds.y + bounds.height + gridSize; row += gridSize) {
			//g.drawOval(bounds.x, (int) (row + offY), AppState.getGridSize(), AppState.getGridSize());
			for (double col = startCol; col < bounds.x + bounds.width + gridSize; col += gridSize) {
				//g.drawOval((int) (col + offX), bounds.y, AppState.getGridSize(), AppState.getGridSize());
				//Ellipse2D.Double dot = new Ellipse2D.Double((col + offX), (row + offY), AppState.getGridSize(), AppState.getGridSize());
				//g.drawOval((int) (col + offX), (int) (row + offY), AppState.getGridSize(), AppState.getGridSize());
				g.fillOval((int) (col + offX), (int) (row + offY), AppState.getGridSize(), AppState.getGridSize());
			}
		}
	}
}
