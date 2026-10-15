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
 * <https://www.gnu.org/licenses/> and specifically the Affero license
 * text at <https://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.client.ui.token;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.ImageObserver;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.server.proto.BarTokenOverlayDto;
import net.rptools.maptool.server.proto.BarTokenOverlayDto.SideDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * An overlay that paints a percentage as a bar. The bar can be smooth or it can be cut into fix
 * sized pieces.
 *
 * @author Jay
 */
public abstract class BarTokenOverlay extends AbstractTokenOverlay {

  /*---------------------------------------------------------------------------------------------
   * Instance Variables
   *-------------------------------------------------------------------------------------------*/

  /** The number of increments painted for the bar. */
  private int increments;

  /** The side of the token where the bar is painted. */
  private Side side = Side.TOP;

  /*---------------------------------------------------------------------------------------------
   * Class Variables
   *-------------------------------------------------------------------------------------------*/

  /** Logger instance for this class. */
  private static final Logger LOGGER = LogManager.getLogger(BarTokenOverlay.class);

  /*---------------------------------------------------------------------------------------------
   * Constructors
   *-------------------------------------------------------------------------------------------*/

  /**
   * @param name Name of the new bar.
   */
  public BarTokenOverlay(String name) {
    super(name);
  }

  /*---------------------------------------------------------------------------------------------
   * Instance Methods
   *-------------------------------------------------------------------------------------------*/

  /**
   * @return Getter for increments
   */
  public int getIncrements() {
    return increments;
  }

  /**
   * @param increments Setter for increments
   */
  public void setIncrements(int increments) {
    this.increments = increments;
  }

  /**
   * Calculate the bar size in pixels handling increments if any.
   *
   * @param size The maximum size of the bar
   * @param value The percentage of the bar that is painted.
   * @return The size of the bar to be painted.
   */
  protected int calcBarSize(int size, double value) {
    if (value <= 0) {
      return 0;
    }
    if (value >= 1) {
      return size;
    }

    var increments = getIncrements();
    if (increments <= 0) {
      return (int) Math.ceil(size * value);
    }
    double iSize = 1.0 / (increments - 1);
    return (int) (size * findIncrement(value, increments) * iSize);
  }

  /**
   * Find the increment for the passed value
   *
   * <p>Requires {@code increments > 0}.
   *
   * @param value Find the increment for this value.
   * @return The increment for the value, where the result satisfies {@code 0 <= result && result <
   *     increments}.
   */
  protected static int findIncrement(double value, int increments) {
    if (value <= 0) {
      return 0;
    }
    if (value >= 1) {
      return increments - 1;
    }
    return (int) Math.ceil(value * (increments - 1));
  }

  /**
   * @return Getter for side
   */
  public Side getSide() {
    return side;
  }

  /**
   * @param side Setter for side
   */
  public void setSide(Side side) {
    this.side = side;
  }

  /*---------------------------------------------------------------------------------------------
   * AbstractTokenOverlay Method implementation
   *-------------------------------------------------------------------------------------------*/

  @Override
  public void paintOverlay(
      Graphics2D g, Token token, Rectangle bounds, Object value, ImageObserver observer) {
    if (value == null) return;
    double val = 0;
    if (value instanceof Number) {
      val = ((Number) value).doubleValue();
    } else {
      try {
        val = Double.parseDouble(value.toString());
      } catch (NumberFormatException e) {
        return; // Bad value so don't paint.
      } // endtry
    } // endif
    if (val < 0) val = 0;
    if (val > 1) val = 1;
    paintOverlay(g, token, bounds, val, observer);
  }

  /*---------------------------------------------------------------------------------------------
   * Abstract Methods
   *-------------------------------------------------------------------------------------------*/

  /**
   * Paint the overlay for the passed token.
   *
   * @param g Graphics used to paint. It is already translated so that 0,0 is the upper left corner
   *     of the token. It is also clipped so that the overlay can not draw out of the token's
   *     bounding box.
   * @param token The token being painted.
   * @param bounds The bounds of the actual token. This will be different than the clip since the
   *     clip also has to take into account the edge of the window. If you draw based on the clip it
   *     will be off for partial token painting.
   * @param value A value between 0 and 1 inclusive used to paint the bar.
   * @param observer An image observer to notify as images become available. Used to support
   *     non-blocking rendering for image-based bars.
   */
  protected abstract void paintOverlay(
      Graphics2D g, Token token, Rectangle bounds, double value, ImageObserver observer);

  /*---------------------------------------------------------------------------------------------
   * Side enumeration
   *-------------------------------------------------------------------------------------------*/

  /**
   * The side where the bar is painted.
   *
   * @author Jay
   */
  public enum Side {
    /** Draw the bar at the top of the token */
    TOP,

    /** Draw the bar at the bottom of the token */
    BOTTOM,

    /** Draw the bar on the left side of the token */
    LEFT,

    /** Draw the bar on the right side of the token */
    RIGHT;
  }

  public static BarTokenOverlay fromDto(BarTokenOverlayDto dto) {
    var bar =
        switch (dto.getType()) {
          case DRAWN -> DrawnBarTokenOverlay.fromDtoImpl(dto);
          case MULTIPLE_IMAGE -> MultipleImageBarTokenOverlay.fromDtoImpl(dto);
          case SINGLE_IMAGE -> SingleImageBarTokenOverlay.fromDtoImpl(dto);
          case TWO_IMAGES -> TwoImageBarTokenOverlay.fromDtoImpl(dto);
          case TWO_TONE -> TwoToneBarTokenOverlay.fromDtoImpl(dto);
          case UNRECOGNIZED -> null;
        };
    if (bar != null) {
      bar.fillFrom(dto.getCommon());
      bar.setIncrements(dto.getIncrements());
      bar.setSide(
          switch (dto.getSide()) {
            case TOP, UNRECOGNIZED -> Side.TOP;
            case LEFT -> Side.LEFT;
            case RIGHT -> Side.RIGHT;
            case BOTTOM -> Side.BOTTOM;
          });
    }
    return bar;
  }

  public BarTokenOverlayDto toDto() {
    return toDtoImpl().build();
  }

  protected BarTokenOverlayDto.Builder toDtoImpl() {
    var dto = BarTokenOverlayDto.newBuilder();
    dto.setCommon(getCommonDto());
    dto.setIncrements(getIncrements());
    dto.setSide(
        switch (side) {
          case TOP -> SideDto.TOP;
          case BOTTOM -> SideDto.BOTTOM;
          case LEFT -> SideDto.LEFT;
          case RIGHT -> SideDto.RIGHT;
        });
    return dto;
  }
}
