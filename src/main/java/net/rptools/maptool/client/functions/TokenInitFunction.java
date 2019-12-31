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
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.InitiativeList;
import net.rptools.maptool.model.InitiativeList.TokenInitiative;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;

/**
 * Set the token initiative
 *
 * @author Jay
 */
public class TokenInitFunction extends AbstractFunction {

  /** Getter has 0 to 2, setter has 1 to 3 */
  private TokenInitFunction() {
    super(0, 4, "setInitiative", "getInitiative", "addToInitiative");
  }

  /** singleton instance of this function */
  private static final TokenInitFunction singletonInstance = new TokenInitFunction();

  /** @return singleton instance */
  public static TokenInitFunction getInstance() {
    return singletonInstance;
  };

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> args)
      throws ParserException {
    if (functionName.equalsIgnoreCase("getInitiative")) {
      FunctionUtil.checkNumberParam(functionName, args, 0, 2);
      Token token = FunctionUtil.getTokenFromParam(parser, functionName, args, 0, 1);
      return getInitiative(token);
    } else if (functionName.equalsIgnoreCase("addToInitiative")) {
      FunctionUtil.checkNumberParam(functionName, args, 0, 4);
      boolean allowDuplicates =
          args.size() > 0 ? FunctionUtil.paramAsBoolean(functionName, args, 0, true) : false;
      String state = args.size() > 1 && !"".equals(args.get(1)) ? args.get(1).toString() : null;
      Token token = FunctionUtil.getTokenFromParam(parser, functionName, args, 2, 3);
      return addToInitiative(allowDuplicates, state, token);
    } else { // setInitiative
      FunctionUtil.checkNumberParam(functionName, args, 1, 3);
      String value = args.get(0).toString();
      Token token = FunctionUtil.getTokenFromParam(parser, functionName, args, 1, 2);
      return setInitiative(token, value);
    }
  }

  /**
   * Return a string containing the initiatives of the token
   *
   * @param token the token
   * @return a String list of the initiatives
   */
  public static String getInitiative(Token token) {
    String ret = "";
    List<TokenInitiative> tis = token.getInitiatives();
    if (tis.isEmpty()) return I18N.getText("macro.function.TokenInit.notOnList");
    for (TokenInitiative ti : tis) {
      if (ret.length() > 0) ret += ", ";
      ret += ti.getState();
    } // endif
    return ret;
  }

  /**
   * Add a token to the initiative. Can also assign the token an initiative state.
   *
   * @param allowDuplicates are duplicates allowed
   * @param state the initiative to assign to the token
   * @param token the token to add to the initiative
   * @return 1 if the token was added, 0 otherwise
   */
  public static BigDecimal addToInitiative(boolean allowDuplicates, String state, Token token)
      throws ParserException {
    boolean hasPermission = MapTool.getFrame().getInitiativePanel().hasOwnerPermission(token);
    if (!MapTool.getParser().isMacroTrusted() && !hasPermission) {
      String message;
      if (MapTool.getFrame().getInitiativePanel().isOwnerPermissions()) {
        message = I18N.getText("macro.function.initiative.gmOrOwner", "addToInitiative");
      } else {
        message = I18N.getText("macro.function.initiative.gmOnly", "addToInitiative");
      }
      throw new ParserException(message);
    } // endif

    InitiativeList list = token.getZoneRenderer().getZone().getInitiativeList();
    // insert the token if needed
    TokenInitiative ti = null;
    if (allowDuplicates || list.indexOf(token).isEmpty()) {
      ti = list.insertToken(-1, token);
      if (state != null) ti.setState(state);
    } else {
      setInitiative(token, state);
    } // endif
    return ti != null ? BigDecimal.ONE : BigDecimal.ZERO;
  }

  /**
   * Set an initiative value to all initiative entries of a token.
   *
   * @param token the token to set the initiative of
   * @param state the initiative to assign to the token
   * @return the value assigned to the initiative
   */
  public static Object setInitiative(Token token, String state) {
    List<InitiativeList.TokenInitiative> tis = token.getInitiatives();
    for (InitiativeList.TokenInitiative ti : tis) ti.setState(state);
    if (tis.isEmpty()) {
      return I18N.getText("macro.function.TokenInit.notOnListSet");
    } else {
      return state;
    }
  }
}
