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
import net.rptools.lib.GeometryUtil;
import net.rptools.lib.GeometryUtil.PointNode;
import org.locationtech.jts.geom.GeometryFactory;

/** Represents the boundary of a piece of topology. */
public class AreaMeta {
  Area area;
  Point2D centerPoint;
  List<AreaFace> faceList = new ArrayList<AreaFace>();

  // Only used during construction
  boolean isHole;
  PointNode pointNodeList;
  GeneralPath path;
  PointNode lastPointNode;

  public AreaMeta() {}

  public Point2D getCenterPoint() {
    if (centerPoint == null) {
      centerPoint =
          new Point2D.Double(
              area.getBounds().x + area.getBounds().width / 2,
              area.getBounds().y + area.getBounds().height / 2);
    }
    return centerPoint;
  }

  /**
   * @param origin
   * @param faceAway If `true`, only return segments facing away from origin.
   * @return
   */
  public List<VisibleAreaSegment> getFacingSegments(
      GeometryFactory geometryFactory, Point2D origin, boolean faceAway) {
    List<VisibleAreaSegment> segments = new ArrayList<>();
    List<AreaFace> currentSegmentFaces = new ArrayList<>();

    for (AreaFace face : faceList) {
      double originAngle = GeometryUtil.getAngle(origin, face.getMidPoint());
      double delta = Math.abs(GeometryUtil.getAngleDelta(originAngle, face.getFacing()));

      boolean shouldIncludeFace = (delta > 90) == faceAway;
      if (shouldIncludeFace) {
        // Since we're including this face, the existing segment can be extended.
        currentSegmentFaces.add(face);
      } else if (!currentSegmentFaces.isEmpty()) {
        // Since we're skipping this face, the segment is broken and we must start a new one.
        segments.add(new VisibleAreaSegment(geometryFactory, origin, currentSegmentFaces));
        currentSegmentFaces = new ArrayList<>();
      }
    }
    // In case there is still current segment, we add it.
    if (!currentSegmentFaces.isEmpty()) {
      segments.add(new VisibleAreaSegment(geometryFactory, origin, currentSegmentFaces));
    }

    return segments;
  }

  public boolean isHole() {
    return isHole;
  }

  public void addPoint(float x, float y) {
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
    PointNode pointNode = new PointNode(new Point2D.Double(x, y));

    // Don't add if we haven't moved
    if (lastPointNode != null && lastPointNode.point.equals(pointNode.point)) {
      return;
    }
    if (path == null) {
      path = new GeneralPath();
      path.moveTo(x, y);

      pointNodeList = pointNode;
    } else {
      path.lineTo(x, y);

      lastPointNode.next = pointNode;
      pointNode.previous = lastPointNode;
    }
    lastPointNode = pointNode;
  }

  public void close() {
    area = new Area(path);

    // Close the circle
    lastPointNode.next = pointNodeList;
    pointNodeList.previous = lastPointNode;
    lastPointNode = null;

    // For some odd reason, sometimes the first and last point are the same, which causes
    // bugs in the way areas are calculated
    if (pointNodeList.point.equals(pointNodeList.previous.point)) {
      // Pull out the dupe node
      PointNode trueLastPoint = pointNodeList.previous.previous;
      trueLastPoint.next = pointNodeList;
      pointNodeList.previous = trueLastPoint;
    }
    computeIsHole();
    computeFaces();

    // Don't need point list anymore
    pointNodeList = null;
    path = null;
    // System.out.println("AreaMeta.skippedPoints: " + skippedPoints + " h:" + isHole + " f:" +
    // faceList.size());
  }

  private void computeIsHole() {
    double angle = 0;

    PointNode currNode = pointNodeList.next;

    while (currNode != pointNodeList) {
      double currAngle =
          GeometryUtil.getAngleDelta(
              GeometryUtil.getAngle(currNode.previous.point, currNode.point),
              GeometryUtil.getAngle(currNode.point, currNode.next.point));

      angle += currAngle;
      currNode = currNode.next;
    }
    isHole = angle < 0;
  }

  private void computeFaces() {
    PointNode node = pointNodeList;
    do {
      faceList.add(new AreaFace(node.point, node.next.point));
      node = node.next;
    } while (!node.point.equals(pointNodeList.point));
  }
}
