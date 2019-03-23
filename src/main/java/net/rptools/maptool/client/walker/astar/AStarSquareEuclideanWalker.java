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

import java.util.Arrays;
import net.rptools.maptool.client.walker.WalkerMetric;
import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.Zone;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AStarSquareEuclideanWalker extends AbstractAStarWalker {
  private static final Logger log = LogManager.getLogger(AStarSquareEuclideanWalker.class);

  private static final int[] NORTH = {0, -1};
  private static final int[] WEST = {-1, 0};
  private static final int[] SOUTH = {0, 1};
  private static final int[] EAST = {1, 0};
  private static final int[] NORTH_EAST = {1, -1};
  private static final int[] SOUTH_EAST = {1, 1};
  private static final int[] NORTH_WEST = {-1, -1};
  private static final int[] SOUTH_WEST = {-1, 1};

  private final WalkerMetric metric;

  private final int[][] neighborMap;

  private double diagonalMultiplier = 1;

  public AStarSquareEuclideanWalker(Zone zone, WalkerMetric metric) {
    super(zone);

    this.metric = metric;

    // If we exposed this list of coordinates to the user, they could define their own movement
    // criteria, including whether to favor the diagonals or the non-diagonals.
    switch (metric) {
      case NO_DIAGONALS:
        neighborMap = new int[][] {NORTH, EAST, SOUTH, WEST};
        break;
      case ONE_TWO_ONE:
        diagonalMultiplier = 1.5;
        neighborMap =
            new int[][] {NORTH, EAST, SOUTH, WEST, NORTH_EAST, SOUTH_EAST, SOUTH_WEST, NORTH_WEST};
        break;
      case ONE_ONE_ONE:
      case MANHATTAN:
        // promote straight directions to avoid 'only-diagonals' effect
        neighborMap =
            new int[][] {NORTH, EAST, SOUTH, WEST, NORTH_EAST, SOUTH_EAST, SOUTH_WEST, NORTH_WEST};
        break;
      default:
        // promote diagonals over straight directions by putting them at the front of the array
        neighborMap =
            new int[][] {NORTH_EAST, SOUTH_EAST, SOUTH_WEST, NORTH_WEST, NORTH, EAST, SOUTH, WEST};
        break;
    }
  }

  @Override
  protected double getDiagonalMultiplier(int[] neighborArray) {
    if (Arrays.equals(neighborArray, NORTH_EAST)
        || Arrays.equals(neighborArray, SOUTH_EAST)
        || Arrays.equals(neighborArray, SOUTH_WEST)
        || Arrays.equals(neighborArray, NORTH_WEST)) return diagonalMultiplier;
    else return 1;
  }

  private double metricDistance(CellPoint current, CellPoint goal) {
    int xDist = current.x - goal.x;
    int yDist = current.y - goal.y;

    double distance;
    int crossProductTieBreaker = 0;

    switch (metric) {
      case MANHATTAN:
      case NO_DIAGONALS:
        distance = Math.abs(xDist) + Math.abs(yDist);
        break;
      default:
      case ONE_TWO_ONE:
        xDist = Math.abs(current.x - goal.x);
        yDist = Math.abs(current.y - goal.y);
        if (xDist > yDist) {
          distance = Math.floor(diagonalMultiplier * yDist) + (xDist - yDist);
        } else {
          distance = Math.floor(diagonalMultiplier * xDist) + (yDist - xDist);
        }
        break;
      case ONE_ONE_ONE:
        distance = Math.max(Math.abs(xDist), Math.abs(yDist));
        break;
    }

    // break ties to prefer better looking paths that are along the straight line from the
    // starting point to the goal
    if ((goal.x > current.x && goal.y > current.y) || (goal.x < current.x && goal.y < current.y)) {
      crossProductTieBreaker = Math.abs(xDist * crossY - crossX * yDist);
    } else {
      crossProductTieBreaker = Math.abs(xDist * crossY + crossX * yDist);
    }

    return distance += crossProductTieBreaker * 0.001;
  }

  @Override
  public int[][] getNeighborMap(int x, int y) {
    return neighborMap;
  }

  @Override
  protected double hScore(CellPoint p1, CellPoint p2) {
    return metricDistance(p1, p2);
  }
}
