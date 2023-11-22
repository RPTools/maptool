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
import java.awt.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import javax.swing.*;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.htmlframe.HTMLDialog;
import net.rptools.maptool.client.ui.htmlframe.HTMLFrame;
import net.rptools.maptool.client.ui.htmlframe.HTMLOverlayManager;
import net.rptools.maptool.client.ui.token.*;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
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
import net.rptools.maptool.model.drawing.DrawableColorPaint;
import net.rptools.maptool.model.drawing.DrawableTexturePaint;
import net.rptools.maptool.server.ServerPolicy;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.maptool.util.MapToolSysInfoProvider;
import net.rptools.maptool.util.SysInfoProvider;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;

public class getInfoFunction extends AbstractFunction {

  /** The singleton instance. */
  private static final getInfoFunction instance = new getInfoFunction();

  private SysInfoProvider sysInfoProvider;

  private getInfoFunction() {
    super(1, 1, "getInfo");
    sysInfoProvider = new MapToolSysInfoProvider();
  }

  // region the following is here mostly for testing purpose, until we find a better way to inject
  protected void setSysInfoProvider(SysInfoProvider sysInfoProvider) {
    this.sysInfoProvider = sysInfoProvider;
  }

  protected void resetSysInfoProvider() {
    sysInfoProvider = new MapToolSysInfoProvider();
  }

  // endregion

  /**
   * Gets the instance of getInfoFunction.
   *
   * @return the instance.
   */
  public static getInfoFunction getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> param)
      throws ParserException {
    String infoType = param.get(0).toString();

    if (infoType.equalsIgnoreCase("map") || infoType.equalsIgnoreCase("zone")) {
      return getMapInfo();
    } else if (infoType.equalsIgnoreCase("client")) {
      return getClientInfo();
    } else if (infoType.equalsIgnoreCase("server")) {
      return getServerInfo();
    } else if (infoType.equalsIgnoreCase("campaign")) {
      return getCampaignInfo();
    } else if (infoType.equalsIgnoreCase("theme")) {
      return getThemeInfo();
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

    if (!MapTool.getParser().isMacroTrusted() && !zone.isVisible()) {
      throw new ParserException(I18N.getText("macro.function.general.noPerm", "getInfo('map')"));
    }

    minfo.addProperty("name", zone.getName());
    minfo.addProperty("display name", zone.getDisplayName());
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

    String visionType = zone.getVisionType().name();
    minfo.addProperty("vision type", visionType);
    minfo.addProperty("vision distance", zone.getTokenVisionDistance());
    minfo.addProperty("lighting style", zone.getLightingStyle().name());
    minfo.addProperty("has fog", zone.hasFog());
    minfo.addProperty("ai rounding", zone.getAStarRounding().name());

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

    {
      final var backgroundPaint = zone.getBackgroundPaint();
      String background = null;
      if (backgroundPaint instanceof DrawableColorPaint dcp) {
        background = String.format("#%h", zone.getGridColor());
      } else if (backgroundPaint instanceof DrawableTexturePaint dtp) {
        background = "asset://" + dtp.getAssetId().toString();
      }
      minfo.addProperty("background paint", background);
    }
    {
      final var fogPaint = zone.getFogPaint();
      String fog = null;
      if (fogPaint instanceof DrawableColorPaint dcp) {
        fog = String.format("#%h", zone.getGridColor());
      } else if (fogPaint instanceof DrawableTexturePaint dtp) {
        fog = "asset://" + dtp.getAssetId().toString();
      }
      minfo.addProperty("fog paint", fog);
    }
    {
      final var mapAsset = zone.getMapAssetId();
      minfo.addProperty("map asset", mapAsset == null ? null : "asset://" + mapAsset.toString());
    }

    return minfo;
  }

  /**
   * Retrieves the client side preferences that do not have server over rides as a json object.
   *
   * @return the client side preferences
   */
  private JsonObject getClientInfo() {
    JsonObject cinfo = new JsonObject();

    cinfo.addProperty("face edge", FunctionUtil.getDecimalForBoolean(AppPreferences.getFaceEdge()));
    cinfo.addProperty(
        "face vertex", FunctionUtil.getDecimalForBoolean(AppPreferences.getFaceVertex()));
    cinfo.addProperty("portrait size", AppPreferences.getPortraitSize());
    cinfo.addProperty("show portrait", AppPreferences.getShowPortrait());
    cinfo.addProperty("show stat sheet", AppPreferences.getShowStatSheet());
    cinfo.addProperty("file sync directory", AppPreferences.getFileSyncPath());
    cinfo.addProperty("show avatar in chat", AppPreferences.getShowAvatarInChat());
    cinfo.addProperty(
        "suppress tooltips for macroLinks", AppPreferences.getSuppressToolTipsForMacroLinks());
    cinfo.addProperty("use tooltips for inline rolls", AppPreferences.getUseToolTipForInlineRoll());
    cinfo.addProperty("version", MapTool.getVersion());
    cinfo.addProperty(
        "isFullScreen", FunctionUtil.getDecimalForBoolean(MapTool.getFrame().isFullScreen()));
    cinfo.addProperty("timeInMs", System.currentTimeMillis());
    cinfo.addProperty("timeDate", getTimeDate());
    cinfo.addProperty("isoTimeDate", getIsoTimeDate());
    cinfo.addProperty("isHosting", MapTool.isHostingServer());
    cinfo.addProperty("isPersonalServer", MapTool.isPersonalServer());
    cinfo.addProperty("userLanguage", MapTool.getLanguage());

    JsonObject dialogs = new JsonObject();
    Set<String> dialogNames = HTMLDialog.getDialogNames();
    for (String name : dialogNames) {
      Optional<JsonObject> props = HTMLDialog.getDialogProperties(name);
      props.ifPresent(jsonObject -> dialogs.add(name, jsonObject));
    }
    cinfo.add("dialogs", dialogs);

    JsonObject frames = new JsonObject();
    Set<String> frameNames = HTMLFrame.getFrameNames();
    for (String name : frameNames) {
      Optional<JsonObject> props = HTMLFrame.getFrameProperties(name);
      props.ifPresent(jsonObject -> frames.add(name, jsonObject));
    }
    cinfo.add("frames", frames);

    JsonObject overlays = new JsonObject();
    ConcurrentSkipListSet<HTMLOverlayManager> registeredOverlays =
        MapTool.getFrame().getOverlayPanel().getOverlays();
    for (HTMLOverlayManager o : registeredOverlays) {
      overlays.add(o.getName(), o.getProperties());
    }
    cinfo.add("overlays", overlays);

    if (MapTool.getParser().isMacroTrusted()) {
      getInfoOnTokensOfType(cinfo, "library tokens", "lib:", "libversion", "unknown");
      getInfoOnTokensOfType(cinfo, "image tokens", "image:", "libversion", "unknown");
      JsonObject udfList = new JsonObject();
      UserDefinedMacroFunctions UDF = UserDefinedMacroFunctions.getInstance();
      for (String name : UDF.getAliases()) {
        udfList.addProperty(name, UDF.getFunctionLocation(name));
      }
      cinfo.add("user defined functions", udfList);
      cinfo.addProperty("client id", MapTool.getClientId());
    }
    return cinfo;
  }

  /**
   * Gets info on tokens with names starting with the prefix.
   *
   * @param cinfo json object to add info to
   * @param token_type token type
   * @param prefix token prefix (e.g. "lib:" "image:")
   * @param versionProperty Property (if any) to get token version from
   * @param unknownVersionText text to show if version is unknown
   */
  private void getInfoOnTokensOfType(
      JsonObject cinfo,
      String token_type,
      String prefix,
      String versionProperty,
      String unknownVersionText) {
    JsonObject libInfo = new JsonObject();
    for (ZoneRenderer zr : MapTool.getFrame().getZoneRenderers()) {
      Zone zone = zr.getZone();
      for (Token token : zone.getAllTokens()) {
        if (token.getName().toLowerCase().startsWith(prefix)) {
          if (token.getProperty(versionProperty) != null) {
            libInfo.addProperty(token.getName(), token.getProperty(versionProperty).toString());
          } else {
            libInfo.addProperty(token.getName(), unknownVersionText);
          }
        }
      }
    }
    if (libInfo.size() > 0) {
      cinfo.add(token_type, libInfo);
    }
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

    return sp.toJSON();
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
        FunctionUtil.getDecimalForBoolean(cp.isInitiativeMovementLock()));
    cinfo.addProperty(
        "initiative owner permissions",
        FunctionUtil.getDecimalForBoolean(cp.isInitiativeOwnerPermissions()));

    JsonArray zoneIds = new JsonArray();
    JsonObject zinfo = new JsonObject();
    for (Zone z : c.getZones()) {
      zoneIds.add(z.getId().toString());
      zinfo.addProperty(z.getName(), z.getId().toString());
    }
    cinfo.add("zoneIDs", zoneIds);
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
        linfo.addProperty("scale", ls.isScaleWithToken());
        // List<Light> lights = new ArrayList<Light>();
        // for (Light light : ls.getLightList()) {
        // lights.add(light);
        // }
        JsonArray lightList = new JsonArray();
        for (Light light : ls.getLightList()) {
          lightList.add(gson.toJsonTree(light));
        }
        linfo.add("light segments", lightList);
        ltinfo.add(linfo);
      }
      llinfo.add(ltype, ltinfo);
    }
    cinfo.add("light sources", llinfo);

    JsonObject sinfo = new JsonObject();
    for (BooleanTokenOverlay bto : c.getTokenStatesMap().values()) {
      String group = bto.getGroup();
      if (group == null || group.isEmpty()) {
        group = "no group";
      }
      JsonArray sgroup;
      if (sinfo.has(group)) {
        sgroup = sinfo.get(group).getAsJsonArray();
      } else {
        sgroup = new JsonArray();
      }
      JsonObject state = new JsonObject();
      state.addProperty("name", bto.getName());
      state.addProperty("type", bto.getClass().getSimpleName());
      state.addProperty("group", group);
      state.addProperty("isShowGM", bto.isShowGM() ? BigDecimal.ONE : BigDecimal.ZERO);
      state.addProperty("isShowOwner", bto.isShowOwner() ? BigDecimal.ONE : BigDecimal.ZERO);
      state.addProperty("isShowOthers", bto.isShowOthers() ? BigDecimal.ONE : BigDecimal.ZERO);
      state.addProperty(
          "isImageOverlay", (bto instanceof ImageTokenOverlay) ? BigDecimal.ONE : BigDecimal.ZERO);
      state.addProperty("mouseOver", bto.isMouseover() ? BigDecimal.ONE : BigDecimal.ZERO);
      state.addProperty("opacity", bto.getOpacity());
      state.addProperty("order", bto.getOrder());
      if (bto instanceof FlowColorDotTokenOverlay) {
        state.addProperty("gridSize", ((FlowColorDotTokenOverlay) bto).getGrid());
      }
      if (bto instanceof CornerImageTokenOverlay) {
        state.addProperty("corner", ((CornerImageTokenOverlay) bto).getCorner().name());
      }

      sgroup.add(state);
      sinfo.add(group, sgroup);
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
      bar.addProperty("type", tbo.getClass().getSimpleName());
      bar.addProperty("side", tbo.getSide().toString());
      bar.addProperty("increment", tbo.getIncrements());
      bar.addProperty("mouseOver", tbo.isMouseover() ? BigDecimal.ONE : BigDecimal.ZERO);
      bar.addProperty("isShowGM", tbo.isShowGM() ? BigDecimal.ONE : BigDecimal.ZERO);
      bar.addProperty("isShowOwner", tbo.isShowOwner() ? BigDecimal.ONE : BigDecimal.ZERO);
      bar.addProperty("isShowOthers", tbo.isShowOthers() ? BigDecimal.ONE : BigDecimal.ZERO);

      bgroup.add(bar);
      barinfo.add(group, bgroup);
    }
    cinfo.add("bars", barinfo);

    return cinfo;
  }

  /**
   * Get Theme Info
   *
   * @return JsonObject of theme information
   */
  private JsonObject getThemeInfo() {
    JsonObject theme = new JsonObject();

    // Currently, just the color info is returned.
    for (Map.Entry<Object, Object> entry : UIManager.getDefaults().entrySet()) {
      if (entry.getValue() instanceof Color) {
        Color color = (Color) entry.getValue();
        theme.addProperty((String) entry.getKey(), Integer.toHexString(color.getRGB()));
      }
    }
    return theme;
  }

  /**
   * Retrieves debug information
   *
   * @return the debug information.
   * @throws ParserException if an error occurs.
   */
  private JsonObject getDebugInfo() {
    return sysInfoProvider.getSysInfoJSON();
  }
}
