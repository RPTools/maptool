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

import java.awt.Point;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import org.locationtech.jts.geom.GeometryFactory;

/**
 * Represents a hole in the topology.
 *
 * <p>An ocean may contain islands of topology within it, and may or may not be contained within an
 * island.
 */
public class AreaOcean implements AreaContainer {

  private AreaMeta meta;
  private AreaIsland parentIsland = null;
  private Set<AreaIsland> islandSet = new HashSet<AreaIsland>();

  /**
   * Creates a new ocean with a given boundary.
   *
   * @param meta The boundary of a hole.
   */
  public AreaOcean(@Nullable AreaMeta meta) {
    assert meta == null || meta.isHole();
    this.meta = meta;
  }

  public AreaIsland getParentIsland() {
    return parentIsland;
  }

  public void setParentIsland(AreaIsland parentIsland) {
    this.parentIsland = parentIsland;
  }

  @Override
  public List<VisibleAreaSegment> getVisibleBoundarySegements(
      GeometryFactory geometryFactory, Point origin, boolean frontSegments) {
    if (meta == null) {
      return Collections.emptyList();
    }

    return meta.getFacingSegments(geometryFactory, origin, frontSegments);
  }

  public AreaOcean getDeepestOceanAt(Point2D point) {

    if (meta != null && !meta.area.contains(point)) {
      return null;
    }

    // If the point is in an island, then let the island figure it out
    for (AreaIsland island : islandSet) {
      AreaOcean ocean = island.getDeepestOceanAt(point);
      if (ocean != null) {
        return ocean;
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
