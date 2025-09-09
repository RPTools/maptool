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
package net.rptools.maptool.client.ui.zone.callout;

/** The anchor location for the callout. */
public enum CalloutPopupLocation {
  /** The anchor location is the top left of the callout. */
  TOP_LEFT(0.0, 0.0),
  /** The anchor location is the top middle of the callout. */
  TOP(0.5, 0.0),
  /** The anchor location is the top right of the callout. */
  TOP_RIGHT(1.0, 0.0),
  /** The anchor location is the left of the callout. */
  LEFT(0.0, 0.5),
  /** The anchor location is the center of the callout. */
  CENTER(0.5, 0.5),
  /** The anchor location is the right of the callout. */
  RIGHT(1.0, 0.5),
  /** The anchor location is the bottom left of the callout. */
  BOTTOM_LEFT(0.0, 1.0),
  /** The anchor location is the bottom middle of the callout. */
  BOTTOM(0.5, 1.0),
  /** The anchor location is the bottom right of the callout. */
  BOTTOM_RIGHT(1.0, 1.0);

  static {
    TOP_LEFT.opposite = BOTTOM_RIGHT;
    TOP.opposite = BOTTOM;
    TOP_RIGHT.opposite = BOTTOM_LEFT;
    LEFT.opposite = RIGHT;
    CENTER.opposite = CENTER;
    RIGHT.opposite = LEFT;
    BOTTOM_LEFT.opposite = TOP_RIGHT;
    BOTTOM.opposite = TOP;
    BOTTOM_RIGHT.opposite = TOP_LEFT;
  }

  /** The x offset from the top left in multiples of width for this anchor point. */
  private final double widthMultiplier;

  /** The y offset from the top left in multiples of height for this anchor point. */
  private final double heightMultiplier;

  /** The anchor point that is opposite to this. */
  private CalloutPopupLocation opposite;

  /**
   * Creates a new {@code CalloutPopupLocation}.
   *
   * @param widthMult the x offset from top left in multiples of the width.
   * @param heightMult the y offset from the top left in multiples of the height.
   */
  CalloutPopupLocation(double widthMult, double heightMult) {
    widthMultiplier = widthMult;
    heightMultiplier = heightMult;
  }

  /**
   * Returns the x offset from the top left in multiples of the width.
   *
   * @return the x offset from the top left in multiples of the width.
   */
  public double getWidthMultiplier() {
    return widthMultiplier;
  }

  /**
   * Returns the y offset from the top left in multiples of the height.
   *
   * @return the y offset from the top left in multiples of the height.
   */
  public double getHeightMultiplier() {
    return heightMultiplier;
  }

  /**
   * Returns the anchor point that is opposite to this anchor point.
   *
   * @return the anchor point that is opposite to this anchor point.
   */
  public CalloutPopupLocation getOpposite() {
    return opposite;
  }
}
