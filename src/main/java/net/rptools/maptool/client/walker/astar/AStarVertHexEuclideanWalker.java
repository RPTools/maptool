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
package net.rptools.maptool.client.walker.astar;

import net.rptools.maptool.model.Zone;

public class AStarVertHexEuclideanWalker extends AbstractAStarHexEuclideanWalker {

  public AStarVertHexEuclideanWalker(Zone zone) {
    super(zone);
    initNeighborMaps();
  }

  @Override
  protected void initNeighborMaps() {
    // @formatter:off
    oddNeighborMap =
        new int[][] {
          {-1, 0, 1}, {0, -1, 1}, {1, 0, 1}, {0, 0, 0}, {0, 0, 0}, {-1, 1, 1}, {0, 1, 1}, {1, 1, 1}
        };

    evenNeighborMap =
        new int[][] {
          {-1, -1, 1},
          {0, -1, 1},
          {1, -1, 1},
          {0, 0, 0},
          {0, 0, 0},
          {-1, 0, 1},
          {0, 1, 1},
          {1, 0, 1}
        };
    // @formatter:on
  }

  @Override
  protected int[][] getNeighborMap(int x, int y) {
    return x % 2 == 0 ? evenNeighborMap : oddNeighborMap;
  }
}
