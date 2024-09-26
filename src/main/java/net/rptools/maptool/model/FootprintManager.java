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

import static java.util.Map.entry;

import java.awt.*;
import java.util.*;
import java.util.List;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.tool.TokenFootprintCreator;
import net.rptools.maptool.util.FootPrintToolbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FootprintManager {
  public static final Map<String, String> GRID_FOOTPRINT_TYPE =
      Map.ofEntries(
          entry(GridFactory.HEX_HORI, GridFactory.HEX_HORI),
          entry(GridFactory.HEX_VERT, GridFactory.HEX_VERT),
          entry(GridFactory.ISOMETRIC, GridFactory.SQUARE),
          entry(GridFactory.ISOMETRIC_HEX, GridFactory.NONE),
          entry(GridFactory.NONE, GridFactory.NONE),
          entry(GridFactory.SQUARE, GridFactory.SQUARE));
  protected static final String DEFAULT_GRID_TYPE = AppPreferences.getDefaultGridType();
  private static final Logger log = LoggerFactory.getLogger(FootprintManager.class);
  protected static boolean useAppDefaults = false;
  protected static String currentGridType = DEFAULT_GRID_TYPE;
  protected static Map<String, List<TokenFootprint>> campaignFootprints = new HashMap<>();
  protected static List<TokenFootprint> currentGridFootprints = new ArrayList<>();
  protected TokenFootprint defaultFootprint;

  public FootprintManager(boolean useAppDefaults) {
    load(useAppDefaults);
  }

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

  public static Map<String, String> getGridFootprintType() {
    return GRID_FOOTPRINT_TYPE;
  }

  public static Map<String, List<TokenFootprint>> getCampaignFootprints() {
    return campaignFootprints;
  }

  public static List<TokenFootprint> getGridFootprints(String gridTypeName) {
    return getCampaignFootprints().get(GRID_FOOTPRINT_TYPE.get(gridTypeName));
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

  public static TokenFootprint createTokenFootprint(
      String gridTypeName,
      String name,
      boolean isDefault,
      Double scale,
      boolean localiseName,
      String localisedName,
      Set<CellPoint> cellPoints) {
    Point[] pointArray =
        FootPrintToolbox.cellPointListToPointArray(FootPrintToolbox.cellPointSetToList(cellPoints));
    TokenFootprint newPrint = new TokenFootprint(name, isDefault, scale, localiseName, pointArray);
    if (!localisedName.isEmpty()) {
      newPrint.setLocalizedName(localisedName);
    }
    newPrint.addOffsetTranslator(createOffsetTranslator(gridTypeName));
    return newPrint;
  }

  public static void writeAllFootprintsToCampaign(Map<String, List<TokenFootprint>> footprints) {
    CampaignProperties props = MapTool.getCampaign().getCampaignProperties();
    for (String key : footprints.keySet()) {
      props.setGridFootprints(key, footprints.get(key));
    }
    MapTool.getCampaign().mergeCampaignProperties(props);
  }

  public static void writeGridFootprintsToCampaign(
      List<TokenFootprint> footprints, String gridTypeName) {
    gridTypeName = GRID_FOOTPRINT_TYPE.get(gridTypeName);
    CampaignProperties props = MapTool.getCampaign().getCampaignProperties();
    props.setGridFootprints(gridTypeName, footprints);
    MapTool.getCampaign().mergeCampaignProperties(props);
  }

  public static void writeFootprintToCampaign(TokenFootprint footprint, String gridTypeName) {
    log.debug("writeFootprintToCampaign - " + footprint + " - " + gridTypeName);
    gridTypeName = GRID_FOOTPRINT_TYPE.get(gridTypeName);
    List<TokenFootprint> list = getGridFootprints(gridTypeName);
    list.add(footprint);
    CampaignProperties props = MapTool.getCampaign().getCampaignProperties();
    props.setGridFootprints(gridTypeName, list);
    MapTool.getCampaign().mergeCampaignProperties(props);
  }

  void load(boolean useDefaults) {
    setUseAppDefaults(useDefaults);
    load();
  }

  void load() {
    if (useAppDefaults) {
      load(DEFAULT_GRID_TYPE);
    } else {
      setCurrentGrid(MapTool.getFrame().getCurrentZoneRenderer().getZone().getGrid());
    }
  }

  void load(String gridType) {
    log.debug("gridType" + gridType);
    log.debug("useAppDefaults:" + useAppDefaults);
    currentGridType = gridType;
    if (useAppDefaults) {
      campaignFootprints =
          Map.ofEntries(
              entry(GridFactory.HEX_HORI, TokenFootprintCreator.makeHorizHex()),
              entry(GridFactory.HEX_VERT, TokenFootprintCreator.makeVertHex()),
              entry(GridFactory.NONE, TokenFootprintCreator.makeGridless()),
              entry(GridFactory.SQUARE, TokenFootprintCreator.makeSquare()));
    } else {
      campaignFootprints = MapTool.getCampaign().getCampaignProperties().getGridFootprints();
    }
    currentGridFootprints = campaignFootprints.get(GRID_FOOTPRINT_TYPE.get(currentGridType));
    defaultFootprint =
        currentGridFootprints.stream().filter(TokenFootprint::isDefault).toList().getFirst();
  }

  boolean isUseAppDefaults() {
    return useAppDefaults;
  }

  void setUseAppDefaults(boolean value) {
    useAppDefaults = value;
  }

  void setCurrentGrid(Grid grid) {
    setCurrentGridType(GridFactory.getGridType(grid));
  }

  void setCurrentGridType(String gridType) {
    load(gridType);
  }

  public void changeCurrentGridType(String gridType) {
    currentGridFootprints = campaignFootprints.get(GRID_FOOTPRINT_TYPE.get(gridType));
  }
}
