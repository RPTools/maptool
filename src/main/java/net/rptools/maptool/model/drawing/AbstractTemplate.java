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
package net.rptools.maptool.model.drawing;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.ZonePoint;

/**
 * Base class for the radius, line, and cone templates.
 *
 * @author jgorrell
 * @version $Revision: 5945 $ $Date: 2013-06-03 04:35:50 +0930 (Mon, 03 Jun 2013) $ $Author:
 *     azhrei_fje $
 */
public abstract class AbstractTemplate extends AbstractDrawing {
  /*---------------------------------------------------------------------------------------------
   * Instance Variables
   *-------------------------------------------------------------------------------------------*/

  /** The current width of this template in squares. */
  private int radius;

  /** The location of the vertex where painting starts. */
  private ZonePoint vertex = new ZonePoint(0, 0);

  /** The id of the zone where this drawable is painted. */
  private GUID zoneId;

  /*---------------------------------------------------------------------------------------------
   * Class Variables
   *-------------------------------------------------------------------------------------------*/

  /** Maximum radius value allowed. */
  public static final int MAX_RADIUS = 30;

  /** Minimum radius value allowed. */
  public static final int MIN_RADIUS = 1;

  /** Extra padding added to insure the wide lines do not get clipped. */
  public static final int BOUNDS_PADDING = 10;

  /** The alpha forced on all background fills. */
  public static final float DEFAULT_BG_ALPHA = 0.20f;

  /** The directions that can be drawn. All is for a radius and the other values are for cones. */
  public static enum Direction {
    /** Draw a Radius */
    ALL,

    // Draw a cone in the indicated direction. Order is important!
    /** Draw a cone directly to the west (left) of the selection point. */
    WEST,
    /** Draw a cone directly to the north west (upper left quadrant) of the selection point. */
    NORTH_WEST,
    /** Draw a cone directly to the north (up) of the selection point. */
    NORTH,
    /** Draw a cone directly to the north east (upper right quadrant) of the selection point. */
    NORTH_EAST,
    /** Draw a cone directly to the east (right) of the selection point. */
    EAST,
    /** Draw a cone directly to the south east (lower right quadrant) of the selection point. */
    SOUTH_EAST,
    /** Draw a cone directly to the south (down) of the selection point. */
    SOUTH,
    /** Draw a cone directly to the south west (lower left quadrant) of the selection point. */
    SOUTH_WEST;

    /**
     * Find the direction to draw a cone from two points. The first point would be the mouse
     * location and the second would be the vertex of the cone.
     *
     * @param x1 Mouse X coordinate.
     * @param y1 Mouse Y coordinate.
     * @param x2 Vertex X coordinate.
     * @param y2 Vertex Y coordinate.
     * @return The direction from the vertex (point 2) to the mouse (point 1).
     */
    public static Direction findDirection(int x1, int y1, int x2, int y2) {
      double dX = x1 - x2;
      double dY = y1 - y2;
      double angle = Math.atan2(dY, dX);
      int value = (int) Math.floor(((angle / Math.PI + 1.0) / 2.0) * 16.0);
      if (value >= 15) value = 0;
      return values()[((value + 1) / 2) + 1];
    }
  }

  /** The quadrants for drawing. */
  public static enum Quadrant {
    /** Draw in the north east (upper right) quadrant. */
    NORTH_EAST,
    /** Draw in the north west (upper left) quadrant. */
    NORTH_WEST,
    /** Draw in the south east (lower right) quadrant. */
    SOUTH_EAST,
    /** Draw in the south west (lower left) quadrant. */
    SOUTH_WEST
  }

  /*---------------------------------------------------------------------------------------------
   * Instance Methods
   *-------------------------------------------------------------------------------------------*/

  /**
   * Set the radius of the template in squares.
   *
   * @param squares The number of squares in the radius for this template.
   */
  public void setRadius(int squares) {
    if (squares > MAX_RADIUS) squares = MAX_RADIUS;
    radius = squares;
  }

  /**
   * Get the radius for this RadiusTemplate.
   *
   * @return Returns the current value of radius in squares.
   */
  public int getRadius() {
    return radius;
  }

  /**
   * Get the vertex for this RadiusTemplate.
   *
   * @return Returns the current value of vertex.
   */
  public ZonePoint getVertex() {
    return vertex;
  }

  /**
   * Set the value of vertex for this RadiusTemplate.
   *
   * @param vertex The vertex to set.
   */
  public void setVertex(ZonePoint vertex) {
    this.vertex = vertex;
  }

  /**
   * Get the zoneId for this RadiusTemplate.
   *
   * @return Returns the current value of zoneId.
   */
  public GUID getZoneId() {
    return zoneId;
  }

  /**
   * Set the value of zoneId for this RadiusTemplate.
   *
   * @param zoneId The zoneId to set.
   */
  public void setZoneId(GUID zoneId) {
    this.zoneId = zoneId;
  }

  /**
   * Paint the border or area of the template
   *
   * @param g Where to paint
   * @param border Paint the border?
   * @param area Paint the area?
   */
  protected void paint(Graphics2D g, boolean border, boolean area) {
    if (radius == 0) return;
    Zone zone = MapTool.getCampaign().getZone(zoneId);
    if (zone == null) return;

    // Find the proper distance
    int gridSize = zone.getGrid().getSize();
    for (int y = 0; y < radius; y++) {
      for (int x = 0; x < radius; x++) {

        // Get the offset to the corner of the square
        int xOff = x * gridSize;
        int yOff = y * gridSize;

        // Template specific painting
        if (border) paintBorder(g, x, y, xOff, yOff, gridSize, getDistance(x, y));
        if (area) paintArea(g, x, y, xOff, yOff, gridSize, getDistance(x, y));
      } // endfor
    } // endfor
  }

  /**
   * Paint the close horizontal line of a cell's border. All directions are relevant to the vertex.
   *
   * @param g The painter.
   * @param xOff X Offset to cell from vertex in screen coordinates.
   * @param yOff Y Offset to cell from vertex in screen coordinates.
   * @param gridSize Size of a cell in screen coordinates.
   * @param q The quadrant the cell is in relative to the vertex.
   */
  protected void paintCloseHorizontalBorder(
      Graphics2D g, int xOff, int yOff, int gridSize, Quadrant q) {
    int x = vertex.x + getXMult(q) * xOff;
    int y = vertex.y + getYMult(q) * yOff;
    Line2D l = new Line2D.Double(x, y, x + getXMult(q) * gridSize, y);
    g.draw(l);
  }

  /**
   * Paint the close vertical line of a cell's border. All directions are relevant to the vertex.
   *
   * @param g The painter.
   * @param xOff X Offset to cell from vertex in screen coordinates.
   * @param yOff Y Offset to cell from vertex in screen coordinates.
   * @param gridSize Size of a cell in screen coordinates.
   * @param q The quadrant the cell is in relative to the vertex.
   */
  protected void paintCloseVerticalBorder(
      Graphics2D g, int xOff, int yOff, int gridSize, Quadrant q) {
    int x = vertex.x + getXMult(q) * xOff;
    int y = vertex.y + getYMult(q) * yOff;
    Line2D l = new Line2D.Double(x, y, x, y + getYMult(q) * gridSize);
    g.draw(l);
  }

  /**
   * Fill the area of a cell.
   *
   * @param g The painter.
   * @param xOff X Offset to cell from vertex in screen coordinates.
   * @param yOff Y Offset to cell from vertex in screen coordinates.
   * @param gridSize Size of a cell in screen coordinates.
   * @param q The quadrant the cell is in relative to the vertex.
   */
  protected void paintArea(Graphics2D g, int xOff, int yOff, int gridSize, Quadrant q) {
    int x = vertex.x + getXMult(q) * xOff + ((getXMult(q) - 1) / 2) * gridSize;
    int y = vertex.y + getYMult(q) * yOff + ((getYMult(q) - 1) / 2) * gridSize;
    g.fill(new Rectangle(x, y, gridSize, gridSize));
  }

  /**
   * Paint the far horizontal line of a cell's border. All directions are relevant to the vertex.
   *
   * @param g The painter.
   * @param xOff X Offset to cell from vertex in screen coordinates.
   * @param yOff Y Offset to cell from vertex in screen coordinates.
   * @param gridSize Size of a cell in screen coordinates.
   * @param q The quadrant the cell is in relative to the vertex.
   */
  protected void paintFarHorizontalBorder(
      Graphics2D g, int xOff, int yOff, int gridSize, Quadrant q) {
    int x = vertex.x + getXMult(q) * xOff;
    int y = vertex.y + getYMult(q) * yOff + getYMult(q) * gridSize;
    Line2D l = new Line2D.Double(x, y, x + getXMult(q) * gridSize, y);
    g.draw(l);
  }

  /**
   * Paint the far vertical line of a cell's border. All directions are relevant to the vertex.
   *
   * @param g The painter.
   * @param xOff X Offset to cell from vertex in screen coordinates.
   * @param yOff Y Offset to cell from vertex in screen coordinates.
   * @param gridSize Size of a cell in screen coordinates.
   * @param q The quadrant the cell is in relative to the vertex.
   */
  protected void paintFarVerticalBorder(
      Graphics2D g, int xOff, int yOff, int gridSize, Quadrant q) {
    int x = vertex.x + getXMult(q) * xOff + getXMult(q) * gridSize;
    int y = vertex.y + getYMult(q) * yOff;
    Line2D l = new Line2D.Double(x, y, x, y + getYMult(q) * gridSize);
    g.draw(l);
  }

  /**
   * Get the multiplier in the X direction.
   *
   * @param q Quadrant being accessed
   * @return -1 for west and +1 for east
   */
  protected int getXMult(Quadrant q) {
    return ((q == Quadrant.NORTH_WEST || q == Quadrant.SOUTH_WEST) ? -1 : +1);
  }

  /**
   * Get the multiplier in the X direction.
   *
   * @param q Quadrant being accessed
   * @return -1 for north and +1 for south
   */
  protected int getYMult(Quadrant q) {
    return ((q == Quadrant.NORTH_WEST || q == Quadrant.NORTH_EAST) ? -1 : +1);
  }

  /**
   * Get the distance to a specific coordinate.
   *
   * @param x delta-X of the coordinate.
   * @param y delta-Y of the coordinate.
   * @return Number of cells to the passed coordinate.
   */
  public int getDistance(int x, int y) {
    if (x > y) return x + (y / 2) + 1 + (y & 1);
    return y + (x / 2) + 1 + (x & 1);
  }

  /*---------------------------------------------------------------------------------------------
   * Overridden AbstractDrawing Methods
   *-------------------------------------------------------------------------------------------*/

  /** @see net.rptools.maptool.model.drawing.AbstractDrawing#draw(java.awt.Graphics2D) */
  @Override
  protected void draw(Graphics2D g) {
    paint(g, true, false);
  }

  /** @see net.rptools.maptool.model.drawing.AbstractDrawing#drawBackground(java.awt.Graphics2D) */
  @Override
  protected void drawBackground(Graphics2D g) {

    // Adjust alpha automatically
    Composite old = g.getComposite();
    if (old != AlphaComposite.Clear)
      g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, DEFAULT_BG_ALPHA));
    paint(g, false, true);
    g.setComposite(old);
  }

  /*---------------------------------------------------------------------------------------------
   * Abstract Methods
   *-------------------------------------------------------------------------------------------*/

  /**
   * Paint the border of the template. Note that all coordinates are for the south east quadrant,
   * just change the signs of the x/y and xOff/yOff offsets to get to the other quadrants.
   *
   * @param g Where to paint
   * @param x Distance from vertex along X axis in cell coordinates.
   * @param y Distance from vertex along Y axis in cell coordinates.
   * @param xOff Distance from vertex along X axis in screen coordinates.
   * @param yOff Distance from vertex along Y axis in screen coordinates.
   * @param gridSize The size of one side of the grid in screen coordinates.
   * @param distance The distance in cells from the vertex to the cell which is offset from the
   *     vertex by <code>x</code> & <code>y</code>.
   */
  protected abstract void paintBorder(
      Graphics2D g, int x, int y, int xOff, int yOff, int gridSize, int distance);

  /**
   * Paint the border of the template. Note that all coordinates are for the south east quadrant,
   * just change the signs of the x/y and xOff/yOff offsets to get to the other quadrants.
   *
   * @param g Where to paint
   * @param x Distance from vertex along X axis in cell coordinates.
   * @param y Distance from vertex along Y axis in cell coordinates.
   * @param xOff Distance from vertex along X axis in screen coordinates.
   * @param yOff Distance from vertex along Y axis in screen coordinates.
   * @param gridSize The size of one side of the grid in screen coordinates.
   * @param distance The distance in cells from the vertex to the cell which is offset from the
   *     vertex by <code>x</code> & <code>y</code>.
   */
  protected abstract void paintArea(
      Graphics2D g, int x, int y, int xOff, int yOff, int gridSize, int distance);
}
