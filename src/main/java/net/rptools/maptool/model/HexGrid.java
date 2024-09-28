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
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.function.BiConsumer;
import net.rptools.maptool.client.AppState;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.client.ui.theme.Images;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.model.TokenFootprint.OffsetTranslator;
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
  // the ratio = minor_radius / diameter
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
  protected static final BufferedImage pathHighlight =
      RessourceManager.getImage(Images.GRID_BORDER_HEX);

  @Override
  public Point2D.Double getCenterOffset() {
    return new Point2D.Double(0, 0);
  }

  /**
   * vRadius / uRadius, or size / diameter
   *
   * <p>Together with {@link #getSize()} this completely defines the hex proportions. All other
   * properties (except orientation) can be derived from these two.
   */
  private double hexRatio = REGULAR_HEX_RATIO;

  /** Distance from centerpoint to middle of a face. Set to gridSize/2. */
  private transient double minorRadius;

  /**
   * The projection of a sloped edge onto the diameter.
   *
   * <p>For a regular hexagon this is half the edge length, but for stretched hexagons it could be
   * different.
   */
  protected transient double edgeProjection;

  /**
   * Length all edges. For a regular hexagon, this will also be the distance from the center point
   * to any vertex, but for a stretch hexagon this does not hold different.
   */
  protected transient double edgeLength;

  @Override
  public boolean isHex() {
    return true;
  }

  public boolean isHexHorizontal() {
    return false;
  }

  @Override
  public Point2D.Double getCellCenter(CellPoint cell) {
    // hex grids have their pixel xy at their center
    ZonePoint zonePoint = convert(cell);
    return new Point2D.Double(zonePoint.x, zonePoint.y);
  }

  @Override
  protected Area createCellShape() {
    var hex = new GeneralPath();
    hex.moveTo(0, minorRadius);
    hex.lineTo(edgeProjection, 0);
    hex.lineTo(edgeProjection + edgeLength, 0);
    hex.lineTo(edgeProjection + edgeLength + edgeProjection, minorRadius);
    hex.lineTo(edgeProjection + edgeLength, getSize());
    hex.lineTo(edgeProjection, getSize());
    orientHex(hex);

    return new Area(hex);
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

  protected Object readResolve() {
    setDimensions(getSize(), getSize() / hexRatio);
    return super.readResolve();
  }

  @Override
  public void setSize(int size) {
    if (hexRatio == 0) {
      hexRatio = REGULAR_HEX_RATIO;
    }
    // Using size as the edge-to-edge distance or
    // minor diameter of the hex.
    size = constrainSize(size);
    // Preserve the "aspect ratio" in this method.
    setDimensions(size, size / hexRatio);

    // The call to the super.setSize() must be last as it calls createCellShape()
    // which needs the values set above.
    super.setSize(size);
  }

  private void setDimensions(int size, double diameter) {
    // Update internal variables to agree with the new dimensions.
    minorRadius = size / 2.;
    hexRatio = size / diameter;
    // Since we're just scaling a regular hexagon, these simple relations still hold.
    edgeLength = diameter / 2;
    edgeProjection = edgeLength / 2;

    // If we instead wanted equal edge lengths, we have to solve this equation:
    // $3 * edge^2 + 2*diameter * edge - (size^2 + diameter^2) = 0$
    // Giving:
    // edgeLength = (-diameter + 2 * Math.sqrt(diameter * diameter + 0.75 * size * size)) / 3.
    // and:
    // edgeProjection = (diameter - edgeLength) / 2
  }

  private GeneralPath createHalfShape(
      double minorRadius, double edgeProjection, double edgeLength) {
    GeneralPath hex = new GeneralPath();
    hex.moveTo(0, minorRadius);
    hex.lineTo(edgeProjection, 0);
    hex.lineTo(edgeProjection + edgeLength, 0);
    hex.lineTo(edgeProjection + edgeLength + edgeProjection, minorRadius);

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
    var scale = renderer.getScale();
    var scaledMinorRadius = minorRadius * scale;
    var scaledEdgeLength = edgeLength * scale;
    var scaledEdgeProjection = edgeProjection * scale;
    var scaledHex = createHalfShape(scaledMinorRadius, scaledEdgeProjection, scaledEdgeLength);

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
          }
        }
        break;
      case 1:
        if (yPxl >= minorRadius) {
          if (xPxl < (edgeProjection - (yPxl - minorRadius) * m)) {
            gridX = xSect - 1;
          }
        } else {
          if (xPxl < (yPxl * m)) {
            gridX = xSect - 1;
          } else {
            gridY = ySect - 1;
          }
        }

        break;
    }
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

    u =
        (int) Math.round(cpU * (edgeProjection + edgeLength) + (edgeLength / 2 + edgeProjection))
            + getOffsetU();
    v = (int) (cpV * 2 * minorRadius + ((cpU & 1) == 0 ? 1 : 2) * minorRadius + getOffsetV());

    return new ZonePoint(u, v);
  }

  @Override
  public double getSecondDimension() {
    return getURadius() * 2;
  }

  @Override
  public void setSecondDimension(double length) {
    setDimensions(getSize(), length);
  }

  @Override
  protected Area createGridArea(int gridRadius) {
    // Start at the top and go clockwise.
    var path = new Path2D.Double();

    BiConsumer<Double, Double> moveTo =
        isHexHorizontal() ? ((x, y) -> path.moveTo(y, x)) : ((x, y) -> path.moveTo(x, y));
    BiConsumer<Double, Double> lineTo =
        isHexHorizontal() ? ((x, y) -> path.lineTo(y, x)) : ((x, y) -> path.lineTo(x, y));

    var x = edgeLength / 2 + edgeProjection;
    var y = -gridRadius * 2 * minorRadius;
    moveTo.accept(x, y);

    for (int i = 1; i <= gridRadius; ++i) {
      // One step east.
      x += edgeLength;
      lineTo.accept(x, y);

      // One step southeast
      x += edgeProjection;
      y += minorRadius;
      lineTo.accept(x, y);
    }

    // Finish up the northeast cell, then continue southward.
    x -= edgeProjection;
    y += minorRadius;
    lineTo.accept(x, y);

    for (int i = 1; i <= gridRadius; ++i) {
      // One step southeast
      x += edgeProjection;
      y += minorRadius;
      lineTo.accept(x, y);

      // One step southwest.
      x -= edgeProjection;
      y += minorRadius;
      lineTo.accept(x, y);
    }

    // Finish up the southeast cell, then continue southwest.
    x -= edgeLength;
    lineTo.accept(x, y);

    for (int i = 1; i <= gridRadius; ++i) {
      // One step southwest.
      x -= edgeProjection;
      y += minorRadius;
      lineTo.accept(x, y);

      // One step west.
      x -= edgeLength;
      lineTo.accept(x, y);
    }

    // Finish up the southern cell, then continue northwest.
    x -= edgeProjection;
    y -= minorRadius;
    lineTo.accept(x, y);

    for (int i = 1; i <= gridRadius; ++i) {
      // One step west;
      x -= edgeLength;
      lineTo.accept(x, y);

      // One step northwest
      x -= edgeProjection;
      y -= minorRadius;
      lineTo.accept(x, y);
    }

    // Finish up the southwest cell, then continue northward.
    x += edgeProjection;
    y -= minorRadius;
    lineTo.accept(x, y);

    for (int i = 1; i <= gridRadius; ++i) {
      // One step northwest.
      x -= edgeProjection;
      y -= minorRadius;
      lineTo.accept(x, y);

      // One step norhteast.
      x += edgeProjection;
      y -= minorRadius;
      lineTo.accept(x, y);
    }

    // Finish up the northwest cell, then continue northeast.
    x += edgeLength;
    lineTo.accept(x, y);

    for (int i = 1; i <= gridRadius; ++i) {
      // One step northeast.
      x += edgeProjection;
      y -= minorRadius;
      lineTo.accept(x, y);

      // One step east
      x += edgeLength;
      lineTo.accept(x, y);
    }

    path.closePath();

    return new Area(path);
  }

  @Override
  protected Area getScaledGridArea(Token token, int gridRadius) {
    gridRadius += (int) (token.getFootprint(this).getBounds(this).getWidth() / getSize() / 2);
    return getGridAreaFromCache(gridRadius).createTransformedArea(getGridOffset(token));
  }

  protected abstract OffsetTranslator getOffsetTranslator();

  public static HexGrid fromDto(HexGridDto dto) {
    HexGrid grid = null;
    if (dto.getVertical()) {
      grid = new HexGridVertical();
    } else {
      grid = new HexGridHorizontal();
    }

    // Exact values do not matter, just the proportions. Grid itself will scale to the right size.
    grid.setDimensions(100, 100 / grid.hexRatio);
    return grid;
  }

  protected void fillDto(GridDto.Builder dto) {
    var hexDto = HexGridDto.newBuilder();
    hexDto.setVertical(!this.isHexHorizontal());
    hexDto.setHexRatio(hexRatio);
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
