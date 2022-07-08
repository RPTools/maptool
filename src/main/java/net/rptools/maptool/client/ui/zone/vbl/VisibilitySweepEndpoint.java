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
import java.util.Collections;
import java.util.List;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;

/**
 * Represents a vertex used in the visibility sweep.
 *
 * <p>This class conveniently associates a point in space with the wall that are incident to it. The
 * walls are distinguished as starting or ending at this point, in agreement with the direction of
 * the sweep.
 */
public final class VisibilitySweepEndpoint {
  private final Coordinate point;
  private final double pseudoangle;
  private final double distance;
  private final List<LineSegment> startsWalls = new ArrayList<>();
  private final List<LineSegment> endsWalls = new ArrayList<>();

  public VisibilitySweepEndpoint(Coordinate point, Coordinate origin) {
    this.point = point;

    final var dx = point.getX() - origin.getX();
    final var dy = point.getY() - origin.getY();
    final var p = dx / (Math.abs(dx) + Math.abs(dy)); // -1 .. 1 increasing with x
    this.pseudoangle =
        (dy <= 0)
            ? p - 1 // -2 .. 0 increasing with x
            : 1 - p; // 0 .. 2 decreasing with x
    this.distance = point.distance(origin);
  }

  public Coordinate getPoint() {
    return point;
  }

  public List<LineSegment> getStartsWalls() {
    return Collections.unmodifiableList(startsWalls);
  }

  public List<LineSegment> getEndsWalls() {
    return Collections.unmodifiableList(endsWalls);
  }

  public void startsWall(LineSegment wall) {
    startsWalls.add(wall);
  }

  public void endsWall(LineSegment wall) {
    endsWalls.add(wall);
  }

  public double getPseudoangle() {
    return pseudoangle;
  }

  public double getDistance() {
    return distance;
  }

  @Override
  public String toString() {
    return String.format("VisibilitySweepEndpoint(%s)", point.toString());
  }
}
