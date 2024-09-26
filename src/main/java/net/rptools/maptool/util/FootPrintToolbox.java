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
package net.rptools.maptool.util;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.stream.Collectors;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.*;

public class FootPrintToolbox {
  public static final CellPoint ZERO_CELLPOINT = new CellPoint(0, 0);

  public static List<TokenFootprint> getGridFootprints(String gridType) {
    return FootprintManager.getCampaignFootprints()
        .getOrDefault(FootprintManager.getGridFootprintType().get(gridType), null);
  }

  public static Grid getCurrentMapGrid() {
    return MapTool.getFrame().getCurrentZoneRenderer().getZone().getGrid();
  }

  public static String getCurrentMapGridType() {
    return getGridType(getCurrentMapGrid());
  }

  public static String getGridType(Grid grid) {
    return GridFactory.getGridType(grid);
  }

  public static ZonePoint zonePointFromCellCentre(Point2D pt) {
    return new ZonePoint((int) pt.getX(), (int) pt.getY());
  }

  public static List<ZonePoint> cellPointsToZonePoints(Grid grid, List<CellPoint> cellPoints) {
    return cellPoints.stream().map(grid::convert).collect(Collectors.toSet()).stream().toList();
  }

  public static Point[] cellSetToPointArray(Set<CellPoint> set) {
    return cellPointListToPointArray(cellPointSetToList(set));
  }

  public static Point[] cellPointListToPointArray(List<CellPoint> cellPoints) {
    return cellPoints.stream().map(cp -> new Point(cp.x, cp.y)).toList().toArray(new Point[0]);
  }

  public static List<CellPoint> cellPointSetToList(Set<CellPoint> cellPoints) {
    return cellPoints.stream().toList();
  }

  public static List<TokenFootprint> getGridFootprints() {
    return getGridFootprints(getGridType(getCurrentMapGrid()));
  }

  public static List<CellPoint> sortCellList(List<CellPoint> cellList) {
    cellList.sort(Comparator.comparingInt(o -> o.x));
    cellList.sort(Comparator.comparingInt(o -> o.y));
    return cellList;
  }

  public static TokenFootprint getDefaultFootprint(List<TokenFootprint> list) {
    return list.stream().filter(TokenFootprint::isDefault).findFirst().orElseGet(list::getFirst);
  }

  public static String stringifyFootprint(TokenFootprint footprint) {
    if (footprint == null) {
      return "null";
    }
    return "\n---TokenFootprint---\nName:\t\t\t"
        + footprint.getName()
        + "\nLocalizedName:\t"
        + footprint.getLocalizedName()
        + "\nDefault:\t\t"
        + footprint.isDefault()
        + "\nScale:\t\t\t"
        + footprint.getScale()
        + "\nCells:\t\t\t"
        + footprint.getOccupiedCells(ZERO_CELLPOINT)
        + "\nGUID:\t\t\t"
        + footprint.getId();
  }
}
