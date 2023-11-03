/*
 * This software Copyright by the RPTools.net development team, and
 * licensed under the Affero GPL Version 3 or, at your option, any later
 * version.
 *
 * MapTool Source Code is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public
 * License * along with this source Code.  If not, please visit
 * <http://www.gnu.org/licenses/> and specifically the Affero license
 * text at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.model.grid;

import com.google.common.base.Stopwatch;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import javax.swing.*;
import net.rptools.lib.FileUtil;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.DeveloperOptions;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.tool.PointerTool;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.client.walker.WalkerMetric;
import net.rptools.maptool.client.walker.ZoneWalker;
import net.rptools.maptool.events.MapToolEventBus;
import net.rptools.maptool.model.*;
import net.rptools.maptool.model.TokenFootprint.OffsetTranslator;
import net.rptools.maptool.model.zones.GridChanged;
import net.rptools.maptool.server.Mapper;
import net.rptools.maptool.server.proto.GridDto;
import net.rptools.maptool.util.GraphicsUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Base class for grids.
 *
 * @author trevor
 */
public abstract class Grid implements Cloneable {
  protected static final Logger log = LogManager.getLogger();

  public Grid() {
    setSizeInPixels(AppPreferences.getDefaultGridSize());
  }

  public Grid(Grid grid) {
    setSizeInPixels(grid.getSizeInPixels());
    if ((int) this.cellType.polygonProperties.inradius != (int) this.sizeInPixels
        || !this.getGridTypeName().equals(grid.getGridTypeName())) {
      cellType = new GridCellType(getSizeInPixels(), getGridTypeName());
    }
    setOffset(grid.offsetX, grid.offsetY);
  }

  /**
   * The minimum grid size (minimum on any dimension). The default value is 9 because the algorithm
   * for determining whether a given square cell can be entered due to fog blocking the cell is
   * based on the cell being split into 3x3, then the center further being split into 3x3; thus at
   * least 9 pixels horizontally and vertically are required.
   */
  public static final int MIN_GRID_SIZE_IN_PIXELS = 9;

  public static final int MAX_GRID_SIZE_IN_PIXELS = 350;
  protected static final int CIRCLE_SEGMENTS = 60;
  private final Dimension NO_DIM = new Dimension();
  private final DirectionCalculator calculator = new DirectionCalculator();
  private static Area cellArea;
  private final int cellEdgeCount = GridFactory.getGridCellFaceCount(this);
  public double lastScale = -1;
  private GridCellType cellType;
  private GeneralPath closedCellPath;
  private Area exposedFogArea;
  private GeneralPath halfCellPath;
  private static final Map<Integer, Area> gridShapeCache = new ConcurrentHashMap<>();

  protected synchronized Map<Integer, Area> getGridShapeCache() {
    return gridShapeCache;
  }

  private String gridTypeName;
  protected Map<KeyStroke, Action> movementKeys = null;
  private static int offsetX = 0;
  private static int offsetY = 0;
  private final GridRenderStyle renderStyle = new GridRenderStyle();
  public volatile GeneralPath scaledShape;
  private static int sizeInPixels;
  private Area visibleArea;

  private Zone zone;

  protected synchronized void setGridShapeCache(int gridRadius, Area newGridArea) {
    final AffineTransform at = new AffineTransform();
    final double gridScale = (double) MAX_GRID_SIZE_IN_PIXELS / getSizeInPixels();
    at.scale(gridScale, gridScale);

    getGridShapeCache().put(gridRadius, newGridArea.createTransformedArea(at));

    // Verify combined Area is a single union of polygons
    if (!newGridArea.isSingular()) {
      log.warn(
          "gridShape {} is not singular, this is unexpected and could affect performance.",
          gridRadius);
    }
  }

  public void drawCoordinatesOverlay(Graphics2D g, ZoneRenderer renderer) {
    // Do nothing -- my default
  }

  public String getGridTypeName() {
    return gridTypeName;
  }

  public GridCellType getCellType() {
    return cellType;
  }

  public void setCellType(GridCellType cellType_) {
    cellType = cellType_;
  }

  public GridRenderStyle getRenderStyle() {
    return renderStyle;
  }

  public GeneralPath getHalfCellPath() {
    return halfCellPath;
  }

  public GeneralPath getClosedCellPath() {
    return closedCellPath;
  }

  public Area getVisibleArea() {
    return visibleArea;
  }

  public Area getExposedFogArea() {
    return exposedFogArea;
  }

  /**
   * Set the facing options for tokens/objects on a grid. Each grid type can providing facings to
   * the edges, the vertices, both, or neither.
   *
   * <p>If both are false, tokens on that grid will not be able to rotate with the mouse and
   * keyboard controls for setting facing.
   *
   * @param faceEdges - Tokens can face edges.
   * @param faceVertices - Tokens can face vertices.
   */
  public void setFacings(boolean faceEdges, boolean faceVertices) {
    // Handle it in the individual grid types
  }

  public int[] getFacingAngles() {
    return null;
  }

  /**
   * Return the Point (double precision) for pixel center of Cell
   *
   * @param cell The cell to get the center of.
   * @return Point of the coordinates.
   */
  public abstract Point2D.Double getCellCenter(CellPoint cell);

  protected List<TokenFootprint> loadFootprints(String path, OffsetTranslator... translators)
      throws IOException {
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

  public boolean useMetric() {
    return false; // only square & iso use metrics
  }

  public boolean isHex() {
    return false;
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
    // Grid newGrid = (Grid) super.clone();
    // return newGrid;
  }

  /**
   * Returns Coordinates in Cell-space of a {@link ZonePoint}.
   *
   * @param zp The {@link ZonePoint} to convert.
   * @return Coordinates in Cell-space of the {@link ZonePoint}.
   */
  public abstract CellPoint convert(ZonePoint zp);

  /**
   * Returns a {@link ZonePoint} whose position within the cell depends on the grid type:
   *
   * <ul>
   *   <li><i>SquareGrid</i> - top right of cell (x_min, y_min)
   *   <li><i>HexGrid</i> - center of cell<br>
   * </ul>
   *
   * <p>For HexGrids Use getCellOffset() to move ZonePoint from center to top right.
   *
   * @param cp the {@link CellPoint} to convert.
   * @return a {@link ZonePoint} within the cell.
   */
  public abstract ZonePoint convert(CellPoint cp);

  public ZonePoint getNearestVertex(ZonePoint point) {
    int gridx = (int) Math.round((point.x - getOffsetX()) / getCellWidth());
    int gridy = (int) Math.round((point.y - getOffsetY()) / getCellHeight());

    return new ZonePoint(
        (int) (gridx * getCellWidth() + getOffsetX()),
        (int) (gridy * getCellHeight() + getOffsetY()));
  }

  public abstract GridCapabilities getCapabilities();

  public int getTokenSpace() {
    return getSizeInPixels();
  }

  public double getCellWidth() {
    return 0;
  }

  public double getCellHeight() {
    return 0;
  }

  /**
   * @return the difference in pixels between the center of a cell and its converted zonepoint.
   */
  public abstract Point2D.Double getCenterOffset();

  /**
   * @return The offset required to translate from the center of a cell to the top right (x_min,
   *     y_min) of the cell's bounding rectangle. Used for non-square grids only.<br>
   *     <br>
   *     Why? Because mySquareGrid.convert(CellPoint cp) returns a ZonePoint in the top right
   *     corner(x_min, y_min) of the square-cell, whereas myHexGrid.convert(CellPoint cp) returns a
   *     ZonePoint in the center of the hex-cell. Thus adding the CellOffset allows us to position
   *     the ZonePoint returned by myHexGrid.convert(CellPoint cp) in an equivalent position to that
   *     returned by mySquareGrid.convert(CellPoint cp)....I think ;)
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

  public Area getCellArea() {
    return cellArea;
  }

  //  protected void setCellShape(Area cellShape) {
  //    this.cellShape = cellShape;
  //  }

  public BufferedImage getCellHighlight() {
    return null;
  }

  public Area createCellShape() {
    return createCellShape(getSizeInPixels());
  }

  public Area createCellShape(int size) {
    if (size != getSizeInPixels() || closedCellPath == null) {
      createCellPaths(size);
    }
    return cellArea;
  }

  /**
   * @param offsetX The grid's x offset component
   * @param offsetY The grid's y offset component
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

  public void createScaledShape(double scale) {
    if (scaledShape != null && scale == lastScale && cellArea != null) return;
    if (cellArea == null || closedCellPath == null) createCellPaths(this.getSizeInPixels());
    AffineTransform at = new AffineTransform();
    //    at.scale(scale, scale);
    AffineTransform.getScaleInstance(scale, scale);
    scaledShape.transform(at);
  }

  public ZoneWalker createZoneWalker() {
    return null;
  }

  /**
   * Constrains size to {@code MIN_GRID_SIZE <= size <= MAX_GRID_SIZE}
   *
   * @param size the size value to constrain.
   * @return The size after it has been constrained.
   */
  protected final int constrainSize(int size) {
    if (size < MIN_GRID_SIZE_IN_PIXELS) {
      size = MIN_GRID_SIZE_IN_PIXELS;
    } else if (size > MAX_GRID_SIZE_IN_PIXELS) {
      size = MAX_GRID_SIZE_IN_PIXELS;
    }
    return size;
  }

  /**
   * @return The size of the grid<br>
   *     <br>
   *     *<i>SquareGrid</i> - edge length<br>
   *     *<i>HexGrid</i> - edge to edge diameter
   */
  public int getSizeInPixels() {
    return sizeInPixels;
  }

  /**
   * Sets the grid size and creates the grid cell shape
   *
   * @param sizeInPixels_ The size of the grid<br>
   *     <i>SquareGrid</i> - edge length<br>
   *     <i>HexGrid</i> - edge to edge diameter
   */
  public void setSizeInPixels(int sizeInPixels_) {
    if (getSizeInPixels() != sizeInPixels_) {
      if (gridTypeName != null) {
        int constrained = constrainSize(sizeInPixels_);
        if (getSizeInPixels() != constrained) {
          sizeInPixels = constrained;
          if (cellType == null) cellType = new GridCellType(constrained, gridTypeName);
          cellArea = createCellShape(constrained);
          fireGridChanged();
        }
      }
    }
  }

  /**
   * Called by SightType and Light class to return a vision area based upon a specified distance
   *
   * @param shape CIRCLE, GRID, SQUARE or CONE
   * @param token Used to position the shape and to provide footprint
   * @param range As specified in the vision or light definition
   * @param arcAngle Only used by cone
   * @param offsetAngle Arc distance from facing, only used by cone
   * @param scaleWithToken used to increase the area based on token footprint
   * @return Area
   */
  public Area getShapedArea(
      ShapeType shape,
      Token token,
      double range,
      double arcAngle,
      int offsetAngle,
      boolean scaleWithToken) {
    if (shape == null) {
      shape = ShapeType.CIRCLE;
    }
    int visionDistance = zone.getTokenVisionInPixels();
    double visionRange =
        (range == 0) ? visionDistance : range * getSizeInPixels() / zone.getUnitsPerCell();

    if (scaleWithToken) {
      double footprintWidth = token.getFootprint(this).getBounds(this).getWidth() / 2;

      // Test for gridless maps
      if (cellArea == null) {
        double tokenBoundsWidth = token.getBounds(getZone()).getWidth() / 2;
        visionRange += tokenBoundsWidth;
      } else {
        // For grids, this will be the same, but for Hex's we'll use the smaller side depending on
        // which Hex type you choose
        double footprintHeight = token.getFootprint(this).getBounds(this).getHeight() / 2;
        visionRange += Math.min(footprintWidth, footprintHeight);
      }
    }

    Area visibleArea = new Area();
    switch (shape) {
      case CIRCLE:
        visibleArea =
            GraphicsUtil.createLineSegmentEllipse(
                -visionRange, -visionRange, visionRange, visionRange, CIRCLE_SEGMENTS);
        break;
      case GRID:
        visibleArea = getGridArea(token, range, scaleWithToken, visionRange);
        break;
      case SQUARE:
        visibleArea =
            new Area(
                new Rectangle2D.Double(
                    -visionRange, -visionRange, visionRange * 2, visionRange * 2));
        break;
      case CONE:
        if (token.getFacing() == null) {
          token.setFacing(0);
        }

        Arc2D cone =
            new Arc2D.Double(
                -visionRange,
                -visionRange,
                visionRange * 2,
                visionRange * 2,
                360.0 - (arcAngle / 2.0) + (offsetAngle * 1.0),
                arcAngle,
                Arc2D.PIE);

        // Flatten the cone to remove 'curves'
        GeneralPath path = new GeneralPath();
        path.append(cone.getPathIterator(null, 1), false);
        Area tempvisibleArea = new Area(path);

        // Rotate
        tempvisibleArea =
            tempvisibleArea.createTransformedArea(
                AffineTransform.getRotateInstance(-Math.toRadians(token.getFacing())));

        Rectangle footprint = token.getFootprint(this).getBounds(this);
        footprint.x = -footprint.width / 2;
        footprint.y = -footprint.height / 2;

        visibleArea.add(new Area(footprint));
        visibleArea.add(tempvisibleArea);
        break;
      case HEX:
        footprint = token.getFootprint(this).getBounds(this);
        double x = footprint.getCenterX();
        double y = footprint.getCenterY();

        double footprintWidth = token.getFootprint(this).getBounds(this).getWidth();
        double footprintHeight = token.getFootprint(this).getBounds(this).getHeight();
        double adjustment = Math.min(footprintWidth, footprintHeight);
        x -= adjustment / 2;
        y -= adjustment / 2;

        visibleArea = createHex(x, y, visionRange, 0);
        break;
      default:
        visibleArea =
            GraphicsUtil.createLineSegmentEllipse(
                -visionRange, -visionRange, visionRange * 2, visionRange * 2, CIRCLE_SEGMENTS);
        break;
    }

    return visibleArea;
  }

  /**
   * Return the cell distance between two cells. Does not take into account terrain or VBL.
   * Overridden by Hex &amp; Gridless grids.
   *
   * @param cellA the first cell
   * @param cellB the second cell
   * @param wmetric the walker metric
   * @return the distance (in cells) between the two cells
   */
  public double cellDistance(CellPoint cellA, CellPoint cellB, WalkerMetric wmetric) {
    int distance;
    int distX = Math.abs(cellA.x - cellB.x);
    int distY = Math.abs(cellA.y - cellB.y);
    if (wmetric == WalkerMetric.NO_DIAGONALS || wmetric == WalkerMetric.MANHATTAN) {
      distance = distX + distY;
    } else if (wmetric == WalkerMetric.ONE_ONE_ONE) {
      distance = Math.max(distX, distY);
    } else if (wmetric == WalkerMetric.ONE_TWO_ONE) {
      distance = Math.max(distX, distY) + Math.min(distX, distY) / 2;
    } else {
      System.out.println("Incorrect WalkerMetric in method cellDistance of Grid.java");
      distance = -1; // error, should not happen;
    }
    return distance;
  }

  protected Area createCellArea(int sides, double radius, double x, double y, double rotation) {
    if (sides == 0) return null;
    createCellPaths(sides, radius, x, y, rotation, isIsometric());
    Shape s = closedCellPath;
    return (Area) s;
  }

  private void createCellPaths(int size) {
    String typeName = getGridTypeName();
    int sides = getCellEdgeCount();
    boolean isIso = isIsometric();
    boolean isHor = typeName.toLowerCase().contains("horiz");
    createCellPaths(
        geCellEdgeCount(),
        size,
        getOffsetX(),
        getOffsetY(),
        isIso && sides != 6 ? 45 : isIso && sides == 6 ? 15 : isHor && sides == 6 ? 90 : 0,
        isIso);
  }

  private int getCellEdgeCount() {
    return this.cellEdgeCount;
  }

  private int geCellEdgeCount() {
    return this.cellEdgeCount;
  }

  protected void createCellPaths(
      int sides, double radius, double x, double y, double rotation, boolean squish) {
    if (cellType == null) {
      log.info("!!!");
      cellType = new GridCellType(sizeInPixels, gridTypeName);
    }
    if (cellType.polygonProperties == null
        || (int) cellType.polygonProperties.circumradius != (int) radius) {
      cellType.setPixelsPerCell(radius);
      cellType.setPolygonProperties();
    }
    GeneralPath pth = cellType.polygonProperties.polygonPath;
    GeneralPath pth_half = cellType.polygonProperties.polygonHalfPath;
    //    int halfway = (int) Math.ceil(sides / 2);
    //    for (int i = 0; i < sides - 1; i++) {
    //      if (i == halfway) pth_half = pth;
    //      if (i == 0) {
    //        pth.moveTo(
    //            radius * Math.cos(i * Math.TAU / sides), radius * Math.sin(i * Math.TAU / sides));
    //      } else {
    //        pth.lineTo(
    //            radius * Math.cos(i * 2 * Math.TAU / sides),
    //            radius * Math.sin(i * 2 * Math.TAU / sides));
    //      }
    //    }
    //
    //    pth.closePath();

    AffineTransform at = new AffineTransform();
    if (rotation != 0) at.rotate(Math.toRadians(rotation));
    if (squish) at.scale(1, 0.5);
    at.translate(x, y);
    cellArea = (Area) at.createTransformedShape(pth);
    pth.transform(at);
    pth_half.transform(at);
    closedCellPath = pth;
    scaledShape = closedCellPath;
    halfCellPath = pth_half;

    PathIterator pi = closedCellPath.getPathIterator(at);
    String strOut = "";
    while (!pi.isDone()) {
      double[] coords = new double[6];
      // strOut += " -> " + coords[0] + ":" + coords[1] + ", ";
      pi.next();
    }
    // log.info("Cell path " + strOut);
    /*
    AffineTransform at           = new AffineTransform();
    Path2D.Float    tmpPath      = new Path2D.Float();

    tmpPath.moveTo(radius, 0);
    double arc     = Math.TAU / sides;
    int    halfway = (int) Math.ceil(sides / 2);

    at.rotate(Math.toRadians(rotation));
    for (int i = 0; i < sides - 1; i++) {
      at.rotate(arc);
      tmpPath = (Path2D.Float) at.createTransformedShape(tmpPath);
      // tmpPath.transform(at);
      tmpPath.lineTo(radius, 0);
      if (i == halfway) tmpPath_half = tmpPath;
    }
    tmpPath.closePath();
    at.translate(x, y);
    if (squish) at.scale(1, 0.5);

    cellArea = new Area(new Path2D.Float(tmpPath, at));
    tmpPath.transform(at);
    tmpPath_half.transform(at);
    closedCellPath = tmpPath;
    halfCellPath = tmpPath_half;

    PathIterator pi         = closedCellPath.getPathIterator(at);
    double[]     lastCoords = new double[6];
    String       strOut     = "";
    while (!pi.isDone()) {
      double[] coords = new double[6];
      int      type   = pi.currentSegment(coords);
      strOut += " - " + coords[0] + ":" + coords[1] + ", ";
      pi.next();
    }
    log.info("Cell path " + strOut);*/
  }

  protected Area createHex(double x, double y, double radius, double rotation) {
    return createCellArea(6, radius, x, y, rotation);
    /* GeneralPath hexPath = new GeneralPath();

    for (int i = 0; i < 6; i++) {
      if (i == 0) {
        hexPath.moveTo(
                x + radius * Math.cos(i * 2 * Math.PI / 6), y + radius * Math.sin(i * 2 * Math.PI / 6));
      } else {
        hexPath.lineTo(
                x + radius * Math.cos(i * 2 * Math.PI / 6), y + radius * Math.sin(i * 2 * Math.PI / 6));
      }
    }

    if (rotation != 0) {
      AffineTransform atArea = AffineTransform.getRotateInstance(rotation);
      return new Area(atArea.createTransformedShape(hexPath));
    } else {
      return new Area(hexPath);
    }*/
  }

  private void fireGridChanged() {
    gridShapeCache.clear();
    new MapToolEventBus().getMainEventBus().post(new GridChanged(this.zone));
  }

  /**
   * Draws the grid scaled to the renderer's scale and within the renderer's boundaries.
   *
   * @param renderer the {@link ZoneRenderer} that represents the screen view.
   * @param g the {@link Graphics2D} class used for drawing.
   * @param bounds the bounds of the drawing area.
   */
  public void draw(ZoneRenderer renderer, Graphics2D g, Rectangle bounds) {
    double scale = renderer.getScale();
    exposedFogArea = zone.getExposedArea();
    visibleArea = renderer.getVisibleScreenArea();
    // Do nothing
  }

  /**
   * Returns a rectangle of pixels bounding the CellPoint, taking into account the grid offset.
   *
   * @param cp the CellPoint to bound.
   * @return the bounding rectangle.
   */
  public abstract Rectangle getBounds(CellPoint cp);

  /**
   * Override if getCapabilities.isSecondDimensionAdjustmentSupported() returns true
   *
   * @return length the curent value of the second settable dimension
   */
  public double getSecondDimension() {
    return 0;
  }

  /**
   * Override if getCapabilities.isSecondDimensionAdjustmentSupported() returns true
   *
   * @param length the second settable dimension
   */
  public void setSecondDimension(double length) {}

  /**
   * Installs a list of which which actions go with which keystrokes for the purpose of moving the
   * token.
   *
   * @param callback The object whose methods are invoked when the event occurs
   * @param actionMap the map of existing keystrokes we want to add ourselves to
   */
  public abstract void installMovementKeys(PointerTool callback, Map<KeyStroke, Action> actionMap);

  public abstract void uninstallMovementKeys(Map<KeyStroke, Action> actionMap);

  /**
   * Tests the grid cell location to determine whether a token is allowed to move into it when such
   * movement is player-initiated. (The GM may always move a token into a given grid cell.) This
   * implementation only handles square grids. When a hex and/or gridless implementation is created,
   * this method should be refactored to the {@link SquareGrid} class and this method changed to
   * always return <code>true</code>.
   *
   * <p>Theory of operation:
   *
   * <ol>
   *   <li>Break the area to check into a 3x3 set of pieces.
   *   <li>Determine which direction the token is coming from.
   *   <li>For the three pieces of the 3x3 set which are closest to that incoming direction, if all
   *       of the three contain fog, the cell cannot be entered. Return <code>false</code>.
   *       Otherwise, at least one does not contain fog. Proceed to the next step.
   *   <li>Select the region encompassed by the center of the 3x3 set of pieces.
   *   <li>Break this region into another 3x3 set of pieces.
   *   <li>If at least 6 of these pieces are fog-free, then the space is open. Return <code>true
   *       </code>.
   *   <li>If at least 4 of these pieces contain fog, then the space is closed. Return <code>false
   *       </code>.
   * </ol>
   *
   * @param token token whose movement is being validated; passed in case token state is needed
   * @param areaToCheck destination area to check, measured in ZonePoint units
   * @param dirx direction token is traveling along the X axis
   * @param diry direction token is traveling along the Y axis
   * @param exposedFog area in which fog has been cleared away
   * @return true or false whether the token may move into the area
   */
  public boolean validateMove(
      Token token, Rectangle areaToCheck, int dirx, int diry, Area exposedFog) {
    int direction = calculator.getDirection(dirx, diry);

    Rectangle bounds = new Rectangle();
    int bit = 1;

    if (areaToCheck.width < 9 || (dirx == 0 && diry == 0)) {
      direction = (512 - 1) & ~DirectionCalculator.CENTER;
    }

    for (int dy = 0; dy < 3; dy++) {
      for (int dx = 0; dx < 3; dx++, bit *= 2) {
        if ((direction & bit) == 0) {
          continue;
        }
        oneThird(areaToCheck, dx, dy, bounds);

        // The 'fog' variable defines areas where fog has been cleared away
        if (!exposedFog.contains(bounds)) {
          continue;
        }
        return checkCenterRegion(areaToCheck, exposedFog);
      }
    }
    // Everything is covered with fog. Or at least, the three regions that we wanted to use to enter
    // the destination area.
    return false;
  }

  /**
   * Returns an area based upon the token's cell footprint.
   *
   * @param bounds the bounds of the cell.
   * @return the {@link Area} based on the footprint.
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
   * @param regionToCheck rectangular region to check for hard fog
   * @param fog defines areas where fog is currently covering the background
   * @return {@code true} if at least 6 regions are open.
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
        if (bounds.width < 1 || bounds.height < 1) {
          continue;
        }
        if (!fog.intersects(bounds)) {
          if (++closedSpace > 3) {
            return false;
          }
        } else {
          if (++openSpace > 5) {
            return true;
          }
        }
      }
    }
    log.info(
        "Center region of size {} contains neither 4+ closed spaces nor 6+ open spaces?!",
        regionToCheck.getSize());
    return openSpace >= closedSpace;
  }

  /**
   * Check the region by subdividing into 3x3 and checking to see if at least {@code tolerance} are
   * open.
   *
   * @param regionToCheck rectangular region to check for hard fog
   * @param fog defines areas where fog is currently covering the background
   * @param tolerance the number of open regions to check for.
   * @return {code true} if there are at least {@code tolerance} open regions.
   */
  public boolean checkRegion(Rectangle regionToCheck, Area fog, int tolerance) {
    Rectangle bounds = new Rectangle();

    int closedSpace = 0;
    int openSpace = 0;
    for (int dy = 0; dy < 3; dy++) {
      for (int dx = 0; dx < 3; dx++) {
        oneThird(regionToCheck, dx, dy, bounds);
        if (bounds.width < 1 || bounds.height < 1) {
          continue;
        }
        if (!fog.intersects(bounds)) {
          if (++closedSpace > (9 - tolerance)) {
            return false;
          }
        } else {
          if (++openSpace > tolerance) {
            return true;
          }
        }
      }
    }
    log.info(
        "Center region of size {} contains neither {}+ closed spaces nor {}+ open spaces?!",
        regionToCheck.getSize(),
        9 - tolerance,
        tolerance);
    return openSpace >= closedSpace;
  }

  /**
   * Divides the specified region into one of nine parts, where the column and row range from 0..2.
   * The destination Rectangle must already exist (no check for this is made) and it must not be a
   * reference to the same object as the region to divide (also not checked).
   *
   * @param regionToDivide region to subdivide
   * @param column column in the 3x3 grid
   * @param row row in the 3x3 grid
   * @param destination one of nine possible pieces represented as a Rectangle
   */
  private void oneThird(Rectangle regionToDivide, int column, int row, Rectangle destination) {
    int width = regionToDivide.width * column / 3;
    int height = regionToDivide.height * row / 3;
    destination.x = regionToDivide.x + width;
    destination.y = regionToDivide.y + height;
    destination.width =
        regionToDivide.width * (column + 1) / 3 - regionToDivide.width * column / 3; // don't
    // simplify
    // or
    // roundoff
    // will be
    // introduced
    destination.height = regionToDivide.height * (row + 1) / 3 - regionToDivide.height * row / 3;
  }

  /**
   * Returns an Area with a given radius that is shaped and aligned to the current grid
   *
   * @param token token which to center the grid area on
   * @param range range in units grid area extends out to
   * @param scaleWithToken whether grid area should expand by the size of the token
   * @param visionRange token's vision in pixels
   * @return the {@link Area} conforming to the current grid layout
   */
  protected Area getGridArea(
      Token token, double range, boolean scaleWithToken, double visionRange) {

    final Area visibleArea;

    if (range > 0) {
      final Stopwatch stopwatch = Stopwatch.createStarted();
      final int gridRadius = (int) (range / getZone().getUnitsPerCell());

      if (scaleWithToken) {
        visibleArea = getScaledGridArea(token, gridRadius);
      } else {
        visibleArea = getGridAreaFromCache(gridRadius).createTransformedArea(getGridOffset(token));
      }

      if (stopwatch.elapsed(TimeUnit.MILLISECONDS) > 50) {
        log.debug(
            "Excessive time to generate {}r grid light, took {}ms",
            gridRadius,
            stopwatch.elapsed(TimeUnit.MILLISECONDS));
      }
    } else {
      // Fall back to regular circle in daylight, etc.
      visibleArea =
          GraphicsUtil.createLineSegmentEllipse(
              -visionRange, -visionRange, visionRange, visionRange, CIRCLE_SEGMENTS);
    }

    return visibleArea;
  }

  /**
   * Returns a combined Area where radius included the tokens footprint. e.g. a 15ft light on a Huge
   * token radiates 15ft from all sides of the token.
   *
   * @param token token to generate area from
   * @param gridRadius distance from token edge to generate area
   * @return the {@link Area} conforming to the current grid layout scaled to include the tokens
   *     size
   */
  protected Area getScaledGridArea(Token token, int gridRadius) {
    final double offsetX = token.getX() + token.getFootprint(this).getBounds(this).getWidth() / 2;
    final double offsetY = token.getY() + token.getFootprint(this).getBounds(this).getHeight() / 2;
    final Area gridArea = getGridAreaFromCache(gridRadius);
    Area occupiedArea = new Area();

    for (CellPoint occupiedCell : token.getOccupiedCells(this)) {
      final double x = (occupiedCell.x * getSizeInPixels()) - offsetX;
      final double y = (occupiedCell.y * getSizeInPixels()) - offsetY;
      final AffineTransform at = new AffineTransform();
      at.translate(x, y);

      occupiedArea.add(gridArea.createTransformedArea(at));
    }

    return occupiedArea;
  }

  /**
   * Returns translated coordinates to adjust the grid area for token footprints that are an odd
   * number of cells and SCALE keyword is not used to adjust the Area proportionally.
   *
   * @param token source token to check footprint against
   * @return the {@link AffineTransform} to align an {@link Area} to the current grid
   */
  protected AffineTransform getGridOffset(Token token) {
    double footprintWidth = token.getFootprint(this).getBounds(this).getWidth();

    final AffineTransform at = new AffineTransform();
    if ((footprintWidth / getSizeInPixels()) % 2 != 0) {
      double coordinateOffset = getSizeInPixels() / -2f;
      at.translate(coordinateOffset, coordinateOffset);
    }
    return at;
  }

  /**
   * Generates an Area that conforms to the current grid cells to the specified radius and caches
   * the results
   *
   * @param gridRadius radius of the Area measured using ONE_TWO_ONE metric
   * @return the {@link Area} conforming to the current grid layout for the given radius
   */
  protected Area createGridArea(int gridRadius) {
    final Area cellArea = new Area(createCellShape(getSizeInPixels()));
    final Set<Point> points = generateRadius(gridRadius);
    Area gridArea = new Area();

    for (Point point : points) {
      final AffineTransform at = new AffineTransform();
      at.translate((point.x) * getSizeInPixels(), (point.y) * getSizeInPixels());
      gridArea.add(cellArea.createTransformedArea(at));
    }

    setGridShapeCache(gridRadius, gridArea);

    return gridArea;
  }

  /**
   * Generates a set of {@link Point} used to create a grid area that only includes the outer most
   * edge of cells
   *
   * @param radius The maximum radius to generate the ring of cell points for this range
   * @return a {@link HashSet} that includes all cells that only equal in distance to the given
   *     radius
   */
  protected Set<Point> generateRing(int radius) {
    return generateRadius(radius, radius);
  }

  /**
   * Generates a set of {@link Point} used to create a grid area
   *
   * @param radius The maximum radius to generate all cell points within this range
   * @return a {@link HashSet} that includes all cells up to the radius
   */
  protected Set<Point> generateRadius(int radius) {
    return generateRadius(0, radius);
  }

  /**
   * Generates a set of {@link Point} used to create a grid area
   *
   * @param minRadius The minimum radius to generate the ring of cell points for this range
   * @param maxRadius The maximum radius to generate the ring of cell points for this range
   * @return a {@link HashSet} that includes all cells between the minRadius to the maxRadius
   */
  protected Set<Point> generateRadius(int minRadius, int maxRadius) {
    Set<Point> points = new HashSet<>();
    CellPoint start = new CellPoint(0, 0);

    WalkerMetric metric = getCurrentMetric();

    for (int y = -maxRadius; y <= maxRadius; y++) {
      for (int x = -maxRadius; x <= maxRadius; x++) {
        double distance = cellDistance(start, new CellPoint(x, y), metric);
        if (distance >= minRadius && distance <= maxRadius) {
          points.add(new Point(x, y));
        }
      }
    }

    return points;
  }

  /**
   * Future change may include getting a metric from a different property/source
   *
   * @return the current {@link WalkerMetric} depending on if a server is running or not
   */
  protected WalkerMetric getCurrentMetric() {
    return MapTool.isPersonalServer()
        ? AppPreferences.getMovementMetric()
        : MapTool.getServerPolicy().getMovementMetric();
  }

  /**
   * Retrieve the generated grid conformed {@link Area} from cache if it exists, otherwise generate,
   * store, and return it.
   *
   * @param gridRadius The radius of the {@link Area} to retrieve from cache.
   * @return the {@link Area} from cache for the given gridRadius
   */
  protected Area getGridAreaFromCache(int gridRadius) {
    // If not already in cache, create and cache it
    // Or if the flag is enabled, recreate cache
    if (DeveloperOptions.Toggle.IgnoreGridShapeCache.isEnabled()
        || !getGridShapeCache().containsKey(gridRadius)) {
      createGridArea(gridRadius);
    }

    double rescale = getSizeInPixels() / (double) MAX_GRID_SIZE_IN_PIXELS;
    final AffineTransform at = new AffineTransform();
    at.scale(rescale, rescale);

    return getGridShapeCache().get(gridRadius).createTransformedArea(at);
  }

  public static Grid fromDto(GridDto dto) {
    Grid grid = null;
    switch (dto.getTypeCase()) {
      case GRIDLESS_GRID -> grid = new GridlessGrid();
      case HEX_GRID -> {
        grid = HexGrid.fromDto(dto.getHexGrid());
      }
      case SQUARE_GRID -> grid = new SquareGrid();
      case ISOMETRIC_GRID -> grid = new IsometricGrid();
    }
    Grid.offsetX = dto.getOffsetX();
    Grid.offsetY = dto.getOffsetY();
    Grid.sizeInPixels = dto.getSize();
    if (dto.hasCellShape()) {
      Grid.cellArea = Mapper.map(dto.getCellShape());
    } else {
      Grid.cellArea = null;
    }
    return grid;
  }

  protected abstract void fillDto(GridDto.Builder dto);

  public GridDto toDto() {
    var dto = GridDto.newBuilder();
    fillDto(dto);
    dto.setOffsetX(offsetX);
    dto.setOffsetY(offsetY);
    dto.setSize(sizeInPixels);
    if (cellArea != null) {
      dto.setCellShape(Mapper.map(cellArea));
    }
    return dto.build();
  }

  public void setGridTypeName(String typeName) {
    this.gridTypeName = typeName;
  }

  class DirectionCalculator {
    // TODO: modify for grid types and axial coordinates
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

      if (dirx > 0) {
        direction &= (LeftCol | MidCol); // two left columns
      }
      if (dirx < 0) {
        direction &= (MidCol | RightCol); // two right columns
      }

      if (diry > 0) {
        direction &= (TopRow | MidRow); // two top rows
      }
      if (diry < 0) {
        direction &= (MidRow | BotRow); // two bottom rows
      }

      if (dirx == 0) {
        direction &= ~MidRow;
      }
      if (diry == 0) {
        direction &= ~MidCol;
      }

      direction &= ~CENTER; // Always turn off the center since we don't check it using the outside
      // iterations...

      return direction;
    }
  }
}
