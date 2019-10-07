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
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.language.I18N;
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
    super(0, 3, "setInitiative", "getInitiative");
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
    MapToolVariableResolver res = (MapToolVariableResolver) parser.getVariableResolver();

    if (functionName.equalsIgnoreCase("getInitiative")) {
      FunctionUtil.checkNumberParam(functionName, args, 0, 2);
      Token token = FunctionUtil.getTokenFromParam(res, functionName, args, 0, 1);
      return getInitiative(token);
    } else {
      FunctionUtil.checkNumberParam(functionName, args, 1, 3);
      String value = args.get(0).toString();
      Token token = FunctionUtil.getTokenFromParam(res, functionName, args, 1, 2);
      return setInitiative(token, value);
    }
  }

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

  public static Object setInitiative(Token token, String value) {
    if (token.getInitiatives().isEmpty())
      return I18N.getText("macro.function.TokenInit.notOnListSet");
    MapTool.serverCommand().updateTokenProperty(token, "setInitiative", value);
    return value;
  }
}
