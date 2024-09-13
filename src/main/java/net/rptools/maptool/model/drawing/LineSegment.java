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
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import net.rptools.maptool.model.GUID;
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

  @SuppressWarnings("ConstantValue")
  private Object readResolve() {
    if (width == null) {
      width = 2.f;
    }

    return this;
  }

  /**
   * Manipulate the points by calling {@link #getPoints} and then adding {@link Point} objects to
   * the returned {@link List}.
   *
   * @return the list of point
   */
  public List<Point> getPoints() {
    // This is really, really ugly, but we need to flush the area on any change to the shape
    // and typically the reason for calling this method is to change the list
    area = null;
    return points;
  }

  public Area getArea() {
    if (area == null) {
      area = createLineArea();
    }
    return area;
  }

  @Override
  public DrawableDto toDto() {
    var dto = LineSegmentDrawableDto.newBuilder();
    dto.setId(getId().toString())
        .setLayer(getLayer().name())
        .setWidth(getWidth())
        .setSquareCap(isSquareCap());

    if (getName() != null) dto.setName(StringValue.of(getName()));

    getPoints().forEach(p -> dto.addPoints(Mapper.map(p)));
    return DrawableDto.newBuilder().setLineSegment(dto).build();
  }

  private Area createLineArea() {
    if (points.size() < 1) return null;
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
  protected void draw(Graphics2D g) {
    width = ((BasicStroke) g.getStroke()).getLineWidth();
    squareCap = ((BasicStroke) g.getStroke()).getEndCap() == BasicStroke.CAP_SQUARE;
    Area area = getArea();
    if (area != null) g.fill(area);
  }

  @Override
  protected void drawBackground(Graphics2D g) {
    // do nothing
  }

  /*
   * (non-Javadoc)
   *
   * @see net.rptools.maptool.model.drawing.Drawable#getBounds()
   */
  public Rectangle getBounds() {
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
