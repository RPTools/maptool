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
import java.util.LinkedList;
import java.util.List;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Zone;
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
        "getAllMapNames",
        "getAllMapDisplayNames",
        "getCurrentMapName",
        "getMapDisplayName",
        "getVisibleMapNames",
        "getVisibleMapDisplayNames",
        "setCurrentMap",
        "getMapVisible",
        "setMapVisible",
        "setMapName",
        "setMapDisplayName",
        "copyMap",
        "getMapName",
        "setMapSelectButton");
  }

  public static MapFunctions getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {
    if (functionName.equalsIgnoreCase("getCurrentMapName")) {
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
      String mapName = parameters.get(0).toString();
      final var zr = FunctionUtil.getZoneRenderer(functionName, mapName);
      MapTool.getFrame().setCurrentZoneRenderer(zr);
      return mapName;

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
      String oldMapName = parameters.get(0).toString();
      String newMapName = parameters.get(1).toString();
      Zone zone = FunctionUtil.getZoneRenderer(functionName, oldMapName).getZone();
      zone.setName(newMapName);
      MapTool.serverCommand().renameZone(zone.getId(), newMapName);
      if (zone == MapTool.getFrame().getCurrentZoneRenderer().getZone()) {
        MapTool.getFrame().setCurrentZoneRenderer(MapTool.getFrame().getCurrentZoneRenderer());
      }
      return zone.getName();

    } else if ("setMapDisplayName".equalsIgnoreCase(functionName)) {
      FunctionUtil.blockUntrustedMacro(functionName);
      FunctionUtil.checkNumberParam(functionName, parameters, 2, 2);
      String mapName = parameters.get(0).toString();
      String newMapDisplayName = parameters.get(1).toString();

      Zone zone = FunctionUtil.getZoneRenderer(functionName, mapName).getZone();
      if (newMapDisplayName.equals(zone.getDisplayName())) {
        // The name is the same, so nothing to do.
        return newMapDisplayName;
      }

      final var nameChangeAccepted = zone.setPlayerAlias(newMapDisplayName);
      if (!nameChangeAccepted) {
        throw new ParserException(
            I18N.getText("macro.function.map.duplicateDisplay", functionName));
      }

      MapTool.serverCommand().changeZoneDispName(zone.getId(), newMapDisplayName);
      if (zone == MapTool.getFrame().getCurrentZoneRenderer().getZone()) {
        MapTool.getFrame().setCurrentZoneRenderer(MapTool.getFrame().getCurrentZoneRenderer());
      }

      return zone.getDisplayName();

    } else if ("copyMap".equalsIgnoreCase(functionName)) {
      FunctionUtil.blockUntrustedMacro(functionName);
      FunctionUtil.checkNumberParam(functionName, parameters, 2, 2);
      String oldName = parameters.get(0).toString();
      String newName = parameters.get(1).toString();
      Zone oldMap = FunctionUtil.getZoneRenderer(functionName, oldName).getZone();
      Zone newMap = new Zone(oldMap);
      newMap.setName(newName);
      MapTool.addZone(newMap, false);
      MapTool.serverCommand().putZone(newMap);
      return newMap.getName();

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
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 1);
      String displayName = parameters.get(0).toString();
      FunctionUtil.blockUntrustedMacro(functionName);

      for (ZoneRenderer zr : MapTool.getFrame().getZoneRenderers()) {
        if (displayName.equals(zr.getZone().getDisplayName())) {
          return zr.getZone().getName();
        }
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
    }

    throw new ParserException(I18N.getText("macro.function.general.unknownFunction", functionName));
  }
}
