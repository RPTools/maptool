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
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import javax.annotation.Nonnull;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.server.Mapper;
import net.rptools.maptool.server.proto.drawing.DrawableDto;
import net.rptools.maptool.server.proto.drawing.OvalDrawableDto;

/** An oval. */
public class Oval extends Rectangle {
  /**
   * @param x the x offset
   * @param y the y offset
   * @param width the width of the oval
   * @param height the height of the oval
   */
  public Oval(int x, int y, int width, int height) {
    super(x, y, width, height);
  }

  public Oval(GUID id, int x, int y, int width, int height) {
    super(id, x, y, width, height);
  }

  public Oval(Oval other) {
    super(other);
  }

  @Override
  public Drawable copy() {
    return new Oval(this);
  }

  @Override
  protected void draw(Zone zone, Graphics2D g) {
    int minX = Math.min(startPoint.x, endPoint.x);
    int minY = Math.min(startPoint.y, endPoint.y);

    int width = Math.abs(startPoint.x - endPoint.x);
    int height = Math.abs(startPoint.y - endPoint.y);

    g.drawOval(minX, minY, width, height);
  }

  @Override
  protected void drawBackground(Zone zone, Graphics2D g) {
    int minX = Math.min(startPoint.x, endPoint.x);
    int minY = Math.min(startPoint.y, endPoint.y);

    int width = Math.abs(startPoint.x - endPoint.x);
    int height = Math.abs(startPoint.y - endPoint.y);

    g.fillOval(minX, minY, width, height);
  }

  @Override
  public @Nonnull Area getArea(Zone zone) {
    java.awt.Rectangle r = getBounds(zone);
    return new Area(new Ellipse2D.Double(r.x, r.y, r.width, r.height));
  }

  public DrawableDto toDto() {
    var dto =
        OvalDrawableDto.newBuilder()
            .setId(getId().toString())
            .setLayer(getLayer().name())
            .setStartPoint(Mapper.map(getStartPoint()))
            .setEndPoint(Mapper.map(getEndPoint()));

    if (getName() != null) dto.setName(StringValue.of(getName()));

    return DrawableDto.newBuilder().setOvalDrawable(dto).build();
  }

  public static Oval fromDto(OvalDrawableDto dto) {
    var id = GUID.valueOf(dto.getId());
    var startPoint = dto.getStartPoint();
    var endPoint = dto.getEndPoint();
    var drawable =
        new Oval(id, startPoint.getX(), startPoint.getY(), endPoint.getX(), endPoint.getY());
    if (dto.hasName()) {
      drawable.setName(dto.getName().getValue());
    }
    drawable.setLayer(Zone.Layer.valueOf(dto.getLayer()));
    return drawable;
  }
}
