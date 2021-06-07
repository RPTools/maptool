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

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.Token.TerrainModifierOperation;
import net.rptools.maptool.model.Zone;

public class AStarCellPoint extends CellPoint implements Comparable<AStarCellPoint> {
  AStarCellPoint parent;
  double h;
  double f;
  double terrainModifier;
  boolean isAStarCanceled = false;
  TerrainModifierOperation terrainModifierOperation;

  // Store if it's valid to move from Point2D to this cell.
  Map<Point2D, Boolean> validMoves = new HashMap<Point2D, Boolean>();

  public AStarCellPoint() {
    super(0, 0);
  }

  @Override
  public boolean isAStarCanceled() {
    return isAStarCanceled;
  }

  /**
   * Sets the A* cancellation status to the cell.
   *
   * @param aStarCanceled whether A* couldn't find a path to the cell
   */
  public void setAStarCanceled(boolean aStarCanceled) {
    isAStarCanceled = aStarCanceled;
  }

  /**
   * Create an A* node from a position.
   *
   * <p>The node will have no scores or data carried over from a cell point.
   *
   * @param x The x cell position of the node.
   * @param y The y cell position of the node.
   */
  public AStarCellPoint(int x, int y) {
    super(x, y);
  }

  /**
   * Create an A* node from a cell point.
   *
   * <p>The distance travelled is copied from the cell point, making this useful for creating nodes
   * that need to carry over data, such as after a waypoint.
   *
   * @param p The cell point to associate with the A* node.
   */
  public AStarCellPoint(CellPoint p) {
    super(p.x, p.y, p.distanceTraveled, p.distanceTraveledWithoutTerrain);
  }

  /**
   * Create a node with terrain modifiers.
   *
   * <p>This is only used to associate terrain modifiers with a position. It is not queued to be
   * used as part of A* itself.
   *
   * @param p The cell at which the node should exist.
   * @param mod The amount of the terrain modifier.
   * @param operation Which operation is used to calculate the terrain's effect.
   */
  public AStarCellPoint(CellPoint p, double mod, TerrainModifierOperation operation) {
    super(p.x, p.y);
    terrainModifier = mod;
    terrainModifierOperation = operation;
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
    return (int) this.distanceTraveledWithoutTerrain != this.distanceTraveledWithoutTerrain;
  }

  public double fCost() {
    return h + gCost();
  }

  public Point2D toPoint() {
    return new Point2D.Double(x, y);
  }

  public Point2D toCenterPoint(Zone zone) {
    Rectangle bounds = zone.getGrid().getBounds(this);
    return new Point2D.Double(bounds.getCenterX(), bounds.getCenterY());
  }

  // To store as a map of valid moves, true if you can move into this cellpoint from another
  // cellpoint
  public void setValidMove(Point2D key, boolean value) {
    validMoves.put(key, value);
  }

  public Boolean isValidMove(Point2D key) {
    return validMoves.get(key);
  }

  public void setValidMove(AStarCellPoint key, boolean value) {
    validMoves.put(key.toPoint(), value);
  }

  public Boolean isValidMove(AStarCellPoint key) {
    return validMoves.get(key.toPoint());
  }

  public Set<Point2D> getValidMoves() {
    Set<Point2D> validMovePoints = new HashSet<Point2D>();

    for (Entry<Point2D, Boolean> entry : validMoves.entrySet()) {
      if (entry.getValue()) {
        validMovePoints.add(
            new Point2D.Double(entry.getKey().getX() - x, entry.getKey().getY() - y));
      }
    }

    return validMovePoints;
  }

  // Draws a shape for display of all valid moves
  public Shape getValidMoveShape(Zone zone) {
    Path2D validMoveShape = new Path2D.Double();

    Rectangle cellBounds = zone.getGrid().getBounds(this);
    double x1 = cellBounds.getCenterX();
    double y1 = cellBounds.getCenterY();

    for (Entry<Point2D, Boolean> entry : validMoves.entrySet()) {
      validMoveShape.moveTo(x1, y1);

      if (entry.getValue()) {
        CellPoint cp = new CellPoint((int) entry.getKey().getX(), (int) entry.getKey().getY());
        Rectangle entryBounds = zone.getGrid().getBounds(cp);
        double x2 = entryBounds.getCenterX();
        double y2 = entryBounds.getCenterY();
        validMoveShape.lineTo(x2, y2);
      }
    }

    validMoveShape.closePath();

    return validMoveShape;
  }

  @Override
  public int compareTo(AStarCellPoint other) {
    return Double.compare(fCost(), other.fCost());
  }
}
