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

public class AreaIsland implements AreaContainer {

  private AreaMeta meta;
  private Set<AreaOcean> oceanSet = new HashSet<AreaOcean>();

  public AreaIsland(AreaMeta meta) {
    this.meta = meta;
  }

  public Set<VisibleAreaSegment> getVisibleAreaSegments(Point2D origin) {

    return meta.getVisibleAreas(origin);
  }

  public AreaOcean getDeepestOceanAt(Point2D point) {

    if (!meta.area.contains(point)) {
      return null;
    }

    for (AreaOcean ocean : oceanSet) {
      AreaOcean deepOcean = ocean.getDeepestOceanAt(point);
      if (deepOcean != null) {
        return deepOcean;
      }
    }

    // If we don't have an ocean that contains the point then
    // the point is not technically in an ocean
    return null;
  }

  public Set<AreaOcean> getOceans() {
    return new HashSet<AreaOcean>(oceanSet);
  }

  public void addOcean(AreaOcean ocean) {
    oceanSet.add(ocean);
  }

  ////
  // AREA CONTAINER
  public Area getBounds() {
    return meta.area;
  }
}
