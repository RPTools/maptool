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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.rptools.lib.CodeTimer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.LineSegment;

record NearestWallResult(LineSegment wall, Coordinate point, double distance) {}

public class VisibilityProblem {
  private static final Logger log = LogManager.getLogger(VisibilityProblem.class);

  private final Coordinate origin;

  /**
   * All endpoints to check during the sweep.
   *
   * <p>Endpoints reference other endpoints, forming a graph where each edge is a vision blocking
   * line segment connecting two endpoints.
   */
  private final EndpointSet endpointSet;

  /**
   * Build a new problem set.
   *
   * @param origin The point at which all vision rays begin.
   * @param visionBounds The bounds of the vision, in order to avoid the need for infinite polygonal
   *     areas.
   */
  public VisibilityProblem(Coordinate origin, Envelope visionBounds) {
    this.origin = origin;
    this.endpointSet = new EndpointSet(origin, visionBounds);
  }

  public void add(Coordinate... string) {
    add(Arrays.asList(string));
  }

  public void add(List<Coordinate> string) {
    if (string.size() < 2) {
      return;
    }

    // Always plainly add the first point.

    var previous = endpointSet.add(string.get(0));
    for (int i = 1; i < string.size(); ++i) {
      var endpoint = endpointSet.add(string.get(i));

      previous.startsWall(endpoint);
      endpoint.endsWall(previous);

      previous = endpoint;
    }
  }

  // Don't actually call this, it's for testing purposes.
  private void verifyEndpoints(Iterable<VisibilitySweepEndpoint> endpoints) {
    for (var endpoint : endpoints) {
      endpoint.verify();
    }
  }

  public @Nullable Coordinate[] solve() {
    final var timer = CodeTimer.get();

    if (endpointSet.size() == 0) {
      // No topology, apparently.
      return null;
    }

    timer.start("add bounds");
    final var envelope = endpointSet.getBounds();
    // Exact expansion distance doesn't matter, we just don't want the boundary walls to overlap
    // endpoints from real walls.
    envelope.expandBy(1.0);
    // Because we definitely have geometry, the envelope will always be a non-trivial rectangle. Add
    // the rectangle's sides as wall so the sweep is well-defined. Careful to create the segments
    // counterclockwise!
    add(
        new Coordinate(envelope.getMinX(), envelope.getMinY()),
        new Coordinate(envelope.getMaxX(), envelope.getMinY()),
        new Coordinate(envelope.getMaxX(), envelope.getMaxY()),
        new Coordinate(envelope.getMinX(), envelope.getMaxY()),
        new Coordinate(envelope.getMinX(), envelope.getMinY()));
    timer.stop("add bounds");

    // Now that we have valid geometry and a bounding box, we can continue with the sweep.
    timer.start("initialize");
    endpointSet.simplify();
    final var endpoints = new ArrayList<>(endpointSet.getEndpoints());
    // verifyEndpoints(endpoints);
    timer.stop("initialize");

    Set<LineSegment> openWalls = new HashSet<>();

    // This initial sweep just makes sure we have the correct open set to start.
    for (final var endpoint : endpoints) {
      if (endpoint == null) {
        continue;
      }
      for (var otherEndpoint : endpoint.getStartsWalls()) {
        openWalls.add(new LineSegment(endpoint.getPoint(), otherEndpoint.getPoint()));
      }
      for (var otherEndpoint : endpoint.getEndsWalls()) {
        openWalls.remove(new LineSegment(otherEndpoint.getPoint(), endpoint.getPoint()));
      }
    }

    // Now for the real sweep. Make sure to process the first point once more at the end to ensure
    // the sweep covers the full 360 degrees.
    endpoints.add(endpoints.get(0));
    List<Coordinate> visionPoints = new ArrayList<>();
    for (final var endpoint : endpoints) {
      if (endpoint == null) {
        continue;
      }

      assert !openWalls.isEmpty();

      final var ray = new LineSegment(origin, endpoint.getPoint());
      final var nearestWallResult = findNearestOpenWall(openWalls, ray);

      for (var otherEndpoint : endpoint.getStartsWalls()) {
        openWalls.add(new LineSegment(endpoint.getPoint(), otherEndpoint.getPoint()));
      }
      for (var otherEndpoint : endpoint.getEndsWalls()) {
        openWalls.remove(new LineSegment(otherEndpoint.getPoint(), endpoint.getPoint()));
      }

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
          if (!endpoint.getPoint().equals(newNearestWallResult.wall().p0)) {
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

    timer.start("close polygon");
    // Ensure a closed loop.
    // TODO Are there not cases where this is already done?
    visionPoints.add(visionPoints.get(0));
    timer.stop("close polygon");

    timer.start("build result");
    try {
      return visionPoints.toArray(Coordinate[]::new);
    } finally {
      timer.stop("build result");
    }
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
}
