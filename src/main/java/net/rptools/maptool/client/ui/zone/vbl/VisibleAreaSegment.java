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
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;
import net.rptools.maptool.util.GraphicsUtil;
import org.jetbrains.annotations.NotNull;

public class VisibleAreaSegment implements Comparable<VisibleAreaSegment> {
  private final Point2D origin;
  private final List<AreaFace> faceList = new LinkedList<AreaFace>();

  private Point2D centerPoint;
  private Path2D path;

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
      Rectangle2D bounds = getPath().getBounds2D();
      centerPoint = new Point2D.Double(bounds.getCenterX(), bounds.getCenterY());
    }
    return centerPoint;
  }

  public Path2D getPath() {
    if (path == null) {
      List<Point2D> pathPoints = new LinkedList<Point2D>();

      for (AreaFace face : faceList) {
        // Initial point
        if (pathPoints.size() == 0) {
          pathPoints.add(face.getP1());
        }
        pathPoints.add(face.getP2());
      }

      for (Point2D p : pathPoints) {
        if (path == null) {
          path = new GeneralPath();
          path.moveTo((float) p.getX(), (float) p.getY());
          continue;
        }
        path.lineTo((float) p.getX(), (float) p.getY());
      }
    }
    return path;
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

  ////
  // COMPARABLE
  public int compareTo(@NotNull VisibleAreaSegment o) {
    return Long.compare(getDistanceSqFromOrigin(), o.getDistanceSqFromOrigin());
  }
}
