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
// vertical hex = flat top hex
public enum GridType {
  NONE(0, false, false, "None", "NONE", 0f),
  GRIDLESS(1, false, false, "Gridless", "GRIDLESS", 0f),
  HEX_V(2, false, false, "Vertical Hex", "HEX_VERT", 1.1180339887498948482045868343656f),
  HEX_H(3, false, true, "Horizontal Hex", "HEX_HORI", 1.1180339887498948482045868343656f),
  ISOMETRIC_SQUARE(
      4, true, false, "Isometric Square", "ISOMETRIC", 1.4142135623730950488016887242097f),
  ISOMETRIC_HEX(5, true, false, "Isometric Hex", null, 1f),
  ISOMETRIC_PLACEHOLDER(6, true, false, "Isometric Weird", null, 1f),
  SQUARE(7, false, false, "Square", "SQUARE", 1f),
  TRIANGLE(8, false, false, "Triangle", null, 1f),
  UNSET(9, false, false, "Not set", "TYPE_NOT_SET", 0f);
  private final int index;
  private final boolean isometric;
  private final boolean horizontal;
  private final String friendly;
  private final String oldName;
  private final float edgeMultiplier;
  // A regular hexagon is one where all angles are 60 degrees.
  // the ratio = minor_radius / edge_length
  public static final double REGULAR_HEX_RATIO = Math.sqrt(3) / 2;

  GridType(
      int index,
      boolean isometric,
      boolean horizontal,
      String friendly,
      String oldName,
      float edgeMultiplier) {
    this.index = index;
    this.isometric = isometric;
    this.horizontal = horizontal;
    this.friendly = friendly;
    this.oldName = oldName;
    this.edgeMultiplier = edgeMultiplier;
  }

  public int index() {
    return index;
  }

  public boolean isometric() {
    return isometric;
  }

  public boolean horizontal() {
    return horizontal;
  }

  public String oldName() {
    return oldName;
  }

  public String friendly() {
    return friendly;
  }

  public float edgeMultiplier() {
    return edgeMultiplier;
  }
}
