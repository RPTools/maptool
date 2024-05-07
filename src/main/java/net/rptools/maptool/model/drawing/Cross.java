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
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import javax.annotation.Nonnull;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.server.Mapper;
import net.rptools.maptool.server.proto.drawing.CrossDrawableDto;
import net.rptools.maptool.server.proto.drawing.DrawableDto;

/** An Cross */
public class Cross extends AbstractDrawing {
  protected Point startPoint;
  protected Point endPoint;
  private transient java.awt.Rectangle bounds;

  public Cross(int startX, int startY, int endX, int endY) {
    startPoint = new Point(startX, startY);
    endPoint = new Point(endX, endY);
  }

  public Cross(GUID id, int startX, int startY, int endX, int endY) {
    super(id);
    startPoint = new Point(startX, startY);
    endPoint = new Point(endX, endY);
  }

  public Cross(Cross other) {
    super(other);

    this.startPoint = new Point(other.startPoint);
    this.endPoint = new Point(other.endPoint);
  }

  @Override
  public Drawable copy() {
    return new Cross(this);
  }

  @Override
  public @Nonnull Area getArea(Zone zone) {
    return new Area(getBounds(zone));
  }

  @Override
  public DrawableDto toDto() {
    var dto =
        CrossDrawableDto.newBuilder()
            .setId(getId().toString())
            .setLayer(getLayer().name())
            .setStartPoint(Mapper.map(getStartPoint()))
            .setEndPoint(Mapper.map(getEndPoint()));

    if (getName() != null) dto.setName(StringValue.of(getName()));

    return DrawableDto.newBuilder().setCrossDrawable(dto).build();
  }

  public static Cross fromDto(CrossDrawableDto dto) {
    var id = GUID.valueOf(dto.getId());
    var startPoint = dto.getStartPoint();
    var endPoint = dto.getEndPoint();
    var drawable =
        new Cross(id, startPoint.getX(), startPoint.getY(), endPoint.getX(), endPoint.getY());
    if (dto.hasName()) {
      drawable.setName(dto.getName().getValue());
    }
    drawable.setLayer(Zone.Layer.valueOf(dto.getLayer()));
    return drawable;
  }

  @Override
  public java.awt.Rectangle getBounds(Zone zone) {
    if (bounds == null) {
      int x = Math.min(startPoint.x, endPoint.x);
      int y = Math.min(startPoint.y, endPoint.y);
      int width = Math.abs(endPoint.x - startPoint.x);
      int height = Math.abs(endPoint.y - startPoint.y);

      bounds = new java.awt.Rectangle(x, y, width, height);
    }

    return bounds;
  }

  public Point getStartPoint() {
    return startPoint;
  }

  public Point getEndPoint() {
    return endPoint;
  }

  @Override
  protected void draw(Zone zone, Graphics2D g) {

    int minX = Math.min(startPoint.x, endPoint.x);
    int minY = Math.min(startPoint.y, endPoint.y);

    int width = Math.abs(startPoint.x - endPoint.x);
    int height = Math.abs(startPoint.y - endPoint.y);

    Object oldAA = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    // g.drawRect(minX, minY, width, height);

    g.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
    g.drawLine(startPoint.x, endPoint.y, endPoint.x, startPoint.y);

    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAA);
  }

  @Override
  protected void drawBackground(Zone zone, Graphics2D g) {
    int minX = Math.min(startPoint.x, endPoint.x);
    int minY = Math.min(startPoint.y, endPoint.y);

    int width = Math.abs(startPoint.x - endPoint.x);
    int height = Math.abs(startPoint.y - endPoint.y);

    Object oldAA = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    g.fillRect(minX, minY, width, height);
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAA);
  }
}
