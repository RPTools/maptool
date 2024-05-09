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

import com.google.protobuf.StringValue;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import javax.annotation.Nonnull;
import net.rptools.maptool.client.AppState;
import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.ZonePoint;
import net.rptools.maptool.server.proto.drawing.DrawableDto;
import net.rptools.maptool.server.proto.drawing.LineTemplateDto;

/**
 * A drawing tool that will draw a line template between 2 vertices.
 *
 * @author jgorrell
 * @version $Revision: 5967 $ $Date: 2013-06-02 15:05:50 -0400 (Sun, 02 Jun 2013) $ $Author:
 *     azhrei_fje $
 */
public class LineTemplate extends AbstractTemplate {
  /*---------------------------------------------------------------------------------------------
   * Instance Variables
   *-------------------------------------------------------------------------------------------*/

  /** Are straight lines drawn double width? */
  private boolean doubleWide = AppState.useDoubleWideLine();

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

  public LineTemplate() {}

  public LineTemplate(GUID id) {
    super(id);
  }

  public LineTemplate(LineTemplate other) {
    super(other);

    this.doubleWide = other.doubleWide;
    this.pathVertex = new ZonePoint(other.pathVertex);

    if (other.path != null) {
      this.path = new ArrayList<>(other.path.size());
      for (final var cellPoint : other.path) {
        this.path.add(new CellPoint(cellPoint));
      }
    }

    if (other.pool != null) {
      this.pool = new ArrayList<>(other.pool.size());
      for (final var cellPoint : other.pool) {
        this.pool.add(new CellPoint(cellPoint));
      }
    }

    this.quadrant = other.quadrant;
    this.mouseSlopeGreater = other.mouseSlopeGreater;
  }

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

  @Override
  protected void paint(Zone zone, Graphics2D g, boolean border, boolean area) {
    if (zone == null) {
      return;
    }
    // Need to paint? We need a line and to translate the painting
    if (pathVertex == null || getRadius() == 0 || calcPath() == null) {
      return;
    }

    // Paint each element in the path
    int gridSize = zone.getGrid().getSize();
    ListIterator<CellPoint> i = path.listIterator();
    while (i.hasNext()) {
      CellPoint p = i.next();
      int xOff = p.x * gridSize;
      int yOff = p.y * gridSize;
      int distance = getDistance(p.x, p.y);

      // Paint what is needed.
      if (area) {
        paintArea(g, p.x, p.y, xOff, yOff, gridSize, distance);
      } // endif
      if (border) {
        paintBorder(g, p.x, p.y, xOff, yOff, gridSize, i.previousIndex());
      } // endif
    } // endfor
  }

  /**
   * @see net.rptools.maptool.model.drawing.AbstractTemplate#setVertex(ZonePoint)
   */
  @Override
  public void setVertex(ZonePoint vertex) {
    clearPath();
    super.setVertex(vertex);
  }

  /**
   * @see net.rptools.maptool.model.drawing.AbstractTemplate#setRadius(int)
   */
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
    int dx = pathVertex.x - vertex.x;
    int dy = pathVertex.y - vertex.y;

    // Start the line at 0,0
    clearPath();
    path = new ArrayList<CellPoint>();
    path.add(getPointFromPool(0, 0));
    MathContext mc = MathContext.DECIMAL128;
    MathContext rmc = new MathContext(MathContext.DECIMAL64.getPrecision(), RoundingMode.DOWN);
    if (dx != 0 && dy != 0) {
      // Calculate quadrant and the slope
      setQuadrant(
          (dx < 0)
              ? (dy < 0 ? Quadrant.NORTH_WEST : Quadrant.SOUTH_WEST)
              : (dy < 0 ? Quadrant.NORTH_EAST : Quadrant.SOUTH_EAST));
      BigDecimal m = BigDecimal.valueOf(dy).divide(BigDecimal.valueOf(dx), mc).abs();

      // Find the path
      CellPoint p = path.get(path.size() - 1);
      while (getDistance(p.x, p.y) <= radius) {
        int x = p.x;
        int y = p.y;

        // Which border does the point exit the cell?
        double xValue = BigDecimal.valueOf(y + 1).divide(m, mc).round(rmc).doubleValue();
        double yValue = BigDecimal.valueOf(x + 1).multiply(m, mc).round(rmc).doubleValue();
        if (xValue == x + 1 && yValue == y + 1) {
          // Special case, right on the diagonal
          if (doubleWide || !mouseSlopeGreater) path.add(getPointFromPool(x + 1, y));
          if (doubleWide || mouseSlopeGreater) path.add(getPointFromPool(x, y + 1));
          path.add(getPointFromPool(x + 1, y + 1));
        } else if (Math.floor(xValue) == x) {
          path.add(getPointFromPool(x, y + 1));
        } else if (Math.floor(yValue) == y) {
          path.add(getPointFromPool(x + 1, y));
        } else {
          // System.err.println("I can't do math: dx=" + dx + " dy=" + dy + " m=" + m + " x=" + x +
          // " xValue=" + xValue + " y=" + y + " yValue=" + yValue);
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
      if (doubleWide) path.add(getPointFromPool(xTouch, yTouch));
      while (getDistance(x, y) <= radius) {
        path.add(getPointFromPool(x, y));
        if (doubleWide) path.add(getPointFromPool(x + xTouch, y + yTouch));
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

  /**
   * Get the doubleWide for this LineTemplate.
   *
   * @return Returns the current value of doubleWide.
   */
  public boolean isDoubleWide() {
    return doubleWide;
  }

  /**
   * Set the value of doubleWide for this LineTemplate.
   *
   * @param aDoubleWide The doubleWide to set.
   */
  public void setDoubleWide(boolean aDoubleWide) {
    doubleWide = aDoubleWide;
  }

  /**
   * @return Getter for path
   */
  public List<CellPoint> getPath() {
    return path;
  }

  /**
   * @param path Setter for the path to set
   */
  public void setPath(List<CellPoint> path) {
    this.path = path;
  }

  /*---------------------------------------------------------------------------------------------
   * Drawable Interface Methods
   *-------------------------------------------------------------------------------------------*/

  @Override
  public Drawable copy() {
    return new LineTemplate(this);
  }

  @Override
  public Rectangle getBounds(Zone zone) {
    // Get all of the numbers needed for the calculation
    if (zone == null) {
      return new Rectangle();
    }
    int gridSize = zone.getGrid().getSize();
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
      ZonePoint p = zone.getGrid().convert(pt);
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
      int tmp;
      tmp = vertex.x - (maxp.x - vertex.x);
      maxp.x = vertex.x - (minp.x - vertex.x);
      minp.x = tmp;
    }
    if (getYMult(getQuadrant()) < 0) {
      int tmp;
      tmp = vertex.y - (maxp.y - vertex.y);
      maxp.y = vertex.y - (minp.y - vertex.y);
      minp.y = tmp;
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
  public @Nonnull Area getArea(Zone zone) {
    if (path == null) {
      calcPath();
    }
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
      int rx = getVertex().x + getXMult(q) * xOff + ((getXMult(q) - 1) / 2) * gridSize;
      int ry = getVertex().y + getYMult(q) * yOff + ((getYMult(q) - 1) / 2) * gridSize;
      result.add(new Area(new Rectangle(rx, ry, gridSize, gridSize)));
    }
    return result;
  }

  @Override
  public DrawableDto toDto() {
    var dto = LineTemplateDto.newBuilder();
    dto.setId(getId().toString())
        .setLayer(getLayer().name())
        .setRadius(getRadius())
        .setVertex(getVertex().toDto())
        .setMouseSlopeGreater(isMouseSlopeGreater())
        .setDoubleWide(isDoubleWide());
    if (getPathVertex() != null) {
      dto.setPathVertex(getPathVertex().toDto());
    }
    if (getQuadrant() != null) {
      dto.setQuadrant(getQuadrant().name());
    }

    if (getName() != null) dto.setName(StringValue.of(getName()));

    return DrawableDto.newBuilder().setLineTemplate(dto).build();
  }

  public static LineTemplate fromDto(LineTemplateDto dto) {
    var id = GUID.valueOf(dto.getId());
    var drawable = new LineTemplate(id);
    drawable.setRadius(dto.getRadius());
    var vertex = dto.getVertex();
    drawable.setVertex(new ZonePoint(vertex.getX(), vertex.getY()));
    if (!dto.getQuadrant().isEmpty()) {
      drawable.setQuadrant(AbstractTemplate.Quadrant.valueOf(dto.getQuadrant()));
    }
    drawable.setMouseSlopeGreater(dto.getMouseSlopeGreater());
    var pathVertex = dto.getPathVertex();
    drawable.setPathVertex(new ZonePoint(pathVertex.getX(), pathVertex.getY()));
    drawable.setDoubleWide(dto.getDoubleWide());
    if (dto.hasName()) {
      drawable.setName(dto.getName().getValue());
    }
    drawable.setLayer(Zone.Layer.valueOf(dto.getLayer()));
    return drawable;
  }
}
