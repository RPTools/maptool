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
package net.rptools.maptool.client.functions;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.token.BarTokenOverlay;
import net.rptools.maptool.client.ui.token.BooleanTokenOverlay;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Campaign;
import net.rptools.maptool.model.CampaignProperties;
import net.rptools.maptool.model.Grid;
import net.rptools.maptool.model.GridFactory;
import net.rptools.maptool.model.Light;
import net.rptools.maptool.model.LightSource;
import net.rptools.maptool.model.LookupTable;
import net.rptools.maptool.model.SightType;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.server.ServerPolicy;
import net.rptools.maptool.util.SysInfo;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;

public class getInfoFunction extends AbstractFunction {

  /** The singleton instance. */
  private static final getInfoFunction instance = new getInfoFunction();

  private getInfoFunction() {
    super(1, 1, "getInfo");
  }

  /**
   * Gets the instance of getInfoFunction.
   *
   * @return the instance.
   */
  public static getInfoFunction getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> param)
      throws ParserException {
    String infoType = param.get(0).toString();

    if (infoType.equalsIgnoreCase("map") || infoType.equalsIgnoreCase("zone")) {
      return getMapInfo();
    } else if (infoType.equalsIgnoreCase("client")) {
      return getClientInfo();
    } else if (infoType.equals("server")) {
      return getServerInfo();
    } else if (infoType.equals("campaign")) {
      return getCampaignInfo();
    } else if (infoType.equalsIgnoreCase("debug")) {
      return getDebugInfo();
    } else {
      throw new ParserException(
          I18N.getText("macro.function.getInfo.invalidArg", param.get(0).toString()));
    }
  }

  /**
   * Retrieves the information about the current zone/map and returns it as a JSON Object.
   *
   * @return The information about the map.
   * @throws ParserException when there is an error.
   */
  private JsonObject getMapInfo() throws ParserException {
    JsonObject minfo = new JsonObject();
    Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();

    if (!MapTool.getParser().isMacroTrusted()) {
      if (!zone.isVisible()) {
        throw new ParserException(I18N.getText("macro.function.general.noPerm", "getInfo('map')"));
      }
    }

    minfo.addProperty("name", zone.getName());
    minfo.addProperty("image x scale", zone.getImageScaleX());
    minfo.addProperty("image y scale", zone.getImageScaleY());
    minfo.addProperty("player visible", zone.isVisible() ? 1 : 0);

    if (MapTool.getParser().isMacroTrusted()) {
      minfo.addProperty("id", zone.getId().toString());
      minfo.addProperty("creation time", zone.getCreationTime());
      minfo.addProperty("width", zone.getWidth());
      minfo.addProperty("height", zone.getHeight());
      minfo.addProperty("largest Z order", zone.getLargestZOrder());
    }

    String visionType = "off";
    switch (zone.getVisionType()) {
      case DAY:
        visionType = "day";
        break;
      case NIGHT:
        visionType = "night";
        break;
      case OFF:
        visionType = "off";
        break;
    }
    minfo.addProperty("vision type", visionType);

    JsonObject ginfo = new JsonObject();

    Grid grid = zone.getGrid();

    ginfo.addProperty("type", GridFactory.getGridType(grid));
    ginfo.addProperty("color", String.format("%h", zone.getGridColor()));
    ginfo.addProperty("units per cell", zone.getUnitsPerCell());
    ginfo.addProperty("cell height", zone.getGrid().getCellHeight());
    ginfo.addProperty("cell width", zone.getGrid().getCellWidth());
    ginfo.addProperty("cell offset width", zone.getGrid().getCellOffset().getWidth());
    ginfo.addProperty("cell offset height", zone.getGrid().getCellOffset().getHeight());
    ginfo.addProperty("size", zone.getGrid().getSize());
    ginfo.addProperty("x offset", zone.getGrid().getOffsetX());
    ginfo.addProperty("y offset", zone.getGrid().getOffsetY());
    ginfo.addProperty("second dimension", grid.getSecondDimension());

    minfo.add("grid", ginfo);
    return minfo;
  }

  /**
   * Retrieves the client side preferences that do not have server over rides as a json object.
   *
   * @return the client side preferences
   */
  private JsonObject getClientInfo() {
    JsonObject cinfo = new JsonObject();

    cinfo.addProperty("face edge", AppPreferences.getFaceEdge() ? BigDecimal.ONE : BigDecimal.ZERO);
    cinfo.addProperty(
        "face vertex", AppPreferences.getFaceVertex() ? BigDecimal.ONE : BigDecimal.ZERO);
    cinfo.addProperty("portrait size", AppPreferences.getPortraitSize());
    cinfo.addProperty("show portrait", AppPreferences.getShowPortrait());
    cinfo.addProperty("show stat sheet", AppPreferences.getShowStatSheet());
    cinfo.addProperty("file sync directory", AppPreferences.getFileSyncPath());
    cinfo.addProperty("version", MapTool.getVersion());
    cinfo.addProperty(
        "isFullScreen", MapTool.getFrame().isFullScreen() ? BigDecimal.ONE : BigDecimal.ZERO);
    cinfo.addProperty("timeInMs", System.currentTimeMillis());
    cinfo.addProperty("timeDate", getTimeDate());
    cinfo.addProperty("isoTimeDate", getIsoTimeDate());
    if (MapTool.getParser().isMacroTrusted()) {
      JsonObject libInfo = new JsonObject();
      for (ZoneRenderer zr : MapTool.getFrame().getZoneRenderers()) {
        Zone zone = zr.getZone();
        for (Token token : zone.getTokens()) {
          if (token.getName().toLowerCase().startsWith("lib:")) {
            if (token.getProperty("libversion") != null) {
              libInfo.addProperty(token.getName(), token.getProperty("libversion").toString());
            } else {
              libInfo.addProperty(token.getName(), "unknown");
            }
          }
        }
      }
      if (libInfo.size() > 0) {
        cinfo.add("library tokens", libInfo);
      }
      JsonArray udf = new JsonArray();
      for (String name : UserDefinedMacroFunctions.getInstance().getAliases()) {
        udf.add(name);
      }
      cinfo.add("user defined functions", udf);
      cinfo.addProperty("client id", MapTool.getClientId());
    }
    return cinfo;
  }

  private String getTimeDate() {
    Calendar cal = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    return sdf.format(cal.getTime());
  }

  private String getIsoTimeDate() {
    return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(ZonedDateTime.now());
  }

  /**
   * Retrieves the server side preferences as a json object.
   *
   * @return the server side preferences
   */
  private JsonObject getServerInfo() {
    ServerPolicy sp = MapTool.getServerPolicy();
    JsonObject sinfo = sp.toJSON();

    return (sinfo);
  }

  /**
   * Retrieves information about the campaign as a json object.
   *
   * @return the campaign information.
   * @throws ParserException if an error occurs.
   */
  private JsonObject getCampaignInfo() throws ParserException {

    Gson gson = new Gson();

    if (!MapTool.getParser().isMacroTrusted()) {
      throw new ParserException(
          I18N.getText("macro.function.general.noPerm", "getInfo('campaign')"));
    }
    JsonObject cinfo = new JsonObject();
    Campaign c = MapTool.getCampaign();
    CampaignProperties cp = c.getCampaignProperties();

    cinfo.addProperty("id", c.getId().toString());
    cinfo.addProperty(
        "initiative movement locked",
        cp.isInitiativeMovementLock() ? BigDecimal.ONE : BigDecimal.ZERO);
    cinfo.addProperty(
        "initiative owner permissions",
        cp.isInitiativeOwnerPermissions() ? BigDecimal.ONE : BigDecimal.ZERO);

    JsonObject zinfo = new JsonObject();
    for (Zone z : c.getZones()) {
      zinfo.addProperty(z.getName(), z.getId().toString());
    }
    cinfo.add("zones", zinfo);

    JsonArray tinfo = new JsonArray();
    for (LookupTable table : c.getLookupTableMap().values()) {
      tinfo.add(table.getName());
    }
    cinfo.add("tables", tinfo);

    JsonObject llinfo = new JsonObject();
    for (String ltype : c.getLightSourcesMap().keySet()) {
      JsonArray ltinfo = new JsonArray();
      for (LightSource ls : c.getLightSourceMap(ltype).values()) {
        JsonObject linfo = new JsonObject();
        linfo.addProperty("name", ls.getName());
        linfo.addProperty("max range", ls.getMaxRange());
        linfo.addProperty("type", ls.getType().toString());
        // List<Light> lights = new ArrayList<Light>();
        // for (Light light : ls.getLightList()) {
        // lights.add(light);
        // }
        JsonArray lightList = new JsonArray();
        for (Light light : ls.getLightList()) {
          lightList.add(gson.toJson(light));
        }
        linfo.add("light segments", lightList);
        ltinfo.add(linfo);
      }
      llinfo.add(ltype, ltinfo);
    }
    cinfo.add("light sources", llinfo);

    JsonObject sinfo = new JsonObject();
    for (BooleanTokenOverlay states : c.getTokenStatesMap().values()) {
      String group = states.getGroup();
      if (group == null) {
        group = "no group";
      }
      if (sinfo.has(group)) {
        JsonArray sgroup = sinfo.get(group).getAsJsonArray();
      } else {
        JsonArray sgroup = new JsonArray();
        sgroup.add(states.getName());
        sinfo.add(group, sgroup);
      }
    }
    cinfo.add("states", sinfo);

    JsonArray remoteRepos = new JsonArray();
    for (String repo : c.getRemoteRepositoryList()) {
      remoteRepos.add(repo);
    }
    cinfo.add("remote repository", remoteRepos);

    JsonObject sightInfo = new JsonObject();
    for (SightType sightType : c.getSightTypeMap().values()) {
      JsonObject si = new JsonObject();
      si.addProperty("arc", sightType.getArc());
      si.addProperty("distance", sightType.getArc());
      si.addProperty("multiplier", sightType.getMultiplier());
      si.addProperty("shape", sightType.getShape().toString());
      si.addProperty("type", sightType.getOffset());
      sightInfo.add(sightType.getName(), si);
    }
    cinfo.add("sight", sightInfo);

    JsonObject barinfo = new JsonObject();
    for (BarTokenOverlay tbo : c.getTokenBarsMap().values()) {
      String group = tbo.getGroup();
      if (group == null) {
        group = "no group";
      }
      JsonArray bgroup;
      if (barinfo.has(group)) {
        bgroup = barinfo.get(group).getAsJsonArray();
      } else {
        bgroup = new JsonArray();
      }
      JsonObject bar = new JsonObject();
      bar.addProperty("name", tbo.getName());
      bar.addProperty("side", tbo.getSide().toString());
      bar.addProperty("increment", tbo.getIncrements());
      bgroup.add(bar);
    }
    cinfo.add("bars", barinfo);

    return cinfo;
  }

  /**
   * Retrieves debug information
   *
   * @return the debug information.
   * @throws ParserException if an error occurs.
   */
  private JsonObject getDebugInfo() throws ParserException {
    SysInfo info = new SysInfo();
    return info.getSysInfoJSON();
  }
}
