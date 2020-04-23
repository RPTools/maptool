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
package net.rptools.maptool.client.walker;

import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import net.rptools.maptool.client.ui.zone.RenderPathWorker;
import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.Path;
import net.rptools.maptool.model.Token.TerrainModifierOperation;
import net.rptools.maptool.model.Zone;

public abstract class AbstractZoneWalker implements ZoneWalker {

  protected List<PartialPath> partialPaths =
      Collections.synchronizedList(new ArrayList<PartialPath>());
  protected final Zone zone;
  protected boolean restrictMovement = false;
  protected Set<TerrainModifierOperation> terrainModifiersIgnored;
  protected Area tokenVBL;
  protected RenderPathWorker renderPathWorker;

  public AbstractZoneWalker(Zone zone) {
    this.zone = zone;
  }

  public Zone getZone() {
    return zone;
  }

  public CellPoint getLastPoint() {
    synchronized (partialPaths) {
      if (partialPaths.isEmpty()) {
        return null;
      } else {
        return partialPaths.get(partialPaths.size() - 1).end;
      }
    }
  }

  public void setWaypoints(CellPoint... points) {
    partialPaths.clear();
    addWaypoints(points);
  }

  public void addWaypoints(CellPoint... points) {
    CellPoint previous =
        partialPaths.size() > 0 ? partialPaths.get(partialPaths.size() - 1).end : null;
    for (CellPoint current : points) {
      if (previous != null) {
        partialPaths.add(new PartialPath(previous, current, calculatePath(previous, current)));
      }
      previous = current;
    }
  }

  public CellPoint replaceLastWaypoint(CellPoint point) {
    return replaceLastWaypoint(
        point, false, Collections.singleton(TerrainModifierOperation.NONE), null);
  }

  @Override
  public CellPoint replaceLastWaypoint(
      CellPoint point,
      boolean restrictMovement,
      Set<TerrainModifierOperation> terrainModifiersIgnored,
      Area tokenVBL) {

    this.restrictMovement = restrictMovement;
    this.terrainModifiersIgnored = terrainModifiersIgnored;
    this.tokenVBL = tokenVBL;

    if (partialPaths.isEmpty()) {
      return null;
    }
    PartialPath oldPartial = partialPaths.remove(partialPaths.size() - 1);

    // short circuit exit if the point hasn't changed.
    // if (oldPartial.end.equals(point))
    // return null;

    partialPaths.add(
        new PartialPath(oldPartial.start, point, calculatePath(oldPartial.start, point)));
    return oldPartial.end;
  }

  public Path<CellPoint> getPath(RenderPathWorker renderPathWorker) {
    this.renderPathWorker = renderPathWorker;
    return getPath();
  }

  public Path<CellPoint> getPath() {
    Path<CellPoint> path = new Path<CellPoint>();

    PartialPath last = null;

    synchronized (partialPaths) {
      for (PartialPath partial : partialPaths) {
        if (partial.path != null && partial.path.size() > 1) {
          path.addAllPathCells(partial.path.subList(0, partial.path.size() - 1));
        }
        last = partial;
      }
    }

    if (last != null) {
      path.addPathCell(last.end);
    }

    for (CellPoint cp : path.getCellPath()) {
      if (isWaypoint(cp)) {
        path.addWayPoint(cp);
      }
    }

    return path;
  }

  public boolean isWaypoint(CellPoint point) {
    if (point == null) {
      return false;
    }

    PartialPath last = null;
    for (PartialPath partial : partialPaths) {
      if (partial.start.equals(point)) {
        return true;
      }

      last = partial;
    }
    if (last != null && last.end != null && last.end.equals(point)) {
      return true;
    }

    return false;
  }

  /**
   * @see
   *     net.rptools.maptool.client.walker.ZoneWalker#removeWaypoint(net.rptools.maptool.model.CellPoint)
   */
  public boolean removeWaypoint(CellPoint aPoint) {
    if (aPoint == null || partialPaths == null || partialPaths.isEmpty()) {
      return false;
    }

    // Find the partial path with the given end point
    ListIterator<PartialPath> i = partialPaths.listIterator();
    while (i.hasNext()) {
      PartialPath path = i.next();
      if (path.end.equals(aPoint)) {
        // If this is the last partial path then done, otherwise
        // combine this path and the next and replace them with a combined path
        if (!i.hasNext()) {
          return false;
        }
        i.remove();
        PartialPath path2 = i.next();
        i.set(new PartialPath(path.start, path2.end, calculatePath(path.start, path2.end)));
        return true;
      } // endif
    } // endwhile
    return false;
  }

  /**
   * @see
   *     net.rptools.maptool.client.walker.ZoneWalker#toggleWaypoint(net.rptools.maptool.model.CellPoint)
   */
  public boolean toggleWaypoint(CellPoint aPoint) {
    if (removeWaypoint(aPoint)) {
      return true;
    }
    addWaypoints(aPoint);
    return true;
  }

  /** @see java.lang.Object#toString() */
  @Override
  public String toString() {
    StringBuilder s = new StringBuilder("Path: ");
    for (PartialPath path : partialPaths) {
      s.append("\n   ");
      s.append(path.toString());
    } // endfor
    return s.toString();
  }

  protected abstract List<CellPoint> calculatePath(CellPoint start, CellPoint end);

  protected static class PartialPath {

    final CellPoint start;
    final CellPoint end;
    final List<CellPoint> path;

    public PartialPath(CellPoint start, CellPoint end, List<CellPoint> path) {
      this.start = start;
      this.end = end;
      this.path = path;

      // Get the distance traveled from the last partial path, eg from last waypoint...
      if (!path.isEmpty()) {
        this.end.distanceTraveled = path.get(path.size() - 1).distanceTraveled;
        this.end.distanceTraveledWithoutTerrain =
            path.get(path.size() - 1).distanceTraveledWithoutTerrain;
      }
    }

    /** @see java.lang.Object#toString() */
    @Override
    public String toString() {
      StringBuilder s = new StringBuilder("PartialPath([");
      s.append(start.x);
      s.append(",");
      s.append(start.y);
      s.append("], [");
      s.append(end.x);
      s.append(",");
      s.append(end.y);
      s.append("]");
      return s.toString();
    }
  }
}
