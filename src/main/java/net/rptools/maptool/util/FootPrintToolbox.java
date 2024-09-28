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

import static java.util.Map.entry;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.stream.Collectors;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.*;
import net.rptools.maptool.tool.TokenFootprintCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FootPrintToolbox {
  protected static final String DEFAULT_GRID_TYPE = AppPreferences.getDefaultGridType();
  private static final Logger log = LoggerFactory.getLogger(FootPrintToolbox.class);
  public static final CellPoint ZERO_CELLPOINT = new CellPoint(0, 0);

  /** Map between grid type constants pointing to the one holding its footprints */
  public static final Map<String, String> GRID_FOOTPRINT_TYPE =
      Map.ofEntries(
          entry(GridFactory.HEX_HORI, GridFactory.HEX_HORI),
          entry(GridFactory.HEX_VERT, GridFactory.HEX_VERT),
          entry(GridFactory.ISOMETRIC, GridFactory.SQUARE),
          entry(GridFactory.ISOMETRIC_HEX, GridFactory.NONE),
          entry(GridFactory.NONE, GridFactory.NONE),
          entry(GridFactory.SQUARE, GridFactory.SQUARE));

  // spotless:off
  public static Map<String, String> getGridFootprintType() { return GRID_FOOTPRINT_TYPE; }
  public static List<TokenFootprint> getGridFootprints(String gridType) {
    return getCampaignFootprints().getOrDefault(lookupGridType(gridType), null);
  }
  public static String lookupGridType(String gridType){ return getGridFootprintType().get(gridType); }
  public static String getGridType(Grid grid) {
    return GridFactory.getGridType(grid);
  }
  public static Grid getCurrentMapGrid() {
    return MapTool.getFrame().getCurrentZoneRenderer().getZone().getGrid();
  }
  public static String getCurrentMapGridType() {
    return getGridType(getCurrentMapGrid());
  }
  public static Map<String, List<TokenFootprint>> getCampaignFootprints() {
    return MapTool.getCampaign().getCampaignProperties().getGridFootprints();
  }
  public static List<TokenFootprint> getCurrentGridFootprints() {
    return getGridFootprints(getGridType(getCurrentMapGrid()));
  }

  /**
   * Generate default footprints from the TokenFootprintCreator
   * @return map of grids to footprints
   */
  public static Map<String, List<TokenFootprint>> getDefaultCampaignFootprints() {
    return Map.ofEntries(
            entry(GridFactory.HEX_HORI, TokenFootprintCreator.makeHorizHex()),
            entry(GridFactory.HEX_VERT, TokenFootprintCreator.makeVertHex()),
            entry(GridFactory.NONE, TokenFootprintCreator.makeGridless()),
            entry(GridFactory.SQUARE, TokenFootprintCreator.makeSquare()));
  }

  /**
   * Find the default footprint in the list
   * @param list list of token footprints
   * @return the default footprint
   */
  public static TokenFootprint getDefaultFootprint(List<TokenFootprint> list) {
    return list.stream().filter(TokenFootprint::isDefault).findFirst().orElseGet(list::getFirst);
  }

  /** Get the default footprint for the default grid type in preferences */
  public static TokenFootprint getGlobalDefaultFootprint() {
    return MapTool.getCampaign()
            .getCampaignProperties()
            .getGridFootprints()
            .get(AppPreferences.getDefaultGridType())
            .stream()
            .filter(TokenFootprint::isDefault)
            .findAny()
            .orElse(
                    MapTool.getCampaign()
                            .getCampaignProperties()
                            .getGridFootprints()
                            .get(AppPreferences.getDefaultGridType())
                            .getFirst());
  }

  /**
   *  Write list of footprints to campaign for specified grid type
   *
   * @param footprints  list of footprints
   * @param gridTypeName grid type to store them under
   */
  public static void writeGridFootprintsToCampaign(
          List<TokenFootprint> footprints, String gridTypeName) {
    gridTypeName = lookupGridType(gridTypeName);
    CampaignProperties props = MapTool.getCampaign().getCampaignProperties();
    props.setGridFootprints(gridTypeName, footprints);
    MapTool.getCampaign().mergeCampaignProperties(props);
  }

  /**
   * Write map of footprints to campaign
   *
   * @param footprints Lists of footprints mapped to grid type name
   */
  public static void writeAllFootprintsToCampaign(Map<String, List<TokenFootprint>> footprints) {
    CampaignProperties props = MapTool.getCampaign().getCampaignProperties();
    for (String key : footprints.keySet()) {
      props.setGridFootprints(key, footprints.get(key));
    }
    MapTool.getCampaign().mergeCampaignProperties(props);
  }

  /**
   * Write single footprint to campaign list for grid type
   * @param footprint token footprint
   * @param gridTypeName list name to store under
   */
  public static void writeFootprintToCampaign(TokenFootprint footprint, String gridTypeName) {
    log.info("writeFootprintToCampaign - " + footprint + " - " + gridTypeName);
    List<TokenFootprint> list = getGridFootprints(gridTypeName);
    List<TokenFootprint> replacement = new ArrayList<>(list);
    replacement.add(footprint);
    writeGridFootprintsToCampaign(replacement, gridTypeName);
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

  public static List<CellPoint> sortCellList(List<CellPoint> cellList) {
    cellList.sort(Comparator.comparingInt(o -> o.x));
    cellList.sort(Comparator.comparingInt(o -> o.y));
    return cellList;
  }

  public static TokenFootprint createTokenFootprint(
          String gridTypeName,
          String name,
          boolean isDefault,
          Double scale,
          boolean localiseName,
          String localisedName,
          Set<CellPoint> cellPoints) {
    Point[] pointArray = cellPointListToPointArray(cellPointSetToList(cellPoints));
    TokenFootprint newPrint = new TokenFootprint(name, isDefault, scale, localiseName, pointArray);
    if (!(localisedName.isEmpty() || localisedName.isBlank())) {
      newPrint.setLocalizedName(localisedName);
    }
    newPrint.addOffsetTranslator(createOffsetTranslator(gridTypeName));
    return newPrint;
  }
  public static TokenFootprint.OffsetTranslator createOffsetTranslator(String gridTypeName) {
    gridTypeName = GRID_FOOTPRINT_TYPE.get(gridTypeName);
    if (gridTypeName.equals(GridFactory.HEX_HORI)) {
      return (originPoint, offsetPoint) -> {
        if ((originPoint.y & 1) == 1 && (offsetPoint.y & 1) == 0) {
          offsetPoint.x++;
        }
      };
    } else if (gridTypeName.equals(GridFactory.HEX_VERT)) {
      return (originPoint, offsetPoint) -> {
        if ((originPoint.x & 1) == 1 && (offsetPoint.x & 1) == 0) {
          offsetPoint.y++;
        }
      };
    } else {
      return (originPoint, offsetPoint) -> offsetPoint = originPoint;
    }
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
  // spotless:on
}
