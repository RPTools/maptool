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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.locationtech.jts.geom.Coordinate;

/**
 * Represents a vertex used in the visibility sweep.
 *
 * <p>This class conveniently associates a point in space with the wall that are incident to it. The
 * walls are distinguished as starting or ending at this point, in agreement with the direction of
 * the sweep.
 */
public final class VisibilitySweepEndpoint {
  private final Coordinate point;
  private final List<VisibilitySweepEndpoint> startsWalls = new ArrayList<>();
  private final List<VisibilitySweepEndpoint> endsWalls = new ArrayList<>();

  public VisibilitySweepEndpoint(Coordinate point) {
    this.point = point;
  }

  public Coordinate getPoint() {
    return point;
  }

  public Collection<VisibilitySweepEndpoint> getStartsWalls() {
    return Collections.unmodifiableCollection(startsWalls);
  }

  public Collection<VisibilitySweepEndpoint> getEndsWalls() {
    return Collections.unmodifiableCollection(endsWalls);
  }

  public void startsWall(VisibilitySweepEndpoint end) {
    startsWalls.add(end);
  }

  public boolean removeStartedWall(VisibilitySweepEndpoint start) {
    return startsWalls.remove(start);
  }

  public void endsWall(VisibilitySweepEndpoint start) {
    endsWalls.add(start);
  }

  public boolean removeEndedWall(VisibilitySweepEndpoint start) {
    return endsWalls.remove(start);
  }

  /**
   * Adds the walls from {@code duplicate} into {@code this}.
   *
   * @param duplicate The duplicate endpoint to merge into {@code this}.
   */
  public void mergeDuplicate(VisibilitySweepEndpoint duplicate) {
    startsWalls.addAll(duplicate.startsWalls);
    endsWalls.addAll(duplicate.endsWalls);

    // Need to replace all references to `duplicate` with references to `this.
    for (final var otherEndpoint : duplicate.startsWalls) {
      final var index = otherEndpoint.endsWalls.indexOf(duplicate);
      assert index >= 0 : "Endpoint wall is invalid";
      otherEndpoint.endsWalls.set(index, this);
    }
    for (final var otherEndpoint : duplicate.endsWalls) {
      final var index = otherEndpoint.startsWalls.indexOf(duplicate);
      assert index >= 0 : "Endpoint wall is invalid";
      otherEndpoint.startsWalls.set(index, this);
    }
  }

  /** Don't actually call this, it is for testing / development purposes. */
  public void verify() {
    for (var otherEndpoint : this.startsWalls) {
      assert otherEndpoint.endsWalls.contains(this);
    }
    for (var otherEndpoint : this.endsWalls) {
      assert otherEndpoint.startsWalls.contains(this);
    }
  }

  @Override
  public String toString() {
    return String.format("VisibilitySweepEndpoint(%s)", point.toString());
  }
}
