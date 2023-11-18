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
package net.rptools.maptool.model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Set;
import net.rptools.maptool.client.AppState;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.client.ui.theme.Images;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.model.TokenFootprint.OffsetTranslator;
import net.rptools.maptool.server.Mapper;
import net.rptools.maptool.server.proto.GridDto;
import net.rptools.maptool.server.proto.HexGridDto;

/**
 * An abstract hex grid class that uses generic Cartesian-coordinates for calculations to allow for
 * various hex grid orientations.
 *
 * <p>The v-axis points along the direction of edge to edge hexes
 */
public abstract class HexGrid extends Grid {

  // A regular hexagon is one where all angles are 60 degrees.
  // the ratio = minor_radius / edge_length
  public static final double REGULAR_HEX_RATIO = Math.sqrt(3) / 2;

  /** One DirectionCalculator object is shared by all instances of this hex grid class. */
  private static final DirectionCalculator calculator = new DirectionCalculator();

  private static final GridCapabilities GRID_CAPABILITIES =
      new GridCapabilities() {
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
          return true;
        }

        public boolean isCoordinatesSupported() {
          return false;
        }
      };
  protected static BufferedImage pathHighlight = RessourceManager.getImage(Images.GRID_BORDER_HEX);

  @Override
  public Point2D.Double getCenterOffset() {
    return new Point2D.Double(0, 0);
  }

  /** minorRadius / edgeLength */
  private double hexRatio = REGULAR_HEX_RATIO;

  /**
   * One-half the length of an edge. Set to sqrt(edgeLength^2 - minorRadius^2), i.e. one side of a
   * right triangle.
   */
  private double edgeProjection;

  /** Distance from centerpoint to middle of a face. Set to gridSize/2. */
  private double minorRadius;

  /**
   * Distance from centerpoint to vertex. Set to minorRadius/hexRatio (basically, uses 30 degree
   * cosine to calculate sqrt(3)/2).
   */
  private double edgeLength;

  // Hex defining variables scaled for zoom
  private double scaledEdgeProjection;
  private double scaledMinorRadius;
  private double scaledEdgeLength;

  /** Cached value from the last request to scale the hex grid */
  private double lastScale = -1;

  /** Cached value of the hex shape using <code>lastScale</code> */
  private transient GeneralPath scaledHex;

  /**
   * The offset required to translate from the center of a cell to the top right (x_min, y_min) of
   * the cell's bounding rectangle.
   */
  private Dimension cellOffset;

  public HexGrid() {
    super();
  }

  @Override
  public boolean isHex() {
    return true;
  }

  public boolean isHexHorizontal() {
    return false;
  }

  public boolean isHexVertical() {
    return false;
  }

  public double getEdgeProjection() {
    return edgeProjection;
  }

  public void setEdgeProjection(double edgeProjection) {
    this.edgeProjection = edgeProjection;
  }

  public double getMinorRadius() {
    return minorRadius;
  }

  public void setMinorRadius(double minorRadius) {
    this.minorRadius = minorRadius;
  }

  public double getEdgeLength() {
    return edgeLength;
  }

  public void setEdgeLength(double edgeLength) {
    this.edgeLength = edgeLength;
  }

  @Override
  public Point2D.Double getCellCenter(CellPoint cell) {
    // hex grids have their pixel xy at their center
    ZonePoint zonePoint = convert(cell);
    return new Point2D.Double(zonePoint.x, zonePoint.y);
  }

  @Override
  protected Area createCellShape(int size) {
    // don't use size. it has already been used to set the minorRadius
    // and will only introduce a rounding error.
    Area a = new Area(createShape(minorRadius, edgeProjection, edgeLength));
    //    System.out.println("HexGrid.createCellShape(): " + a.getBounds().width + ":" +
    // a.getBounds().height);
    return a;
  }

  @Override
  public Rectangle getBounds(CellPoint cp) {
    // This is naive, but, give it a try
    ZonePoint zp = convert(cp);
    Shape shape = getCellShape();

    int w = shape.getBounds().width;
    int h = shape.getBounds().height;

    zp.x -= w / 2;
    zp.y -= h / 2;

    // System.out.println(new Rectangle(zp.x, zp.y, w, h));
    return new Rectangle(zp.x, zp.y, w, h);
  }

  /**
   * @return Distance from the center to edge of a hex
   */
  public double getVRadius() {
    return minorRadius;
  }

  /**
   * @return Distance from the center to vertex of a hex
   */
  public double getURadius() {
    return edgeLength / 2 + edgeProjection;
  }

  @Override
  public Dimension getCellOffset() {
    return cellOffset;
  }

  /**
   * A generic form of getCellOffset() where V is the axis of edge to edge hexes.
   *
   * @return The offset required to translate from the center of a cell to the least edge (v_min)
   */
  public double getCellOffsetV() {
    return -getVRadius();
  }

  /**
   * A generic form of getCellOffset() where U is the axis perpendicular to the line of edge to edge
   * hexes.
   *
   * @return The offset required to translate from the center of a cell to the least vertex (u_min)
   */
  public double getCellOffsetU() {
    return -getURadius();
  }

  /**
   * The offset required to translate from the center of a cell to the top right (x_min, y_min) of
   * the cell's bounding rectangle.
   *
   * @return a {@link Dimension} object where width and height is translated to the grid
   */
  protected abstract Dimension setCellOffset();

  @Override
  public void setSize(int size) {
    if (hexRatio == 0) {
      hexRatio = REGULAR_HEX_RATIO;
    }
    // Using size as the edge-to-edge distance or
    // minor diameter of the hex.
    size = constrainSize(size);

    minorRadius = (double) size / 2;
    edgeLength = minorRadius / hexRatio;
    // edgeProjection = Math.sqrt(edgeLength * edgeLength - minorRadius * minorRadius); //
    // Pythagorus
    edgeProjection = edgeLength / 2; // It's an isosceles triangle, after all!

    scaledHex = null;

    // Cell offset gives the offset to apply to the cell zone coords to draw images/tokens
    cellOffset = setCellOffset();
    // The call to the super.setSize() must be last as it calls createCellShape()
    // which needs the values set above.
    super.setSize(size);
  }

  protected void createShape(double scale) {
    if (lastScale == scale && scaledHex != null) {
      return;
    }
    scaledMinorRadius = minorRadius * scale;
    scaledEdgeLength = edgeLength * scale;
    scaledEdgeProjection = edgeProjection * scale;

    scaledHex = createHalfShape(scaledMinorRadius, scaledEdgeProjection, scaledEdgeLength);

    lastScale = scale;
  }

  private GeneralPath createShape(double minorRadius, double edgeProjection, double edgeLength) {
    GeneralPath hex = new GeneralPath();
    hex.moveTo(0, (int) minorRadius);
    hex.lineTo((int) edgeProjection, 0);
    hex.lineTo((int) (edgeProjection + edgeLength), 0);
    hex.lineTo((int) (edgeProjection + edgeLength + edgeProjection), (int) minorRadius);
    hex.lineTo((int) (edgeProjection + edgeLength), (int) (minorRadius * 2));
    hex.lineTo((int) (edgeProjection), (int) (minorRadius * 2));

    orientHex(hex);
    return hex;
  }

  private GeneralPath createHalfShape(
      double minorRadius, double edgeProjection, double edgeLength) {
    GeneralPath hex = new GeneralPath();
    hex.moveTo(0, (int) minorRadius);
    hex.lineTo((int) edgeProjection, 0);
    hex.lineTo((int) (edgeProjection + edgeLength), 0);
    hex.lineTo((int) (edgeProjection + edgeLength + edgeProjection), (int) minorRadius);

    orientHex(hex);
    return hex;
  }

  @Override
  public boolean validateMove(
      Token token, Rectangle areaToCheck, int dirx, int diry, Area exposedFog) {
    // For a hex grid, we calculate the center of the areaToCheck and use that to calculate the
    // CellPoint.
    ZonePoint actual =
        new ZonePoint(
            areaToCheck.x + areaToCheck.width / 2, areaToCheck.y + areaToCheck.height / 2);

    // The first step is to check the center of the destination hex; if it's not in the exposed fog,
    // there's no reason to check
    // the rest of the pieces since we can just return false right away.
    if (!token.isSnapToGrid()) {
      // If we're not SnapToGrid, use the actual mouse coordinates
      return exposedFog.contains(actual.x, actual.y);
    }
    // If we are SnapToGrid, round off the position and check that instead.
    CellPoint cp = convertZP(actual.x, actual.y);
    if (cp.x == 3 && cp.y == 0) {
      cp.y = 0; // hook for setting breakpoint in debugger while testing
    }

    ZonePoint snappedZP = convertCP(cp.x, cp.y);
    if (!exposedFog.contains(snappedZP.x, snappedZP.y)) {
      return false;
    }

    // The next step is to check the triangle that covers the hex face we are leaving from and teh
    // one we
    // are entering through to see if either contain any fog. If they do, the movement is
    // disallowed.
    int direction = calculator.getDirection(dirx, diry);
    if (direction < DirectionCalculator.NW || direction > DirectionCalculator.SW) {
      // we're not really moving so return 'true' -- it's a valid movement
      // XXX When does this happen?
      return true;
    }
    boolean result;
    // can we move into the new cell?
    result = checkOneSlice(snappedZP, direction, exposedFog);

    // If this one is false, don't bother checking the other one...
    if (!result) {
      return false;
    }

    snappedZP.translate(-dirx, -diry);
    cp = convert(snappedZP); // takes grid orientation and cellOffset into account
    snappedZP = convert(cp);
    result =
        checkOneSlice(
            snappedZP,
            calculator.oppositeDirection(direction),
            exposedFog); // can we exit our own cell?
    return result;
  }

  /*
   * (non-Javadoc)
   *
   * @see net.rptools.maptool.model.Grid#validateMove(java.awt.Rectangle, int, int, java.awt.geom.Area)
   */

  private boolean checkOneSlice(ZonePoint zp, int dir, Area exposedFog) {
    Shape s = calculator.getFogAreaToCheck(dir);

    // The resulting Shape is 4x larger than it should be. Use a transform to correct it.
    AffineTransform af = new AffineTransform();
    af.translate(zp.x, zp.y);
    af.scale(minorRadius / 100, minorRadius / 100);
    Area transformed = new Area(af.createTransformedShape(s));

    // Create an Area based on the pie slice, then calculate the intersection with the exposed area.
    // If the result
    // is exactly the same as the original pie slice, then the entire slice must have been contained
    // with the
    // exposed area. That means it's fine for a token to move into the grid cell. Whew. ;-)
    Area a = new Area(transformed);
    a.intersect(exposedFog);
    return a.equals(transformed);
  }

  /**
   * Default orientation is for a vertical hex grid Override for other orientations
   *
   * @param hex a grid to orient
   */
  protected void orientHex(GeneralPath hex) {
    return;
  }

  @Override
  public GridCapabilities getCapabilities() {
    return GRID_CAPABILITIES;
  }

  @Override
  public int getTokenSpace() {
    return (int) (getVRadius() * 2);
  }

  protected abstract void setGridDrawTranslation(Graphics2D g, double u, double v);

  protected abstract double getRendererSizeU(ZoneRenderer renderer);

  protected abstract double getRendererSizeV(ZoneRenderer renderer);

  protected abstract int getOffV(ZoneRenderer renderer);

  protected abstract int getOffU(ZoneRenderer renderer);

  @Override
  public void draw(ZoneRenderer renderer, Graphics2D g, Rectangle bounds) {
    createShape(renderer.getScale());

    int offU = getOffU(renderer);
    int offV = getOffV(renderer);
    int count = 0;

    Object oldAntiAlias = SwingUtil.useAntiAliasing(g);
    g.setColor(new Color(getZone().getGridColor()));
    g.setStroke(new BasicStroke(AppState.getGridSize()));

    for (double v = offV % (scaledMinorRadius * 2) - (scaledMinorRadius * 2);
        v < getRendererSizeV(renderer);
        v += scaledMinorRadius) {
      double offsetU = (int) ((count & 1) == 0 ? 0 : -(scaledEdgeProjection + scaledEdgeLength));
      count++;

      double start =
          offU % (2 * scaledEdgeLength + 2 * scaledEdgeProjection)
              - (2 * scaledEdgeLength + 2 * scaledEdgeProjection);
      double end = getRendererSizeU(renderer) + 2 * scaledEdgeLength + 2 * scaledEdgeProjection;
      double incr = 2 * scaledEdgeLength + 2 * scaledEdgeProjection;
      for (double u = start; u < end; u += incr) {
        setGridDrawTranslation(g, u + offsetU, v);
        g.draw(scaledHex);
        setGridDrawTranslation(g, -(u + offsetU), -v);
      }
    }
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAntiAlias);
  }

  /**
   * Generic form of getOffsetX for ease of transforming to other grid orientations.
   *
   * @return The U component of the grid's offset.
   */
  protected abstract int getOffsetU();

  /**
   * Generic form of getOffsetY for ease of transforming to other grid orientations.
   *
   * @return The V component of the grid's offset.
   */
  protected abstract int getOffsetV();

  /**
   * A method used by HexGrid.convert(ZonePoint zp) to allow for alternate grid orientations
   *
   * @param zpU Zone point U dimension
   * @param zpV Zone point V dimension
   * @return Coordinates in Cell-space of the ZonePoint
   */
  protected CellPoint convertZP(int zpU, int zpV) {
    int xSect;
    int ySect;

    int offsetZpU = zpU - getOffsetU();
    int offsetZpV = zpV - getOffsetV();

    if (offsetZpU < 0) {
      xSect = (int) (offsetZpU / (edgeProjection + edgeLength)) - 1;
    } else {
      xSect = (int) (offsetZpU / (edgeProjection + edgeLength));
    }
    if (offsetZpV < 0) {
      if ((xSect & 1) == 1) {
        ySect = (int) ((offsetZpV - minorRadius) / (2 * minorRadius)) - 1;
      } else {
        ySect = (int) (offsetZpV / (2 * minorRadius)) - 1;
      }
    } else {
      if ((xSect & 1) == 1) {
        ySect = (int) ((offsetZpV - minorRadius) / (2 * minorRadius));
      } else {
        ySect = (int) (offsetZpV / (2 * minorRadius));
      }
    }
    int xPxl = Math.abs((int) (offsetZpU - xSect * (edgeProjection + edgeLength)));
    int yPxl = Math.abs((int) (offsetZpV - ySect * (2 * minorRadius)));

    int gridX = xSect;
    int gridY = ySect;

    double m = edgeProjection / minorRadius;

    // System.out.format("gx:%d gy:%d px:%d py:%d m:%f\n", xSect, ySect, xPxl, yPxl, m);
    // System.out.format("gx:%d gy:%d px:%d py:%d\n", xSect, ySect, zp.x, zp.y);

    switch (xSect & 1) {
      case 0:
        if (yPxl <= minorRadius) {
          if (xPxl < edgeProjection - yPxl * m) {
            gridX = xSect - 1;
            gridY = ySect - 1;
          }
        } else {
          if (xPxl < (yPxl - minorRadius) * m) {
            gridX = xSect - 1;
            // gridY = ySect;
          }
        }
        break;
      case 1:
        if (yPxl >= minorRadius) {
          if (xPxl < (edgeProjection - (yPxl - minorRadius) * m)) {
            gridX = xSect - 1;
            // gridY = ySect;
          } else {
            // gridX = xSect;
            // gridY = ySect;
          }
        } else {
          if (xPxl < (yPxl * m)) {
            gridX = xSect - 1;
            // gridY = ySect;
          } else {
            // gridX = xSect;
            gridY = ySect - 1;
          }
        }

        break;
    }
    // System.out.format("gx:%d gy:%d\n", gridX, gridY);
    return new CellPoint(gridX, gridY);
  }

  /**
   * A method used by HexGrid.convert(CellPoint cp) to allow for alternate grid orientations
   *
   * @param cpU Cell point U dimension
   * @param cpV Cell point V dimension
   * @return A ZonePoint positioned at the center of the Hex
   */
  protected ZonePoint convertCP(int cpU, int cpV) {
    int u, v;

    u = (int) Math.round(cpU * (edgeProjection + edgeLength) + edgeLength) + getOffsetU();
    v = (int) (cpV * 2 * minorRadius + ((cpU & 1) == 0 ? 1 : 2) * minorRadius + getOffsetV());

    return new ZonePoint(u, v);
  }

  @Override
  public double getSecondDimension() {
    return getURadius() * 2;
  }

  @Override
  public void setSecondDimension(double length) {
    if (length < minorRadius * 2) {
      hexRatio = REGULAR_HEX_RATIO;
    } else {
      // some linear algebra and a quadratic equation results in:
      double aspectRatio = length / (2 * minorRadius);
      double a = 0.75;
      double c = -(aspectRatio * aspectRatio + 1) * minorRadius * minorRadius;
      double b = minorRadius * aspectRatio;
      edgeLength = (-b + Math.sqrt(b * b - 4 * a * c)) / (2 * a);
      hexRatio = minorRadius / edgeLength;
    }
  }

  @Override
  protected Area createGridArea(int gridRadius) {
    final Area cellArea = new Area(createCellShape(getSize()));
    final Set<Point> points = generateRing(gridRadius);
    Area gridArea = new Area();

    // HACK! Hex cellShape is ever so off from grid so adding them to a single Area can produce gap
    // artifacts in the rendering
    // TODO: Look at cellShape and see if it needs adjusting, and if so what does that affect
    // downstream if anything?
    final double hexScale = 1.025;

    for (Point point : points) {
      final CellPoint cellPoint = new CellPoint(point.x, point.y);
      final ZonePoint zp = cellPoint.convertToZonePoint(this);

      final AffineTransform at = new AffineTransform();
      at.translate(zp.x, zp.y);

      if (isHexHorizontal()) {
        at.scale(1, hexScale);
      } else {
        at.scale(hexScale, 1);
      }

      gridArea.add(cellArea.createTransformedArea(at));
    }

    // Fill inner Hex Area with one large area to save time
    final int hexRadius = gridRadius * getSize();

    if (isHexHorizontal()) {
      gridArea.add(createHex(getSize(), getSize(), hexRadius, 0));
    } else {
      gridArea.add(createHex(getSize(), -getSize(), hexRadius, Math.toRadians(90)));
    }

    setGridShapeCache(gridRadius, gridArea);

    return gridArea;
  }

  @Override
  protected Area getScaledGridArea(Token token, int gridRadius) {
    gridRadius += (int) (token.getFootprint(this).getBounds(this).getWidth() / getSize() / 2);
    return getGridAreaFromCache(gridRadius).createTransformedArea(getGridOffset(token));
  }

  protected abstract OffsetTranslator getOffsetTranslator();

  public static HexGrid fromDto(HexGridDto dto) {
    HexGrid grid = null;
    if (dto.getVertical()) grid = new HexGridVertical();
    else grid = new HexGridHorizontal();

    grid.hexRatio = dto.getHexRatio();
    grid.edgeProjection = dto.getEdgeProjection();
    grid.minorRadius = dto.getMinorRadius();
    grid.edgeLength = dto.getEdgeLength();
    grid.scaledEdgeProjection = dto.getScaledEdgeProjection();
    grid.scaledMinorRadius = dto.getScaledMinorRadius();
    grid.scaledEdgeLength = dto.getScaledEdgeLength();
    grid.lastScale = dto.getLastScale();
    var point = dto.getCellOffset();
    grid.cellOffset = new Dimension(point.getX(), point.getY());
    return grid;
  }

  protected void fillDto(GridDto.Builder dto) {
    var hexDto = HexGridDto.newBuilder();
    hexDto.setVertical(this instanceof HexGridVertical);
    hexDto.setHexRatio(hexRatio);
    hexDto.setEdgeProjection(edgeProjection);
    hexDto.setMinorRadius(minorRadius);
    hexDto.setEdgeLength(edgeLength);
    hexDto.setScaledEdgeProjection(scaledEdgeProjection);
    hexDto.setScaledMinorRadius(scaledMinorRadius);
    hexDto.setScaledEdgeLength(scaledEdgeLength);
    hexDto.setLastScale(lastScale);
    hexDto.setCellOffset(Mapper.map(cellOffset));
    dto.setHexGrid(hexDto);
  }

  static class DirectionCalculator {

    private static final int NW = 0;
    private static final int N = 1;
    private static final int NE = 2;
    private static final int SE = 3;
    private static final int S = 4;
    private static final int SW = 5;
    private Shape[] pieSlices = null;

    /**
     * Given delta movement on the X and Y axes, determine which direction that would be for the
     * current grid type. Note that horizontal and vertical hex grids will be different.
     *
     * @param dirx movement on the X axis
     * @param diry movement on the Y axis
     * @return direction being moved
     */
    public int getDirection(int dirx, int diry) {
      int direction = -1;
      // @formatter:off
      if (dirx > 0 && diry > 0) direction = NW;
      if (dirx > 0 && diry < 0) direction = SW;
      if (dirx < 0 && diry > 0) direction = NE;
      if (dirx < 0 && diry < 0) direction = SE;
      if (dirx == 0 && diry > 0) direction = N;
      if (dirx == 0 && diry < 0) direction = S;
      // @formatter:on
      return direction;
    }

    /**
     * Given a particular direction returns the opposite direction. Used for finding the "pie slice"
     * on the 'other side' of the hex grid cell.
     *
     * @param dir one of the constants <code>DirectionCalculator.NW</code> .. <code>
     *            DirectionCalculator.SW</code> (0..5)
     * @return
     */
    public int oppositeDirection(int dir) {
      return (dir + 3) % 6;
    }

    /**
     * <div style="float: left">Image of a <i>vertical hex</i> grid:<br>
     * <img src="doc-files/HexGridVertical.png" title="Vertical Hex"> </div> <div>
     *
     * <p>Returns a {@link Shape} that can be used to test for exposed fog areas in the direction
     * specified by <code>dir</code>. Note that <code>dir</code> is the direction from which a token
     * is entering a grid cell. So if the token is coming from the North, <code>dir</code> should be
     * Direction.Calculator.N (i.e. "2"). The resulting Shape returned would be the isosceles
     * triangle that represents one-sixth of the hex in an upward direction. The returned Shape has
     * its origin at the center of the hex grid cell.
     *
     * @param dir direction that the movement is coming from
     * @return a {@link Shape} representing one slice of the 6-slice pie </div>
     */
    public Shape getFogAreaToCheck(int dir) {
      // pieSlices = null; // debugging -- forces the following IF statement to always be true
      if (pieSlices == null) {
        double[][] coords = {
          {0, 0, -114, 0, -57, -100}, // NW
          {0, 0, -57, -100, 57, -100}, // N
          {0, 0, 57, -100, 114, 0}, // NE
          {0, 0, 114, 0, 57, 100}, // SE
          {0, 0, 57, 100, -57, 100}, // S
          {0, 0, -57, 100, -114, 0}, // SW
        };
        pieSlices = new Shape[6];
        for (int i = 0; i < 6; i++) {
          double[] row = coords[i];
          GeneralPath slice = new GeneralPath();
          slice.moveTo(row[0], row[1]);
          slice.lineTo(row[2], row[3]);
          slice.lineTo(row[4], row[5]);
          pieSlices[i] = slice;
        }
      }
      return pieSlices[dir];
    }
  }
}
