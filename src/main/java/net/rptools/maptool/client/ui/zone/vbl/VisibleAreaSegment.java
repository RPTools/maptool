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

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import net.rptools.maptool.util.GraphicsUtil;
import org.jetbrains.annotations.NotNull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

public class VisibleAreaSegment implements Comparable<VisibleAreaSegment> {
  private final GeometryFactory geometryFactory;
  private final Point2D origin;
  private final List<AreaFace> faceList;

  private final Point2D centerPoint;
  private final Geometry boundary;

  public VisibleAreaSegment(
      GeometryFactory geometryFactory, Point2D origin, List<AreaFace> faceList) {
    assert !faceList.isEmpty();

    this.geometryFactory = geometryFactory;
    this.origin = origin;
    this.faceList = new ArrayList<>(faceList);

    this.boundary = getBoundaryPoints(faceList).getEnvelope();

    var center = this.boundary.getEnvelopeInternal().centre();
    centerPoint = new Point2D.Double(center.getX(), center.getY());
  }

  private long getDistanceSqFromOrigin() {
    return (long) centerPoint.distanceSq(origin);
  }

  public Geometry getBoundingBox() {
    return boundary;
  }

  public Geometry calculateVisionBlockedBySegment(int distance) {
    final List<Coordinate> coordinates = new ArrayList<>();
    for (final var face : faceList) {
      if (coordinates.isEmpty()) {
        coordinates.add(toCoordinate(face.getP1()));
        coordinates.add(
            0, toCoordinate(GraphicsUtil.getProjectedPoint(origin, face.getP1(), distance)));
      }

      coordinates.add(toCoordinate(face.getP2()));
      coordinates.add(
          0, toCoordinate(GraphicsUtil.getProjectedPoint(origin, face.getP2(), distance)));
    }
    // We need a closed ring.
    coordinates.add(coordinates.get(0));

    return geometryFactory.createPolygon(coordinates.toArray(Coordinate[]::new));
  }

  private Geometry getBoundaryPoints(List<AreaFace> faceList) {
    if (faceList.isEmpty()) {
      return geometryFactory.createMultiPoint();
    }
    Coordinate[] pathPoints = new Coordinate[1 + faceList.size()];
    int index = 0;
    for (AreaFace face : faceList) {
      // Initial point
      if (index == 0) {
        pathPoints[index++] = toCoordinate(face.getP1());
      }
      pathPoints[index++] = toCoordinate(face.getP2());
    }
    return geometryFactory.createMultiPointFromCoords(pathPoints);
  }

  private static Coordinate toCoordinate(Point2D point2D) {
    return new Coordinate(point2D.getX(), point2D.getY());
  }

  ////
  // COMPARABLE
  public int compareTo(@NotNull VisibleAreaSegment o) {
    return Long.compare(getDistanceSqFromOrigin(), o.getDistanceSqFromOrigin());
  }
}
