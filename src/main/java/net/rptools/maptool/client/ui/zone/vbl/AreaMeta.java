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

import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.prep.PreparedGeometry;

/** Represents the boundary of a piece of topology. */
public class AreaMeta {
  private Area area;
  private List<Coordinate> vertices = new ArrayList<>();

  // Only used during construction
  private boolean isHole;
  private GeneralPath path;

  public AreaMeta() {}

  public boolean contains(Point2D point) {
    return area.contains(point);
  }

  public Area getBounds() {
    return new Area(area);
  }

  /**
   * @param origin
   * @param faceAway If `true`, only return segments facing away from origin.
   * @return
   */
  public List<LineString> getFacingSegments(
      GeometryFactory geometryFactory,
      Coordinate origin,
      boolean faceAway,
      PreparedGeometry vision) {
    final var requiredOrientation = faceAway ? Orientation.CLOCKWISE : Orientation.COUNTERCLOCKWISE;
    List<LineString> segments = new ArrayList<>();
    List<Coordinate> currentSegmentPoints = new ArrayList<>();

    Coordinate current = null;
    for (Coordinate coordinate : vertices) {
      assert currentSegmentPoints.size() == 0 || currentSegmentPoints.size() >= 2;

      final var previous = current;
      current = coordinate;
      if (previous == null) {
        continue;
      }

      final var faceLineSegment = new LineSegment(previous, coordinate);
      final var orientation = faceLineSegment.orientationIndex(origin);
      final var shouldIncludeFace =
          (orientation == requiredOrientation)
              && vision.intersects(faceLineSegment.toGeometry(geometryFactory));

      if (shouldIncludeFace) {
        // Since we're including this face, the existing segment can be extended.
        if (currentSegmentPoints.isEmpty()) {
          // Also need the first point.
          currentSegmentPoints.add(faceLineSegment.p0);
        }
        currentSegmentPoints.add(faceLineSegment.p1);
      } else if (!currentSegmentPoints.isEmpty()) {
        // Since we're skipping this face, the segment is broken and we must start a new one.
        segments.add(
            geometryFactory.createLineString(currentSegmentPoints.toArray(Coordinate[]::new)));
        currentSegmentPoints.clear();
      }
    }
    assert currentSegmentPoints.size() == 0 || currentSegmentPoints.size() >= 2;
    // In case there is still current segment, we add it.
    if (!currentSegmentPoints.isEmpty()) {
      segments.add(
          geometryFactory.createLineString(currentSegmentPoints.toArray(Coordinate[]::new)));
    }

    return segments;
  }

  public boolean isHole() {
    return isHole;
  }

  public void addPoint(double x, double y) {
    // Cut out redundant points
    // TODO: This works ... in concept, but in practice it can create holes that pop outside of
    // their parent bounds
    // for really thin diagonal lines. At some point this could be moved to a post processing step,
    // after the
    // islands have been placed into their oceans. But that's an optimization for another day
    // if (lastPointNode != null && GeometryUtil.getDistance(lastPointNode.point, new
    // Point2D.Float(x, y)) < 1.5) {
    // skippedPoints++;
    // return;
    // }
    final var vertex = new Coordinate(x, y);
    if (!vertices.isEmpty()) {
      final var lastVertex = vertices.get(vertices.size() - 1);
      // Don't add if we haven't moved
      if (lastVertex.equals(vertex)) {
        return;
      }
    }
    vertices.add(vertex);

    if (path == null) {
      path = new GeneralPath();
      path.moveTo(x, y);
    } else {
      path.lineTo(x, y);
    }
  }

  public void close() {
    area = new Area(path);

    // Close the circle.
    // For some odd reason, sometimes the first and last point are already the same, so don't add
    // the point again in that case.
    final var first = vertices.get(0);
    final var last = vertices.get(vertices.size() - 1);
    if (!first.equals(last)) {
      vertices.add(first);
    }

    isHole = Orientation.isCCW(vertices.toArray(Coordinate[]::new));

    // Don't need this anymore
    path = null;
    // System.out.println("AreaMeta.skippedPoints: " + skippedPoints + " h:" + isHole + " f:" +
    // faceList.size());
  }
}
