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

import java.awt.Color;
import java.math.BigDecimal;
import java.util.List;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolUtil;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.util.StringUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;

public class TokenHaloFunction extends AbstractFunction {
  // TODO: This is a copy of the array in the {@link TokenPopupMenu} (which is apparently temporary)
  private static final TokenHaloFunction instance = new TokenHaloFunction();

  private TokenHaloFunction() {
    super(0, 3, "getHalo", "setHalo");
  }

  /**
   * Gets the singleton Halo instance.
   *
   * @return the Halo instance.
   */
  public static TokenHaloFunction getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> args)
      throws ParserException {

    if (functionName.equals("getHalo")) {
      return getHalo(parser, args);
    } else {
      return setHalo(parser, args);
    }
  }

  /**
   * Gets the halo for the token.
   *
   * @param token the token to get the halo for.
   * @return the halo.
   */
  public Object getHalo(Token token) {
    if (token.getHaloColor() != null) {
      return "#" + Integer.toHexString(token.getHaloColor().getRGB()).substring(2);
    } else {
      return "None";
    }
  }

  /**
   * Sets the halo color of the token.
   *
   * @param token the token to set halo of.
   * @param value the value to set.
   * @throws ParserException if there is an error determining color.
   */
  public void setHalo(Token token, Object value) throws ParserException {
    if (value instanceof Color) {
      token.setHaloColor((Color) value);
    } else if (value instanceof BigDecimal) {
      token.setHaloColor(new Color(((BigDecimal) value).intValue()));
    } else {
      String col = value.toString();
      if (StringUtil.isEmpty(col)
          || col.equalsIgnoreCase("none")
          || col.equalsIgnoreCase("default")) {
        token.setHaloColor(null);
      } else {
        String hex = col;
        Color color = MapToolUtil.getColor(hex);
        token.setHaloColor(color);
      }
    }
    // TODO: This works for now but could result in a lot of resending of data
    Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
    zone.putToken(token);
    MapTool.serverCommand().putToken(zone.getId(), token);
  }

  /**
   * Gets the halo of the token.
   *
   * @param parser The parser that called the object.
   * @param args The arguments.
   * @return the halo color.
   * @throws ParserException if an error occurs.
   */
  private Object getHalo(Parser parser, List<Object> args) throws ParserException {
    Token token;

    if (args.size() == 1) {
      if (!MapTool.getParser().isMacroTrusted()) {
        throw new ParserException(I18N.getText("macro.function.general.noPermOther", "getHalo"));
      }
      token = FindTokenFunctions.findToken(args.get(0).toString(), null);
      if (token == null) {
        throw new ParserException(
            I18N.getText("macro.function.general.unknownToken", "getHalo", args.get(0).toString()));
      }
    } else if (args.size() == 0) {
      MapToolVariableResolver res = (MapToolVariableResolver) parser.getVariableResolver();
      token = res.getTokenInContext();
      if (token == null) {
        throw new ParserException(I18N.getText("macro.function.general.noImpersonated", "getHalo"));
      }
    } else {
      throw new ParserException(
          I18N.getText("macro.function.general.tooManyParam", "getHalo", 1, args.size()));
    }
    return getHalo(token);
  }

  /**
   * Sets the halo of the token.
   *
   * @param parser The parser that called the object.
   * @param args The arguments.
   * @return the halo color.
   * @throws ParserException if an error occurs.
   */
  private Object setHalo(Parser parser, List<Object> args) throws ParserException {

    Token token;
    Object value = args.get(0);

    switch (args.size()) {
      case 0:
        throw new ParserException(
            I18N.getText("macro.function.general.notEnoughParam", "setHalo", 1, args.size()));
      default:
        throw new ParserException(
            I18N.getText("macro.function.general.tooManyParam", "setHalo", 2, args.size()));
      case 1:
        MapToolVariableResolver res = (MapToolVariableResolver) parser.getVariableResolver();
        token = res.getTokenInContext();
        if (token == null) {
          throw new ParserException(
              I18N.getText("macro.function.general.noImpersonated", "setHalo"));
        }
        break;
      case 2:
        if (!MapTool.getParser().isMacroTrusted()) {
          throw new ParserException(I18N.getText("macro.function.general.noPermOther", "setHalo"));
        }
        token = FindTokenFunctions.findToken(args.get(1).toString(), null);
        if (token == null) {
          throw new ParserException(
              I18N.getText(
                  "macro.function.general.unknownToken", "setHalo", args.get(1).toString()));
        }
    }
    setHalo(token, value);
    return value;
  }
}
