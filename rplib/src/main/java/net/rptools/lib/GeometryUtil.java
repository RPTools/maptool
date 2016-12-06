/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package net.rptools.lib;

import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GeometryUtil {
	public static Line2D findClosestLine(Point2D origin, PointNode pointList) {
		Line2D line = null;
		double distance = 0;

		PointNode node = pointList;
		do {
			Line2D newLine = new Line2D.Double(node.previous.point, node.point);
			double newDistance = getDistanceToCenter(origin, newLine);
			if (line == null || newDistance < distance) {
				line = newLine;
				distance = newDistance;
			}
			node = node.next;
		} while (node != pointList);

		return line;
	}

	public static double getDistanceToCenter(Point2D p, Line2D line) {
		Point2D midPoint = new Point2D.Double((line.getP1().getX() + line.getP2().getX()) / 2, (line.getP1().getY() + line.getP2().getY()) / 2);

		return Math.hypot(midPoint.getX() - p.getX(), midPoint.getY() - p.getY());
	}

	public static Point2D getCloserPoint(Point2D origin, Line2D line) {
		double dist1 = Math.hypot(origin.getX() - line.getP1().getX(), origin.getY() - line.getP1().getY());
		double dist2 = Math.hypot(origin.getX() - line.getP2().getX(), origin.getY() - line.getP2().getY());

		return dist1 < dist2 ? line.getP1() : line.getP2();
	}

	public static double getAngle(Point2D origin, Point2D target) {
		double angle = Math.toDegrees(Math.atan2((origin.getY() - target.getY()), (target.getX() - origin.getX())));
		if (angle < 0) {
			angle += 360;
		}
		return angle;
	}

	public static double getAngleDelta(double sourceAngle, double targetAngle) {
		// Normalize
		targetAngle -= sourceAngle;

		if (targetAngle > 180) {
			targetAngle -= 360;
		}
		if (targetAngle < -180) {
			targetAngle += 360;
		}
		return targetAngle;
	}

	public static class PointNode {
		public PointNode previous;
		public PointNode next;
		public Point2D point;

		public PointNode(Point2D point) {
			this.point = point;
		}
	}

	public static double getDistanceXXX(Point2D p1, Point2D p2) {
		double a = p2.getX() - p1.getX();
		double b = p2.getY() - p1.getY();
		return Math.sqrt(a * a + b * b); // Was just "a+b" -- was that on purpose?  A shortcut speed-up perhaps?
	}

	public static Set<Line2D> getFrontFaces(PointNode nodeList, Point2D origin) {
		Set<Line2D> frontFaces = new HashSet<Line2D>();

		Line2D closestLine = GeometryUtil.findClosestLine(origin, nodeList);
		Point2D closestPoint = GeometryUtil.getCloserPoint(origin, closestLine);
		PointNode closestNode = nodeList;
		do {
			if (closestNode.point.equals(closestPoint)) {
				break;
			}
			closestNode = closestNode.next;
		} while (closestNode != nodeList);

		Point2D secondPoint = closestLine.getP1().equals(closestPoint) ? closestLine.getP2() : closestLine.getP1();
		Point2D thirdPoint = secondPoint.equals(closestNode.next.point) ? closestNode.previous.point : closestNode.next.point;

		// Determine whether the first line segment is visible
		Line2D l1 = new Line2D.Double(origin, secondPoint);
		Line2D l2 = new Line2D.Double(closestNode.point, thirdPoint);
		boolean frontFace = !(l1.intersectsLine(l2));
		if (frontFace) {
			frontFaces.add(new Line2D.Double(closestPoint, secondPoint));
		}
		Point2D startPoint = closestNode.previous.point.equals(secondPoint) ? secondPoint : closestNode.point;
		Point2D endPoint = closestNode.point.equals(startPoint) ? secondPoint : closestNode.point;
		double originAngle = GeometryUtil.getAngle(origin, startPoint);
		double pointAngle = GeometryUtil.getAngle(startPoint, endPoint);
		int lastDirection = GeometryUtil.getAngleDelta(originAngle, pointAngle) > 0 ? 1 : -1;

		//		System.out.format("%s: %.2f %s, %.2f %s => %.2f : %d : %s\n", frontFace, originAngle, startPoint.toString(), pointAngle, endPoint.toString(), getAngleDelta(originAngle, pointAngle), lastDirection, (closestNode.previous.point.equals(secondPoint) ? "second" : "closest").toString());
		PointNode node = secondPoint.equals(closestNode.next.point) ? closestNode.next : closestNode;
		do {
			Point2D point = node.point;
			Point2D nextPoint = node.next.point;

			originAngle = GeometryUtil.getAngle(origin, point);
			pointAngle = GeometryUtil.getAngle(origin, nextPoint);

			//			System.out.println(point + ":" + originAngle + ", " + nextPoint + ":"+ pointAngle + ", " + getAngleDelta(originAngle, pointAngle));
			if (GeometryUtil.getAngleDelta(originAngle, pointAngle) > 0) {
				if (lastDirection < 0) {
					frontFace = !frontFace;
					lastDirection = 1;
				}
			} else {
				if (lastDirection > 0) {
					frontFace = !frontFace;
					lastDirection = -1;
				}
			}
			if (frontFace) {
				frontFaces.add(new Line2D.Double(nextPoint, point));
			}
			node = node.next;
		} while (!node.point.equals(closestPoint));

		return frontFaces;
	}

	public static int countAreaPoints(Area area) {
		int count = 0;
		for (PathIterator iter = area.getPathIterator(null); !iter.isDone(); iter.next()) {
			count++;
		}
		return count;
	}

	public static Area createLine(List<Point2D> points, int width) {
		Point2D lastPoint = null;
		Line2D lastLine = null;
		for (Point2D point : points) {
			if (lastPoint == null) {
				lastPoint = point;
				continue;
			}
			if (lastLine == null) {
				// First line segment
				lastLine = new Line2D.Double(lastPoint, point);

				// Keep track 
				continue;
			}
		}
		GeneralPath path = new GeneralPath();
		return new Area(path);
	}
}
