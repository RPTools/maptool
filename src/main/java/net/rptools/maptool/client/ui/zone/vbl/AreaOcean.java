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
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Set;

public class AreaOcean implements AreaContainer {

  private AreaMeta meta;
  private Set<AreaIsland> islandSet = new HashSet<AreaIsland>();

  public AreaOcean(AreaMeta meta) {
    this.meta = meta;
  }

  public Set<VisibleAreaSegment> getVisibleAreaSegments(Point2D origin) {

    Set<VisibleAreaSegment> segSet = new HashSet<VisibleAreaSegment>();

    // If an island contains the point, then we're
    // not in this ocean, short circuit out
    for (AreaIsland island : islandSet) {
      if (island.getBounds().contains(origin)) {
        return segSet;
      }
    }

    // Inside boundaries
    for (AreaIsland island : islandSet) {
      segSet.addAll(island.getVisibleAreaSegments(origin));
    }

    // Outside boundary
    if (meta != null) {
      segSet.addAll(meta.getVisibleAreas(origin));
    }

    return segSet;
  }

  public AreaOcean getDeepestOceanAt(Point2D point) {

    if (meta != null && !meta.area.contains(point)) {
      return null;
    }

    // If the point is in an island, then let the island figure it out
    for (AreaIsland island : islandSet) {
      if (island.getBounds().contains(point)) {
        return island.getDeepestOceanAt(point);
      }
    }

    return this;
  }

  public Set<AreaIsland> getIslands() {
    return new HashSet<AreaIsland>(islandSet);
  }

  public void addIsland(AreaIsland island) {
    islandSet.add(island);
  }

  ////
  // AREA CONTAINER
  public Area getBounds() {
    return meta != null ? meta.area : null;
  }
}
