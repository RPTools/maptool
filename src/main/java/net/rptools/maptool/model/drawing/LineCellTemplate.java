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

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.ZonePoint;

/**
 * A drawing tool that will draw a line template between 2 vertices.
 *
 * @author naciron
 */
public class LineCellTemplate extends AbstractTemplate {
  /*---------------------------------------------------------------------------------------------
   * Instance Variables
   *-------------------------------------------------------------------------------------------*/

  /** This vertex is used to determine the path. */
  private ZonePoint pathVertex;

  /** The calculated path for this line. */
  private List<CellPoint> path;

  /** The pool of points. */
  private List<CellPoint> pool;

  /**
   * The line is drawn in this quadrant. A string is used as a hack to get around the hessian
   * library's problem w/ serialization of enums
   */
  private String quadrant = null;

  /** Flag used to determine mouse position relative to vertex position */
  private boolean mouseSlopeGreater;

  /*---------------------------------------------------------------------------------------------
   * Overridden AbstractTemplate Methods
   *-------------------------------------------------------------------------------------------*/

  /**
   * @see net.rptools.maptool.model.drawing.AbstractTemplate#paintArea(java.awt.Graphics2D, int,
   *     int, int, int, int, int)
   */
  @Override
  protected void paintArea(
      Graphics2D g, int x, int y, int xOff, int yOff, int gridSize, int distance) {
    paintArea(g, xOff, yOff, gridSize, getQuadrant());
  }

  /**
   * This method is cheating, the distance parameter was replaced with the offset into the path.
   *
   * @see net.rptools.maptool.model.drawing.AbstractTemplate#paintBorder(java.awt.Graphics2D, int,
   *     int, int, int, int, int)
   */
  @Override
  protected void paintBorder(
      Graphics2D g, int x, int y, int xOff, int yOff, int gridSize, int pElement) {
    // Have to scan 3 points behind and ahead, since that is the maximum number of points
    // that can be added to the path from any single intersection.
    boolean[] noPaint = new boolean[4];
    for (int i = pElement - 3; i < pElement + 3; i++) {
      if (i < 0 || i >= path.size() || i == pElement) continue;
      CellPoint p = path.get(i);

      // Ignore diagonal cells and cells that are not adjacent
      int dx = p.x - x;
      int dy = p.y - y;
      if (Math.abs(dx) == Math.abs(dy) || Math.abs(dx) > 1 || Math.abs(dy) > 1) continue;

      // Remove the border between the 2 points
      noPaint[dx != 0 ? (dx < 0 ? 0 : 2) : (dy < 0 ? 3 : 1)] = true;
    } // endif

    // Paint the borders as needed
    if (!noPaint[0]) paintCloseVerticalBorder(g, xOff, yOff, gridSize, getQuadrant());
    if (!noPaint[1]) paintFarHorizontalBorder(g, xOff, yOff, gridSize, getQuadrant());
    if (!noPaint[2]) paintFarVerticalBorder(g, xOff, yOff, gridSize, getQuadrant());
    if (!noPaint[3]) paintCloseHorizontalBorder(g, xOff, yOff, gridSize, getQuadrant());
  }

  /**
   * @see net.rptools.maptool.model.drawing.AbstractTemplate#paint(java.awt.Graphics2D, boolean,
   *     boolean)
   */
  @Override
  protected void paint(Graphics2D g, boolean border, boolean area) {
    if (MapTool.getCampaign().getZone(getZoneId()) == null) {
      return;
    }
    // Need to paint? We need a line and to translate the painting
    if (pathVertex == null) return;
    if (getRadius() == 0) return;
    if (calcPath() == null) return;

    // Paint each element in the path
    int gridSize = MapTool.getCampaign().getZone(getZoneId()).getGrid().getSize();
    ListIterator<CellPoint> i = path.listIterator();
    while (i.hasNext()) {
      CellPoint p = i.next();
      int xOff = p.x * gridSize;
      int yOff = p.y * gridSize;
      int distance = getDistance(p.x, p.y);

      if (quadrant.equals(Quadrant.NORTH_EAST.name())) {
        yOff = yOff - gridSize;
      } else if (quadrant.equals(Quadrant.SOUTH_WEST.name())) {
        xOff = xOff - gridSize;
      } else if (quadrant.equals(Quadrant.NORTH_WEST.name())) {
        xOff = xOff - gridSize;
        yOff = yOff - gridSize;
      }

      // Paint what is needed.
      if (area) {
        paintArea(g, p.x, p.y, xOff, yOff, gridSize, distance);
      } // endif
      if (border) {
        paintBorder(g, p.x, p.y, xOff, yOff, gridSize, i.previousIndex());
      } // endif
    } // endfor
  }

  /** @see net.rptools.maptool.model.drawing.AbstractTemplate#setVertex(ZonePoint) */
  @Override
  public void setVertex(ZonePoint vertex) {
    clearPath();
    super.setVertex(vertex);
  }

  /** @see net.rptools.maptool.model.drawing.AbstractTemplate#setRadius(int) */
  @Override
  public void setRadius(int squares) {
    if (squares == getRadius()) return;
    clearPath();
    super.setRadius(squares);
  }

  /*---------------------------------------------------------------------------------------------
   * Instance Methods
   *-------------------------------------------------------------------------------------------*/

  /**
   * Calculate the path
   *
   * @return The new path or <code>null</code> if there is no path.
   */
  protected List<CellPoint> calcPath() {
    if (getRadius() == 0) return null;
    if (pathVertex == null) return null;
    int radius = getRadius();

    // Is there a slope?
    ZonePoint vertex = getVertex();
    if (vertex.equals(pathVertex)) return null;

    double dx = pathVertex.x - vertex.x;
    double dy = pathVertex.y - vertex.y;
    setQuadrant(
        (dx < 0)
            ? (dy < 0 ? Quadrant.NORTH_WEST : Quadrant.SOUTH_WEST)
            : (dy < 0 ? Quadrant.NORTH_EAST : Quadrant.SOUTH_EAST));

    // Start the line at 0,0
    clearPath();
    path = new ArrayList<CellPoint>();
    path.add(getPointFromPool(0, 0));

    MathContext mc = MathContext.DECIMAL128;
    MathContext rmc = new MathContext(MathContext.DECIMAL64.getPrecision(), RoundingMode.CEILING);
    if (dx != 0 && dy != 0) {
      BigDecimal m = BigDecimal.valueOf(dy).divide(BigDecimal.valueOf(dx), mc).abs();

      // Find the path
      CellPoint p = path.get(path.size() - 1);
      while (getDistance(p.x, p.y) < radius) {
        int x = p.x;
        int y = p.y;

        // Which border does the point exit the cell?
        double xValue = BigDecimal.valueOf(y + 1).divide(m, mc).round(rmc).doubleValue();
        double yValue = BigDecimal.valueOf(x + 1).multiply(m, mc).round(rmc).doubleValue();

        if (xValue == x + 1 && yValue == y + 1) {
          // Special case, right on the diagonal
          path.add(getPointFromPool(x + 1, y + 1));
        } else if (Math.round(xValue) == x + 1) {
          path.add(getPointFromPool(x + 1, y + 1));
        } else if (Math.round(xValue) == x) {
          path.add(getPointFromPool(x, y + 1));
        } else if (Math.round(yValue) == y + 1) {
          path.add(getPointFromPool(x + 1, y + 1));
        } else if (Math.round(yValue) == y) {
          path.add(getPointFromPool(x + 1, y));
        } else {
          return path;
        } // endif
        p = path.get(path.size() - 1);
      } // endwhile

      // Clear the last of the pool
      if (pool != null) {
        pool.clear();
        pool = null;
      } // endif
    } else {
      // Straight line
      int xInc = dx != 0 ? 1 : 0;
      int yInc = dy != 0 ? 1 : 0;
      int x = xInc;
      int y = yInc;
      int xTouch = (dx != 0) ? 0 : -1;
      int yTouch = (dy != 0) ? 0 : -1;
      while (getDistance(x, y) <= radius) {
        path.add(getPointFromPool(x, y));
        x += xInc;
        y += yInc;
      } // endwhile
    } // endif
    return path;
  }

  /**
   * Get a point from the pool or create a new one.
   *
   * @param x The x coordinate of the new point.
   * @param y The y coordinate of the new point.
   * @return The new point.
   */
  public CellPoint getPointFromPool(int x, int y) {
    CellPoint p = null;
    if (pool != null) {
      p = pool.remove(pool.size() - 1);
      if (pool.isEmpty()) pool = null;
    } // endif
    if (p == null) {
      p = new CellPoint(0, 0);
    } // endif
    p.x = x;
    p.y = y;
    return p;
  }

  /**
   * Add a point back to the pool.
   *
   * @param p Add this point back
   */
  public void addPointToPool(CellPoint p) {
    if (pool != null) pool.add(p);
  }

  /**
   * Get the pathVertex for this LineTemplate.
   *
   * @return Returns the current value of pathVertex.
   */
  public ZonePoint getPathVertex() {
    return pathVertex;
  }

  /**
   * Set the value of pathVertex for this LineTemplate.
   *
   * @param pathVertex The pathVertex to set.
   */
  public void setPathVertex(ZonePoint pathVertex) {
    if (pathVertex.equals(this.pathVertex)) return;
    clearPath();
    this.pathVertex = pathVertex;
  }

  /** Clear the current path. This will cause it to be recalculated during the next draw. */
  public void clearPath() {
    if (path != null) pool = path;
    path = null;
  }

  /**
   * Get the quadrant for this LineTemplate.
   *
   * @return Returns the current value of quadrant.
   */
  public Quadrant getQuadrant() {
    if (quadrant != null) return Quadrant.valueOf(quadrant);
    return null;
  }

  /**
   * Set the value of quadrant for this LineTemplate.
   *
   * @param quadrant The quadrant to set.
   */
  public void setQuadrant(Quadrant quadrant) {
    if (quadrant != null) this.quadrant = quadrant.name();
    else this.quadrant = null;
  }

  /**
   * Get the mouseSlopeGreater for this LineTemplate.
   *
   * @return Returns the current value of mouseSlopeGreater.
   */
  public boolean isMouseSlopeGreater() {
    return mouseSlopeGreater;
  }

  /**
   * Set the value of mouseSlopeGreater for this LineTemplate.
   *
   * @param aMouseSlopeGreater The mouseSlopeGreater to set.
   */
  public void setMouseSlopeGreater(boolean aMouseSlopeGreater) {
    mouseSlopeGreater = aMouseSlopeGreater;
  }

  /** @return Getter for path */
  public List<CellPoint> getPath() {
    return path;
  }

  /** @param path Setter for the path to set */
  public void setPath(List<CellPoint> path) {
    this.path = path;
  }

  /*---------------------------------------------------------------------------------------------
   * Drawable Interface Methods
   *-------------------------------------------------------------------------------------------*/

  /** @see net.rptools.maptool.model.drawing.Drawable#getBounds() */
  public Rectangle getBounds() {
    // Get all of the numbers needed for the calculation
    if (MapTool.getCampaign().getZone(getZoneId()) == null) {
      return new Rectangle();
    }
    int gridSize = MapTool.getCampaign().getZone(getZoneId()).getGrid().getSize();
    ZonePoint vertex = getVertex();

    // Find the point that is farthest away in the path, then adjust
    ZonePoint minp = null;
    ZonePoint maxp = null;
    if (path == null) {
      calcPath();
      if (path == null) {
        // If the calculated path is still null, then the line is invalid.
        return new Rectangle();
      }
    }
    for (CellPoint pt : path) {
      ZonePoint p = MapTool.getCampaign().getZone(getZoneId()).getGrid().convert(pt);
      p = new ZonePoint(vertex.x + p.x, vertex.y + p.y);

      if (minp == null) {
        minp = new ZonePoint(p.x, p.y);
        maxp = new ZonePoint(p.x, p.y);
      }
      minp.x = Math.min(minp.x, p.x);
      minp.y = Math.min(minp.y, p.y);

      maxp.x = Math.max(maxp.x, p.x);
      maxp.y = Math.max(maxp.y, p.y);
    }
    maxp.x += gridSize;
    maxp.y += gridSize;

    // The path is only calculated for the south-east quadrant, so
    // appropriately reflect the bounding box around the starting vertex.
    if (getXMult(getQuadrant()) < 0) {
      minp.x -= gridSize;
      maxp.x -= gridSize;
    }
    if (getYMult(getQuadrant()) < 0) {
      minp.y -= gridSize;
      maxp.y -= gridSize;
    }
    int width = (maxp.x - minp.x);
    int height = (maxp.y - minp.y);

    // Account for pen size
    // We don't really know what the pen size will be, so give a very rough
    // overestimate
    // We'll have to figure this out someday
    minp.x -= 10;
    minp.y -= 10;
    width += 20;
    height += 20;

    return new Rectangle(minp.x, minp.y, width, height);
  }

  @Override
  public Area getArea() {
    if (path == null) {
      calcPath();
    }
    Zone zone = MapTool.getCampaign().getZone(getZoneId());
    if (path == null || zone == null || getRadius() == 0 || pathVertex == null) {
      return new Area();
    }
    // Create an area by merging all the squares along the path
    Area result = new Area();
    int gridSize = zone.getGrid().getSize();
    Quadrant q = getQuadrant();

    // Mimic paintArea to get the correct area
    ListIterator<CellPoint> i = path.listIterator();
    while (i.hasNext()) {
      CellPoint p = i.next();
      int xOff = p.x * gridSize;
      int yOff = p.y * gridSize;

      if (quadrant.equals(Quadrant.NORTH_EAST.name())) {
        yOff = yOff - gridSize;
      } else if (quadrant.equals(Quadrant.SOUTH_WEST.name())) {
        xOff = xOff - gridSize;
      } else if (quadrant.equals(Quadrant.NORTH_WEST.name())) {
        xOff = xOff - gridSize;
        yOff = yOff - gridSize;
      }

      int rx = getVertex().x + getXMult(q) * xOff + ((getXMult(q) - 1) / 2) * gridSize;
      int ry = getVertex().y + getYMult(q) * yOff + ((getYMult(q) - 1) / 2) * gridSize;
      result.add(new Area(new Rectangle(rx, ry, gridSize, gridSize)));
    }
    return result;
  }
}
