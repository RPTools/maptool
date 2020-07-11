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
import net.rptools.maptool.model.Token;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;

public class TokenLabelFunction extends AbstractFunction {
  /** The singleton instance. */
  private static final TokenLabelFunction instance = new TokenLabelFunction();

  public static TokenLabelFunction getInstance() {
    return instance;
  }

  private TokenLabelFunction() {
    super(0, 2, "getLabel", "setLabel");
  }

  /**
   * Gets the label for the specified token.
   *
   * @param token The token to get the label for.
   * @return the label.
   */
  public static String getLabel(Token token) {
    return token.getLabel() != null ? token.getLabel() : "";
  }

  /**
   * Sets the label for the specified token.
   *
   * @param token The token to set the label for.
   * @param label the label to set.
   */
  public static void setLabel(Token token, String label) {
    MapTool.serverCommand().updateTokenProperty(token, Token.Update.setLabel, label);
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> args)
      throws ParserException {
    if (functionName.equals("getLabel")) {
      return getLabel((MapToolVariableResolver) resolver, args);
    } else if ("setLabel".equalsIgnoreCase(functionName)) {
      return setLabel((MapToolVariableResolver) resolver, args);
    }
    throw new ParserException(I18N.getText("macro.function.general.unknownFunction", functionName));
  }

  /**
   * Gets the label of the token
   *
   * @param args The arguments passed.
   * @return the name of the token.
   * @throws ParserException when an error occurs.
   */
  private static Object getLabel(MapToolVariableResolver resolver, List<Object> args)
      throws ParserException {
    Token token;

    if (args.size() == 1) {
      if (!MapTool.getParser().isMacroTrusted()) {
        throw new ParserException(I18N.getText("macro.function.general.noPermOther", "getLabel"));
      }

      token = FindTokenFunctions.findToken(args.get(0).toString(), null);
      if (token == null) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.unknownToken", "getLabel", args.get(0).toString()));
      }
    } else if (args.isEmpty()) {
      token = resolver.getTokenInContext();
      if (token == null) {
        throw new ParserException(
            I18N.getText("macro.function.general.noImpersonated", "getLabel"));
      }
    } else {
      throw new ParserException(
          I18N.getText("macro.function.general.tooManyParam", "getLabel", 1, args.size()));
    }
    return getLabel(token);
  }

  /**
   * Sets the label of the token.
   *
   * @param args The arguments passed.
   * @return the new name of the token.
   * @throws ParserException when an error occurs.
   */
  private static Object setLabel(MapToolVariableResolver resolver, List<Object> args)
      throws ParserException {
    FunctionUtil.checkNumberParam("setLabel", args, 1, 3);

    String label = args.get(0).toString();
    Token token = FunctionUtil.getTokenFromParam(resolver, "setLabel", args, 1, 2);
    setLabel(token, label);
    return label;
  }
}
