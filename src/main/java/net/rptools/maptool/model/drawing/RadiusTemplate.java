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
import java.awt.*;
import java.awt.Rectangle;
import java.awt.geom.Area;
import javax.annotation.Nonnull;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.ZonePoint;
import net.rptools.maptool.server.proto.drawing.DrawableDto;
import net.rptools.maptool.server.proto.drawing.RadiusTemplateDto;

/**
 * The radius template draws a highlight over all the squares effected from a specific spine.
 *
 * @author jgorrell
 * @version $Revision: 5945 $ $Date: 2013-06-03 04:35:50 +0930 (Mon, 03 Jun 2013) $ $Author:
 *     azhrei_fje $
 */
public class RadiusTemplate extends AbstractTemplate {

  public RadiusTemplate() {}

  public RadiusTemplate(GUID id) {
    super(id);
  }

  public RadiusTemplate(RadiusTemplate other) {
    super(other);
  }

  @Override
  public Drawable copy() {
    return new RadiusTemplate(this);
  }

  /**
   * Paint the border at a specific radius.
   *
   * @param g Where to paint
   * @param x Distance from vertex along X axis in cell coordinates.
   * @param y Distance from vertex along Y axis in cell coordinates.
   * @param xOff Distance from vertex along X axis in screen coordinates.
   * @param yOff Distance from vertex along Y axis in screen coordinates.
   * @param gridSize The size of one side of the grid in screen coordinates.
   * @param distance The distance in cells from the vertex to the cell which is offset from the
   *     vertex by {@code x & y}.
   * @param radius The radius where the border is painted.
   * @see net.rptools.maptool.model.drawing.AbstractTemplate#paintBorder(java.awt.Graphics2D, int,
   *     int, int, int, int, int)
   */
  protected void paintBorderAtRadius(
      Graphics2D g, int x, int y, int xOff, int yOff, int gridSize, int distance, int radius) {
    // At the border?
    if (distance == radius) {
      // Paint lines between vertical boundaries if needed
      if (getDistance(x + 1, y) > radius) {
        for (Quadrant q : Quadrant.values()) {
          paintFarVerticalBorder(g, xOff, yOff, gridSize, q);
        }
      }

      // Paint lines between horizontal boundaries if needed
      if (getDistance(x, y + 1) > radius) {
        for (Quadrant q : Quadrant.values()) {
          paintFarHorizontalBorder(g, xOff, yOff, gridSize, q);
        }
      }
    }
  }

  /*---------------------------------------------------------------------------------------------
   * Overridden AbstractTemplate Methods
   *-------------------------------------------------------------------------------------------*/

  /**
   * @see net.rptools.maptool.model.drawing.AbstractTemplate#paintBorder(java.awt.Graphics2D, int,
   *     int, int, int, int, int)
   */
  @Override
  protected void paintBorder(
      Graphics2D g, int x, int y, int xOff, int yOff, int gridSize, int distance) {
    paintBorderAtRadius(g, x, y, xOff, yOff, gridSize, distance, getRadius());

    // At the center?
    // FIXME This is wrong because it draws the filled rectangle at CellPoint(0,0) and it should be
    // at the
    // origin of the radius template. Perhaps the transform is missing a call to translate()?
    // if (x == 0 && y == 0)
    // g.fillRect(getVertex().x + xOff - 4, getVertex().y + yOff - 4, 7, 7);
  }

  /**
   * @see net.rptools.maptool.model.drawing.AbstractTemplate#paintArea(java.awt.Graphics2D, int,
   *     int, int, int, int, int)
   */
  @Override
  protected void paintArea(
      Graphics2D g, int x, int y, int xOff, int yOff, int gridSize, int distance) {
    // Only squares w/in the radius
    if (distance <= getRadius()) {
      // Paint the squares
      for (Quadrant q : Quadrant.values()) {
        paintArea(g, xOff, yOff, gridSize, q);
      }
    }
  }

  /*---------------------------------------------------------------------------------------------
   * Drawable Interface Methods
   *-------------------------------------------------------------------------------------------*/

  @Override
  public Rectangle getBounds(Zone zone) {
    if (zone == null) {
      return new Rectangle();
    }
    int gridSize = zone.getGrid().getSize();
    int quadrantSize = getRadius() * gridSize + BOUNDS_PADDING;
    ZonePoint vertex = getVertex();
    return new Rectangle(
        vertex.x - quadrantSize, vertex.y - quadrantSize, quadrantSize * 2, quadrantSize * 2);
  }

  @Override
  public @Nonnull Area getArea(Zone zone) {
    if (zone == null) {
      return new Area();
    }
    int gridSize = zone.getGrid().getSize();
    int r = getRadius();
    ZonePoint vertex = getVertex();
    Area result = new Area();
    for (int x = 0; x < r; x++) {
      for (int y = 0; y < r; y++) {
        if (getDistance(x, y) <= r) {
          int xOff = x * gridSize;
          int yOff = y * gridSize;
          // Add all four quadrants
          for (Quadrant q : Quadrant.values()) {
            int rx = vertex.x + getXMult(q) * xOff + ((getXMult(q) - 1) / 2) * gridSize;
            int ry = vertex.y + getYMult(q) * yOff + ((getYMult(q) - 1) / 2) * gridSize;
            result.add(new Area(new Rectangle(rx, ry, gridSize, gridSize)));
          }
        }
      }
    }
    return result;
  }

  @Override
  public DrawableDto toDto() {
    var dto = RadiusTemplateDto.newBuilder();
    dto.setId(getId().toString())
        .setLayer(getLayer().name())
        .setRadius(getRadius())
        .setVertex(getVertex().toDto());

    if (getName() != null) dto.setName(StringValue.of(getName()));

    return DrawableDto.newBuilder().setRadiusTemplate(dto).build();
  }

  public static RadiusTemplate fromDto(RadiusTemplateDto dto) {
    var id = GUID.valueOf(dto.getId());
    var drawable = new RadiusTemplate(id);
    drawable.setRadius(dto.getRadius());
    var vertex = dto.getVertex();
    drawable.setVertex(new ZonePoint(vertex.getX(), vertex.getY()));
    if (dto.hasName()) {
      drawable.setName(dto.getName().getValue());
    }
    drawable.setLayer(Zone.Layer.valueOf(dto.getLayer()));
    return drawable;
  }
}
