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

import java.util.Objects;
import net.rptools.maptool.model.CellPoint;

public class AStarCellPoint {
  final CellPoint position;
  final boolean isOddStepOfOneTwoOneMovement;

  AStarCellPoint parent;
  double g;
  double h;

  /**
   * Create an A* node from coordinates.
   *
   * <p>The node will have no scores or data carried over from a cell point. This is useful for
   * creating brand new nodes, such as for neighbors of existing cells.
   *
   * @param x The x cell position of the node.
   * @param y The y cell position of the node.
   * @param isOddStepOfOneTwoOneMovement If the movement is 1-2-1 and an odd path was taken so far.
   */
  public AStarCellPoint(int x, int y, boolean isOddStepOfOneTwoOneMovement) {
    this(new CellPoint(x, y), isOddStepOfOneTwoOneMovement);
  }

  /**
   * Create an A* node from a cell point.
   *
   * <p>The distance travelled is copied from the cell point. This is usefull for creating nodes
   * that need to carry over data, such as after a waypoint.
   *
   * @param p The cell point to associate with the A* node.
   * @param isOddStepOfOneTwoOneMovement If the movement is 1-2-1 and an odd path was taken so far.
   */
  public AStarCellPoint(CellPoint p, boolean isOddStepOfOneTwoOneMovement) {
    this.position = new CellPoint(p.x, p.y, p.distanceTraveled, p.distanceTraveledWithoutTerrain);
    this.isOddStepOfOneTwoOneMovement = isOddStepOfOneTwoOneMovement;
  }

  /**
   * Check if the path taken to this node has an odd number of diagonal steps.
   *
   * <p>The cost of the nth diagonal step under 1-2-1 movement depends on whether n is odd or even.
   * This method allows that to be easily decided for a given node.
   *
   * @return `true` if the path to this number has an odd number of diagonal steps.
   */
  public boolean isOddStepOfOneTwoOneMovement() {
    return this.isOddStepOfOneTwoOneMovement;
  }

  public void replaceG(AStarCellPoint previousNode) {
    g = previousNode.g;
    position.distanceTraveled = previousNode.position.distanceTraveled;
    position.distanceTraveledWithoutTerrain = previousNode.position.distanceTraveledWithoutTerrain;
  }

  public double fCost() {
    return h + g;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof AStarCellPoint)) {
      return false;
    }
    var that = (AStarCellPoint) o;
    return this.position.equals(that.position)
        && this.isOddStepOfOneTwoOneMovement == that.isOddStepOfOneTwoOneMovement;
  }

  @Override
  public int hashCode() {
    return Objects.hash(position, this.isOddStepOfOneTwoOneMovement);
  }
}
