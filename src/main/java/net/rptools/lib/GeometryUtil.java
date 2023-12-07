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
import java.util.Collection;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.locationtech.jts.awt.ShapeReader;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.noding.NodableSegmentString;
import org.locationtech.jts.noding.NodedSegmentString;
import org.locationtech.jts.noding.SegmentString;
import org.locationtech.jts.noding.snapround.SnapRoundingNoder;
import org.locationtech.jts.operation.polygonize.Polygonizer;

public class GeometryUtil {
  private static final Logger log = LogManager.getLogger(GeometryUtil.class);
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

    // Make sure the geometry is noded and precise before polygonizing.
    final var strings = new ArrayList<NodableSegmentString>(coords.size());
    for (var string : coords) {
      strings.add(new NodedSegmentString(string, null));
    }
    final var noder = new SnapRoundingNoder(precisionModel);
    noder.computeNodes(strings);
    final Collection<? extends SegmentString> nodedStrings = noder.getNodedSubstrings();

    // Now build the polygons from our corrected geometry.
    for (var string : nodedStrings) {
      final var lineString = geometryFactory.createLineString(string.getCoordinates());
      polygonizer.add(lineString);
    }

    final var danglingEdges = polygonizer.getDangles().size();
    final var cutEdges = polygonizer.getCutEdges().size();
    final var invalidRings = polygonizer.getInvalidRingLines().size();
    if (danglingEdges != 0 || cutEdges != 0 || invalidRings != 0) {
      log.error(
          "Found invalid geometry: {} dangling edges; {} cut edges; {} invalid rings",
          danglingEdges,
          cutEdges,
          invalidRings);
    }

    return polygonizer.getGeometry();
  }
}
