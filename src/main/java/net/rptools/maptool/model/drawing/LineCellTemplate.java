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
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.ZonePoint;
import net.rptools.maptool.server.proto.drawing.DrawableDto;
import net.rptools.maptool.server.proto.drawing.LineCellTemplateDto;

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
  private ZonePoint pathVertex = null;

  /** The calculated path for this line. */
  private transient List<CellPoint> path;

  /** The pool of points. */
  private transient List<CellPoint> pool;

  /** The line is drawn in this quadrant. */
  private transient Quadrant quadrant = null;

  public LineCellTemplate() {}

  public LineCellTemplate(GUID id) {
    super(id);
  }

  public LineCellTemplate(LineCellTemplate other) {
    super(other);
    this.pathVertex = new ZonePoint(other.pathVertex);
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
    final var path = getPath();
    if (path == null) {
      return;
    }

    for (int i = pElement - 3; i < pElement + 3; i++) {
      if (i < 0 || i >= path.size() || i == pElement) {
        continue;
      }
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
    final var path = getPath();
    if (path == null) {
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

      switch (getQuadrant()) {
        case NORTH_EAST -> {
          yOff = yOff - gridSize;
        }
        case SOUTH_WEST -> {
          xOff = xOff - gridSize;
        }
        case NORTH_WEST -> {
          xOff = xOff - gridSize;
          yOff = yOff - gridSize;
        }
        case SOUTH_EAST -> {
          // Nothing to do.
        }
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
    if (squares == getRadius()) {
      return;
    }
    clearPath();
    super.setRadius(squares);
  }

  /*---------------------------------------------------------------------------------------------
   * Instance Methods
   *-------------------------------------------------------------------------------------------*/

  /**
   * Calculate the path
   *
   * <p>The path is always calculated as if it were in the south-east quadrant. I.e., the x and y
   * coordinates of the path points will never decrease.
   *
   * @return The new path or <code>null</code> if there is no path.
   */
  protected @Nullable List<CellPoint> calcPath() {
    int radius = getRadius();
    ZonePoint vertex = getVertex();

    if (radius == 0 || vertex == null || pathVertex == null) {
      return null;
    }
    // Is there a slope?
    if (vertex.equals(pathVertex)) {
      return null;
    }

    double dx = Math.abs(pathVertex.x - vertex.x);
    double dy = Math.abs(pathVertex.y - vertex.y);
    final boolean isShallowSlope = dx >= dy;

    // To start, a half cell deviation is enough to switch rows.
    double deviationInY = 0.5;

    final var path = new ArrayList<CellPoint>();
    // Start the line at 0,0
    CellPoint p = getPointFromPool(0, 0);
    path.add(p);

    // In this loop we pretend we have a shallow slope. If that's not true, we'll fix it afterward.
    double slope = isShallowSlope ? (dy / dx) : (dx / dy);
    assert slope >= 0;
    while (getDistance(p.x, p.y) < radius) {
      p = getPointFromPool(p.x, p.y);

      // Step to the next column.
      ++p.x;

      // Step to the next row if the ideal line has deviated enough.
      // y-value always goes up, so we don't need to check the < 0 case.
      deviationInY += slope;
      if (deviationInY >= 1) {
        ++p.y;
        deviationInY -= 1;
      }

      path.add(p);
    }

    if (!isShallowSlope) {
      // All our x-values should be y-values and vice versa. So swap them all.
      for (final var point : path) {
        final var tmp = point.x;
        point.x = point.y;
        point.y = tmp;
      }
    }

    // Clear out the last of the pool.
    if (pool != null) {
      pool.clear();
      pool = null;
    }

    return path;
  }

  /**
   * Get a point from the pool or create a new one.
   *
   * @param x The x coordinate of the new point.
   * @param y The y coordinate of the new point.
   * @return The new point.
   */
  private CellPoint getPointFromPool(int x, int y) {
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
   * Get the pathVertex for this LineTemplate.
   *
   * @return Returns the current value of pathVertex.
   */
  public ZonePoint getPathVertex() {
    return pathVertex == null ? null : new ZonePoint(pathVertex);
  }

  /**
   * Set the value of pathVertex for this LineTemplate.
   *
   * @param pathVertex The pathVertex to set.
   */
  public void setPathVertex(ZonePoint pathVertex) {
    if (pathVertex.equals(this.pathVertex)) {
      return;
    }
    clearPath();
    this.pathVertex = pathVertex;
  }

  /** Clear the current path. This will cause it to be recalculated during the next draw. */
  private void clearPath() {
    quadrant = null;
    if (path != null) {
      pool = path;
    }
    path = null;
  }

  /**
   * Get the quadrant for this LineTemplate.
   *
   * @return Returns the current value of quadrant.
   */
  private @Nonnull Quadrant getQuadrant() {
    if (quadrant == null) {
      final var vertex = getVertex();
      if (vertex == null || pathVertex == null || pathVertex.equals(vertex)) {
        // Not a valid line, so quadrant is meaningless. Just pick one.
        quadrant = Quadrant.NORTH_WEST;
      } else {
        double dx = pathVertex.x - vertex.x;
        double dy = pathVertex.y - vertex.y;
        quadrant =
            (dx < 0)
                ? (dy < 0 ? Quadrant.NORTH_WEST : Quadrant.SOUTH_WEST)
                : (dy < 0 ? Quadrant.NORTH_EAST : Quadrant.SOUTH_EAST);
      }
    }

    return quadrant;
  }

  /**
   * @return Getter for path
   */
  private @Nullable List<CellPoint> getPath() {
    if (path == null) {
      path = calcPath();
    }

    return path;
  }

  /*---------------------------------------------------------------------------------------------
   * Drawable Interface Methods
   *-------------------------------------------------------------------------------------------*/

  @Override
  public Drawable copy() {
    return new LineCellTemplate(this);
  }

  @Override
  public Rectangle getBounds(Zone zone) {
    // Get all of the numbers needed for the calculation
    if (zone == null) {
      return new Rectangle();
    }
    // The end of the path is the point further away from vertex.
    final var path = getPath();
    if (path == null) {
      // If the path is null, the line is invalid.
      return new Rectangle();
    }

    var first = new CellPoint(path.getFirst());
    var last = new CellPoint(path.getLast());

    // `first` should be (0, 0), but let's not rely on that.
    final var quadrant = getQuadrant();
    first.x *= getXMult(quadrant);
    last.x *= getXMult(quadrant);
    first.y *= getYMult(quadrant);
    last.y *= getYMult(quadrant);

    // Now convert to zone points.
    ZonePoint firstZonePoint = zone.getGrid().convert(first);
    ZonePoint lastZonePoint = zone.getGrid().convert(last);

    ZonePoint vertex = getVertex();
    int gridSize = zone.getGrid().getSize();
    ZonePoint minZonePoint =
        new ZonePoint(
            vertex.x + Math.min(firstZonePoint.x, lastZonePoint.x),
            vertex.y + Math.min(firstZonePoint.y, lastZonePoint.y));
    ZonePoint maxZonePoint =
        new ZonePoint(
            vertex.x + Math.max(firstZonePoint.x, lastZonePoint.x) + gridSize,
            vertex.y + Math.max(firstZonePoint.y, lastZonePoint.y) + gridSize);

    // Account for pen size
    // We don't really know what the pen size will be, so give a very rough
    // overestimate
    // We'll have to figure this out someday
    minZonePoint.x -= 10;
    minZonePoint.y -= 10;
    maxZonePoint.x += 10;
    maxZonePoint.y += 10;

    return new Rectangle(
        minZonePoint.x,
        minZonePoint.y,
        maxZonePoint.x - minZonePoint.x,
        maxZonePoint.y - minZonePoint.y);
  }

  @Override
  public @Nonnull Area getArea(Zone zone) {
    if (zone == null) {
      return new Area();
    }

    final var path = getPath();
    if (path == null) {
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

      switch (q) {
        case NORTH_EAST -> {
          yOff = yOff - gridSize;
        }
        case SOUTH_WEST -> {
          xOff = xOff - gridSize;
        }
        case NORTH_WEST -> {
          xOff = xOff - gridSize;
          yOff = yOff - gridSize;
        }
        case SOUTH_EAST -> {
          // Nothing to do.
        }
      }

      int rx = getVertex().x + getXMult(q) * xOff + ((getXMult(q) - 1) / 2) * gridSize;
      int ry = getVertex().y + getYMult(q) * yOff + ((getYMult(q) - 1) / 2) * gridSize;
      result.add(new Area(new Rectangle(rx, ry, gridSize, gridSize)));
    }
    return result;
  }

  @Override
  public DrawableDto toDto() {
    var dto = LineCellTemplateDto.newBuilder();
    dto.setId(getId().toString())
        .setLayer(getLayer().name())
        .setRadius(getRadius())
        .setVertex(getVertex().toDto());

    if (pathVertex != null) {
      dto.setPathVertex(pathVertex.toDto());
    }

    if (getName() != null) {
      dto.setName(StringValue.of(getName()));
    }

    return DrawableDto.newBuilder().setLineCellTemplate(dto).build();
  }

  public static LineCellTemplate fromDto(LineCellTemplateDto dto) {
    var id = GUID.valueOf(dto.getId());
    var drawable = new LineCellTemplate(id);
    drawable.setRadius(dto.getRadius());
    var vertex = dto.getVertex();
    drawable.setVertex(new ZonePoint(vertex.getX(), vertex.getY()));
    var pathVertex = dto.getPathVertex();
    drawable.setPathVertex(new ZonePoint(pathVertex.getX(), pathVertex.getY()));
    if (dto.hasName()) {
      drawable.setName(dto.getName().getValue());
    }
    drawable.setLayer(Zone.Layer.valueOf(dto.getLayer()));
    return drawable;
  }
}
