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
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.server.Mapper;
import net.rptools.maptool.server.proto.drawing.DrawableDto;
import net.rptools.maptool.server.proto.drawing.LineSegmentDrawableDto;

/**
 * @author drice
 */
public class LineSegment extends AbstractDrawing {
  private final List<Point> points = new ArrayList<Point>();
  private @Nonnull Float width;
  private boolean squareCap;
  private transient int lastPointCount = -1;
  private transient Rectangle cachedBounds;
  private transient Area area;

  public LineSegment(float width, boolean squareCap) {
    this.width = width;
    this.squareCap = squareCap;
  }

  public LineSegment(GUID id, float width, boolean squareCap) {
    super(id);
    this.width = width;
    this.squareCap = squareCap;
  }

  public LineSegment(LineSegment other) {
    super(other);
    this.width = other.width;
    this.squareCap = other.squareCap;

    for (final var point : other.points) {
      this.points.add(new Point(point));
    }
  }

  public LineSegment(float width, boolean squareCap, Path2D path) {
    this.width = width;
    this.squareCap = squareCap;

    // Assume an unbroken line string.
    double[] coordinates = new double[6];
    for (var it = path.getPathIterator(null); !it.isDone(); it.next()) {
      var type = it.currentSegment(coordinates);
      // type will be SEG_MOVETO or SEG_LINETO or SEG_CLOSE. The first two mean the same to
      // us, while the latter ends the line.
      if (type == PathIterator.SEG_CLOSE) {
        // Should not be possible to be empty, but can't hurt to check.
        if (!points.isEmpty()) {
          addPoint(points.getFirst());
        }
        break;
      } else {
        addPoint((int) coordinates[0], (int) coordinates[1]);
      }
    }
  }

  @Override
  public Drawable copy() {
    return new LineSegment(this);
  }

  @SuppressWarnings("ConstantValue")
  private Object readResolve() {
    if (width == null) {
      width = 2.f;
    }

    return this;
  }

  /**
   * Add a point to the line.
   *
   * @param x
   * @param y
   */
  public void addPoint(int x, int y) {
    area = null;
    points.add(new Point(x, y));
  }

  /**
   * Add a point to the line.
   *
   * @param point
   * @see #addPoint(int, int)
   */
  public void addPoint(Point point) {
    addPoint(point.x, point.y);
  }

  /**
   * @return the list of point
   */
  public List<Point> getPoints() {
    return Collections.unmodifiableList(points);
  }

  @Override
  public @Nonnull Area getArea(Zone zone) {
    if (area == null) {
      area = createLineArea();
    }
    return Objects.requireNonNullElseGet(area, Area::new);
  }

  @Override
  public DrawableDto toDto() {
    var dto = LineSegmentDrawableDto.newBuilder();
    dto.setId(getId().toString())
        .setLayer(getLayer().name())
        .setWidth(getWidth())
        .setSquareCap(isSquareCap());

    if (getName() != null) dto.setName(StringValue.of(getName()));

    points.forEach(p -> dto.addPoints(Mapper.map(p)));
    return DrawableDto.newBuilder().setLineSegment(dto).build();
  }

  public static LineSegment fromDto(LineSegmentDrawableDto dto) {
    var id = GUID.valueOf(dto.getId());
    var drawable = new LineSegment(id, dto.getWidth(), dto.getSquareCap());
    var points = drawable.points;
    var pointDtos = dto.getPointsList();
    pointDtos.forEach(p -> points.add(Mapper.map(p)));
    if (dto.hasName()) {
      drawable.setName(dto.getName().getValue());
    }
    drawable.setLayer(Zone.Layer.valueOf(dto.getLayer()));
    return drawable;
  }

  private Area createLineArea() {
    if (points.size() < 1) {
      return null;
    }
    GeneralPath gp = null;
    for (Point point : points) {
      if (gp == null) {
        gp = new GeneralPath();
        gp.moveTo(point.x, point.y);
        continue;
      }
      gp.lineTo(point.x, point.y);
    }
    BasicStroke stroke = new BasicStroke(width, getStrokeCap(), getStrokeJoin());
    return new Area(stroke.createStrokedShape(gp));
  }

  @Override
  protected void draw(Zone zone, Graphics2D g) {
    width = ((BasicStroke) g.getStroke()).getLineWidth();
    squareCap = ((BasicStroke) g.getStroke()).getEndCap() == BasicStroke.CAP_SQUARE;
    Area area = getArea(zone);
    g.fill(area);
  }

  @Override
  protected void drawBackground(Zone zone, Graphics2D g) {
    // do nothing
  }

  @Override
  public Rectangle getBounds(Zone zone) {
    if (lastPointCount == points.size()) {
      return cachedBounds;
    }
    if (points.size() < 1) return null;
    Rectangle bounds = new Rectangle(points.get(0));
    for (Point point : points) {
      bounds.add(point);
    }

    // Special casing
    if (bounds.width < 1) {
      bounds.width = 1;
    }
    if (bounds.height < 1) {
      bounds.height = 1;
    }
    cachedBounds = bounds;
    lastPointCount = points.size();
    return bounds;
  }

  public float getWidth() {
    return width;
  }

  public boolean isSquareCap() {
    return squareCap;
  }

  public int getStrokeCap() {
    if (squareCap) return BasicStroke.CAP_SQUARE;
    else return BasicStroke.CAP_ROUND;
  }

  public int getStrokeJoin() {
    if (squareCap) return BasicStroke.JOIN_MITER;
    else return BasicStroke.JOIN_ROUND;
  }
}
