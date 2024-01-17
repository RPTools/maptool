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

  /**
   * The center point of the vision.
   *
   * <p>This is the point around which the event line (vision ray) rotates during the sweep.
   * Orientation is always measured relative to this point.
   */
  private final Coordinate origin;

  /**
   * All endpoints to check during the sweep.
   *
   * <p>Endpoints reference other endpoints, forming a graph where each edge is a vision blocking
   * line segment connecting two endpoints.
   */
  private final EndpointSet endpointSet;

  /**
   * The set of walls that are intersected by the current event line.
   *
   * <p>This set is ordered by distance to {@link #origin}, where the distance is measured along the
   * event line. This way, the closest open wall is always the first element in the set. This
   * ordering is only possible for walls intersected by the same event line, so it is important that
   * we remove the walls from a previous event line before adding the walls for the next event line.
   */
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
   * <p>All line segments in the string should be oriented counterclockwise around the origin, but
   * if not we will patch things up.
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
      var current = endpointSet.add(string.get(i));

      final VisibilitySweepEndpoint start;
      final VisibilitySweepEndpoint end;
      if (Orientation.COUNTERCLOCKWISE
          == Orientation.index(origin, previous.getPoint(), current.getPoint())) {
        // Caller is well-behaved passing us correctly oriented segments.
        start = previous;
        end = current;
      } else {
        // Tsk tsk. Caller gave it to us the wrong-way round.
        start = current;
        end = previous;
      }

      start.startsWall(end);
      end.endsWall(start);

      // This check is only valid because of the counter-clockwise orientation enforced above.
      final var isOpen = end.getPoint().y <= origin.y && start.getPoint().y > origin.y;
      if (isOpen) {
        openWalls.add(new LineSegment(start.getPoint(), end.getPoint()));
      }

      previous = current;
    }
  }

  /**
   * Solve the visibility polygon problem.
   *
   * <p>This follows Asano's algorithm as described in section 3.2 of "Efficient Computation of
   * Visibility Polygons". The endpoints are already sorted as required, as is the initial set of
   * open walls. As the algorithm progresses, open walls are maintained as an ordered set to enable
   * efficient polling of the closest wall at any given point in time.
   *
   * @return A visibility polygon, represented as a ring of coordinates. A {@code null} result
   *     indicates that no vision blocking needed to be applied.
   * @see <a href="https://arxiv.org/abs/1403.3905">Efficient Computation of Visibility Polygons,
   *     arXiv:1403.3905</a>
   */
  public @Nullable Coordinate[] solve() {
    if (endpointSet.size() == 0) {
      // No topology, apparently.
      return null;
    }

    final var timer = CodeTimer.get();

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

    return visionPoints;
  }

  // Don't actually call this, it's for testing purposes.
  private void verifyEndpoints(Iterable<VisibilitySweepEndpoint> endpoints) {
    for (var endpoint : endpoints) {
      endpoint.verify();
    }
  }

  /**
   * Performs the core visibility sweep.
   *
   * <p>{@code endpoints} must already be sorted and deduplicated.
   *
   * @return The ring forming the visibility polygon. Since the ring is closed, it will contain at
   *     least three distinct points, and the first point will be duplicated as the last.
   */
  private Coordinate[] sweep(Iterable<VisibilitySweepEndpoint> endpoints) {
    List<Coordinate> visionPoints = new ArrayList<>();

    var previousNearestWall = openWalls.getFirst();
    for (final var endpoint : endpoints) {
      if (endpoint == null) {
        // This was a deduplicated endpoint.
        continue;
      }

      // Find a new nearest wall.
      updateOpenWalls(endpoint, previousNearestWall);
      assert !openWalls.isEmpty();
      final var currentNearestWall = openWalls.getFirst();

      if (!currentNearestWall.equals(previousNearestWall)) {
        addVisionPoints(endpoint.getPoint(), previousNearestWall, currentNearestWall, visionPoints);
        previousNearestWall = currentNearestWall;
      }
    }

    if (visionPoints.size() < 3) {
      // This shouldn't happen, but just in case.
      throw new RuntimeException(
          String.format("Visibility sweep produced too few points: %s", visionPoints));
    }

    // Ensure a closed loop.
    visionPoints.add(visionPoints.getFirst());

    return visionPoints.toArray(Coordinate[]::new);
  }

  /**
   * Updates {@link #openWalls} by removing now-ended walls and adding newly-opened walls.
   *
   * @param endpoint The current endpoint that the event line is pointing at.
   * @param previousNearestWall The nearest open wall for the previous event line.
   */
  private void updateOpenWalls(VisibilitySweepEndpoint endpoint, LineSegment previousNearestWall) {
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
  }

  /**
   * Adds points to {@code visionPoints} given the walls we just transitioned between.
   *
   * @param point The current point in the sweep, that the event line is being pointed at. Used to
   *     determine the various possible cases.
   * @param previousNearestWall The nearest open wall for the previous event line.
   * @param currentNearestWall The nearest open wall for the current event line.
   * @param visionPoints The vision polygon ring that is being built and to which points will be
   *     added.
   */
  private void addVisionPoints(
      Coordinate point,
      LineSegment previousNearestWall,
      LineSegment currentNearestWall,
      List<Coordinate> visionPoints) {
    /*
     * Implies we have changed which wall we are at. Need to figure out projections.
     *
     * There are four cases:
     * 1. The previous nearest open wall ended, meaning its ending point should be added to the
     *    vision points along with the projection to the current nearest open wall.
     * 2. The current nearest open wall started, meaning the projection to the previous nearest wall
     *    and the starting point of the current wall should be added to the vision points.
     * 3. (1) and (2) at the same time, i.e., the previous and current walls are connected. No
     *    projections needed, and only one point (that common point) needs to be added.
     * 4. Neither (1) nor (2), in which case previousNearestWall and currentNearestWall will be the
     *    same wall.
     */

    final var startedCurrentWall = point.equals(currentNearestWall.p0);
    final var endedPreviousWall = point.equals(previousNearestWall.p1);

    final var ray = new LineSegment(origin, point);
    if (startedCurrentWall && endedPreviousWall) {
      // Transition between connected walls. No projections needed.
      visionPoints.add(point);
    } else if (startedCurrentWall) {
      // The previous nearest wall is still open. I.e., we didn't fall of its end but encountered a
      // new closer wall. So we project the current point to the previous nearest wall, then step to
      // the current point.
      visionPoints.add(projectOntoWall(ray, previousNearestWall));
      visionPoints.add(point);
    } else if (endedPreviousWall) {
      // The previous nearest wall is now closed. I.e., we "fell off" it and therefore have
      // encountered a different wall. So we step from the current point (which is on the previous
      // wall) to the projection on the new wall.
      visionPoints.add(point);
      visionPoints.add(projectOntoWall(ray, currentNearestWall));
    } else {
      // No change in walls, so no points need to be added.
    }
  }

  /**
   * Projects an event line ray onto a wall.
   *
   * @param ray A ray representing the event line.
   * @param wall A wall to project {@code ray} onto.
   * @return The point at which {@code ray} would intersect with {@code wall} if they were extended
   *     indefinitely.
   */
  private static @Nonnull Coordinate projectOntoWall(LineSegment ray, LineSegment wall) {
    var intersection = ray.lineIntersection(wall);
    assert intersection != null
        : String.format(
            "Unable to project ray %s onto wall %s despite not permitting collinear walls",
            ray, wall);
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
    // Collinearity of one point implies collinearity of the other, otherwise we would have a
    // definite result above. And this case can't happen for _open_ walls since the event line would
    // only intersect one of them.
    assert p0Orientation != Orientation.COLLINEAR
        : String.format(
            "It should not be possible to get a collinear result in the fallback check for %s and %s",
            s0, s1);

    // If clockwise, this point on s1 is nearer than s0, so s1 as a whole is in front of s0.
    return p0Orientation == Orientation.COUNTERCLOCKWISE ? 1 : -1;
  }
}
