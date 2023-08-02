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
package net.rptools.lib;

import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import org.locationtech.jts.awt.ShapeReader;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.operation.polygonize.Polygonizer;

public class GeometryUtil {
  private static final PrecisionModel precisionModel = new PrecisionModel(1_000_000.0);
  private static final GeometryFactory geometryFactory = new GeometryFactory(precisionModel);

  public static double getAngle(Point2D origin, Point2D target) {
    double angle =
        Math.toDegrees(
            Math.atan2((origin.getY() - target.getY()), (target.getX() - origin.getX())));
    if (angle < 0) {
      angle += 360;
    }
    return angle;
  }

  public static double getAngleDelta(double sourceAngle, double targetAngle) {
    // Normalize
    targetAngle -= sourceAngle;

    if (targetAngle > 180) {
      targetAngle -= 360;
    }
    if (targetAngle < -180) {
      targetAngle += 360;
    }
    return targetAngle;
  }

  public static PrecisionModel getPrecisionModel() {
    return precisionModel;
  }

  public static GeometryFactory getGeometryFactory() {
    return geometryFactory;
  }

  public static Geometry toJts(Area area) {
    final var pathIterator = area.getPathIterator(null);
    final var polygonizer = new Polygonizer(true);
    final var coords = (List<Coordinate[]>) ShapeReader.toCoordinates(pathIterator);
    final var geometries = new ArrayList<LineString>();
    for (final var coordinateArray : coords) {
      for (final var coord : coordinateArray) {
        precisionModel.makePrecise(coord);
      }
      final var lineString = geometryFactory.createLineString(coordinateArray);
      geometries.add(lineString);
    }
    polygonizer.add(geometries);
    return polygonizer.getGeometry();
  }
}
