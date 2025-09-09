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
package net.rptools.maptool.client.tool.drawing;

import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.rptools.maptool.model.ZonePoint;

public class PolyLineStrategy implements Strategy<List<ZonePoint>> {
  private final boolean isFreehand;

  public PolyLineStrategy(boolean isFreehand) {
    this.isFreehand = isFreehand;
  }

  @Override
  public boolean isFreehand() {
    return isFreehand;
  }

  @Override
  public boolean isLinear() {
    return true;
  }

  @Override
  public List<ZonePoint> startNewAtPoint(ZonePoint point) {
    var points = new ArrayList<ZonePoint>();
    points.addLast(new ZonePoint(point));
    return points;
  }

  @Override
  public void pushPoint(List<ZonePoint> state, ZonePoint point) {
    state.addLast(new ZonePoint(point));
  }

  @Override
  public @Nullable DrawingResult getShape(
      List<ZonePoint> state, ZonePoint currentPoint, boolean centerOnOrigin, boolean isFilled) {
    var trimmedPoints = new ArrayList<>(state);
    trimmedPoints.addLast(currentPoint);
    trim(trimmedPoints);
    assert !trimmedPoints.isEmpty() : "The list will always have at least the origin point";

    Shape result;
    if (isFilled && trimmedPoints.size() > 2) {
      var xPoints = new int[trimmedPoints.size()];
      var yPoints = new int[trimmedPoints.size()];
      for (int i = 0; i < trimmedPoints.size(); ++i) {
        var point = trimmedPoints.get(i);
        xPoints[i] = point.x;
        yPoints[i] = point.y;
      }
      result = new Polygon(xPoints, yPoints, trimmedPoints.size());
    } else {
      var newPath = new Path2D.Double();
      var first = trimmedPoints.getFirst();
      newPath.moveTo(first.x, first.y);
      for (var point : trimmedPoints.subList(1, trimmedPoints.size())) {
        newPath.lineTo(point.x, point.y);
      }
      result = newPath;
    }

    Measurement measurement = null;
    if (!isFreehand()) {
      ZonePoint last = trimmedPoints.removeLast();
      ZonePoint secondLast = trimmedPoints.isEmpty() ? last : trimmedPoints.removeLast();

      measurement =
          new Measurement.LineSegment(
              new Point2D.Double(secondLast.x, secondLast.y), new Point2D.Double(last.x, last.y));
    }

    return new DrawingResult(result, measurement);
  }

  private void trim(List<ZonePoint> points) {
    if (points.isEmpty()) {
      return;
    }

    ZonePoint previous = null;
    var iterator = points.iterator();
    while (iterator.hasNext()) {
      var current = iterator.next();

      if (previous != null && previous.equals(current)) {
        iterator.remove();
        continue;
      }

      previous = current;
    }
  }
}
