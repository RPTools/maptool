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

public enum CalloutPopupLocation {
  TOP_LEFT(0.0, 0.0),
  TOP(0.5, 0.0),
  TOP_RIGHT(1.0, 0.0),
  LEFT(0.0, 0.5),
  CENTER(0.5, 0.5),
  RIGHT(1.0, 0.5),
  BOTTOM_LEFT(0.0, 1.0),
  BOTTOM(0.5, 1.0),
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

  private final double widthMultiplier;
  private final double heightMultiplier;
  private CalloutPopupLocation opposite;

  CalloutPopupLocation(double widthMult, double heightMult) {
    widthMultiplier = widthMult;
    heightMultiplier = heightMult;
  }

  public double getWidthMultiplier() {
    return widthMultiplier;
  }

  public double getHeightMultiplier() {
    return heightMultiplier;
  }

  public CalloutPopupLocation getOpposite() {
    return opposite;
  }
}
