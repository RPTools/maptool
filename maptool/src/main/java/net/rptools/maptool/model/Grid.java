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

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.KeyStroke;

import net.rptools.lib.FileUtil;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.tool.PointerTool;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.client.walker.ZoneWalker;
import net.rptools.maptool.model.TokenFootprint.OffsetTranslator;
import net.rptools.maptool.model.Zone.Event;

import org.apache.log4j.Logger;

/**
 * Base class for grids.
 * 
 * @author trevor
 */
public abstract class Grid implements Cloneable {
	private static final Logger log = Logger.getLogger(Grid.class);
	/**
	 * The minimum grid size (minimum on any dimension). The default value is 9 because the algorithm for determining
	 * whether a given square cell can be entered due to fog blocking the cell is based on the cell being split into
	 * 3x3, then the center further being split into 3x3; thus at least 9 pixels horizontally and vertically are
	 * required.
	 */
	public static final int MIN_GRID_SIZE = 9;
	public static final int MAX_GRID_SIZE = 350;

	private static final Dimension NO_DIM = new Dimension();

	private int offsetX = 0;
	private int offsetY = 0;
	private int size;

	private Zone zone;
	private Area cellShape;

	protected Map<KeyStroke, Action> movementKeys = null;

	public Grid() {
		setSize(AppPreferences.getDefaultGridSize());
	}

	public Grid(Grid grid) {
		setSize(grid.getSize());
		setOffset(grid.offsetX, grid.offsetY);
	}

	public void drawCoordinatesOverlay(Graphics2D g, ZoneRenderer renderer) {
		// Do nothing -- my default
	}

	/**
	 * Set the facing options for tokens/objects on a grid. Each grid type can providing facings to the edges, the
	 * vertices, both, or neither.
	 * 
	 * If both are false, tokens on that grid will not be able to rotate with the mouse and keyboard controls for
	 * setting facing.
	 * 
	 * @param faceEdges
	 *            - Tokens can face edges.
	 * @param faceVertices
	 *            - Tokens can face vertices.
	 */
	public void setFacings(boolean faceEdges, boolean faceVertices) {
		// Handle it in the individual grid types
	}

	public int[] getFacingAngles() {
		return null;
	}

	protected List<TokenFootprint> loadFootprints(String path, OffsetTranslator... translators) throws IOException {
		Object obj = FileUtil.objFromResource(path);
		@SuppressWarnings("unchecked")
		List<TokenFootprint> footprintList = (List<TokenFootprint>) obj;
		for (TokenFootprint footprint : footprintList) {
			for (OffsetTranslator ot : translators) {
				footprint.addOffsetTranslator(ot);
			}
		}
		return footprintList;
	}

	public TokenFootprint getDefaultFootprint() {
		for (TokenFootprint footprint : getFootprints()) {
			if (footprint.isDefault()) {
				return footprint;
			}
		}
		// None specified, use the first
		return getFootprints().get(0);
	}

	public TokenFootprint getFootprint(GUID guid) {
		if (guid == null) {
			return getDefaultFootprint();
		}
		for (TokenFootprint footprint : getFootprints()) {
			if (footprint.getId().equals(guid)) {
				return footprint;
			}
		}
		return getDefaultFootprint();
	}

	public abstract List<TokenFootprint> getFootprints();

	public boolean isIsometric() {
		return false;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
		//		Grid newGrid = (Grid) super.clone();
		//		return newGrid;
	}

	/**
	 * @return Coordinates in Cell-space of the ZonePoint
	 */
	public abstract CellPoint convert(ZonePoint zp);

	/**
	 * @return A ZonePoint whose position within the cell depends on the grid type:<br>
	 *         <i>SquareGrid</i> - top right of cell (x_min, y_min)<br>
	 *         <i>HexGrid</i> - center of cell<br>
	 *         For HexGrids Use getCellOffset() to move ZonePoint from center to top right
	 */
	public abstract ZonePoint convert(CellPoint cp);

	public ZonePoint getNearestVertex(ZonePoint point) {
		int gridx = (int) Math.round((point.x - getOffsetX()) / getCellWidth());
		int gridy = (int) Math.round((point.y - getOffsetY()) / getCellHeight());

		return new ZonePoint((int) (gridx * getCellWidth() + getOffsetX()), (int) (gridy * getCellHeight() + getOffsetY()));
	}

	public abstract GridCapabilities getCapabilities();

	public int getTokenSpace() {
		return getSize();
	}

	public double getCellWidth() {
		return 0;
	}

	public double getCellHeight() {
		return 0;
	}

	/**
	 * @return The offset required to translate from the center of a cell to the top right (x_min, y_min) of the cell's
	 *         bounding rectangle. Used for non-square grids only.<br>
	 * <br>
	 *         Why? Because mySquareGrid.convert(CellPoint cp) returns a ZonePoint in the top right corner(x_min, y_min)
	 *         of the square-cell, whereas myHexGrid.convert(CellPoint cp) returns a ZonePoint in the center of the
	 *         hex-cell. Thus adding the CellOffset allows us to position the ZonePoint returned by
	 *         myHexGrid.convert(CellPoint cp) in an equivalent position to that returned by
	 *         mySquareGrid.convert(CellPoint cp)....I think ;)
	 */
	public Dimension getCellOffset() {
		return NO_DIM;
	}

	public Zone getZone() {
		return zone;
	}

	public void setZone(Zone zone) {
		this.zone = zone;
	}

	public Area getCellShape() {
		return cellShape;
	}

	public BufferedImage getCellHighlight() {
		return null;
	}

	protected abstract Area createCellShape(int size);

	protected void setCellShape(Area cellShape) {
		this.cellShape = cellShape;
	}

	/**
	 * @param Both
	 *            The grid's x and y offset components
	 */
	public void setOffset(int offsetX, int offsetY) {
		this.offsetX = offsetX;
		this.offsetY = offsetY;

		fireGridChanged();
	}

	/**
	 * @return The x component of the grid's offset.
	 */
	public int getOffsetX() {
		return offsetX;
	}

	/**
	 * @return The y component of the grid's offset
	 */
	public int getOffsetY() {
		return offsetY;
	}

	public ZoneWalker createZoneWalker() {
		return null;
	}

	/**
	 * Sets the grid size and creates the grid cell shape
	 * 
	 * @param size
	 *            The size of the grid<br>
	 *            <i>SquareGrid</i> - edge length<br>
	 *            <i>HexGrid</i> - edge to edge diameter
	 */
	public void setSize(int size) {
		this.size = constrainSize(size);
		cellShape = createCellShape(size);
		fireGridChanged();
	}

	/**
	 * Constrains size to MIN_GRID_SIZE <= size <= MAX_GRID_SIZE
	 * 
	 * @return The size after it has been constrained
	 */
	protected final int constrainSize(int size) {
		if (size < MIN_GRID_SIZE) {
			size = MIN_GRID_SIZE;
		} else if (size > MAX_GRID_SIZE) {
			size = MAX_GRID_SIZE;
		}
		return size;
	}

	/**
	 * @return The size of the grid<br>
	 * <br>
	 *         *<i>SquareGrid</i> - edge length<br>
	 *         *<i>HexGrid</i> - edge to edge diameter
	 */
	public int getSize() {
		return size;
	}

	/**
	 * Called by SightType and Light class to return a vision area
	 * based upon a specified distance
	 * 
	 * @param shape CIRCLE, SQUARE or CONE
	 * @param token Used to position the shape and to provide footprint
	 * @param range As specified in the vision or light definition
	 * @param arcAngle Only used by cone
	 * @param offsetAngle Arc distance from facing, only used by cone
	 * @return Area
	 */
	public Area getShapedArea(ShapeType shape, Token token, double range, double arcAngle, int offsetAngle) {
		if (shape == null) {
			shape = ShapeType.CIRCLE;
		}
		int visionDistance = zone.getTokenVisionInPixels();
		double visionRange = (range == 0) ? visionDistance : range * getSize() / zone.getUnitsPerCell();
		Area visibleArea = new Area();
		switch (shape) {
		case CIRCLE:
			visibleArea = new Area(new Ellipse2D.Double(-visionRange, -visionRange, visionRange * 2, visionRange * 2));
			break;
		case SQUARE:
			visibleArea = new Area(new Rectangle2D.Double(-visionRange, -visionRange, visionRange * 2, visionRange * 2));
			break;
		case CONE:
			if (token.getFacing() == null) {
				token.setFacing(0);
			}
			//TODO: confirm if we want the offset to be positive-counter-clockwise, negative-clockwise or vice versa
			//simply a matter of changing the sign on offsetAngle
			Area tempvisibleArea = new Area(new Arc2D.Double(-visionRange, -visionRange, visionRange * 2, visionRange * 2, 360.0 - (arcAngle / 2.0) + (offsetAngle * 1.0), arcAngle, Arc2D.PIE));
			// Rotate
			tempvisibleArea = tempvisibleArea.createTransformedArea(AffineTransform.getRotateInstance(-Math.toRadians(token.getFacing())));

			Rectangle footprint = token.getFootprint(this).getBounds(this);
			footprint.x = -footprint.width / 2;
			footprint.y = -footprint.height / 2;
			//footprint = footprint.createTransformedArea(AffineTransform.getTranslateInstance(-footprint.getBounds().getWidth() / 2, -footprint.getBounds().getHeight() / 2));
			visibleArea.add(new Area(footprint));
			visibleArea.add(tempvisibleArea);
			break;
		default:
			visibleArea = new Area(new Ellipse2D.Double(-visionRange, -visionRange, visionRange * 2, visionRange * 2));
			break;
		}
		return visibleArea;
	}

	private void fireGridChanged() {
		if (zone != null) {
			zone.fireModelChangeEvent(new ModelChangeEvent(this, Event.GRID_CHANGED));
		}
	}

	/**
	 * Draws the grid scaled to the renderer's scale and within the renderer's boundaries
	 */
	public void draw(ZoneRenderer renderer, Graphics2D g, Rectangle bounds) {
		// Do nothing
	}

	public abstract Rectangle getBounds(CellPoint cp);

	/**
	 * Override if getCapabilities.isSecondDimensionAdjustmentSupported() returns true
	 * 
	 * @param length
	 *            the second settable dimension
	 * @return
	 */
	public void setSecondDimension(double length) {
	}

	/**
	 * Override if getCapabilities.isSecondDimensionAdjustmentSupported() returns true
	 * 
	 * @return length the curent value of the second settable dimension
	 */
	public double getSecondDimension() {
		return 0;
	}

	/**
	 * Installs a list of which which actions go with which keystrokes for the purpose of moving the token.
	 * 
	 * @param callback
	 *            The object whose methods are invoked when the event occurs
	 * @param actionMap
	 *            the map of existing keystrokes we want to add ourselves to
	 */
	abstract public void installMovementKeys(PointerTool callback, Map<KeyStroke, Action> actionMap);

	abstract public void uninstallMovementKeys(Map<KeyStroke, Action> actionMap);

	static class DirectionCalculator {
		private static final int NW = 1;
		private static final int N = 2;
		private static final int NE = 4;
		private static final int W = 8;
		private static final int CENTER = 16;
		private static final int E = 32;
		private static final int SW = 64;
		private static final int S = 128;
		private static final int SE = 256;

		public int getDirection(int dirx, int diry) {
			int TopRow = (NW | N | NE);
			int MidRow = (W | CENTER | E);
			int BotRow = (SW | S | SE);

			int LeftCol = (NW | W | SW);
			int MidCol = (N | CENTER | S);
			int RightCol = (NE | E | SE);

			int direction = TopRow | MidRow | BotRow;

			if (dirx > 0)
				direction &= (LeftCol | MidCol); // two left columns
			if (dirx < 0)
				direction &= (MidCol | RightCol); // two right columns

			if (diry > 0)
				direction &= (TopRow | MidRow); // two top rows
			if (diry < 0)
				direction &= (MidRow | BotRow); // two bottom rows

			if (dirx == 0)
				direction &= ~MidRow;
			if (diry == 0)
				direction &= ~MidCol;

			direction &= ~CENTER; // Always turn off the center since we don't check it using the outside iterations...

			return direction;
		}
	}

	private static final DirectionCalculator calculator = new DirectionCalculator();

	/**
	 * Tests the grid cell location to determine whether a token is allowed to move into it when such movement is
	 * player-initiated. (The GM may always move a token into a given grid cell.) This implementation only handles
	 * square grids. When a hex and/or gridless implementation is created, this method should be refactored to the
	 * {@link SquareGrid} class and this method changed to always return <code>true</code>.
	 * <p>
	 * Theory of operation:
	 * <ol>
	 * <li>Break the area to check into a 3x3 set of pieces.
	 * <li>Determine which direction the token is coming from.
	 * <li>For the three pieces of the 3x3 set which are closest to that incoming direction, if all of the three contain
	 * fog, the cell cannot be entered. Return <code>false</code>. Otherwise, at least one does not contain fog. Proceed
	 * to the next step.
	 * <li>Select the region encompassed by the center of the 3x3 set of pieces.
	 * <li>Break this region into another 3x3 set of pieces.
	 * <li>If at least 6 of these pieces are fog-free, then the space is open. Return <code>true</code>.
	 * <li>If at least 4 of these pieces contain fog, then the space is closed. Return <code>false</code>.
	 * </ol>
	 * 
	 * @param token
	 *            token whose movement is being validated; passed in case token state is needed
	 * @param areaToCheck
	 *            destination area to check, measured in ZonePoint units
	 * @param dirx
	 *            direction token is traveling along the X axis
	 * @param diry
	 *            direction token is traveling along the Y axis
	 * @param exposedFog
	 *            area in which fog has been cleared away
	 * @return true or false whether the token may move into the area
	 */
	public boolean validateMove(Token token, Rectangle areaToCheck, int dirx, int diry, Area exposedFog) {
		int direction = calculator.getDirection(dirx, diry);

		Rectangle bounds = new Rectangle();
		int bit = 1;

		if (areaToCheck.width < 9 || (dirx == 0 && diry == 0))
			direction = (512 - 1) & ~DirectionCalculator.CENTER;

		for (int dy = 0; dy < 3; dy++) {
			for (int dx = 0; dx < 3; dx++, bit *= 2) {
				if ((direction & bit) == 0)
					continue;
				oneThird(areaToCheck, dx, dy, bounds);

				// The 'fog' variable defines areas where fog has been cleared away
				if (!exposedFog.contains(bounds))
					continue;
				return checkCenterRegion(areaToCheck, exposedFog);
			}
		}
		// Everything is covered with fog.  Or at least, the three regions that we wanted to use to enter the destination area.
		return false;
	}

	/**
	 * Returns an area based upon the token's cell footprint
	 * @param token
	 * @return
	 */
	public Area getTokenCellArea(Rectangle bounds) {
		// Get the cell footprint
		return new Area(bounds);
	}

	public Area getTokenCellArea(Area bounds) {
		// Get the cell footprint
		return new Area(bounds);
	}

	/**
	 * Check the middle region by subdividing into 3x3 and checking to see if at least 6 are open.
	 * 
	 * @param regionToCheck
	 *            rectangular region to check for hard fog
	 * @param fog
	 *            defines areas where fog is currently covering the background
	 * @return
	 */
	public boolean checkCenterRegion(Rectangle regionToCheck, Area fog) {
		Rectangle center = new Rectangle();
		Rectangle bounds = new Rectangle();
		oneThird(regionToCheck, 1, 1, center); // selects the CENTER piece

		int closedSpace = 0;
		int openSpace = 0;
		for (int dy = 0; dy < 3; dy++) {
			for (int dx = 0; dx < 3; dx++) {
				oneThird(center, dx, dy, bounds);
				if (bounds.width < 1 || bounds.height < 1)
					continue;
				if (!fog.intersects(bounds)) {
					if (++closedSpace > 3)
						return false;
				} else {
					if (++openSpace > 5)
						return true;
				}
			}
		}
		if (log.isInfoEnabled())
			log.info("Center region of size " + regionToCheck.getSize() + " contains neither 4+ closed spaces nor 6+ open spaces?!");
		return openSpace >= closedSpace;
	}

	/**
	 * Check the region by subdividing into 3x3 and checking to see if at least 6 are open.
	 * 
	 * @param regionToCheck
	 *            rectangular region to check for hard fog
	 * @param fog
	 *            defines areas where fog is currently covering the background
	 * @param tolerance 
	 * @return
	 */
	public boolean checkRegion(Rectangle regionToCheck, Area fog, int tolerance) {
		Rectangle bounds = new Rectangle();

		int closedSpace = 0;
		int openSpace = 0;
		for (int dy = 0; dy < 3; dy++) {
			for (int dx = 0; dx < 3; dx++) {
				oneThird(regionToCheck, dx, dy, bounds);
				if (bounds.width < 1 || bounds.height < 1)
					continue;
				if (!fog.intersects(bounds)) {
					if (++closedSpace > (9 - tolerance))
						return false;
				} else {
					if (++openSpace > tolerance)
						return true;
				}
			}
		}

		if (log.isInfoEnabled())
			log.info("Center region of size " + regionToCheck.getSize() + " contains neither " + (9 - tolerance) + "+ closed spaces nor " + tolerance + "+ open spaces?!");
		return openSpace >= closedSpace;
	}

	/**
	 * Divides the specified region into one of nine parts, where the column and row range from 0..2. The destination
	 * Rectangle must already exist (no check for this is made) and it must not be a reference to the same object as the
	 * region to divide (also not checked).
	 * 
	 * @param regionToDivide
	 *            region to subdivide
	 * @param column
	 *            column in the 3x3 grid
	 * @param row
	 *            row in the 3x3 grid
	 * @param destination
	 *            one of nine possible pieces represented as a Rectangle
	 */
	private void oneThird(Rectangle regionToDivide, int column, int row, Rectangle destination) {
		int width = regionToDivide.width * column / 3;
		int height = regionToDivide.height * row / 3;
		destination.x = regionToDivide.x + width;
		destination.y = regionToDivide.y + height;
		destination.width = regionToDivide.width * (column + 1) / 3 - regionToDivide.width * column / 3; // don't simplify or roundoff will be introduced
		destination.height = regionToDivide.height * (row + 1) / 3 - regionToDivide.height * row / 3;
	}
}
