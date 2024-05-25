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
import java.util.ArrayList;
import java.util.List;
import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.ZonePoint;
import net.rptools.maptool.server.proto.drawing.DrawableDto;
import net.rptools.maptool.server.proto.drawing.WallTemplateDto;

/**
 * A template that draws consecutive blocks
 *
 * @author Jay
 */
public class WallTemplate extends LineTemplate {
  /**
   * Set the path vertex, it isn't needed by the wall template but the superclass needs it to paint.
   */
  public WallTemplate() {
    setPathVertex(new ZonePoint(0, 0));
  }

  public WallTemplate(GUID id) {
    super(id);
    setPathVertex(new ZonePoint(0, 0));
  }

  public WallTemplate(WallTemplate other) {
    super(other);
  }

  @Override
  public Drawable copy() {
    return new WallTemplate(this);
  }

  /**
   * @see net.rptools.maptool.model.drawing.AbstractTemplate#getRadius()
   */
  @Override
  public int getRadius() {
    return getPath() == null ? 0 : getPath().size();
  }

  /**
   * @see net.rptools.maptool.model.drawing.LineTemplate#setRadius(int)
   */
  @Override
  public void setRadius(int squares) {
    // Do nothing, calculated from path length
  }

  /**
   * @see
   *     net.rptools.maptool.model.drawing.LineTemplate#setVertex(net.rptools.maptool.model.ZonePoint)
   */
  @Override
  public void setVertex(ZonePoint vertex) {
    ZonePoint v = getVertex();
    v.x = vertex.x;
    v.y = vertex.y;
  }

  /**
   * @see net.rptools.maptool.model.drawing.LineTemplate#calcPath()
   */
  @Override
  protected List<CellPoint> calcPath() {
    return getPath(); // Do nothing, path is set by tool.
  }

  @Override
  public DrawableDto toDto() {
    var dto = WallTemplateDto.newBuilder();
    dto.setId(getId().toString())
        .setLayer(getLayer().name())
        .setRadius(getRadius())
        .setVertex(getVertex().toDto())
        .setMouseSlopeGreater(isMouseSlopeGreater())
        .setPathVertex(getPathVertex().toDto())
        .setDoubleWide(isDoubleWide());

    if (getName() != null) dto.setName(StringValue.of(getName()));

    for (var point : getPath()) dto.addPoints(point.toDto());

    return DrawableDto.newBuilder().setWallTemplate(dto).build();
  }

  public static WallTemplate fromDto(WallTemplateDto dto) {
    var id = GUID.valueOf(dto.getId());
    var drawable = new WallTemplate(id);
    drawable.setRadius(dto.getRadius());
    var vertex = dto.getVertex();
    drawable.setVertex(new ZonePoint(vertex.getX(), vertex.getY()));
    drawable.setMouseSlopeGreater(dto.getMouseSlopeGreater());
    var pathVertex = dto.getPathVertex();
    drawable.setPathVertex(new ZonePoint(pathVertex.getX(), pathVertex.getY()));
    drawable.setDoubleWide(dto.getDoubleWide());
    if (dto.hasName()) {
      drawable.setName(dto.getName().getValue());
    }
    drawable.setLayer(Zone.Layer.valueOf(dto.getLayer()));

    var cellpoints = new ArrayList<CellPoint>();
    for (var point : dto.getPointsList()) {
      cellpoints.add(new CellPoint(point.getX(), point.getY()));
    }
    drawable.setPath(cellpoints);

    return drawable;
  }
}
