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
import net.rptools.maptool.model.Zone;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;

public class TokenGMNameFunction extends AbstractFunction {

  private TokenGMNameFunction() {
    super(0, 2, "getGMName", "setGMName");
  }

  /** Singleton instance of GMName. */
  private static final TokenGMNameFunction instance = new TokenGMNameFunction();

  /**
   * Gets the singleton instance of GMName.
   *
   * @return the instance.
   */
  public static final TokenGMNameFunction getInstance() {
    return instance;
  }

  /**
   * Gets the GMName of the specified token.
   *
   * @param token the token to get theGMName of.
   * @return the GMName.
   * @throws ParserException if the user does not have the permission.
   */
  public String getGMName(Token token) throws ParserException {
    if (!MapTool.getParser().isMacroTrusted()) {
      throw new ParserException(I18N.getText("macro.function.general.noPerm", "getGMName"));
    }
    return token.getGMName() != null ? token.getGMName() : "";
  }

  /**
   * Sets the GMName of the token.
   *
   * @param token the token to set the GMName of.
   * @param naeme The name to set the GMName to.
   * @throws ParserException if the user does not have the permission.
   */
  public void setGMName(Token token, String name) throws ParserException {
    if (!MapTool.getParser().isMacroTrusted()) {
      throw new ParserException(I18N.getText("macro.function.general.noPerm", "setGMName"));
    }
    token.setGMName(name);
  }

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> args)
      throws ParserException {

    if (functionName.equals("getGMName")) {
      return getGMName(parser, args);
    } else {
      return setGMName(parser, args);
    }
  }

  /**
   * Gets the GM name of the token
   *
   * @param parser The parser that called the Object.
   * @param args The arguments passed.
   * @return the name of the token.
   * @throws ParserException when an error occurs.
   */
  private Object getGMName(Parser parser, List<Object> args) throws ParserException {
    Token token;

    if (args.size() == 1) {
      token = FindTokenFunctions.findToken(args.get(0).toString(), null);
      if (token == null) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.unknownToken", "getGMName", args.get(0).toString()));
      }
    } else if (args.size() == 0) {
      MapToolVariableResolver res = (MapToolVariableResolver) parser.getVariableResolver();
      token = res.getTokenInContext();
      if (token == null) {
        throw new ParserException(
            I18N.getText("macro.function.general.noImpersonated", "getGMName"));
      }
    } else {
      throw new ParserException(
          I18N.getText("macro.function.general.tooManyParam", "getGMName", 1, args.size()));
    }
    return getGMName(token);
  }

  /**
   * Sets the GM name of the token.
   *
   * @param parser The parser that called the Object.
   * @param args The arguments passed.
   * @return the new name of the token.
   * @throws ParserException when an error occurs.
   */
  private Object setGMName(Parser parser, List<Object> args) throws ParserException {
    Token token;
    if (args.size() == 2) {
      token = FindTokenFunctions.findToken(args.get(1).toString(), null);
      if (token == null) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.unknownToken", "setGMName", args.get(1).toString()));
      }
    } else if (args.size() == 1) {
      MapToolVariableResolver res = (MapToolVariableResolver) parser.getVariableResolver();
      token = res.getTokenInContext();
      if (token == null) {
        throw new ParserException(
            I18N.getText("macro.function.general.noImpersonated", "setGMName"));
      }
    } else if (args.size() == 0) {
      throw new ParserException(
          I18N.getText("macro.function.general.notEnoughParam", "setGMName", 1, args.size()));
    } else {
      throw new ParserException(
          I18N.getText("macro.function.general.tooManyParam", "setGMName", 2, args.size()));
    }
    token.setGMName(args.get(0).toString());
    Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
    MapTool.serverCommand().putToken(zone.getId(), token);
    zone.putToken(token);

    return args.get(0);
  }
}
