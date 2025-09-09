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

import java.awt.Point;
import java.awt.Polygon;
import java.util.Arrays;
import java.util.Collections;
import javax.annotation.Nullable;
import net.rptools.maptool.model.ZonePoint;

public class IsoRectangleStrategy implements Strategy<ZonePoint> {
  @Override
  public ZonePoint startNewAtPoint(ZonePoint point) {
    return point;
  }

  private Point toPoint(ZonePoint point) {
    return new Point(point.x, point.y);
  }

  @Override
  public @Nullable DrawingResult getShape(
      ZonePoint origin_, ZonePoint currentPoint_, boolean centerOnOrigin, boolean isFilled) {
    // Inversion check is not strictly needed, but simplifies some case work below.
    var invertedY = currentPoint_.y < origin_.y;
    var origin = toPoint(invertedY ? currentPoint_ : origin_);
    var currentPoint = toPoint(invertedY ? origin_ : currentPoint_);

    final double diffX = (currentPoint.x - origin.x) / 2.;
    final double diffY = currentPoint.y - origin.y;
    assert diffY >= 0 : "diffY should be forced positive by the above inversion check";

    var p1 = new Point((int) (origin.x + diffX + diffY), (int) (origin.y + (diffY + diffX) / 2));
    var p2 = new Point((int) (origin.x + diffX - diffY), (int) (origin.y + (diffY - diffX) / 2));

    var points = new Point[] {origin, p1, currentPoint, p2};
    // For the sake of measurements, we need to know which point is in each compass direction.
    // Check for edge cases, and force order of `points` to be north, east, south, west.
    if (diffY < Math.abs(diffX)) {
      // Inverted over the y = x / 2 axis. First rotate either p1 or p2 into north position...
      Collections.rotate(Arrays.asList(points), diffX > 0 ? 1 : -1);
      // ... then swap roles of origin and currentPoint due to the inversion.
      Collections.swap(Arrays.asList(points), 1, 3);
    }

    var xPoints =
        new int[] {
          points[0].x, points[1].x, points[2].x, points[3].x,
        };
    var yPoints =
        new int[] {
          points[0].y, points[1].y, points[2].y, points[3].y,
        };
    var polygon = new Polygon(xPoints, yPoints, 4);
    var bounds = polygon.getBounds();
    if (bounds.width == 0 && bounds.height == 0) {
      return null;
    }

    return new DrawingResult(
        polygon, new Measurement.IsoRectangular(points[0], points[3], points[1]));
  }
}
