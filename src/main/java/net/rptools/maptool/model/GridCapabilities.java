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
package net.rptools.maptool.model;

public interface GridCapabilities {
  /**
   * Whether the parent grid type supports snap-to-grid. Some may not, such as the Gridless grid
   * type.
   *
   * @return
   */
  public boolean isSnapToGridSupported();

  /**
   * Whether the parent grid type supports automatic pathing from point A to point B. Usually true
   * except for the Gridless grid type.
   *
   * @return
   */
  public boolean isPathingSupported();

  /**
   * Whether ...
   *
   * @return
   */
  public boolean isPathLineSupported();

  /**
   * Whether the parent grid supports the concept of coordinates to be placed on the grid. Generally
   * this requires a grid type that has some notion of "cell size", which means Gridless need not
   * apply. ;-)
   *
   * @return
   */
  public boolean isCoordinatesSupported();

  /**
   * The secondary dimension should be linked to changes in the primary dimension but the primary
   * dimension is independent of the secondary.
   */
  public boolean isSecondDimensionAdjustmentSupported();
}
