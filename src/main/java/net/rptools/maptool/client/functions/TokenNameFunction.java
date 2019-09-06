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
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;

public class TokenNameFunction extends AbstractFunction {
  /** Singleton instance. */
  private static final TokenNameFunction instance = new TokenNameFunction();

  private TokenNameFunction() {
    super(0, 3, "getName", "setName");
  }

  /**
   * Gets the instance of Name.
   *
   * @return the instance of name.
   */
  public static TokenNameFunction getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> args)
      throws ParserException {
    MapToolVariableResolver resolver = (MapToolVariableResolver) parser.getVariableResolver();
    Token token;

    if (functionName.equals("getName")) {
      FunctionUtil.checkNumberParam(functionName, args, 0, 2);
      token = FunctionUtil.getTokenFromParam(resolver, functionName, args, 0, 1);
    } else {
      FunctionUtil.checkNumberParam(functionName, args, 1, 3);
      token = FunctionUtil.getTokenFromParam(resolver, functionName, args, 1, 2);

      if (args.get(0).toString().equals("")) {
        throw new ParserException(
            I18N.getText("macro.function.tokenName.emptyTokenNameForbidden", "setName"));
      }

      token.setName(args.get(0).toString());

      ZoneRenderer renderer = token.getZoneRenderer();
      MapTool.serverCommand().putToken(renderer.getZone().getId(), token);
    }
    return token.getName();
  }

  /**
   * Gets the name of the token.
   *
   * @param token the token to get the name of.
   * @return the name of the token.
   */
  public String getName(Token token) {
    return token.getName();
  }

  /**
   * Sets the name of the token.
   *
   * @param token The token to set the name of.
   * @param name the name of the token.
   */
  public void setName(Token token, String name) {
    token.setName(name);
  }
}
