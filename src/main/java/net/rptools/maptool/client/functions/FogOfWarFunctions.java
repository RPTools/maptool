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

import java.util.List;
import java.util.Set;
import net.rptools.maptool.client.AppActions;
import net.rptools.maptool.client.AppActions.ZoneAdminClientAction;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.FogUtil;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.GUID;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;

/**
 * @author jfrazierjr
 *     <p>modified by: Lee, Jamz
 */
public class FogOfWarFunctions extends AbstractFunction {
  private static final FogOfWarFunctions instance = new FogOfWarFunctions();

  private FogOfWarFunctions() {
    super(
        0,
        2,
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
    if (parameters.size() > 1) {
      throw new ParserException(
          I18N.getText("macro.function.general.tooManyParam", functionName, 1, parameters.size()));
    }
    ZoneRenderer zoneRenderer =
        (parameters.size() == 1 && parameters.get(0) instanceof String)
            ? getZoneRenderer((String) parameters.get(0))
            : getZoneRenderer(null);

    /*
     * String empty = exposePCOnlyArea(optional String mapName)
     */
    if (functionName.equals("exposePCOnlyArea")) {
      FogUtil.exposePCArea(zoneRenderer);
      return "<!---->";
    }
    /*
     * String empty = exposePCOnlyArea(optional String mapName)
     */
    if (functionName.equals("exposeAllOwnedArea")) {
      FogUtil.exposeAllOwnedArea(zoneRenderer);
      return "<!---->";
    }
    /*
     * String empty = exposeFOW(optional String mapName)
     */
    if (functionName.equals("exposeFOW") || functionName.equals("exposeFoW")) {
      FogUtil.exposeVisibleArea(zoneRenderer, getTokenSelectedSet(zoneRenderer), true);
      return "<!---->";
    }
    /*
     * String empty = exposeFOW(optional String mapName)
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
    Set<GUID> tokens = MapTool.getFrame().getCurrentZoneRenderer().getSelectedTokenSet();
    Set<GUID> ownedTokens = MapTool.getFrame().getCurrentZoneRenderer().getOwnedTokens(tokens);
    return ownedTokens;
  }

  private ZoneRenderer getZoneRenderer(final String name) {
    if (name == null) {
      return MapTool.getFrame().getCurrentZoneRenderer();
    }
    for (ZoneRenderer zr : MapTool.getFrame().getZoneRenderers()) {
      if (zr.getZone().getName().equals(name.toString())) {
        return zr;
      }
    }
    return MapTool.getFrame().getCurrentZoneRenderer();
  }
}
