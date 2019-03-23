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
import java.util.ArrayList;
import java.util.List;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.tokenpanel.InitiativePanel;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.InitiativeList;
import net.rptools.maptool.model.InitiativeList.TokenInitiative;
import net.rptools.maptool.model.InitiativeListModel;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;
import net.sf.json.JSONObject;

/**
 * Advance the initiative
 *
 * @author Jay
 */
public class MiscInitiativeFunction extends AbstractFunction {

  /** Handle adding one, all, all PCs or all NPC tokens. */
  private MiscInitiativeFunction() {
    super(0, 0, "nextInitiative", "sortInitiative", "initiativeSize", "getInitiativeList");
  }

  /** singleton instance of this function */
  private static final MiscInitiativeFunction instance = new MiscInitiativeFunction();

  /** @return singleton instance */
  public static MiscInitiativeFunction getInstance() {
    return instance;
  }

  /**
   * @see net.rptools.parser.function.AbstractFunction#childEvaluate(net.rptools.parser.Parser,
   *     java.lang.String, java.util.List)
   */
  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> args)
      throws ParserException {
    InitiativeList list = MapTool.getFrame().getCurrentZoneRenderer().getZone().getInitiativeList();
    InitiativePanel ip = MapTool.getFrame().getInitiativePanel();
    if (functionName.equals("nextInitiative")) {
      if (!MapTool.getParser().isMacroTrusted()) {
        if (!ip.hasGMPermission()
            && (list.getCurrent() <= 0
                || !ip.hasOwnerPermission(list.getTokenInitiative(list.getCurrent()).getToken()))) {
          String message = I18N.getText("macro.function.initiative.gmOnly", functionName);
          if (ip.isOwnerPermissions())
            message = I18N.getText("macro.function.initiative.gmOrOwner", functionName);
          throw new ParserException(message);
        } // endif
      }
      list.nextInitiative();
      return new BigDecimal(list.getCurrent());
    } else if (functionName.equals("getInitiativeList")) {

      // Save round and zone name
      JSONObject out = new JSONObject();
      out.element("round", list.getRound());
      out.element("map", list.getZone().getName());

      // Get the visible tokens only
      List<JSONObject> tokens = new ArrayList<JSONObject>(list.getTokens().size());
      int current = -1; // Assume that the current isn't visible
      boolean hideNPCs = list.isHideNPC();
      for (TokenInitiative ti : list.getTokens()) {
        if (!MapTool.getParser().isMacroTrusted()
            && !InitiativeListModel.isTokenVisible(ti.getToken(), hideNPCs)) continue;
        if (ti == list.getTokenInitiative(list.getCurrent()))
          current = tokens.size(); // Make sure the current number matches what the user can see
        JSONObject tiJSON = new JSONObject();
        tiJSON.element("holding", ti.isHolding());
        tiJSON.element("initiative", ti.getState());
        tiJSON.element("tokenId", ti.getId().toString());
        tokens.add(tiJSON);
      } // endfor
      out.element("current", current);
      out.element("tokens", tokens);
      return out.toString();
    }
    if (!MapTool.getParser().isMacroTrusted() && !ip.hasGMPermission())
      throw new ParserException(I18N.getText("macro.function.general.onlyGM", functionName));
    if (functionName.equals("sortInitiative")) {
      list.sort();
      return new BigDecimal(list.getSize());
    } else if (functionName.equals("initiativeSize")) {
      return new BigDecimal(list.getSize());
    } // endif
    throw new ParserException(I18N.getText("macro.function.general.unknownFunction", functionName));
  }
}
