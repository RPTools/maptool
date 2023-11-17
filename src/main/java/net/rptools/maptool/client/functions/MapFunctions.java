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

import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolUtil;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.AssetManager;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.GridFactory;
import net.rptools.maptool.model.InvalidGUIDException;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.ZoneFactory;
import net.rptools.maptool.model.drawing.DrawablePaint;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;

public class MapFunctions extends AbstractFunction {
  private static final MapFunctions instance = new MapFunctions();

  private MapFunctions() {
    super(
        0,
        2,
        "getCurrentMapID",
        "getAllMapIDs",
        "getMapIDs",
        "getAllMapNames",
        "getAllMapDisplayNames",
        "getCurrentMapName",
        "getMapDisplayName",
        "getVisibleMapIDs",
        "getVisibleMapNames",
        "getVisibleMapDisplayNames",
        "setCurrentMap",
        "getMapVisible",
        "setMapVisible",
        "setMapName",
        "setMapDisplayName",
        "copyMap",
        "createMap",
        "getMapName",
        "setMapSelectButton",
        "getMapVision",
        "setMapVision");
  }

  public static MapFunctions getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {
    if (functionName.equalsIgnoreCase("getCurrentMapID")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 0);
      ZoneRenderer currentZR = MapTool.getFrame().getCurrentZoneRenderer();
      if (currentZR == null) {
        throw new ParserException(I18N.getText("macro.function.map.none", functionName));
      }
      return currentZR.getZone().getId().toString();
    } else if (functionName.equalsIgnoreCase("getMapIDs")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 2);
      final var mapName = parameters.get(0).toString();
      final var delim = parameters.size() < 2 ? "," : parameters.get(1).toString();
      final var zoneIds =
          MapTool.getCampaign().getZones().stream()
              .filter(zone -> mapName.equals(zone.getName()))
              .map(Zone::getId)
              .map(GUID::toString)
              .toList();
      return FunctionUtil.delimitedResult(delim, zoneIds);
    } else if (functionName.equalsIgnoreCase("getCurrentMapName")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 0);
      ZoneRenderer currentZR = MapTool.getFrame().getCurrentZoneRenderer();
      if (currentZR == null) {
        throw new ParserException(I18N.getText("macro.function.map.none", functionName));
      }
      return currentZR.getZone().getName();

    } else if (functionName.equalsIgnoreCase("getMapDisplayName")) {
      FunctionUtil.blockUntrustedMacro(functionName);
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 1);
      final var zr = FunctionUtil.getZoneRendererFromParam(functionName, parameters, 0);
      return zr.getZone().getDisplayName();

    } else if (functionName.equalsIgnoreCase("setCurrentMap")) {
      FunctionUtil.blockUntrustedMacro(functionName);
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 1);
      String mapNameOrId = parameters.get(0).toString();
      final var zr = FunctionUtil.getZoneRenderer(functionName, mapNameOrId);
      MapTool.getFrame().setCurrentZoneRenderer(zr);
      return mapNameOrId;

    } else if ("getMapVisible".equalsIgnoreCase(functionName)) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 1);
      final var zr = FunctionUtil.getZoneRendererFromParam(functionName, parameters, 0);
      return zr.getZone().isVisible() ? BigDecimal.ONE : BigDecimal.ZERO;

    } else if ("setMapVisible".equalsIgnoreCase(functionName)) {
      FunctionUtil.blockUntrustedMacro(functionName);
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 2);
      boolean visible = FunctionUtil.getBooleanValue(parameters.get(0).toString());
      final var zr = FunctionUtil.getZoneRendererFromParam(functionName, parameters, 1);
      final var zone = zr.getZone();
      // Set the zone and return the visibility of the current map/zone
      zone.setVisible(visible);
      MapTool.serverCommand().setZoneVisibility(zone.getId(), zone.isVisible());
      MapTool.getFrame().getZoneMiniMapPanel().flush();
      MapTool.getFrame().repaint();
      return zone.isVisible() ? BigDecimal.ONE : BigDecimal.ZERO;

    } else if ("setMapName".equalsIgnoreCase(functionName)) {
      FunctionUtil.blockUntrustedMacro(functionName);
      FunctionUtil.checkNumberParam(functionName, parameters, 2, 2);
      String mapNameOrId = parameters.get(0).toString();
      String newMapName = parameters.get(1).toString();
      Zone zone = FunctionUtil.getZoneRenderer(functionName, mapNameOrId).getZone();
      zone.setName(newMapName);
      MapTool.serverCommand().renameZone(zone.getId(), newMapName);
      if (zone == MapTool.getFrame().getCurrentZoneRenderer().getZone()) {
        MapTool.getFrame().setCurrentZoneRenderer(MapTool.getFrame().getCurrentZoneRenderer());
      }
      return zone.getName();

    } else if ("setMapDisplayName".equalsIgnoreCase(functionName)) {
      FunctionUtil.blockUntrustedMacro(functionName);
      FunctionUtil.checkNumberParam(functionName, parameters, 2, 2);
      String mapNameOrId = parameters.get(0).toString();
      String newMapDisplayName = parameters.get(1).toString();

      Zone zone = FunctionUtil.getZoneRenderer(functionName, mapNameOrId).getZone();
      if (newMapDisplayName.equals(zone.getDisplayName())) {
        // The name is the same, so nothing to do.
        return newMapDisplayName;
      }
      zone.setPlayerAlias(newMapDisplayName);

      MapTool.serverCommand().changeZoneDispName(zone.getId(), newMapDisplayName);
      if (zone == MapTool.getFrame().getCurrentZoneRenderer().getZone()) {
        MapTool.getFrame().setCurrentZoneRenderer(MapTool.getFrame().getCurrentZoneRenderer());
      }

      return zone.getDisplayName();

    } else if ("copyMap".equalsIgnoreCase(functionName)) {
      FunctionUtil.blockUntrustedMacro(functionName);
      FunctionUtil.checkNumberParam(functionName, parameters, 2, 2);
      String oldMapNameOrId = parameters.get(0).toString();
      String newName = parameters.get(1).toString();
      Zone oldMap = FunctionUtil.getZoneRenderer(functionName, oldMapNameOrId).getZone();
      Zone newMap = new Zone(oldMap);
      newMap.setName(newName);
      MapTool.addZone(newMap, false);
      MapTool.serverCommand().putZone(newMap);
      return newMap.getName();

    } else if ("createMap".equalsIgnoreCase(functionName)) {
      FunctionUtil.blockUntrustedMacro(functionName);
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 2);
      String mapName = parameters.get(0).toString();
      JsonObject config =
          parameters.size() < 2
              ? new JsonObject()
              : FunctionUtil.paramAsJsonObject(functionName, parameters, 1);

      final var newMap = ZoneFactory.createZone();
      newMap.setName(mapName);

      if (config.has("display name")) {
        newMap.setPlayerAlias(config.getAsJsonPrimitive("display name").getAsString());
      }
      if (config.has("player visible")) {
        final var visible = config.getAsJsonPrimitive("player visible").getAsBoolean();
        newMap.setVisible(visible);
      }
      if (config.has("vision type")) {
        newMap.setVisionType(
            Zone.VisionType.valueOf(
                config.getAsJsonPrimitive("vision type").getAsString().toUpperCase()));
      }
      if (config.has("vision distance")) {
        newMap.setTokenVisionDistance(config.getAsJsonPrimitive("vision distance").getAsInt());
      }
      if (config.has("lighting style")) {
        newMap.setLightingStyle(
            Zone.LightingStyle.valueOf(
                config.getAsJsonPrimitive("lighting style").getAsString().toUpperCase()));
      }
      if (config.has("has fog")) {
        final var hasFog = config.getAsJsonPrimitive("has fog").getAsBoolean();
        newMap.setHasFog(hasFog);
      }
      if (config.has("ai rounding")) {
        final var aiRounding =
            Zone.AStarRoundingOptions.valueOf(
                config.getAsJsonPrimitive("ai rounding").getAsString().toUpperCase());
        newMap.setAStarRounding(aiRounding);
      }

      {
        final var gridConfig =
            config.has("grid") ? config.getAsJsonObject("grid") : new JsonObject();

        final var gridType =
            gridConfig.has("type")
                ? gridConfig.getAsJsonPrimitive("type").getAsString()
                : AppPreferences.getDefaultGridType();
        final var grid =
            GridFactory.createGrid(
                gridType, AppPreferences.getFaceEdge(), AppPreferences.getFaceVertex());

        final var gridColor =
            gridConfig.has("color")
                ? MapToolUtil.getColor(gridConfig.getAsJsonPrimitive("color").getAsString())
                : AppPreferences.getDefaultGridColor();
        newMap.setGridColor(gridColor.getRGB());

        final var gridUnitsPerCell =
            gridConfig.has("units per cell")
                ? gridConfig.getAsJsonPrimitive("units per cell").getAsDouble()
                : AppPreferences.getDefaultUnitsPerCell();
        newMap.setUnitsPerCell(gridUnitsPerCell);

        final var gridSize =
            gridConfig.has("size")
                ? gridConfig.getAsJsonPrimitive("size").getAsInt()
                : AppPreferences.getDefaultGridSize();
        grid.setSize(gridSize);

        final var gridOffsetX =
            gridConfig.has("x offset") ? gridConfig.getAsJsonPrimitive("x offset").getAsInt() : 0;
        final var gridOffsetY =
            gridConfig.has("y offset") ? gridConfig.getAsJsonPrimitive("y offset").getAsInt() : 0;
        grid.setOffset(gridOffsetX, gridOffsetY);

        newMap.setGrid(grid);
      }

      // The background and fog paints can either be an asset:// URL or a color.
      final Map<String, Consumer<DrawablePaint>> mapPaints =
          Map.of(
              "background paint", newMap::setBackgroundPaint,
              "fog paint", newMap::setFogPaint);
      for (final var entry : mapPaints.entrySet()) {
        final var property = entry.getKey();
        if (!config.has(property)) {
          continue;
        }

        final var paint =
            FunctionUtil.getPaintFromString(config.getAsJsonPrimitive(property).getAsString());
        MapToolUtil.uploadTexture(paint);
        entry.getValue().accept(paint);
      }

      if (config.has("map asset")) {
        final var mapAssetId = config.getAsJsonPrimitive("map asset").getAsString();
        final var mapAssetKey = FunctionUtil.getAssetKeyFromString(mapAssetId);
        if (mapAssetKey == null) {
          throw new ParserException(
              I18N.getText("macro.function.map.invalidAsset", functionName, mapAssetId));
        }

        final var mapAsset = AssetManager.getAsset(mapAssetKey);
        if (mapAsset != null) {
          AssetManager.putAsset(mapAsset);
          if (!MapTool.isHostingServer()) {
            MapTool.serverCommand().putAsset(mapAsset);
          }
        }

        newMap.setMapAsset(mapAssetKey);
      }

      MapTool.addZone(newMap, false);
      return newMap.getId();
    } else if ("getVisibleMapIDs".equalsIgnoreCase(functionName)
        || "getAllMapIDs".equalsIgnoreCase(functionName)) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 1);
      String delim = parameters.size() > 0 ? parameters.get(0).toString() : ",";

      boolean allMaps = functionName.equalsIgnoreCase("getAllMapIDs");

      if (allMaps) {
        FunctionUtil.blockUntrustedMacro(functionName);
      }

      List<String> mapIds = new LinkedList<>();
      for (ZoneRenderer zr : MapTool.getFrame().getZoneRenderers()) {
        if (allMaps || zr.getZone().isVisible()) {
          mapIds.add(zr.getZone().getId().toString());
        }
      }

      return FunctionUtil.delimitedResult(delim, mapIds);
    } else if ("getVisibleMapNames".equalsIgnoreCase(functionName)
        || "getAllMapNames".equalsIgnoreCase(functionName)) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 1);
      boolean allMaps = functionName.equalsIgnoreCase("getAllMapNames");

      if (allMaps) {
        FunctionUtil.blockUntrustedMacro(functionName);
      }

      List<String> mapNames = new LinkedList<>();
      for (ZoneRenderer zr : MapTool.getFrame().getZoneRenderers()) {
        if (allMaps || zr.getZone().isVisible()) {
          mapNames.add(zr.getZone().getName());
        }
      }

      String delim = parameters.size() > 0 ? parameters.get(0).toString() : ",";
      return FunctionUtil.delimitedResult(delim, mapNames);

    } else if ("getVisibleMapDisplayNames".equalsIgnoreCase(functionName)
        || "getAllMapDisplayNames".equalsIgnoreCase(functionName)) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 1);
      boolean allMaps = functionName.equalsIgnoreCase("getAllMapDisplayNames");

      if (allMaps) {
        FunctionUtil.blockUntrustedMacro(functionName);
      }

      List<String> mapNames = new LinkedList<>();
      for (ZoneRenderer zr : MapTool.getFrame().getZoneRenderers()) {
        if (allMaps || zr.getZone().isVisible()) {
          mapNames.add(zr.getZone().getDisplayName());
        }
      }

      String delim = parameters.size() > 0 ? parameters.get(0).toString() : ",";
      return FunctionUtil.delimitedResult(delim, mapNames);

    } else if ("getMapName".equalsIgnoreCase(functionName)) {
      FunctionUtil.blockUntrustedMacro(functionName);
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 1);

      final var map = parameters.get(0).toString();

      // First try treat it as a map ID.
      ZoneRenderer match = null;
      if (!GUID.isNotGUID(map)) {
        try {
          match = MapTool.getFrame().getZoneRenderer(GUID.valueOf(map));
        } catch (InvalidGUIDException ignored) {
          // Wasn't a GUID after all.
        }
      }

      if (match == null) {
        // Fall back to look up by display name.
        for (ZoneRenderer zr : MapTool.getFrame().getZoneRenderers()) {
          if (map.equals(zr.getZone().getDisplayName())) {
            match = zr;
            break;
          }
        }
      }
      if (match != null) {
        return match.getZone().getName();
      }

      throw new ParserException(I18N.getText("macro.function.map.notFound", functionName));

    } else if ("setMapSelectButton".equalsIgnoreCase(functionName)) {
      // this is kind of a map function? :)
      FunctionUtil.blockUntrustedMacro(functionName);
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 1);
      boolean vis = !parameters.get(0).toString().equals("0");
      if (MapTool.getFrame().getFullsZoneButton() != null) {
        MapTool.getFrame().getFullsZoneButton().setVisible(vis);
      }
      MapTool.getFrame().getToolbarPanel().getMapselect().setVisible(vis);
      return (MapTool.getFrame().getToolbarPanel().getMapselect().isVisible()
          ? BigDecimal.ONE
          : BigDecimal.ZERO);
    } else if ("setMapVision".equalsIgnoreCase(functionName)) {
      FunctionUtil.blockUntrustedMacro(functionName);
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 1);
      Zone currentZR = MapTool.getFrame().getCurrentZoneRenderer().getZone();
      if (currentZR == null) {
        throw new ParserException(I18N.getText("macro.function.map.none", functionName));
      }
      switch (parameters.get(0).toString().toLowerCase()) {
        case "off" -> MapTool.serverCommand().setVisionType(currentZR.getId(), Zone.VisionType.OFF);
        case "day" -> MapTool.serverCommand().setVisionType(currentZR.getId(), Zone.VisionType.DAY);
        case "night" -> MapTool.serverCommand()
            .setVisionType(currentZR.getId(), Zone.VisionType.NIGHT);
        default -> throw new ParserException(
            I18N.getText("macro.function.general.argumentTypeInvalid", functionName));
      }
      return "";
    } else if ("getMapVision".equalsIgnoreCase(functionName)) {
      FunctionUtil.checkNumberParam(functionName, parameters, 0, 0);
      ZoneRenderer currentZR = MapTool.getFrame().getCurrentZoneRenderer();
      return currentZR.getZone().getVisionType().toString();
    }

    throw new ParserException(I18N.getText("macro.function.general.unknownFunction", functionName));
  }
}
