package net.rptools.maptool.model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.KeyStroke;

import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.AppState;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.tool.PointerTool;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.client.walker.WalkerMetric;
import net.rptools.maptool.client.walker.ZoneWalker;
import net.rptools.maptool.client.walker.astar.AStarSquareEuclideanWalker;

public class IsometricGrid extends Grid {
	/**
	*  An attempt at an isometric style map grid where each cell is a diamond
	*  with the sides angled at 30 degrees.  Each cell is twice as wide as high
	*
	*  Grid size is used for cell height.  Therefore size 50 is 100 wide.
	*
	**/
	private static final int[] ALL_ANGLES = new int[] { -120, -90, -30, 0, 30, 90, 120, 180 };
	private static int[] FACING_ANGLES;
	private static List<TokenFootprint> footprintList;
	
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
		if (footprintList == null) {
			try {
				footprintList = loadFootprints("net/rptools/maptool/model/squareGridFootprints.xml");
			} catch (IOException ioe) {
				MapTool.showError("SquareGrid.error.squareGridNotLoaded", ioe);
			}
		}
		return footprintList;
	}

	@Override
	public CellPoint convert(ZonePoint zp) {
		double tile_width_half = getSize();
		double tile_height_half = getSize()/2;
		double isoX = ((zp.x - getOffsetX()) / tile_width_half + (zp.y - getOffsetY()) / tile_height_half) /2;
		double isoY = ((zp.y - getOffsetY()) / tile_height_half -((zp.x - getOffsetX()) / tile_width_half)) /2;
		int newX = (int) isoX;
		int newY = (int) isoY;
		return new CellPoint(newX, newY);
	}

	@Override
	public ZonePoint convert(CellPoint cp) {
		double tile_width_half = getSize();
		double tile_height_half = getSize()/2;
		double mapX = (cp.x - cp.y) * tile_width_half;
		double mapY = (cp.x + cp.y) * tile_height_half;
		return new ZonePoint((int)(mapX), (int)(mapY));
	}

	@Override
	public Rectangle getBounds(CellPoint cp) {
		ZonePoint zp = convert(cp);
		return new Rectangle(zp.x-getSize(), zp.y, getSize()*2, getSize());
	}

	@Override
	public ZoneWalker createZoneWalker() {
		WalkerMetric metric = MapTool.isPersonalServer() ? AppPreferences.getMovementMetric() : MapTool.getServerPolicy().getMovementMetric();
		return new AStarSquareEuclideanWalker(getZone(), metric);
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
				drawHatch(renderer, g, (int) (col + offX), (int) (row + offY));
			}
		}

		for (double row = startRow-(isoHeight/2); row < bounds.y + bounds.height + gridSize; row += gridSize) {
			for (double col = startCol-(isoWidth/2); col < bounds.x + bounds.width + isoWidth; col += isoWidth) {
				drawHatch(renderer, g, (int) (col + offX), (int) (row + offY));
			}
		}
	}
	
	private void drawHatch(ZoneRenderer renderer, Graphics2D g, int x, int y) {
		double isoWidth = getSize() * renderer.getScale();
		int hatchSize = isoWidth>10?(int)isoWidth/8:2;
		g.setStroke(new BasicStroke(AppState.getGridSize()));
		g.drawLine(x-(hatchSize*2), y-hatchSize, x+(hatchSize*2), y+hatchSize);
		g.drawLine(x-(hatchSize*2), y+hatchSize, x+(hatchSize*2), y-hatchSize);
	}
}
