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
import java.util.List;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.InitiativeList;
import net.rptools.maptool.model.InitiativeList.TokenInitiative;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;

/**
 * Add token to initiative
 *
 * @author Jay
 */
public class TokenAddToInitiativeFunction extends AbstractFunction {

  /** Handle adding one, all, all PCs or all NPC tokens. */
  private TokenAddToInitiativeFunction() {
    super(0, 4, "addToInitiative");
  }

  /** singleton instance of this function */
  private static final TokenAddToInitiativeFunction instance = new TokenAddToInitiativeFunction();

  /** @return singleton instance */
  public static TokenAddToInitiativeFunction getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> args)
      throws ParserException {
    MapToolVariableResolver res = (MapToolVariableResolver) parser.getVariableResolver();

    boolean allowDuplicates =
        args.size() > 0 ? FunctionUtil.paramAsBoolean(functionName, args, 0, true) : false;
    String state = args.size() > 1 && !"".equals(args.get(1)) ? args.get(1).toString() : null;
    Token token = FunctionUtil.getTokenFromParam(res, functionName, args, 2, 3);

    InitiativeList list = token.getZoneRenderer().getZone().getInitiativeList();

    if (!MapTool.getParser().isMacroTrusted()) {
      if (!MapTool.getFrame().getInitiativePanel().hasOwnerPermission(token)) {
        String message = I18N.getText("macro.function.initiative.gmOnly", functionName);
        if (MapTool.getFrame().getInitiativePanel().isOwnerPermissions())
          message = I18N.getText("macro.function.initiative.gmOrOwner", functionName);
        throw new ParserException(message);
      } // endif
    }
    // insert the token if needed
    TokenInitiative ti = null;
    if (allowDuplicates || list.indexOf(token).isEmpty()) {
      ti = list.insertToken(-1, token);
      if (state != null) ti.setState(state);
    } else {
      MapTool.serverCommand().updateTokenProperty(token, "setInitiative", state);
    } // endif
    return ti != null ? BigDecimal.ONE : BigDecimal.ZERO;
  }
}
