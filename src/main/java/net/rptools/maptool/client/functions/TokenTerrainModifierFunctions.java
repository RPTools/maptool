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
import net.rptools.maptool.model.Token;
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
   * @param parser
   * @param functionName
   * @param param
   * @return BigDecimal terrain modifier value
   * @throws ParserException
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
   * @param val the value to set the terrain modifier to.
   * @throws ParserException
   */
  public void setTerrainModifier(Token token, BigDecimal val) throws ParserException {
    if (!MapTool.getParser().isMacroTrusted()) {
      throw new ParserException(
          I18N.getText("macro.function.general.noPerm", "setTerrainModifier"));
    }
    token.setTerrainModifier(val.doubleValue());
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
    BigDecimal val;
    Token token;

    switch (args.size()) {
      case 2:
        token = FindTokenFunctions.findToken(args.get(1).toString(), null);
        if (token == null) {
          throw new ParserException(
              I18N.getText(
                  "macro.function.general.unknownToken",
                  "setTerrainModifier",
                  args.get(1).toString()));
        }
        break;
      case 1:
        MapToolVariableResolver res = (MapToolVariableResolver) parser.getVariableResolver();
        token = res.getTokenInContext();
        if (token == null) {
          throw new ParserException(
              I18N.getText("macro.function.general.noImpersonated", "setTerrainModifier"));
        }
        break;
      case 0:
        throw new ParserException(
            I18N.getText(
                "macro.function.general.notEnoughParam", "setTerrainModifier", 1, args.size()));
      default:
        throw new ParserException(
            I18N.getText(
                "macro.function.general.tooManyParam", "setTerrainModifier", 2, args.size()));
    }

    if (args.get(0) instanceof BigDecimal) {
      val = (BigDecimal) args.get(0);
    } else {
      throw new ParserException(
          I18N.getText(
              "macro.function.general.argumentTypeN",
              "setTerrainModifier",
              1,
              args.get(0).toString()));
    }

    setTerrainModifier(token, val);

    MapTool.getFrame().getCurrentZoneRenderer().getZone().putToken(token);
    MapTool.serverCommand()
        .putToken(MapTool.getFrame().getCurrentZoneRenderer().getZone().getId(), token);

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
    Object val;
    Token token;

    switch (args.size()) {
      case 1:
        token = FindTokenFunctions.findToken(args.get(0).toString(), null);
        if (token == null) {
          throw new ParserException(
              I18N.getText(
                  "macro.function.general.unknownToken",
                  "getTerrainModifier",
                  args.get(0).toString()));
        }
        break;
      case 0:
        MapToolVariableResolver res = (MapToolVariableResolver) parser.getVariableResolver();
        token = res.getTokenInContext();
        if (token == null) {
          throw new ParserException(
              I18N.getText("macro.function.general.noImpersonated", "getTerrainModifier"));
        }
        break;
      default:
        throw new ParserException(
            I18N.getText(
                "macro.function.general.tooManyParam", "getTerrainModifier", 1, args.size()));
    }

    val = getTerrainModifier(token);

    MapTool.getFrame().getCurrentZoneRenderer().getZone().putToken(token);
    MapTool.serverCommand()
        .putToken(MapTool.getFrame().getCurrentZoneRenderer().getZone().getId(), token);

    return val;
  }
}
