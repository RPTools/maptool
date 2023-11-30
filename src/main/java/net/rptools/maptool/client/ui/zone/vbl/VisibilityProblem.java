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
import java.util.List;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.rptools.lib.CodeTimer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.LineSegment;

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

  // Note: we are essentially this collection as a priority queue, so we can always operate on the
  // closest wall. However, TreeSet is faster than PriorityQueue in this case, likely since the
  // size of the collection tends to remain quite small.
  // Note: it's not possible to totally order all walls by distance to `origin`. But it is
  // possible to do it for the set of open walls at any point in time, which is why we can use
  // this structure. So it's important to remove ended walls before adding opened walls, otherwise
  // we might momentarily violate that basic requirement of the comparison.
  // Note: as a rule, the number of open walls is small, usually well under 50.
  private final TreeSet<LineSegment> openWalls;

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
    this.openWalls = new TreeSet<>(this::compareOpenWalls);
  }

  public void add(Coordinate... string) {
    add(Arrays.asList(string));
  }

  /**
   * Adds a string of vision blocking line segments into the problem space.
   *
   * <p>All line segments in the string must be oriented counterclockwise around the origin.
   *
   * <p>Internally, endpoints will be guaranteed to be unique and in a consistent orientation
   * (counterclockwise around the origin, starting with the negative x-axis). Each segment in the
   * string will be opened and closed by its respective endpoints.
   *
   * @param string The vision blocking segments to add
   */
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

      // This check is only valid because of the counter-clockwise orientation enforced above.
      final var isOpen = endpoint.getPoint().y <= origin.y && previous.getPoint().y > origin.y;
      if (isOpen) {
        openWalls.add(new LineSegment(previous.getPoint(), endpoint.getPoint()));
      }

      previous = endpoint;
    }
  }

  // Don't actually call this, it's for testing purposes.
  private void verifyEndpoints(Iterable<VisibilitySweepEndpoint> endpoints) {
    for (var endpoint : endpoints) {
      endpoint.verify();
    }
  }

  private List<Coordinate> sweep(Iterable<VisibilitySweepEndpoint> endpoints) {
    List<Coordinate> visionPoints = new ArrayList<>();

    var previousNearestWall = openWalls.getFirst();
    for (final var endpoint : endpoints) {
      if (endpoint == null) {
        // This was a deduplicated endpoint.
        continue;
      }

      final var currentNearestWall = updateOpenWalls(endpoint, previousNearestWall);
      // If the current nearest wall hasn't changed, the endpoint is occluded and does not
      // contribute to the result.
      if (currentNearestWall != previousNearestWall) {
        consumeEndpoint(endpoint, previousNearestWall, currentNearestWall, visionPoints);

        previousNearestWall = currentNearestWall;
      }
    }

    return visionPoints;
  }

  /**
   * Solve the visibility polygon problem.
   *
   * <p>This follows Asano's algorithm as described in section 3.2 of "Efficient Computation of
   * Visibility Polygons". The endpoints are already sorted as required, as is the initial set of
   * open walls. As the algorithm progresses, open walls are maintained as an ordered set to enable
   * efficient polling of the closest wall at any given point in time.
   *
   * @return A visibility polygon, represented as a ring of coordinates.
   * @see <a href="https://arxiv.org/abs/1403.3905">Efficient Computation of Visibility Polygons,
   *     arXiv:1403.3905</a>
   */
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
    final var endpoints = endpointSet.getEndpoints();
    // verifyEndpoints(endpoints);
    timer.stop("initialize");

    // Now for the real sweep.
    timer.start("sweep");
    final var visionPoints = sweep(endpoints);
    timer.stop("sweep");

    timer.start("sanity check");
    try {
      if (visionPoints.size() < 3) {
        // This shouldn't happen, but just in case.
        log.warn("Sweep produced too few points: {}", visionPoints);
        return null;
      }
    } finally {
      timer.stop("sanity check");
    }
    timer.start("close polygon");
    // TODO Are there not cases where this is already done?
    visionPoints.add(visionPoints.get(0)); // Ensure a closed loop.
    timer.stop("close polygon");

    timer.start("build result");
    try {
      return visionPoints.toArray(Coordinate[]::new);
    } finally {
      timer.stop("build result");
    }
  }

  private LineSegment updateOpenWalls(
      VisibilitySweepEndpoint endpoint, LineSegment previousNearestWall) {
    assert !openWalls.isEmpty();

    for (var otherEndpoint : endpoint.getEndsWalls()) {
      var removed =
          openWalls.remove(new LineSegment(otherEndpoint.getPoint(), endpoint.getPoint()));
      assert removed : "The endpoint's ended walls should be open just prior to this point";
    }

    boolean maybeOccluded =
        !endpoint.getPoint().equals(previousNearestWall.p1)
            // The origin is always oriented counterclockwise to walls, so an occluded point would
            // be oriented clockwise instead.
            && previousNearestWall.orientationIndex(endpoint.getPoint()) == Orientation.CLOCKWISE;

    // The endpoint is behind the wall, and the wall is still open. Only add segments for those
    // ending points not also occluded by the same wall (otherwise the new wall would be
    // completely occluded itself and contribute nothing).
    for (var otherEndpoint : endpoint.getStartsWalls()) {
      // This check relies on the fact that walls cannot cross. So all we have to do is determine
      // that the endpoint does not come after p1 to decide that it is also occluded.
      var occluded =
          maybeOccluded
              && (Orientation.index(origin, previousNearestWall.p1, otherEndpoint.getPoint())
                  == Orientation.CLOCKWISE);
      if (occluded) {
        // Occluded, we can remove the edge entirely. No need to remove it from this endpoint, but
        // remove it from the upcoming one to avoid future work.
        var removed = otherEndpoint.removeEndedWall(endpoint);
        assert removed;
      } else {
        openWalls.add(new LineSegment(endpoint.getPoint(), otherEndpoint.getPoint()));
      }
    }

    // Find a new nearest wall.
    assert !openWalls.isEmpty();
    return openWalls.getFirst();
  }

  private void consumeEndpoint(
      VisibilitySweepEndpoint endpoint,
      LineSegment previousNearestWall,
      LineSegment currentNearestWall,
      List<Coordinate> visionPoints) {
    // Implies we have changed which wall we are at. Need to figure out projections.
    final var ray = new LineSegment(origin, endpoint.getPoint());
    if (!ray.p1.equals(previousNearestWall.p1)) {
      // The previous nearest wall is still open. I.e., we didn't fall of its end but
      // encountered a new closer wall. So we project the current point to the previous
      // nearest wall, then step to the current point.
      assert ray.p1.equals(currentNearestWall.p0)
          : "Uh-oh, this case should only happen if we encountered a newly opened closer wall";
      visionPoints.add(projectOntoOpenWall(ray, previousNearestWall));
      visionPoints.add(currentNearestWall.p0);
    } else {
      // The previous nearest wall is now closed. I.e., we "fell off" it and therefore have
      // encountered a different wall. So we step from the current point (which is on the
      // previous wall) to the projection on the new wall.
      assert ray.p1.equals(previousNearestWall.p1)
          : "Uh-oh, this case should only happen if we left a closed wall for something farther away";

      visionPoints.add(previousNearestWall.p1);
      // Special case: if the two walls are adjacent, they share the current point. We don't
      // need to add the point twice, so just skip in that case.
      if (!previousNearestWall.p1.equals(currentNearestWall.p0)) {
        visionPoints.add(projectOntoOpenWall(ray, currentNearestWall));
      }
    }
  }

  /**
   * Projects an event line ray onto an open wall.
   *
   * <p>Since the wall is open for the event line, the intersection will succeed.
   *
   * @param ray A ray representing the event line.
   * @param wall A wall that is open according to {@code ray}.
   * @return The point at which {@code ray} would intersect with {@code wall} if extended
   *     indefinitely.
   */
  private static @Nonnull Coordinate projectOntoOpenWall(LineSegment ray, LineSegment wall) {
    // TODO This assertion is not quite right: it's okay to project onto an about-to-be-closed wall.
    //  assert isWallOpen(ray, wall) : String.format("Wall %s is not open for ray %s", wall, ray);
    var intersection = ray.lineIntersection(wall);
    assert intersection != null
        : String.format(
            "Unable to project ray %s onto wall %s despite the wall being open", ray, wall);
    return intersection;
  }

  /**
   * Compares two open walls to determine which one is closer to the origin.
   *
   * <p>By construction, we do not have any non-noded intersections, and as a result open walls can
   * be totally ordered by closeness to the origin. Note though that walls cannot in general be
   * totally ordered this way, the property only holds for open walls, with the ordering
   * corresponding to the ordering of intersections with the event line.
   *
   * @param s0 The first wall to compare.
   * @param s1 The second wall to compare.
   * @return {@code -1} if {@code s0} is closer to {@code origin} than {@code s1}; {@code 1} if
   *     {@code s0} is further from {@code origin} than {@code s1}; {@code 0} if {@code s0} and
   *     {@code s1} are the same wall.
   */
  private int compareOpenWalls(LineSegment s0, LineSegment s1) {
    assert s0.orientationIndex(origin) == Orientation.COUNTERCLOCKWISE
        : String.format("Wall %s is not oriented correctly", s0);
    assert s1.orientationIndex(origin) == Orientation.COUNTERCLOCKWISE
        : String.format("Wall %s is not oriented correctly", s1);

    if (s0 == s1) {
      return 0;
    }

    final var pointwise = s0.compareTo(s1);
    if (pointwise == 0) {
      return 0;
    }

    // For orientation checks: counterclockwise indicates the tested point is nearer than the line
    // segment, since by construction all segments are oriented counterclockwise relative to the
    // vision origin.

    // Start by trying to prove that s0 is definitely closer or further than s1.
    var p0Orientation = Orientation.index(s0.p0, s1.p0, s1.p1);
    var p1Orientation = Orientation.index(s0.p1, s1.p0, s1.p1);

    assert p0Orientation != Orientation.COLLINEAR || p1Orientation != Orientation.COLLINEAR
        : String.format(
            "It should not be possible for two open walls %s and %s to be collinear with one another",
            s0, s1);

    if (p0Orientation == Orientation.COLLINEAR) {
      // p1 is authoritative.
      return p1Orientation == Orientation.COUNTERCLOCKWISE ? -1 : 1;
    }
    if (p1Orientation == Orientation.COLLINEAR) {
      // p0 is authoritative.
      return p0Orientation == Orientation.COUNTERCLOCKWISE ? -1 : 1;
    }
    if (p0Orientation == p1Orientation) {
      // There is agreement, so we know our answer.
      return p0Orientation == Orientation.COUNTERCLOCKWISE ? -1 : 1;
    }

    // Indefinite result (one point looks closer, one look further). So test the other segment's
    // points. Actually only need to test one point to be sure.
    p0Orientation = Orientation.index(s1.p0, s0.p0, s0.p1);
    // Colinearity of one point implies colinearity of the other, otherwise we would have a definite
    // result above. And this case can't happen in the context of the sweep algorithm.
    assert p0Orientation != Orientation.COLLINEAR
        : String.format(
            "It should not be possible to get a collinear result in the fallback check for %s and %s",
            s0, s1);

    // If clockwise, this point on s1 is nearer than s0, so s1 as a whole is in front of s0.
    return p0Orientation == Orientation.COUNTERCLOCKWISE ? 1 : -1;
  }
}
