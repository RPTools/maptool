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
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;

public class TokenGMNameFunction extends AbstractFunction {

  private TokenGMNameFunction() {
    super(0, 3, "getGMName", "setGMName");
  }

  /** Singleton instance of GMName. */
  private static final TokenGMNameFunction instance = new TokenGMNameFunction();

  /**
   * Gets the singleton instance of GMName.
   *
   * @return the instance.
   */
  public static TokenGMNameFunction getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> args)
      throws ParserException {

    if (functionName.equals("getGMName")) {
      FunctionUtil.checkNumberParam("getGMName", args, 0, 2);
      Token token = FunctionUtil.getTokenFromParam(parser, functionName, args, 0, 1);
      return getGMName(token);
    } else {
      FunctionUtil.checkNumberParam("setGMName", args, 1, 3);
      String gmName = args.get(0).toString();
      Token token = FunctionUtil.getTokenFromParam(parser, functionName, args, 1, 2);
      return setGMName(token, gmName);
    }
  }

  /**
   * Gets the GMName of the specified token.
   *
   * @param token the token to get theGMName of.
   * @return the GMName.
   * @throws ParserException if the user does not have the permission.
   */
  public static String getGMName(Token token) throws ParserException {
    if (!MapTool.getParser().isMacroTrusted()) {
      throw new ParserException(I18N.getText("macro.function.general.noPerm", "getGMName"));
    }
    return token.getGMName() != null ? token.getGMName() : "";
  }

  /**
   * Sets the GMName of the token, and update server.
   *
   * @param token the token to set the GMName of.
   * @param gmName The name to set the GMName to.
   * @throws ParserException if the user does not have the permission.
   * @return the new GMName of the token.
   */
  public static String setGMName(Token token, String gmName) throws ParserException {
    if (!MapTool.getParser().isMacroTrusted()) {
      throw new ParserException(I18N.getText("macro.function.general.noPerm", "setGMName"));
    }
    MapTool.serverCommand().updateTokenProperty(token, Token.Update.setGMName, gmName);
    return gmName;
  }
}
