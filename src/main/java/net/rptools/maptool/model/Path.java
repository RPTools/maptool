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
package net.rptools.maptool.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.renderer.SelectionSet;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.client.walker.NaiveWalker;
import net.rptools.maptool.server.proto.PathDto;
import net.rptools.maptool.server.proto.drawing.IntPointDto;

public class Path<T extends AbstractPoint> {
  private final List<T> cellList = new LinkedList<T>();
  private final List<T> waypointList = new LinkedList<T>();

  protected Object readResolve() {
    // If any AStarCellPoints could not be converted to CellPoint, we get `null`s in these lists.
    // In such a case, the path is meaningless so we just clear it.
    if (cellList.contains(null) || waypointList.contains(null)) {
      cellList.clear();
      waypointList.clear();
    }

    return this;
  }

  public void addPathCell(T point) {
    cellList.add(point);
  }

  public void addAllPathCells(List<T> cells) {
    cellList.addAll(cells);
  }

  public List<T> getCellPath() {
    return Collections.unmodifiableList(cellList);
  }

  public void replaceLastPoint(T point) {
    cellList.remove(cellList.size() - 1);
    cellList.add(point);
  }

  public void addWayPoint(T point) {
    waypointList.add(point);
  }

  public boolean isWaypoint(T point) {
    return waypointList.contains(point);
  }

  public T getLastWaypoint() {
    if (waypointList.isEmpty()) return null;
    return waypointList.get(waypointList.size() - 1);
  }

  /**
   * Lee: I wonder why this convenience method was never put in. Rectifying...
   *
   * @return way point list for path
   */
  public List<T> getWayPointList() {
    return waypointList;
  }

  /**
   * Returns the last waypoint if there is one, or the last T point if there is not.
   *
   * @return a non-<code>null</code> location
   */
  public T getLastJunctionPoint() {
    T temp = getLastWaypoint();
    return temp != null ? temp : cellList.get(cellList.size() - 1);
  }

  @SuppressWarnings("unchecked")
  public Path<T> derive(
      SelectionSet set,
      Token keyToken,
      Token followerToken,
      int cellOffsetX,
      int cellOffsetY,
      ZonePoint startPoint,
      ZonePoint endPoint) {

    /*
     * Lee: aiming to fix the following here (snapped = snapped to grid): a. fixing snapped tokens full path when following an unsnapped key token b. fixing zone point precision for unsnapped
     * tokens following a snapped key token
     */

    Path<T> path = new Path<T>();
    // Lee: caching
    ZoneRenderer zr = MapTool.getFrame().getCurrentZoneRenderer();
    Zone zone = zr.getZone();
    Grid grid = zone.getGrid();

    if (keyToken.isSnapToGrid() && !followerToken.isSnapToGrid()) {
      ZonePoint buildVal = startPoint;
      Path<ZonePoint> processPath = new Path<ZonePoint>();
      for (T p : cellList) {
        ZonePoint tempPoint = (ZonePoint) buildVal.clone();
        processPath.addPathCell(tempPoint);
        if (waypointList.contains(p)) processPath.addWayPoint(tempPoint);

        if (buildVal.x < endPoint.x) buildVal.x += 100;
        else if (buildVal.x > endPoint.x) buildVal.x -= 100;
        if (buildVal.y < endPoint.y) buildVal.y += 100;
        else if (buildVal.y > endPoint.y) buildVal.y -= 100;
      }

      path = (Path<T>) processPath;

    } else if (!keyToken.isSnapToGrid() && followerToken.isSnapToGrid()) {
      NaiveWalker nw = new NaiveWalker(zone);
      Path<CellPoint> processPath = new Path<CellPoint>();

      CellPoint prevPoint = grid.convert(new ZonePoint(startPoint.x, startPoint.y));
      CellPoint terminalPoint = grid.convert(endPoint);
      CellPoint convPoint;

      Path<ZonePoint> wpl = set.getGridlessPath();
      List<T> waypointCheck = new LinkedList();
      List<ZonePoint> cp = wpl.getCellPath();
      ZonePoint keyStart = cp.get(0);
      ZonePoint diffFromKey = new ZonePoint(keyStart.x - startPoint.x, keyStart.y - startPoint.y);

      // Lee: list is unmodifiable, working around it
      int indexCheck = 0;
      for (ZonePoint zp : cp) {

        if (indexCheck != 0 && indexCheck != cp.size() - 1 && !waypointCheck.contains(zp)) {
          zp.x = zp.x + diffFromKey.x;
          zp.y = zp.y + diffFromKey.y;
          waypointCheck.add((T) zp);
        }

        indexCheck++;
      }

      if (!waypointCheck.isEmpty()) {
        for (T p : waypointCheck) {
          if (p instanceof ZonePoint) convPoint = grid.convert((ZonePoint) p);
          else convPoint = (CellPoint) p;
          processPath.addAllPathCells(nw.calculatePath(prevPoint, convPoint));
          prevPoint = convPoint;
        }
      }
      processPath.addAllPathCells(nw.calculatePath(prevPoint, terminalPoint));

      path = (Path<T>) processPath;

      for (T p : waypointCheck) {
        if (p instanceof ZonePoint) convPoint = grid.convert((ZonePoint) p);
        else convPoint = (CellPoint) p;

        T next = (T) convPoint.clone();
        next.x -= cellOffsetX;
        next.y -= cellOffsetY;
        path.addWayPoint(next);
      }

    } else {
      for (T cp : cellList) {
        T np = (T) cp.clone();
        np.x -= cellOffsetX;
        np.y -= cellOffsetY;
        path.addPathCell(np);
      }

      for (T cp : waypointList) {
        T np = (T) cp.clone();
        np.x -= cellOffsetX;
        np.y -= cellOffsetY;
        path.addWayPoint(np);
      }

      /*
       * Not exactly sure what Lee was trying to do here?
       * I believe he was trying to return all the "cells" a non-STG token moved though?
       * I'll leave the code below in case someone wants to clean it up later.
       * For now, I've restored partial logic back to 1.4.0.5 above.
       */

      /*
      	// Lee: solo movement
      	if (keyToken.isSnapToGrid()) {
      		for (T cp : cellList) {
      			T np = (T) cp.clone();
      			np.x -= cellOffsetX;
      			np.y -= cellOffsetY;
      			path.addPathCell(np);
      		}

      		for (T cp : waypointList) {
      			T np = (T) cp.clone();
      			np.x -= cellOffsetX;
      			np.y -= cellOffsetY;
      			path.addWayPoint(np);
      		}
      	} else {
      		Path<CellPoint> reflectedPath = new Path<CellPoint>();
      		NaiveWalker nw = new NaiveWalker(zone);
      		Path<ZonePoint> wpl = set.getGridlessPath();

      		if (cellList.size() > 2) {

      			CellPoint prevPoint = grid.convert(new ZonePoint(startPoint.x, startPoint.y));
      			CellPoint terminalPoint = grid.convert(endPoint);
      			CellPoint convPoint;

      			// Lee: since we already have the start point
      			((List<T>) cellList).remove(0);

      			for (T p : cellList) {
      				convPoint = grid.convert((ZonePoint) p);
      				reflectedPath.addAllPathCells(nw.calculatePath(prevPoint, convPoint));
      				prevPoint = convPoint;
      			}

      		} else {
      			reflectedPath.addAllPathCells(
      					nw.calculatePath(grid.convert(startPoint), grid.convert(endPoint)));
      		}

      		ZonePoint buildVal = startPoint;
      		Path<ZonePoint> processPath = new Path<ZonePoint>();

      		for (CellPoint p : reflectedPath.getCellPath()) {
      			ZonePoint tempPoint = (ZonePoint) buildVal.clone();
      			processPath.addPathCell(tempPoint);

      			if (buildVal.x < endPoint.x)
      				buildVal.x += 100;
      			else if (buildVal.x > endPoint.x)
      				buildVal.x -= 100;
      			if (buildVal.y < endPoint.y)
      				buildVal.y += 100;
      			else if (buildVal.y > endPoint.y)
      				buildVal.y -= 100;
      		}

      		// processPath.addWayPoint(startPoint);
      		for (T cp : waypointList) {
      			ZonePoint np = (ZonePoint) cp;
      			if (np != startPoint && np != endPoint)
      				processPath.addWayPoint(np);
      		}

      		processPath.addWayPoint(endPoint);

      		// Lee: replacing the last point in derived path for the more
      		// accurate landing point
      		processPath.replaceLastPoint(endPoint);
      		path = (Path<T>) processPath;
      	}
      */
    }
    return path;
  }

  public static Path fromDto(PathDto dto) {
    if (dto.getPointType() == PathDto.PointType.CELL_POINT) {
      final var path = new Path<CellPoint>();
      dto.getCellsList().forEach(p -> path.addPathCell(new CellPoint(p.getX(), p.getY())));
      dto.getWaypointsList().forEach(p -> path.addWayPoint(new CellPoint(p.getX(), p.getY())));
      return path;
    } else {
      final var path = new Path<ZonePoint>();
      dto.getCellsList().forEach(p -> path.addPathCell(new ZonePoint(p.getX(), p.getY())));
      dto.getWaypointsList().forEach(p -> path.addWayPoint(new ZonePoint(p.getX(), p.getY())));
      return path;
    }
  }

  public PathDto toDto() {
    var cellPath = getCellPath();
    if (cellPath.size() == 0) return null;

    var dto = PathDto.newBuilder();

    if (cellPath.get(0) instanceof CellPoint) dto.setPointType(PathDto.PointType.CELL_POINT);
    else dto.setPointType(PathDto.PointType.ZONE_POINT);

    cellPath.forEach(p -> dto.addCells(IntPointDto.newBuilder().setX(p.x).setY(p.y)));
    getWayPointList().forEach(p -> dto.addWaypoints(IntPointDto.newBuilder().setX(p.x).setY(p.y)));

    return dto.build();
  }
}
