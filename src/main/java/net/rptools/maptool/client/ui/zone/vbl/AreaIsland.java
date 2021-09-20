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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.locationtech.jts.geom.GeometryFactory;

import javax.annotation.Nullable;

/**
 * Represents a piece of solid topology.
 *
 * <p>An island can contain holes, known as oceans, and will itself belong to an ocean.
 */
public class AreaIsland implements AreaContainer {

  private AreaMeta meta;
  private boolean isTerrainVbl;
  private AreaOcean parentOcean;
  private Set<AreaOcean> oceanSet = new HashSet<AreaOcean>();

  /**
   * Creates a new island with a given boundary.
   *
   * @param meta The boundary of the island. Must be a hole.
   */
  public AreaIsland(AreaMeta meta, boolean isTerrainVbl) {
    assert !meta.isHole();
    this.meta = meta;
    this.isTerrainVbl = isTerrainVbl;
    this.parentOcean = null;
  }

  /** @return true if this island represents terrain VBL. */
  public boolean isTerrain() {
    return isTerrainVbl;
  }

  public AreaOcean getParentOcean() {
    return parentOcean;
  }

  public void setParentOcean(AreaOcean parentOcean) {
    this.parentOcean = parentOcean;
  }

  @Override
  public @Nullable AreaContainer getDeepestContainerAt(Point2D point) {
    if (!meta.area.contains(point)) {
      // Point not contained within this island, so nothing to return.
      return null;
    }

    for (AreaOcean ocean : oceanSet) {
      AreaContainer deepContainer = ocean.getDeepestContainerAt(point);
      if (deepContainer != null) {
        return deepContainer;
      }
    }

    return this;
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

  @Override
  public List<VisibleAreaSegment> getVisibleBoundarySegements(
      GeometryFactory geometryFactory, Point origin, boolean frontSegments) {
    return meta.getFacingSegments(geometryFactory, origin, !frontSegments);
  }
}
