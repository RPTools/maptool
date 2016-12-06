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

import java.awt.geom.Point2D;

public class LineSegmentId {

	private int x1;
	private int y1;
	private int x2;
	private int y2;

	public LineSegmentId(Point2D p1, Point2D p2) {

		x1 = (int) Math.min(p1.getX(), p2.getX());
		x2 = (int) Math.max(p1.getX(), p2.getX());

		y1 = (int) Math.min(p1.getY(), p2.getY());
		y2 = (int) Math.max(p1.getY(), p2.getY());
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof LineSegmentId)) {
			return false;
		}

		LineSegmentId line = (LineSegmentId) obj;

		return x1 == line.x1 && y1 == line.y1 && x2 == line.x2 && y2 == line.y2;
	}

	@Override
	public int hashCode() {
		// Doesn't have to be unique, only a decent spread
		return x1 + y1 + (x2 + y2) * 31;
	}
}
