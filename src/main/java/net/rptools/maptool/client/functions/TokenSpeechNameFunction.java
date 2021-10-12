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
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;

public class TokenSpeechNameFunction extends AbstractFunction {
  /** Singleton instance. */
  private static final TokenSpeechNameFunction instance = new TokenSpeechNameFunction();

  private TokenSpeechNameFunction() {
    super(0, 3, "getSpeechName", "setSpeechName");
  }

  /**
   * Gets the instance of Name.
   *
   * @return the instance of name.
   */
  public static TokenSpeechNameFunction getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> args)
      throws ParserException {
    Token token;

    if (functionName.equalsIgnoreCase("getSpeechName")) {
      FunctionUtil.checkNumberParam(functionName, args, 0, 2);
      token = FunctionUtil.getTokenFromParam(resolver, functionName, args, 0, 1);
    } else if ("setSpeechName".equalsIgnoreCase(functionName)) {
      FunctionUtil.checkNumberParam(functionName, args, 1, 3);
      String name = args.get(0).toString();
      token = FunctionUtil.getTokenFromParam(resolver, functionName, args, 1, 2);

      setSpeechName(token, name);
    } else {
      throw new ParserException(
          I18N.getText("macro.function.general.unknownFunction", functionName));
    }
    return getSpeechName(token);
  }

  /**
   * Gets the speech name of the token.
   *
   * @param token the token to get the name of.
   * @return the name of the token.
   */
  public static String getSpeechName(Token token) {
    return token.getSpeechName();
  }

  /**
   * Sets the speech name of the token
   *
   * @param token The token to set the name of.
   * @param name the name of the token.
   * @throws ParserException if an error occurs.
   */
  public static void setSpeechName(Token token, String name) throws ParserException {
    MapTool.serverCommand().updateTokenProperty(token, Token.Update.setSpeechName, name);
  }
}
