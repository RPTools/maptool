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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.rptools.maptool.client.AppActions;
import net.rptools.maptool.client.AppActions.ZoneAdminClientAction;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.FogUtil;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;
import net.sf.json.JSONArray;

/**
 * @author jfrazierjr
 *     <p>modified by: Lee, Jamz
 */
public class FogOfWarFunctions extends AbstractFunction {
  private static final FogOfWarFunctions instance = new FogOfWarFunctions();

  private FogOfWarFunctions() {
    super(
        0,
        3,
        "exposePCOnlyArea",
        "exposeFogAtWaypoints",
        "toggleFoW",
        "exposeFOW",
        "exposeAllOwnedArea",
        "restoreFoW");
  }

  public static FogOfWarFunctions getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> parameters)
      throws ParserException {
    if (!MapTool.getParser().isMacroTrusted()) {
      throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));
    }

    int maxParamSize = functionName.equals("exposeFOW") || functionName.equals("exposeFoW") ? 3 : 1;

    if (parameters.size() > maxParamSize) {
      throw new ParserException(
          I18N.getText(
              "macro.function.general.tooManyParam",
              functionName,
              maxParamSize,
              parameters.size()));
    }

    ZoneRenderer zoneRenderer;
    if (parameters.size() >= 1) {
      String mapName = parameters.get(0).toString();
      zoneRenderer = MapTool.getFrame().getZoneRenderer(mapName);
      if (zoneRenderer == null) {
        throw new ParserException(
            I18N.getText("macro.function.moveTokenMap.unknownMap", functionName, mapName));
      }
    } else {
      zoneRenderer = MapTool.getFrame().getCurrentZoneRenderer();
    }

    /*
     * String empty = exposePCOnlyArea(optional String mapName)
     */
    if (functionName.equals("exposePCOnlyArea")) {
      FogUtil.exposePCArea(zoneRenderer);
      return "<!---->";
    }
    /*
     * String empty = exposeAllOwnedArea(optional String mapName)
     */
    if (functionName.equals("exposeAllOwnedArea")) {
      FogUtil.exposeAllOwnedArea(zoneRenderer);
      return "<!---->";
    }
    /*
     * String empty = exposeFOW(optional String mapName, optional String tokens, optional String delim)
     */
    if (functionName.equals("exposeFOW") || functionName.equals("exposeFoW")) {

      Set<GUID> tokenSet;

      if (parameters.size() <= 1) {
        tokenSet = getTokenSelectedSet(zoneRenderer);
      } else {
        String paramStr = parameters.get(1).toString();
        String delim = parameters.size() >= 3 ? parameters.get(2).toString() : ",";

        tokenSet = getTokenSetFromList(zoneRenderer.getZone(), paramStr, delim);
      }
      FogUtil.exposeVisibleArea(zoneRenderer, tokenSet, true);
      return "<!---->";
    }

    /*
     * String empty = restoreFOW(optional String mapName)
     */
    if (functionName.equals("restoreFOW") || functionName.equals("restoreFoW")) {
      FogUtil.restoreFoW(zoneRenderer);
      return "<!---->";
    }
    /*
     * Lee: String empty = toggleFoW()
     */
    if (functionName.equals("toggleFoW")) {
      ((ZoneAdminClientAction) AppActions.TOGGLE_FOG).execute(null);
      return ((ZoneAdminClientAction) AppActions.TOGGLE_FOG).isSelected()
          ? I18N.getText("msg.info.action.enableFoW")
          : I18N.getText("msg.info.action.disableFoW");
    }
    /*
     * Lee: String empty = exposeFogAtWaypoints()
     */
    if (functionName.equals("exposeFogAtWaypoints")) {

      if (((ZoneAdminClientAction) AppActions.TOGGLE_WAYPOINT_FOG_REVEAL).isAvailable()) {
        ((ZoneAdminClientAction) AppActions.TOGGLE_WAYPOINT_FOG_REVEAL).execute(null);

        return ((ZoneAdminClientAction) AppActions.TOGGLE_WAYPOINT_FOG_REVEAL).isSelected()
            ? I18N.getText("msg.info.action.enableRevealFogAtWaypoints")
            : I18N.getText("msg.info.action.disableRevealFogAtWaypoints");
      } else {
        return I18N.getText("msg.info.action.FoWDisabled");
      }
    }
    throw new ParserException(I18N.getText("macro.function.general.unknownFunction", functionName));
  }

  private Set<GUID> getTokenSelectedSet(final ZoneRenderer zr) {
    Set<GUID> tokens = zr.getSelectedTokenSet();
    Set<GUID> ownedTokens = zr.getOwnedTokens(tokens);
    return ownedTokens;
  }

  private Set<GUID> getTokenSetFromList(final Zone zone, final String paramStr, final String delim)
      throws ParserException {
    Set<GUID> tokenSet = new HashSet<GUID>();

    if (delim.equalsIgnoreCase("json")) {
      // A JSON Array was supplied
      Object json = JSONMacroFunctions.convertToJSON(paramStr);
      if (json instanceof JSONArray) {
        for (Object o : (JSONArray) json) {
          String identifier = (String) o;
          Token t = zone.resolveToken(identifier);
          if (t != null) {
            tokenSet.add(t.getId());
          } else {
            throw new ParserException(
                I18N.getText("macro.function.general.unknownToken", "exposeFOW", identifier));
          }
        }
      }
    } else {
      // String List
      String[] strList = paramStr.split(delim);
      for (String s : strList) {
        Token t = zone.resolveToken(s.trim());
        if (t != null) {
          tokenSet.add(t.getId());
        } else {
          throw new ParserException(
              I18N.getText("macro.function.general.unknownToken", "exposeFOW", s));
        }
      }
    }
    return tokenSet;
  }
}
