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
import java.util.LinkedList;
import java.util.List;
import net.rptools.maptool.util.GraphicsUtil;
import org.jetbrains.annotations.NotNull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

public class VisibleAreaSegment implements Comparable<VisibleAreaSegment> {
  private static final GeometryFactory geometryFactory = new GeometryFactory();

  private final Point2D origin;
  private final List<AreaFace> faceList = new LinkedList<AreaFace>();

  private Point2D centerPoint;
  private Geometry boundary = null;

  public VisibleAreaSegment(Point2D origin) {
    this.origin = origin;
  }

  public void addAtEnd(AreaFace face) {
    faceList.add(face);
  }

  public void addAtFront(AreaFace face) {
    faceList.add(0, face);
  }

  public long getDistanceFromOrigin() {
    return (long) (getCenterPoint().distance(origin) * 1000);
  }

  public long getDistanceSqFromOrigin() {
    return (long) getCenterPoint().distanceSq(origin);
  }

  public Point2D getCenterPoint() {
    if (centerPoint == null) {
      var center = getBoundingBox().getEnvelopeInternal().centre();
      centerPoint = new Point2D.Double(center.getX(), center.getY());
    }
    return centerPoint;
  }

  public Area getArea() {
    if (faceList.isEmpty()) {
      return new Area();
    }
    List<Point2D> pathPoints = new LinkedList<Point2D>();

    for (AreaFace face : faceList) {
      // Initial point
      if (pathPoints.size() == 0) {
        pathPoints.add(face.getP1());
        pathPoints.add(
            0, GraphicsUtil.getProjectedPoint(origin, face.getP1(), Integer.MAX_VALUE / 2));
      }
      // Add to the path
      pathPoints.add(face.getP2());
      pathPoints.add(
          0, GraphicsUtil.getProjectedPoint(origin, face.getP2(), Integer.MAX_VALUE / 2));
    }

    GeneralPath path = null;
    for (Point2D p : pathPoints) {
      if (path == null) {
        path = new GeneralPath();
        path.moveTo((float) p.getX(), (float) p.getY());
        continue;
      }
      path.lineTo((float) p.getX(), (float) p.getY());
    }
    return new Area(path);
  }

  public Geometry getBoundingBox() {
    if (boundary == null) {
      boundary = getBoundaryPoints(faceList).getEnvelope();
    }
    return boundary;
  }

  private static Geometry getBoundaryPoints(List<AreaFace> faceList) {
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

  public Geometry getGeometry() {
    if (faceList.isEmpty()) {
      return geometryFactory.createGeometryCollection();
    }

    List<Coordinate> pathPoints = new ArrayList<>();

    for (AreaFace face : faceList) {
      // Initial point
      if (pathPoints.size() == 0) {
        pathPoints.add(toCoordinate(face.getP1()));
        pathPoints.add(
            0,
            toCoordinate(
                GraphicsUtil.getProjectedPoint(origin, face.getP1(), Integer.MAX_VALUE / 2)));
      }
      // Add to the path
      pathPoints.add(toCoordinate(face.getP2()));
      pathPoints.add(
          0,
          toCoordinate(
              GraphicsUtil.getProjectedPoint(origin, face.getP2(), Integer.MAX_VALUE / 2)));
    }
    // We need the ring to be closed.
    pathPoints.add(pathPoints.get(0));

    return geometryFactory.createPolygon(pathPoints.toArray(Coordinate[]::new));
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
