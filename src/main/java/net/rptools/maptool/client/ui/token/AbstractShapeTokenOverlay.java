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
import java.awt.geom.Rectangle2D;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.server.proto.ShapeTokenOverlayDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract sealed class AbstractShapeTokenOverlay extends BooleanTokenOverlay
    permits CrossTokenOverlay,
        DiamondTokenOverlay,
        OTokenOverlay,
        TriangleTokenOverlay,
        YieldTokenOverlay,
        XTokenOverlay {
  private static final Logger log = LogManager.getLogger(AbstractShapeTokenOverlay.class);
  private static final Color DEFAULT_COLOR = Color.RED;
  private static final int DEFAULT_STROKE_WIDTH = 3;

  private static BasicStroke createStroke(float strokeWidth) {
    return new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
  }

  /** Color used when drawing the overlay shape. */
  private Color color;

  /** Stroke used to draw the lines of othe overlay shape. */
  private BasicStroke stroke;

  public AbstractShapeTokenOverlay(String name, Color color, int strokeWidth) {
    super(name);

    if (color == null) {
      color = DEFAULT_COLOR;
    }
    this.color = color;

    if (strokeWidth <= 0) {
      strokeWidth = DEFAULT_STROKE_WIDTH;
    }
    this.stroke = createStroke(strokeWidth);
  }

  public AbstractShapeTokenOverlay(AbstractShapeTokenOverlay other) {
    super(other);
    this.color = other.color;
    this.stroke = other.stroke;
  }

  /**
   * Get the color to use when drawing the overlay's shape.
   *
   * @return Returns the current value of color.
   */
  public final Color getColor() {
    return color;
  }

  /**
   * Set the value of color for the overlay.
   *
   * @param aColor The color to set.
   */
  public final void setColor(Color aColor) {
    color = aColor;
  }

  /**
   * Get the stroke to use when drawing the overlay's shape.
   *
   * @return Returns the current value of stroke.
   */
  public final BasicStroke getStroke() {
    return stroke;
  }

  /**
   * Get the stroke width for the overlay.
   *
   * @return Returns the width of the stroke returned by {@link #getStroke()}.
   */
  public final int getWidth() {
    return (int) stroke.getLineWidth();
  }

  /**
   * Set the stroke width for the overlay.
   *
   * @param strokeWidth The width to set for the stroke.
   */
  public final void setWidth(int strokeWidth) {
    if (strokeWidth <= 0) {
      strokeWidth = DEFAULT_STROKE_WIDTH;
    }
    stroke = createStroke(strokeWidth);
  }

  @Override
  public final void paintOverlay(Graphics2D g, Token token, Rectangle bounds) {
    g.setColor(color);
    g.setStroke(stroke);
    g.draw(getShape(bounds));
  }

  public abstract Shape getShape(Rectangle2D bounds);

  public final ShapeTokenOverlayDto toShapeDto() {
    var dto = ShapeTokenOverlayDto.newBuilder();
    dto.setColor(color.getRGB());
    dto.setStrokeWidth(stroke.getLineWidth());
    dto.setType(
        switch (this) {
          case CrossTokenOverlay ignored -> ShapeTokenOverlayDto.TypeTag.CROSS;
          case DiamondTokenOverlay ignored -> ShapeTokenOverlayDto.TypeTag.DIAMOND;
          case OTokenOverlay ignored -> ShapeTokenOverlayDto.TypeTag.O;
          case TriangleTokenOverlay ignored -> ShapeTokenOverlayDto.TypeTag.TRIANGLE;
          case YieldTokenOverlay ignored -> ShapeTokenOverlayDto.TypeTag.YIELD;
          case XTokenOverlay ignored -> ShapeTokenOverlayDto.TypeTag.X;
        });
    return dto.build();
  }

  public static AbstractShapeTokenOverlay fromDto(ShapeTokenOverlayDto dto) {
    var color = new Color(dto.getColor(), true);
    var strokeWidth = (int) dto.getStrokeWidth();
    var overlay =
        switch (dto.getType()) {
          case X -> new XTokenOverlay(DEFAULT_STATE_NAME, color, strokeWidth);
          case YIELD -> new YieldTokenOverlay(DEFAULT_STATE_NAME, color, strokeWidth);
          case O -> new OTokenOverlay(DEFAULT_STATE_NAME, color, strokeWidth);
          case DIAMOND -> new DiamondTokenOverlay(DEFAULT_STATE_NAME, color, strokeWidth);
          case TRIANGLE -> new TriangleTokenOverlay(DEFAULT_STATE_NAME, color, strokeWidth);
          case CROSS -> new CrossTokenOverlay(DEFAULT_STATE_NAME, color, strokeWidth);
          case UNRECOGNIZED -> null;
        };
    if (overlay == null) {
      log.error("Unrecognized AbstractShapeOverlay type");
      return null;
    }

    return overlay;
  }
}
