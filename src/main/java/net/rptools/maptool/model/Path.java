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
import net.rptools.maptool.client.walker.NaiveWalker;
import net.rptools.maptool.server.proto.PathDto;
import net.rptools.maptool.server.proto.drawing.IntPointDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Path<T extends AbstractPoint> {
  private static final Logger log = LogManager.getLogger(Path.class);

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

  public Path<T> copy() {
    var result = new Path<T>();
    for (var cell : cellList) {
      result.cellList.add(copyPoint(cell));
    }
    for (var waypoint : waypointList) {
      result.waypointList.add(copyPoint(waypoint));
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  private static <U extends AbstractPoint> U copyPoint(U point) {
    return (U) point.clone();
  }

  public void appendWaypoint(T waypoint) {
    cellList.add(copyPoint(waypoint));
    waypointList.add(copyPoint(waypoint));
  }

  public void appendPartialPath(List<T> cells) {
    if (cells.isEmpty()) {
      return;
    }

    // If we have no waypoints yet, we must treat the first of `cells` as a waypoint.
    if (waypointList.isEmpty()) {
      log.warn("Attempt to add a partial path to a path, but no starting waypoint has been set.");
      // Note that we always add the last point as a waypoint, so don't redundantly do so here.
      if (cells.size() >= 2) {
        waypointList.add(copyPoint(cells.getFirst()));
      }
    }

    for (var cell : cells) {
      cellList.add(copyPoint(cell));
    }
    waypointList.add(copyPoint(cells.getLast()));
  }

  public List<T> getCellPath() {
    return Collections.unmodifiableList(cellList);
  }

  public boolean isWaypoint(T point) {
    return waypointList.contains(point);
  }

  /**
   * @return way point list for path
   */
  public List<T> getWayPointList() {
    return Collections.unmodifiableList(waypointList);
  }

  /** Create a related path that can be applied to a follower token. */
  public Path<?> derive(Grid grid, Token keyToken, Token followerToken) {
    if (keyToken.isSnapToGrid() && followerToken.isSnapToGrid()) {
      // Assume T = CellPoint.
      var originCell = grid.convert(new ZonePoint(keyToken.getX(), keyToken.getY()));
      var tokenCell = grid.convert(new ZonePoint(followerToken.getX(), followerToken.getY()));
      return deriveSameSnapToGrid(this, originCell.x - tokenCell.x, originCell.y - tokenCell.y);
    } else if (!keyToken.isSnapToGrid() && !followerToken.isSnapToGrid()) {
      // Assume T = ZonePoint.
      var originPoint = new ZonePoint(keyToken.getX(), keyToken.getY());
      var tokenPoint = new ZonePoint(followerToken.getX(), followerToken.getY());
      return deriveSameSnapToGrid(this, originPoint.x - tokenPoint.x, originPoint.y - tokenPoint.y);
    } else if (keyToken.isSnapToGrid()) {
      // Assume T = CellPoint.
      return deriveFromSnapToGrid(
          (Path<CellPoint>) this,
          grid,
          keyToken.getX() - followerToken.getX(),
          keyToken.getY() - followerToken.getY());
    } else /* (!keyToken.isSnapToGrid) */ {
      // Assume T = ZonePoint.
      return deriveFromNotSnapToGrid(
          (Path<ZonePoint>) this,
          grid,
          keyToken.getX() - followerToken.getX(),
          keyToken.getY() - followerToken.getY());
    }
  }

  private static <T extends AbstractPoint> Path<T> deriveSameSnapToGrid(
      Path<T> path, int offsetX, int offsetY) {
    var result = new Path<T>();
    // Not much to do here except copy the list, offsetting the follower.
    for (T point : path.cellList) {
      var newPoint = copyPoint(point);
      newPoint.x -= offsetX;
      newPoint.y -= offsetY;
      result.cellList.add(newPoint);
    }

    for (T point : path.waypointList) {
      var newPoint = copyPoint(point);
      newPoint.x -= offsetX;
      newPoint.y -= offsetY;
      result.waypointList.add(newPoint);
    }
    return result;
  }

  private static Path<ZonePoint> deriveFromSnapToGrid(
      Path<CellPoint> path, Grid grid, int zoneOffsetX, int zoneOffsetY) {
    var result = new Path<ZonePoint>();
    // Only use the waypoint list, otherwise we get a path full of nothing but waypoints.
    for (CellPoint point : path.waypointList) {
      var newPoint = grid.convert(point);
      newPoint.x -= zoneOffsetX;
      newPoint.y -= zoneOffsetY;
      result.appendWaypoint(newPoint);
    }

    return result;
  }

  private static Path<CellPoint> deriveFromNotSnapToGrid(
      Path<ZonePoint> path, Grid grid, int zoneOffsetX, int zoneOffsetY) {
    var result = new Path<CellPoint>();
    // The waypoints are easy: just map them to the best grid cell. But we need to fill in all the
    // intervening points, so use a naive walker for that.
    // The cell points of path should just be the waypoints, so just ignore them.

    CellPoint previous = null;
    for (ZonePoint point : path.waypointList) {
      var newPoint = new ZonePoint(point);
      newPoint.x -= zoneOffsetX;
      newPoint.y -= zoneOffsetY;
      var current = grid.convert(newPoint);

      if (previous == null) {
        result.appendWaypoint(current);
        previous = current;
        continue;
      }

      var walker = new NaiveWalker();
      var walkerPath = walker.calculatePath(previous, current);
      // Path will be a list: [previous, ..., current]. We already have previous, so chop that off.
      result.appendPartialPath(walkerPath.subList(1, walkerPath.size()));
      previous = current;
    }

    return result;
  }

  public static Path<?> fromDto(PathDto dto) {
    if (dto.getPointType() == PathDto.PointType.CELL_POINT) {
      final var path = new Path<CellPoint>();
      dto.getCellsList().forEach(p -> path.cellList.add(new CellPoint(p.getX(), p.getY())));
      dto.getWaypointsList().forEach(p -> path.waypointList.add(new CellPoint(p.getX(), p.getY())));
      return path;
    } else {
      final var path = new Path<ZonePoint>();
      dto.getCellsList().forEach(p -> path.cellList.add(new ZonePoint(p.getX(), p.getY())));
      dto.getWaypointsList().forEach(p -> path.waypointList.add(new ZonePoint(p.getX(), p.getY())));
      return path;
    }
  }

  public PathDto toDto() {
    var dto = PathDto.newBuilder();

    // An empty path cannot tell what kind of points it is supposed to contain. Arbitrarily assign
    // it as cell points.
    if (cellList.isEmpty() || cellList.getFirst() instanceof CellPoint) {
      dto.setPointType(PathDto.PointType.CELL_POINT);
    } else {
      dto.setPointType(PathDto.PointType.ZONE_POINT);
    }

    cellList.forEach(p -> dto.addCells(IntPointDto.newBuilder().setX(p.x).setY(p.y)));
    waypointList.forEach(p -> dto.addWaypoints(IntPointDto.newBuilder().setX(p.x).setY(p.y)));

    return dto.build();
  }
}
