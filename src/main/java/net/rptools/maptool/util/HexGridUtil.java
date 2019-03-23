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
package net.rptools.maptool.util;

import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.HexGrid;
import net.rptools.maptool.model.HexGridHorizontal;

/**
 * Provides methods to handle hexgrid issues that don't exist with a square grid.
 *
 * @author Tylere
 */
public class HexGridUtil {
  /**
   * Convert to u-v coordinates where the v-axis points along the direction of edge to edge hexes
   */
  private static int[] toUVCoords(CellPoint cp, HexGrid grid) {
    int cpU, cpV;
    if (grid instanceof HexGridHorizontal) {
      cpU = cp.y;
      cpV = cp.x;
    } else {
      cpU = cp.x;
      cpV = cp.y;
    }
    return new int[] {cpU, cpV};
  }

  /**
   * Convert from u-v coords to grid coords
   *
   * @return the point in grid-space
   */
  private static CellPoint fromUVCoords(int u, int v, HexGrid grid) {
    CellPoint cp = new CellPoint(u, v);
    if (grid instanceof HexGridHorizontal) {
      cp.x = v;
      cp.y = u;
    }
    return cp;
  }

  public static CellPoint getWaypoint(HexGrid grid, CellPoint cp, int width, int height) {
    if (width == height) {
      int[] cpUV = toUVCoords(cp, grid);
      return fromUVCoords(cpUV[0], cpUV[1] + (int) ((width - 1) / 2), grid);
    }
    return cp;
  }
}
