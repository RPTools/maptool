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
import java.awt.RenderingHints;
import java.awt.geom.Area;
import javax.annotation.Nonnull;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.server.proto.drawing.BlastTemplateDto;
import net.rptools.maptool.server.proto.drawing.DrawableDto;

/**
 * The blast template draws a square for DnD 4e
 *
 * @author jgorrell
 * @version $Revision: $ $Date: $ $Author: $
 */
public class BlastTemplate extends ConeTemplate {

  /*---------------------------------------------------------------------------------------------
   * Instance Variables
   *-------------------------------------------------------------------------------------------*/

  private int offsetX;
  private int offsetY;

  public BlastTemplate() {}

  public BlastTemplate(GUID id, int offsetX, int offsetY) {
    super(id);
    this.offsetX = offsetX;
    this.offsetY = offsetY;
  }

  /*---------------------------------------------------------------------------------------------
   * Instance Methods
   *-------------------------------------------------------------------------------------------*/

  private Rectangle makeShape(Zone zone) {
    if (zone == null) {
      return new Rectangle();
    }

    int gridSize = zone.getGrid().getSize();
    int size = getRadius() * gridSize;

    return new Rectangle(
        getVertex().x + offsetX * gridSize, getVertex().y + offsetY * gridSize, size, size);
  }

  /*---------------------------------------------------------------------------------------------
   * Overridden *Template Methods
   *-------------------------------------------------------------------------------------------*/

  @Override
  public Rectangle getBounds(Zone zone) {
    Rectangle r = makeShape(zone);
    // We don't know pen width, so add some padding to account for it
    r.x -= 5;
    r.y -= 5;
    r.width += 10;
    r.height += 10;
    return r;
  }

  /**
   * Defines the blast based on the specified square
   *
   * @param relX The X coordinate of the control square relative to the origin square
   * @param relY The Y coordinate of the control square relative to the origin square
   */
  public void setControlCellRelative(int relX, int relY) {

    relX = Math.max(Math.min(relX, MAX_RADIUS), -MAX_RADIUS);
    relY = Math.max(Math.min(relY, MAX_RADIUS), -MAX_RADIUS);

    int radius = Math.max(Math.abs(relX), Math.abs(relY));
    // Number of cells along axis of smaller offset we need to shift the square in order to "center"
    // the blast
    int centerOffset = -(radius / 2);
    // Smallest delta we can apply to centerOffset and still have valid placement
    int lowerBound = -((radius + 1) / 2);
    // Largest delta we can apply to centerOffset and still have valid placement
    int upperBound = (radius / 2) + 1;

    setRadius(radius);
    // The larger magnitude offset determines size and gross positioning, the smaller determines
    // fine positioning
    if (Math.abs(relX) > Math.abs(relY)) {
      if (relX > 0) {
        offsetX = 1;
      } else {
        offsetX = -radius;
      }
      offsetY = centerOffset + Math.min(Math.max(lowerBound, relY), upperBound);
    } else {
      if (relY > 0) {
        offsetY = 1;
      } else {
        offsetY = -radius;
      }
      offsetX = centerOffset + Math.min(Math.max(lowerBound, relX), upperBound);
    }
  }

  /**
   * @see net.rptools.maptool.model.drawing.AbstractTemplate#getDistance(int, int)
   */
  @Override
  public int getDistance(int x, int y) {
    return Math.max(x, y);
  }

  /*---------------------------------------------------------------------------------------------
   * Overridden AbstractDrawing Methods
   *-------------------------------------------------------------------------------------------*/

  @Override
  protected void paint(Zone zone, Graphics2D g, boolean border, boolean area) {
    Object oldAA = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
    try {
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      var shape = makeShape(zone);
      if (area) {
        g.fill(shape);
      }
      if (border) {
        g.draw(shape);
      }
    } finally {
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAA);
    }
  }

  @Override
  public @Nonnull Area getArea(Zone zone) {
    return new Area(makeShape(zone));
  }

  public int getOffsetX() {
    return offsetX;
  }

  public int getOffsetY() {
    return offsetY;
  }

  @Override
  public DrawableDto toDto() {
    var dto = BlastTemplateDto.newBuilder();
    dto.setId(getId().toString())
        .setLayer(getLayer().name())
        .setZoneId(getZoneId().toString())
        .setRadius(getRadius())
        .setVertex(getVertex().toDto())
        .setDirection(getDirection().name())
        .setOffsetX(getOffsetX())
        .setOffsetY(getOffsetY());

    if (getName() != null) dto.setName(StringValue.of(getName()));

    return DrawableDto.newBuilder().setBlastTemplate(dto).build();
  }
}
