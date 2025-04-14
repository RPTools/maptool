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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.locationtech.jts.algorithm.InteriorPointArea;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.algorithm.PointLocation;
import org.locationtech.jts.awt.ShapeReader;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Location;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.operation.valid.IsValidOp;
import org.locationtech.jts.precision.GeometryPrecisionReducer;

public class GeometryUtil {
  private static final Logger log = LogManager.getLogger(GeometryUtil.class);

  private static final PrecisionModel precisionModel = new PrecisionModel(100_000.0);

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

  /**
   * Unions several areas into one.
   *
   * <p>The results are the same as progressively `.add()`ing all areas in the collection, but
   * performs much better when the result is complicated.
   *
   * @param areas The areas to union.
   * @return The union of {@code areas}
   */
  public static Area union(Collection<Area> areas) {
    final var copy = new ArrayList<>(areas);
    copy.replaceAll(Area::new);

    return destructiveUnion(copy);
  }

  /**
   * Like {@link #union(java.util.Collection)}, but will modify the areas and collection for
   * performance gains.
   *
   * @param areas The areas to union.
   * @return The union of {@code areas}
   */
  public static Area destructiveUnion(List<Area> areas) {
    areas.removeIf(Area::isEmpty);

    // Union two-by-two, on repeat until only one is left.
    while (areas.size() >= 2) {
      for (int i = 0; i + 1 < areas.size(); i += 2) {
        final var a = areas.get(i);
        final var b = areas.get(i + 1);

        a.add(b);
        areas.set(i + 1, null);
      }
      areas.removeIf(Objects::isNull);
    }

    if (areas.isEmpty()) {
      // Just in case, maybe it's possible for Area() to have edge cases the produce empty unions?
      return new Area();
    }

    return areas.getFirst();
  }

  public static PrecisionModel getPrecisionModel() {
    return precisionModel;
  }

  public static GeometryFactory getGeometryFactory() {
    return geometryFactory;
  }

  public static MultiPolygon toJts(Area area) {
    final var polygons = toJtsPolygons(area);
    final var geometry = geometryFactory.createMultiPolygon(polygons.toArray(Polygon[]::new));
    assert geometry.isValid()
        : "Returned geometry must be valid, but found this error: "
            + new IsValidOp(geometry).getValidationError();
    return geometry;
  }

  public static Collection<Polygon> toJtsPolygons(Area area) {
    if (area.isEmpty()) {
      return Collections.emptyList();
    }

    final var pathIterator = area.getPathIterator(null, 1. / precisionModel.getScale());
    final var coordinates = (List<Coordinate[]>) ShapeReader.toCoordinates(pathIterator);

    // Now collect all the noded rings into islands (JTS clockwise) and oceans (counterclockwise).
    final List<Island> islands = new ArrayList<>();
    final List<Coordinate[]> oceans = new ArrayList<>();
    for (final var ring : coordinates) {
      if (ring.length < 4) {
        log.warn(
            "Found invalid geometry: ring has only {} points but at least four are required.",
            ring.length);
      } else if (Orientation.isCCW(ring)) {
        oceans.add(ring);
      } else {
        islands.add(new Island(geometryFactory, ring));
      }
    }

    // Now we need to attach oceans to their parent islands to create polygons with holes.
    islands.sort(Comparator.comparingDouble(i -> i.getBoundingBox().getArea()));
    oceanLoop:
    for (final var ocean : oceans) {
      final Envelope oceanBoundingBox = CoordinateArrays.envelope(ocean);
      final Coordinate oceanInteriorPoint =
          InteriorPointArea.getInteriorPoint(geometryFactory.createPolygon(ocean));

      /*
       * Because each island and each ocean is a bounding volume, containment is as simple as
       * checking that the ocean has any point in common with the island, and that the ocean's
       * bounding box is contained within the island.
       *
       * Because `islands` is sorted from small to large, this loop will associate the ocean with
       * the smallest containing island.
       */
      for (var island : islands) {
        if (island.getBoundingBox().contains(oceanBoundingBox)
            && island.containedInBoundary(oceanInteriorPoint)) {
          island.addHole(ocean);
          continue oceanLoop;
        }
      }

      // If we get here, we didn't find a parent island.
      log.warn("Weird, I couldn't find an island for an ocean.  Bad/overlapping VBL?");
    }

    // Our islands are now equivalent to JTS polygons. Build those polygons, and robustly reduce
    // their precision
    final var polygons = new ArrayList<Polygon>();
    for (final var island : islands) {
      // Build the polygon...
      var polygon = island.toPolygon();
      // ... then make sure it is valid, fixing it if not.
      var fixedPolygon = GeometryPrecisionReducer.reduce(polygon, precisionModel);
      switch (fixedPolygon) {
        case Polygon p -> polygons.add(p);
        case MultiPolygon mp -> {
          for (var n = 0; n < mp.getNumGeometries(); ++n) {
            polygons.add((Polygon) mp.getGeometryN(n));
          }
        }
        default ->
            log.error(
                "Found unexpected geometry after fixing polygon: {}. Skipping",
                fixedPolygon.getClass());
      }
    }

    polygons.removeIf(Polygon::isEmpty);

    return polygons;
  }

  public static Point2D coordinateToPoint2D(Coordinate coordinate) {
    return new Point2D.Double(coordinate.getX(), coordinate.getY());
  }

  public static Coordinate point2DToCoordinate(Point2D point2D) {
    return new Coordinate(point2D.getX(), point2D.getY());
  }

  /**
   * Represents intermediate results in {@link #toJtsPolygons(Area)} that allows attaching oceans to
   * parent islands.
   *
   * <p>The ultimate result is a {@link Polygon} that can be obtained via {@link #toPolygon()}.
   */
  private static final class Island {
    private final GeometryFactory geometryFactory;
    private final Coordinate[] boundary;
    private final List<LinearRing> holes = new ArrayList<>();
    private final Envelope boundingBox;

    /**
     * Create an initial island that has no holes.
     *
     * @param geometryFactory The geometry factory used to covert coordinate arrays to {@link
     *     LinearRing} and the island as a whole to a {@link Polygon}.
     * @param boundary The exterior ring bounding the island. Must be closed.
     */
    public Island(GeometryFactory geometryFactory, Coordinate[] boundary) {
      this.geometryFactory = geometryFactory;
      this.boundary = boundary;
      this.boundingBox = CoordinateArrays.envelope(boundary);
    }

    /**
     * @return The axis-aligned bounding box of the island's boundary.
     */
    public Envelope getBoundingBox() {
      return boundingBox;
    }

    /**
     * Adds a hole to the island.
     *
     * @param ring The boundary of the hole. Must be closed.
     */
    public void addHole(Coordinate[] ring) {
      holes.add(geometryFactory.createLinearRing(ring));
    }

    /**
     * Checks whether the point is contained within the boundary island.
     *
     * <p>This operation does not account for holes. It only checks against the island's boundary.
     *
     * @param coordinate The point to check for containment.
     * @return {@code true} if {@code coordinate} is contained within the island's boundary.
     */
    public boolean containedInBoundary(Coordinate coordinate) {
      // Islands include their boundary.
      return Location.EXTERIOR != PointLocation.locateInRing(coordinate, boundary);
    }

    /**
     * Converts the island to a {@link Polygon}.
     *
     * @return A {@code Polygon} with the same boundary as the island, and will holes corresponding
     *     to each of the island's holes.
     */
    public Polygon toPolygon() {
      return geometryFactory.createPolygon(
          geometryFactory.createLinearRing(boundary), holes.toArray(LinearRing[]::new));
    }
  }
}
