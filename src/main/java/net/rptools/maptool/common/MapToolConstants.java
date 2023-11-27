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
package net.rptools.maptool.common;

public class MapToolConstants {
  /**
   * The minimum grid size (minimum on any dimension). The default value is 9 because the algorithm
   * for determining whether a given square cell can be entered due to fog blocking the cell is
   * based on the cell being split into 3x3, then the center further being split into 3x3; thus at
   * least 9 pixels horizontally and vertically are required.
   */
  public static final int MIN_GRID_SIZE = 9;

  public static final int MAX_GRID_SIZE = 700;
  public static final int CIRCLE_SEGMENTS = 60;

  public enum Channel {
    IMAGE
  }
}