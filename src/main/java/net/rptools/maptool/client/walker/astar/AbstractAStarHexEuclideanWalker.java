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

import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.Zone;

public abstract class AbstractAStarHexEuclideanWalker extends AbstractAStarWalker {
  protected int[][] oddNeighborMap;
  protected int[][] evenNeighborMap;

  public AbstractAStarHexEuclideanWalker(Zone zone) {
    super(zone);
  }

  protected abstract void initNeighborMaps();

  protected abstract int[][] getNeighborMap(int x, int y);

  @Override
  protected double hScore(CellPoint p1, CellPoint p2) {
    return euclideanDistance(p1, p2);
  }

  // Adjusted math per: https://www.redblobgames.com/grids/hexagons/#distances
  private double euclideanDistance(CellPoint current, CellPoint goal) {
    // Using Axial coordinates q & r to match hex coordinate conventions
    int aq = current.x;
    int bq = goal.x;
    int ar = current.y;
    int br = goal.y;

    // break ties to prefer better looking paths that are along the straight line from the starting
    // point to the goal
    int crossProductTieBreaker = Math.abs((aq - bq) * crossY - crossX * (ar - br));
    double heuristic =
        ((Math.abs(aq - bq) + Math.abs(aq + ar - bq - br) + Math.abs(ar - br)) / 2)
            + crossProductTieBreaker * 0.001;

    return heuristic;
  }

  protected double getDiagonalMultiplier(int[] neighborArray) {
    return 1;
  }
}
