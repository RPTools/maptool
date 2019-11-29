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

public class TokenTerrainModifierFunctions extends AbstractFunction {

  /** The singleton instance. */
  private static final TokenTerrainModifierFunctions instance = new TokenTerrainModifierFunctions();

  private TokenTerrainModifierFunctions() {
    super(0, 2, "setTerrainModifier", "getTerrainModifier");
  }

  /**
   * Gets the instance of Terrain Modifier.
   *
   * @return the instance.
   */
  public static TokenTerrainModifierFunctions getInstance() {
    return instance;
  }

  /**
   * @param parser the MapTool parser.
   * @param functionName the name of the function.
   * @param param the list of parameters.
   * @return BigDecimal terrain modifier value.
   * @throws ParserException if unknown function name or incorrect function arguments.
   */
  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> param)
      throws ParserException {
    if (functionName.equals("getTerrainModifier")) {
      return getTerrainModifier(parser, param);
    } else if (functionName.equals("setTerrainModifier")) {
      return setTerrainModifier(parser, param);
    } else {
      throw new ParserException(I18N.getText("macro.function.general.unknownFunction"));
    }
  }

  /**
   * Gets the Terrain Modifier.
   *
   * @param token The token to check.
   * @return the terrain modifier value.
   * @throws ParserException if the player does not have permissions to check.
   */
  public double getTerrainModifier(Token token) throws ParserException {
    if (!MapTool.getParser().isMacroTrusted()) {
      throw new ParserException(
          I18N.getText("macro.function.general.noPerm", "getTerrainModifier"));
    }
    return token.getTerrainModifier();
  }

  /**
   * Sets Terrain Modifier.
   *
   * @param token the token to set.
   * @param val the double value to set the terrain modifier to.
   * @throws ParserException if no permission
   */
  public void setTerrainModifier(Token token, double val) throws ParserException {
    if (!MapTool.getParser().isMacroTrusted()) {
      throw new ParserException(
          I18N.getText("macro.function.general.noPerm", "setTerrainModifier"));
    }
    MapTool.serverCommand().updateTokenProperty(token, Token.Update.setTerrainModifier, val);
  }

  /**
   * Sets the Terrain Modifier
   *
   * @param parser The parser that called the object.
   * @param args The arguments.
   * @return the value the terrain modifier will be set to
   * @throws ParserException if an error occurs.
   */
  private Object setTerrainModifier(Parser parser, List<Object> args) throws ParserException {
    FunctionUtil.checkNumberParam("setTerrainModifier", args, 1, 3);

    Double val = FunctionUtil.paramAsDouble("setTerrainModifier", args, 0, false);
    Token token = FunctionUtil.getTokenFromParam(parser, "setTerrainModifier", args, 1, 2);

    setTerrainModifier(token, val);
    return val;
  }

  /**
   * Gets the Terrain Modifier
   *
   * @param parser The parser that called the object.
   * @param args The arguments.
   * @return the value of the terrain modifier
   * @throws ParserException if an error occurs.
   */
  private Object getTerrainModifier(Parser parser, List<Object> args) throws ParserException {
    FunctionUtil.checkNumberParam("getTerrainModifier", args, 0, 2);
    Token token = FunctionUtil.getTokenFromParam(parser, "getTerrainModifier", args, 0, 1);
    return getTerrainModifier(token);
  }
}
