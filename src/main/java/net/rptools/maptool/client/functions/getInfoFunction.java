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

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

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
  private JSONObject getMapInfo() throws ParserException {
    Map<String, Object> minfo = new HashMap<String, Object>();
    Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();

    if (!MapTool.getParser().isMacroTrusted()) {
      if (!zone.isVisible()) {
        throw new ParserException(I18N.getText("macro.function.general.noPerm", "getInfo('map')"));
      }
    }

    minfo.put("name", zone.getName());
    minfo.put("image x scale", zone.getImageScaleX());
    minfo.put("image y scale", zone.getImageScaleY());
    minfo.put("player visible", zone.isVisible() ? 1 : 0);

    if (MapTool.getParser().isMacroTrusted()) {
      minfo.put("id", zone.getId().toString());
      minfo.put("creation time", zone.getCreationTime());
      minfo.put("width", zone.getWidth());
      minfo.put("height", zone.getHeight());
      minfo.put("largest Z order", zone.getLargestZOrder());
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
    minfo.put("vision type", visionType);

    Map<String, Object> ginfo = new HashMap<String, Object>();
    minfo.put("grid", ginfo);

    Grid grid = zone.getGrid();

    ginfo.put("type", GridFactory.getGridType(grid));
    ginfo.put("color", String.format("%h", zone.getGridColor()));
    ginfo.put("units per cell", zone.getUnitsPerCell());
    ginfo.put("cell height", zone.getGrid().getCellHeight());
    ginfo.put("cell width", zone.getGrid().getCellWidth());
    ginfo.put("cell offset width", zone.getGrid().getCellOffset().getWidth());
    ginfo.put("cell offset height", zone.getGrid().getCellOffset().getHeight());
    ginfo.put("size", zone.getGrid().getSize());
    ginfo.put("x offset", zone.getGrid().getOffsetX());
    ginfo.put("y offset", zone.getGrid().getOffsetY());
    ginfo.put("second dimension", grid.getSecondDimension());

    return JSONObject.fromObject(minfo);
  }

  /**
   * Retrieves the client side preferences that do not have server over rides as a json object.
   *
   * @return the client side preferences
   */
  private JSONObject getClientInfo() {
    Map<String, Object> cinfo = new HashMap<String, Object>();

    cinfo.put("face edge", AppPreferences.getFaceEdge() ? BigDecimal.ONE : BigDecimal.ZERO);
    cinfo.put("face vertex", AppPreferences.getFaceVertex() ? BigDecimal.ONE : BigDecimal.ZERO);
    cinfo.put("portrait size", AppPreferences.getPortraitSize());
    cinfo.put("show portrait", AppPreferences.getShowPortrait());
    cinfo.put("show stat sheet", AppPreferences.getShowStatSheet());
    cinfo.put("version", MapTool.getVersion());
    cinfo.put("isFullScreen", MapTool.getFrame().isFullScreen() ? BigDecimal.ONE : BigDecimal.ZERO);
    cinfo.put("timeInMs", System.currentTimeMillis());
    cinfo.put("timeDate", getTimeDate());
    if (MapTool.getParser().isMacroTrusted()) {
      Map<String, Object> libInfo = new HashMap<String, Object>();
      for (ZoneRenderer zr : MapTool.getFrame().getZoneRenderers()) {
        Zone zone = zr.getZone();
        for (Token token : zone.getTokens()) {
          if (token.getName().toLowerCase().startsWith("lib:")) {
            if (token.getProperty("libversion") != null) {
              libInfo.put(token.getName(), token.getProperty("libversion"));
            } else {
              libInfo.put(token.getName(), "unknown");
            }
          }
        }
      }
      if (libInfo.size() > 0) {
        cinfo.put("library tokens", libInfo);
      }
      cinfo.put(
          "user defined functions",
          JSONArray.fromObject(UserDefinedMacroFunctions.getInstance().getAliases()));
      cinfo.put("client id", MapTool.getClientId());
    }
    return JSONObject.fromObject(cinfo);
  }

  private String getTimeDate() {
    Calendar cal = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    return sdf.format(cal.getTime());
  }

  /**
   * Retrieves the server side preferences as a json object.
   *
   * @return the server side preferences
   */
  private JSONObject getServerInfo() {
    ServerPolicy sp = MapTool.getServerPolicy();
    JSONObject sinfo = sp.toJSON();

    return (sinfo);
  }

  /**
   * Retrieves information about the campaign as a json object.
   *
   * @return the campaign information.
   * @throws ParserException if an error occurs.
   */
  private JSONObject getCampaignInfo() throws ParserException {
    if (!MapTool.getParser().isMacroTrusted()) {
      throw new ParserException(
          I18N.getText("macro.function.general.noPerm", "getInfo('campaign')"));
    }
    Map<String, Object> cinfo = new HashMap<String, Object>();
    Campaign c = MapTool.getCampaign();
    CampaignProperties cp = c.getCampaignProperties();

    cinfo.put("id", c.getId().toString());
    cinfo.put(
        "initiative movement locked",
        cp.isInitiativeMovementLock() ? BigDecimal.ONE : BigDecimal.ZERO);
    cinfo.put(
        "initiative owner permissions",
        cp.isInitiativeOwnerPermissions() ? BigDecimal.ONE : BigDecimal.ZERO);

    Map<String, String> zinfo = new HashMap<String, String>();
    for (Zone z : c.getZones()) {
      zinfo.put(z.getName(), z.getId().toString());
    }
    cinfo.put("zones", zinfo);

    Set<String> tinfo = new HashSet<String>();
    for (LookupTable table : c.getLookupTableMap().values()) {
      tinfo.add(table.getName());
    }
    cinfo.put("tables", tinfo);

    Map<String, Object> llinfo = new HashMap<String, Object>();
    for (String ltype : c.getLightSourcesMap().keySet()) {
      Set<Object> ltinfo = new HashSet<Object>();
      for (LightSource ls : c.getLightSourceMap(ltype).values()) {
        HashMap<String, Object> linfo = new HashMap<String, Object>();
        linfo.put("name", ls.getName());
        linfo.put("max range", ls.getMaxRange());
        linfo.put("type", ls.getType());
        // List<Light> lights = new ArrayList<Light>();
        // for (Light light : ls.getLightList()) {
        // lights.add(light);
        // }
        linfo.put("light segments", ls.getLightList());
        ltinfo.add(linfo);
      }
      llinfo.put(ltype, ltinfo);
    }
    cinfo.put("light sources", llinfo);

    Map<String, Set<String>> sinfo = new HashMap<String, Set<String>>();
    for (BooleanTokenOverlay states : c.getTokenStatesMap().values()) {
      String group = states.getGroup();
      if (group == null) {
        group = "no group";
      }
      Set<String> sgroup = sinfo.get(group);
      if (sgroup != null) {
        sgroup.add(states.getName());
      } else {
        sgroup = new HashSet<String>();
        sgroup.add(states.getName());
        sinfo.put(group, sgroup);
      }
    }
    cinfo.put("states", sinfo);

    cinfo.put("remote repository", c.getRemoteRepositoryList());

    Map<String, Object> sightInfo = new HashMap<String, Object>();
    for (SightType sightType : c.getSightTypeMap().values()) {
      Map<String, Object> si = new HashMap<String, Object>();
      si.put("arc", sightType.getArc());
      si.put("distance", sightType.getArc());
      si.put("multiplier", sightType.getMultiplier());
      si.put("shape", sightType.getShape().toString());
      si.put("type", sightType.getOffset());
      sightInfo.put(sightType.getName(), si);
    }
    cinfo.put("sight", sightInfo);

    Map<String, Set<Object>> barinfo = new HashMap<String, Set<Object>>();
    for (BarTokenOverlay tbo : c.getTokenBarsMap().values()) {
      String group = tbo.getGroup();
      if (group == null) {
        group = "no group";
      }
      Set<Object> bgroup = barinfo.get(group);
      if (bgroup == null) {
        bgroup = new HashSet<Object>();
        barinfo.put(group, bgroup);
      }
      Map<String, Object> b = new HashMap<String, Object>();
      b.put("name", tbo.getName());
      b.put("side", tbo.getSide());
      b.put("increment", tbo.getIncrements());
      bgroup.add(b);
    }
    cinfo.put("bars", barinfo);

    return JSONObject.fromObject(cinfo);
  }

  /**
   * Retrieves debug information
   *
   * @return the debug information.
   * @throws ParserException if an error occurs.
   */
  private JSONObject getDebugInfo() throws ParserException {
    SysInfo info = new SysInfo();
    return info.getSysInfoJSON();
  }
}
