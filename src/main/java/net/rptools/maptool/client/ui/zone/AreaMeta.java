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
package net.rptools.maptool.client.ui.zone;

import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Set;
import net.rptools.lib.GeometryUtil;
import net.rptools.lib.GeometryUtil.PointNode;

public class AreaMeta {
  Area area;
  Point2D centerPoint;
  Set<AreaFace> faceSet;

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

  public Set<AreaFace> getFrontFaces(Point2D origin) {
    Set<AreaFace> faces = new HashSet<AreaFace>();
    for (AreaFace face : faceSet) {
      double originAngle = GeometryUtil.getAngle(origin, face.getMidPoint());
      double delta = GeometryUtil.getAngleDelta(originAngle, face.getFacing());

      if (Math.abs(delta) < 90) {
        continue;
      }
      faces.add(face);
    }
    return faces;
  }

  public Area getArea() {
    return new Area(area);
  }

  public boolean isHole() {
    return isHole;
  }

  public void addPoint(float x, float y) {
    PointNode pointNode = new PointNode(new Point2D.Double(x, y));

    // Don't add if we haven't moved
    if (lastPointNode != null) {
      if (lastPointNode.point.equals(pointNode.point)) {
        return;
      }
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
    faceSet = new HashSet<AreaFace>();

    PointNode node = pointNodeList;
    do {
      faceSet.add(new AreaFace(node.point, node.previous.point));
      node = node.next;
    } while (!node.point.equals(pointNodeList.point));
  }
}
