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
package net.rptools.maptool.client.ui.zone.vbl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.util.LineStringExtracter;
import org.locationtech.jts.operation.union.UnaryUnionOp;

record NearestWallResult(LineSegment wall, Coordinate point, double distance) {}

public class VisibilityProblem {
  private static final Logger log = LogManager.getLogger(VisibilityProblem.class);

  private final GeometryFactory geometryFactory;
  private final Coordinate origin;
  private final PreparedGeometry visionGeometry;
  private final List<LineString> visionBlockingSegments;

  public VisibilityProblem(
      GeometryFactory geometryFactory, Coordinate origin, PreparedGeometry visionGeometry) {
    this.geometryFactory = geometryFactory;
    this.origin = origin;
    this.visionGeometry = visionGeometry;
    this.visionBlockingSegments = new ArrayList<>();
  }

  public void add(LineString string) {
    visionBlockingSegments.add(string);
  }

  public @Nullable Geometry solve() {
    if (visionBlockingSegments.isEmpty()) {
      // No topology, apparently.
      return null;
    }

    /*
     * Unioning all the line segments has the nice effect of noding any intersections between line
     * segments. Without this, it may not be valid.
     * Note: if the geometry were only composed of one topology, it would certainly be valid due to
     * its "flat" nature. But even in that case, it is more robust to due the union in case this
     * flatness assumption ever changes.
     */
    final var allWallGeometry = new UnaryUnionOp(visionBlockingSegments).union();
    // Replace the original geometry with the well-defined geometry.
    visionBlockingSegments.clear();
    LineStringExtracter.getLines(allWallGeometry, visionBlockingSegments);

    /*
     * The algorithm requires walls in every direction. The easiest way to accomplish this is to add
     * the boundary of the bounding box.
     */
    final var envelope = allWallGeometry.getEnvelopeInternal();
    envelope.expandToInclude(visionGeometry.getGeometry().getEnvelopeInternal());
    // Exact expansion distance doesn't matter, we just don't want the boundary walls to overlap
    // endpoints from real walls.
    envelope.expandBy(1.0);
    // Because we definitely have geometry, the envelope will always be a non-trivial rectangle.
    visionBlockingSegments.add(((Polygon) geometryFactory.toGeometry(envelope)).getExteriorRing());

    // Now that we have valid geometry and a bounding box, we can continue with the sweep.

    final var endpoints = getSweepEndpoints(origin, visionBlockingSegments);
    Set<LineSegment> openWalls = Collections.newSetFromMap(new IdentityHashMap<>());

    // This initial sweep just makes sure we have the correct open set to start.
    for (final var endpoint : endpoints) {
      openWalls.addAll(endpoint.getStartsWalls());
      openWalls.removeAll(endpoint.getEndsWalls());
    }

    // Now for the real sweep. Make sure to process the first point once more at the end to ensure
    // the sweep covers the full 360 degrees.
    endpoints.add(endpoints.get(0));
    List<Coordinate> visionPoints = new ArrayList<>();
    for (final var endpoint : endpoints) {
      assert !openWalls.isEmpty();

      final var ray = new LineSegment(origin, endpoint.getPoint());
      final var nearestWallResult = findNearestOpenWall(openWalls, ray);

      openWalls.addAll(endpoint.getStartsWalls());
      openWalls.removeAll(endpoint.getEndsWalls());

      // Find a new nearest wall.
      final var newNearestWallResult = findNearestOpenWall(openWalls, ray);

      if (newNearestWallResult.wall() != nearestWallResult.wall()) {
        // Implies we have changed which wall we are at. Need to figure out projections.

        if (openWalls.contains(nearestWallResult.wall())) {
          // The previous nearest wall is still open. I.e., we didn't fall of its end but
          // encountered a new closer wall. So we project the current point to the previous
          // nearest wall, then step to the current point.
          visionPoints.add(nearestWallResult.point());
          visionPoints.add(endpoint.getPoint());
        } else {
          // The previous nearest wall is now closed. I.e., we "fell off" it and therefore have
          // encountered a different wall. So we step from the current point (which is on the
          // previous wall) to the projection on the new wall.
          visionPoints.add(endpoint.getPoint());
          // Special case: if the two walls are adjacent, they share the current point. We don't
          // need to add the point twice, so just skip in that case.
          if (!endpoint.getStartsWalls().contains(newNearestWallResult.wall())) {
            visionPoints.add(newNearestWallResult.point());
          }
        }
      }
    }
    if (visionPoints.size() < 3) {
      // This shouldn't happen, but just in case.
      log.warn("Sweep produced too few points: {}", visionPoints);
      return null;
    }
    visionPoints.add(visionPoints.get(0)); // Ensure a closed loop.

    return geometryFactory.createPolygon(visionPoints.toArray(Coordinate[]::new));
  }

  private static NearestWallResult findNearestOpenWall(
      Set<LineSegment> openWalls, LineSegment ray) {
    assert !openWalls.isEmpty();

    @Nullable LineSegment currentNearest = null;
    @Nullable Coordinate currentNearestPoint = null;
    double nearestDistance = Double.MAX_VALUE;
    for (final var openWall : openWalls) {
      final var intersection = ray.lineIntersection(openWall);
      if (intersection == null) {
        continue;
      }

      final var distance = ray.p0.distance(intersection);
      if (distance < nearestDistance) {
        currentNearest = openWall;
        currentNearestPoint = intersection;
        nearestDistance = distance;
      }
    }

    assert currentNearest != null;
    return new NearestWallResult(currentNearest, currentNearestPoint, nearestDistance);
  }

  /**
   * Builds a list of endpoints for the sweep algorithm to consume.
   *
   * <p>The endpoints will be unique (i.e., no coordinate is represented more than once) and in a
   * consistent orientation (i.e., counterclockwise around the origin). In addition, all endpoints
   * will have their starting and ending walls filled according to which walls are incident to the
   * corresponding point.
   *
   * @param origin The center of vision, by which orientation can be determined.
   * @param visionBlockingSegments The "walls" that are able to block vision. All points in these
   *     walls will be present in the returned list.
   * @return A list of all endpoints in counterclockwise order.
   */
  private static List<VisibilitySweepEndpoint> getSweepEndpoints(
      Coordinate origin, List<LineString> visionBlockingSegments) {
    final Map<Coordinate, VisibilitySweepEndpoint> endpointsByPosition = new HashMap<>();
    for (final var segment : visionBlockingSegments) {
      VisibilitySweepEndpoint current = null;
      for (final var coordinate : segment.getCoordinates()) {
        final var previous = current;
        current =
            endpointsByPosition.computeIfAbsent(
                coordinate, c -> new VisibilitySweepEndpoint(c, origin));
        if (previous == null) {
          // We just started this segment; still need a second point.
          continue;
        }

        final var isForwards =
            Orientation.COUNTERCLOCKWISE
                == Orientation.index(origin, previous.getPoint(), current.getPoint());
        // Make sure the wall always goes in the counterclockwise direction.
        final LineSegment wall =
            isForwards
                ? new LineSegment(previous.getPoint(), coordinate)
                : new LineSegment(coordinate, previous.getPoint());
        if (isForwards) {
          previous.startsWall(wall);
          current.endsWall(wall);
        } else {
          previous.endsWall(wall);
          current.startsWall(wall);
        }
      }
    }
    final List<VisibilitySweepEndpoint> endpoints = new ArrayList<>(endpointsByPosition.values());

    endpoints.sort(
        Comparator.comparingDouble(VisibilitySweepEndpoint::getPseudoangle)
            .thenComparing(VisibilitySweepEndpoint::getDistance));

    return endpoints;
  }
}
