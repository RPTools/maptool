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
import java.util.List;
import java.util.function.Consumer;
import net.rptools.lib.GeometryUtil;
import org.locationtech.jts.algorithm.InteriorPointArea;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.algorithm.PointLocation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Location;

/** Represents the boundary of a piece of topology. */
public class AreaMeta {
  private final Coordinate[] vertices;

  // region These fields are built from `vertices` and exist only for performance reasons.

  private final Coordinate interiorPoint;
  private final Envelope boundingBox;
  private final boolean isOcean;

  // endregion

  /**
   * Creates an AreaMeta that represents the entire plane.
   *
   * <p>Since this is a sea of nothing, it counts as an ocean despite having no endpoints.
   */
  public AreaMeta() {
    this.vertices = new Coordinate[0];
    this.interiorPoint = new Coordinate(0, 0);
    this.boundingBox = null;
    this.isOcean = true;
  }

  public AreaMeta(LinearRing ring) {
    vertices = ring.getCoordinates();
    assert vertices.length >= 4; // Yes, 4, because a ring duplicates its first element as its last.

    // Creating a Polygon here is a necessary evil since JTS does not seem to expose the interior
    // point algorithm for a plain ring. But it doesn't cost very much thankfully.
    this.interiorPoint =
        InteriorPointArea.getInteriorPoint(GeometryUtil.getGeometryFactory().createPolygon(ring));
    boundingBox = CoordinateArrays.envelope(vertices);
    isOcean = Orientation.isCCW(vertices);
  }

  public double getBoundingBoxArea() {
    if (vertices.length == 0) {
      return Double.POSITIVE_INFINITY;
    }

    return boundingBox.getArea();
  }

  public Coordinate getInteriorPoint() {
    return interiorPoint;
  }

  public boolean contains(Coordinate point) {
    if (vertices.length == 0) {
      return true;
    }

    if (!boundingBox.contains(point)) {
      return false;
    }

    // Oceans (holes) are open (do not include their boundary). This makes masks like Wall VBL
    // function correctly by ensuring any intersection with the mask counts as being inside.
    // On the other hand it is not sufficient to make Pit VBL behave correctly on the boundary.
    // though it doesn't break vision.
    final var location = PointLocation.locateInRing(point, vertices);
    if (isOcean) {
      return location == Location.INTERIOR;
    } else {
      return location != Location.EXTERIOR;
    }
  }

  /**
   * Find all sections of the boundary that block vision.
   *
   * <p>For each line segment, the exterior region will be on one side of the segment while the
   * interior region will be on the other side. One of these regions will be an island and one will
   * be an ocean depending on {@link #isOcean()}. The {@code facing} parameter uses this fact to
   * control whether a segment should be included in the result, based on whether the origin is on
   * the island-side of the line segment or on its ocean-side.
   *
   * <p>If {@code origin} is colinear with a line segment, that segment will never be returned.
   *
   * @param origin The vision origin, which is the point by which line segment orientation is
   *     measured.
   * @param facing Whether the island-side or the ocean-side of the returned segments must face
   *     {@code origin}.
   * @param visionBounds The bounding box for vision, used to avoid adding unnecessary far away
   *     segments.
   * @param resultConsumer Each produced segment string will be sent to this consumer.
   */
  public void getFacingSegments(
      Coordinate origin,
      Facing facing,
      Envelope visionBounds,
      Consumer<List<Coordinate>> resultConsumer) {
    if (vertices.length == 0) {
      return;
    }

    final var requiredOrientation =
        facing == Facing.ISLAND_SIDE_FACES_ORIGIN
            ? Orientation.CLOCKWISE
            : Orientation.COUNTERCLOCKWISE;

    List<Coordinate> currentSegmentPoints = new ArrayList<>();
    for (int i = 1; i < vertices.length; ++i) {
      assert currentSegmentPoints.size() == 0 || currentSegmentPoints.size() >= 2;

      final var previous = vertices[i - 1];
      final var current = vertices[i];

      final var shouldIncludeFace =
          // Don't need to be especially precise with the vision check.
          visionBounds.intersects(previous, current)
              && requiredOrientation == Orientation.index(origin, previous, current);

      if (shouldIncludeFace) {
        // Since we're including this face, the existing segment can be extended.
        if (currentSegmentPoints.isEmpty()) {
          // Also need the first point.
          currentSegmentPoints.add(previous);
        }
        currentSegmentPoints.add(current);
      } else if (!currentSegmentPoints.isEmpty()) {
        // Since we're skipping this face, the segment is broken and we must start a new one.
        var string = currentSegmentPoints;
        if (requiredOrientation != Orientation.COUNTERCLOCKWISE) {
          string = string.reversed();
        }
        resultConsumer.accept(string);
        currentSegmentPoints.clear();
      }
    }

    assert currentSegmentPoints.size() == 0 || currentSegmentPoints.size() >= 2;
    if (!currentSegmentPoints.isEmpty()) {
      var string = currentSegmentPoints;
      if (requiredOrientation != Orientation.COUNTERCLOCKWISE) {
        string = string.reversed();
      }
      resultConsumer.accept(string);
    }
  }

  public boolean isOcean() {
    return isOcean;
  }
}
