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
