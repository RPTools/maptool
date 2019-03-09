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

import java.util.LinkedList;
import java.util.List;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Zone;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;
import net.sf.json.JSONArray;

public class MapFunctions extends AbstractFunction {
  private static final MapFunctions instance = new MapFunctions();

  private MapFunctions() {
    super(
        0,
        2,
        "getAllMapNames",
        "getCurrentMapName",
        "getVisibleMapNames",
        "setCurrentMap",
        "getMapVisible",
        "setMapVisible",
        "setMapName",
        "copyMap");
  }

  public static MapFunctions getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> parameters)
      throws ParserException {
    if (functionName.equals("getCurrentMapName")) {
      checkNumberOfParameters(functionName, parameters, 0, 0);
      return MapTool.getFrame().getCurrentZoneRenderer().getZone().getName();
    } else if (functionName.equals("setCurrentMap")) {
      checkTrusted(functionName);
      checkNumberOfParameters(functionName, parameters, 1, 1);
      String mapName = parameters.get(0).toString();
      ZoneRenderer zr = getNamedMap(functionName, mapName);
      if (zr != null) {
        MapTool.getFrame().setCurrentZoneRenderer(zr);
        return mapName;
      }
      throw new ParserException(
          I18N.getText("macro.function.moveTokenMap.unknownMap", functionName, mapName));

    } else if ("getMapVisible".equalsIgnoreCase(functionName)) {
      checkNumberOfParameters(functionName, parameters, 0, 1);
      if (parameters.size() > 0) {
        String mapName = parameters.get(0).toString();
        return getNamedMap(functionName, mapName).getZone().isVisible() ? "1" : "0";
      } else {
        // Return the visibility of the current map/zone
        return MapTool.getFrame().getCurrentZoneRenderer().getZone().isVisible() ? "1" : "0";
      }

    } else if ("setMapVisible".equalsIgnoreCase(functionName)) {
      checkTrusted(functionName);
      checkNumberOfParameters(functionName, parameters, 1, 2);
      boolean visible = AbstractTokenAccessorFunction.getBooleanValue(parameters.get(0).toString());
      Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
      if (parameters.size() > 1) {
        String mapName = parameters.get(1).toString();
        zone = getNamedMap(functionName, mapName).getZone();
      }
      // Set the zone and return the visibility of the current map/zone
      zone.setVisible(visible);
      MapTool.serverCommand().setZoneVisibility(zone.getId(), zone.isVisible());
      MapTool.getFrame().getZoneMiniMapPanel().flush();
      MapTool.getFrame().repaint();
      return zone.isVisible() ? "1" : "0";

    } else if ("setMapName".equalsIgnoreCase(functionName)) {
      checkTrusted(functionName);
      checkNumberOfParameters(functionName, parameters, 2, 2);
      String oldMapName = parameters.get(0).toString();
      String newMapName = parameters.get(1).toString();
      Zone zone = getNamedMap(functionName, oldMapName).getZone();
      zone.setName(newMapName);
      MapTool.serverCommand().renameZone(zone.getId(), newMapName);
      if (zone == MapTool.getFrame().getCurrentZoneRenderer().getZone())
        MapTool.getFrame().setCurrentZoneRenderer(MapTool.getFrame().getCurrentZoneRenderer());
      return zone.getName();

    } else if ("copyMap".equalsIgnoreCase(functionName)) {
      checkTrusted(functionName);
      checkNumberOfParameters(functionName, parameters, 2, 2);
      String oldName = parameters.get(0).toString();
      String newName = parameters.get(1).toString();
      Zone oldMap = getNamedMap(functionName, oldName).getZone();
      Zone newMap = new Zone(oldMap);
      newMap.setName(newName);
      MapTool.addZone(newMap, false);
      MapTool.serverCommand().putZone(newMap);
      return newMap.getName();

    } else {
      checkNumberOfParameters(functionName, parameters, 0, 1);
      boolean allMaps = functionName.equals("getAllMapNames");

      if (allMaps) checkTrusted(functionName);

      List<String> mapNames = new LinkedList<String>();
      for (ZoneRenderer zr : MapTool.getFrame().getZoneRenderers()) {
        if (allMaps || zr.getZone().isVisible()) {
          mapNames.add(zr.getZone().getName());
        }
      }
      String delim = parameters.size() > 0 ? parameters.get(0).toString() : ",";
      if ("json".equals(delim)) {
        return JSONArray.fromObject(mapNames);
      } else {
        return StringFunctions.getInstance().join(mapNames, delim);
      }
    }
  }

  /**
   * Find the map/zone for a given map name
   *
   * @param functionName String Name of the calling function.
   * @param mapName String Name of the searched for map.
   * @return ZoneRenderer The map/zone.
   * @throws ParserException if the map is not found
   */
  private ZoneRenderer getNamedMap(String functionName, String mapName) throws ParserException {
    for (ZoneRenderer zr : MapTool.getFrame().getZoneRenderers()) {
      if (mapName.equals(zr.getZone().getName())) {
        return zr;
      }
    }
    throw new ParserException(
        I18N.getText("macro.function.moveTokenMap.unknownMap", functionName, mapName));
  }

  /**
   * Checks that the number of objects in the list <code>parameters</code> is within given bounds
   * (inclusive). Throws a <code>ParserException</code> if the check fails.
   *
   * @param functionName this is used in the exception message
   * @param parameters a list of parameters
   * @param min the minimum amount of parameters (inclusive)
   * @param max the maximum amount of parameters (inclusive)
   * @throws ParserException if there were more or less parameters than allowed
   */
  private void checkNumberOfParameters(
      String functionName, List<Object> parameters, int min, int max) throws ParserException {
    int numberOfParameters = parameters.size();
    if (numberOfParameters < min) {
      throw new ParserException(
          I18N.getText(
              "macro.function.general.notEnoughParam", functionName, min, numberOfParameters));
    } else if (numberOfParameters > max) {
      throw new ParserException(
          I18N.getText(
              "macro.function.general.tooManyParam", functionName, max, numberOfParameters));
    }
  }

  /**
   * Checks whether or not the function is trusted
   *
   * @param functionName Name of the macro function
   * @throws ParserException Returns trust error message and function name
   */
  private void checkTrusted(String functionName) throws ParserException {
    if (!MapTool.getParser().isMacroTrusted()) {
      throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));
    }
  }
}
