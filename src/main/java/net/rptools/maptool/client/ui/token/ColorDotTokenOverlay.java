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
package net.rptools.maptool.client.ui.token;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.Objects;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.drawing.AbstractTemplate.Quadrant;
import net.rptools.maptool.server.proto.ColorDotTokenOverlayDto;
import net.rptools.maptool.server.proto.QuadrantDto;

/**
 * Token overlay that draws a colored dot in one of the corners.
 *
 * @author giliath
 * @version $Revision$ $Date$ $Author$
 */
public final class ColorDotTokenOverlay extends BooleanTokenOverlay {
  /** Color used when filling the dot. */
  private Color color;

  /**
   * @deprecated This is a hold over from when ColorDotTokenOverlay inherited from XTokenOverlay. It
   *     didn't do anything then, and does nothing now.
   */
  @Deprecated private BasicStroke stroke;

  /** The corner where the dot is placed */
  private Quadrant corner;

  /**
   * Create a new dot token overlay
   *
   * @param name Name of the token overlay
   * @param color Color of the dot
   * @param corner Corner containing the dot
   */
  public ColorDotTokenOverlay(String name, Color color, Quadrant corner) {
    super(name);
    this.color = Objects.requireNonNullElse(color, Color.red);
    this.corner = Objects.requireNonNullElse(corner, Quadrant.SOUTH_EAST);
  }

  public ColorDotTokenOverlay(ColorDotTokenOverlay other) {
    super(other);
    this.color = other.color;
    this.corner = other.corner;
  }

  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  /**
   * @return Getter for corner
   */
  public Quadrant getCorner() {
    return corner;
  }

  @Override
  public ColorDotTokenOverlay clone() {
    return new ColorDotTokenOverlay(this);
  }

  public Shape getShape(Rectangle2D bounds) {
    var size = bounds.getWidth() / 5;
    var offset = bounds.getWidth() - size;
    final double x =
        switch (corner) {
          case SOUTH_EAST, NORTH_EAST -> offset;
          case SOUTH_WEST, NORTH_WEST -> 0;
        };
    final double y =
        switch (corner) {
          case SOUTH_EAST, SOUTH_WEST -> offset;
          case NORTH_EAST, NORTH_WEST -> 0;
        };

    return new Ellipse2D.Double(x + bounds.getMinX(), y + bounds.getMinY(), size, size);
  }

  @Override
  public void paintOverlay(Graphics2D g, Token aToken, Rectangle bounds) {
    g.setColor(getColor());
    var shape = getShape(bounds);
    g.fill(shape);
  }

  public ColorDotTokenOverlayDto toColorDotDto() {
    var dto = ColorDotTokenOverlayDto.newBuilder();
    dto.setColor(color.getRGB());
    // Fixes #5828
    dto.setQuadrant(QuadrantDto.valueOf(corner.name()));
    return dto.build();
  }

  public static ColorDotTokenOverlay fromDto(ColorDotTokenOverlayDto dto) {
    var color = new Color(dto.getColor(), true);
    var quadrant = Quadrant.valueOf(dto.getQuadrant().name());
    return new ColorDotTokenOverlay(DEFAULT_STATE_NAME, color, quadrant);
  }
}
