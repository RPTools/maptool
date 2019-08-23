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
      checkNumberOfParameters(functionName, args, 0, 2);
      token = getTokenFromParam(resolver, functionName, args, 0, 1);
    } else {
      checkNumberOfParameters(functionName, args, 1, 3);
      token = getTokenFromParam(resolver, functionName, args, 1, 2);

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

  /**
   * Gets the token from the specified index or returns the token in context. This method will check
   * the list size before trying to retrieve the token so it is safe to use for functions that have
   * the token as a optional argument.
   *
   * @param res the variable resolver
   * @param functionName The function name (used for generating exception messages).
   * @param param The parameters for the function.
   * @param indexToken The index to find the token at.
   * @param indexMap The index to find the map name at. If -1, use current map instead.
   * @return the token.
   * @throws ParserException if a token is specified but the macro is not trusted, or the specified
   *     token can not be found, or if no token is specified and no token is impersonated.
   */
  private Token getTokenFromParam(
      MapToolVariableResolver res,
      String functionName,
      List<Object> param,
      int indexToken,
      int indexMap)
      throws ParserException {

    String mapName =
        indexMap >= 0 && param.size() > indexMap ? param.get(indexMap).toString() : null;
    Token token;
    if (param.size() > indexToken) {
      if (!MapTool.getParser().isMacroTrusted()) {
        throw new ParserException(I18N.getText("macro.function.general.noPermOther", functionName));
      }
      token = FindTokenFunctions.findToken(param.get(indexToken).toString(), mapName);
      if (token == null) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.unknownToken",
                functionName,
                param.get(indexToken).toString()));
      }
    } else {
      token = res.getTokenInContext();
      if (token == null) {
        throw new ParserException(
            I18N.getText("macro.function.general.noImpersonated", functionName));
      }
    }
    return token;
  }

  /**
   * Checks that the number of objects in the list <code>parameters</code> is within given bounds
   * (inclusive). Throws a <code>ParserException</code> if the check fails.
   *
   * @param functionName this is used in the exception message
   * @param parameters a list of parameters
   * @param min the minimum amount of parameters (inclusive)
   * @param max the maximum amount of parameters (inclusive)
   * @throws ParserException if there were more or less parameters than allowed
   */
  private void checkNumberOfParameters(
      String functionName, List<Object> parameters, int min, int max) throws ParserException {
    int numberOfParameters = parameters.size();
    if (numberOfParameters < min) {
      throw new ParserException(
          I18N.getText(
              "macro.function.general.notEnoughParam", functionName, min, numberOfParameters));
    } else if (numberOfParameters > max) {
      throw new ParserException(
          I18N.getText(
              "macro.function.general.tooManyParam", functionName, max, numberOfParameters));
    }
  }
}
