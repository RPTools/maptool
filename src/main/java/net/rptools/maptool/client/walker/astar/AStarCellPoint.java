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
import java.util.Map.Entry;
import java.util.Set;
import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.Zone;

public class AStarCellPoint extends CellPoint implements Comparable<AStarCellPoint> {
  AStarCellPoint parent;
  double h;
  double f;
  double terrainModifier;

  // Store if it's valid to move from Point2D to this cell.
  HashMap<Point2D, Boolean> validMoves = new HashMap<Point2D, Boolean>();

  public AStarCellPoint() {
    super(0, 0);
  }

  public AStarCellPoint(int x, int y) {
    super(x, y);
  }

  public AStarCellPoint(CellPoint p) {
    super(p.x, p.y, p.distanceTraveled);
  }

  public AStarCellPoint(CellPoint p, double mod) {
    super(p.x, p.y);
    terrainModifier = mod;
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
