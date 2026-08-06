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
import net.rptools.maptool.client.tool.drawing.BlastTemplateTool;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.ZonePoint;
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

  public BlastTemplate(BlastTemplate other) {
    super(other);
    this.offsetX = other.offsetX;
    this.offsetY = other.offsetY;
  }

  /*---------------------------------------------------------------------------------------------
   * Instance Methods
   *-------------------------------------------------------------------------------------------*/

  @Override
  public Drawable copy() {
    return new BlastTemplate(this);
  }

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
    setDirection(findDirection());
  }

  /**
   * Directly set the blast control cell offsets. Useful for applying changes directly to a blast
   * template from a copy which has already used <code>setControlCellRelative</code> to calculate
   * valid offsets.
   */
  public void setControlCellOffset(int controlCellOffsetX, int controlCellOffsetY) {
    offsetX = controlCellOffsetX;
    offsetY = controlCellOffsetY;
    setDirection(findDirection());
  }

  /**
   * Determine the {@link net.rptools.maptool.model.drawing.AbstractTemplate.Direction} of the
   * {@link BlastTemplate} using its radius and offsets. NW/NE/SE/SW need to be exactly at the
   * extremes, otherwise generalised to N/E/S/W.
   *
   * @return the direction of the blast from the center vertex.
   */
  public Direction findDirection() {

    Direction direction;
    int radius = getRadius();

    if (offsetY == -radius) {
      // Blast is above the vertex
      if (offsetX == -radius) {
        direction = Direction.NORTH_WEST;
      } else if (offsetX == 1) {
        direction = Direction.NORTH_EAST;
      } else {
        direction = Direction.NORTH;
      }
    } else if (offsetY == 1) {
      // Blast is below the vertex
      if (offsetX == -radius) {
        direction = Direction.SOUTH_WEST;
      } else if (offsetX == 1) {
        direction = Direction.SOUTH_EAST;
      } else {
        direction = Direction.SOUTH;
      }
    } else if (offsetX == -radius) {
      // Blast is left of the vertex
      direction = Direction.WEST;
    } else if (offsetX == 1) {
      // Blast is right of the vertex
      direction = Direction.EAST;
    } else {
      // should not get here
      return null;
    }

    return direction;
  }

  /**
   * Set the {@link BlastTemplate}'s {@link Direction} and then set the control offsets for that
   * direction. While this is coarser than setting the control offsets explicitly, it provides an
   * alternative and user-friendly way of setting the control offsets when not using the {@link
   * BlastTemplateTool}.
   *
   * @param direction The direction to draw the blast from the center vertex.
   */
  public void setDirectionControlCellOffset(Direction direction) {
    if (direction != null) {
      setDirection(direction);
      int radius = getRadius();
      switch (direction) {
        case Direction.NORTH_WEST -> setControlCellOffset(-radius, -radius);
        case Direction.NORTH -> setControlCellOffset(-radius / 2, -radius);
        case Direction.NORTH_EAST -> setControlCellOffset(1, -radius);
        case Direction.EAST -> setControlCellOffset(1, -radius / 2);
        case Direction.SOUTH_EAST -> setControlCellOffset(1, 1);
        case Direction.SOUTH -> setControlCellOffset(-radius / 2, 1);
        case Direction.SOUTH_WEST -> setControlCellOffset(-radius, 1);
        case Direction.WEST -> setControlCellOffset(-radius, -radius / 2);
        default -> setControlCellOffset(1, 1);
      }
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
        .setRadius(getRadius())
        .setVertex(getVertex().toDto())
        .setDirection(getDirection().name())
        .setOffsetX(getOffsetX())
        .setOffsetY(getOffsetY());

    if (getName() != null) dto.setName(StringValue.of(getName()));

    return DrawableDto.newBuilder().setBlastTemplate(dto).build();
  }

  public static BlastTemplate fromDto(BlastTemplateDto dto) {
    var id = GUID.valueOf(dto.getId());
    var drawable = new BlastTemplate(id, dto.getOffsetX(), dto.getOffsetY());
    drawable.setRadius(dto.getRadius());
    var vertex = dto.getVertex();
    drawable.setVertex(new ZonePoint(vertex.getX(), vertex.getY()));
    drawable.setDirection(AbstractTemplate.Direction.valueOf(dto.getDirection()));
    if (dto.hasName()) {
      drawable.setName(dto.getName().getValue());
    }
    drawable.setLayer(Zone.Layer.valueOf(dto.getLayer()));
    return drawable;
  }
}
