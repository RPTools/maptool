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
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Token;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;

public class TokenVisibleFunction extends AbstractFunction {

  /** The singleton instance. */
  private static final TokenVisibleFunction instance = new TokenVisibleFunction();

  private TokenVisibleFunction() {
    super(
        0,
        2,
        "setVisible",
        "getVisible",
        "setOwnerOnlyVisible",
        "getOwnerOnlyVisible",
        "setAlwaysVisible",
        "getAlwaysVisible");
  }

  /**
   * Gets the instance of Visible.
   *
   * @return the instance.
   */
  public static TokenVisibleFunction getInstance() {
    return instance;
  }

  public boolean getBooleanVisible(Token token) throws ParserException {
    if (!MapTool.getParser().isMacroTrusted()) {
      throw new ParserException(I18N.getText("macro.function.general.noPerm", "getVisible"));
    }
    boolean ownerOnly = token.isVisibleOnlyToOwner() && !AppUtil.playerOwns(token);
    if (ownerOnly) {
      return false;
    }
    return token.isVisible();
  }

  /**
   * Gets if the token is visible.
   *
   * @param token The token to check.
   * @return if the token is visible.
   * @throws ParserException if the player does not have permissions to check.
   */
  public Object getVisible(Token token) throws ParserException {
    return getBooleanVisible(token) ? BigDecimal.ONE : BigDecimal.ZERO;
  }

  /**
   * Sets if the token is visible or not.
   *
   * @param token the token to set.
   * @param val the value to set the visible flag to.
   * @throws ParserException
   */
  public void setVisible(Token token, Object val) throws ParserException {
    if (!MapTool.getParser().isMacroTrusted()) {
      throw new ParserException(I18N.getText("macro.function.general.noPerm", "setVisible"));
    }
    boolean set;
    if (val instanceof Integer) {
      set = ((Integer) val).intValue() != 0;
    } else if (val instanceof Boolean) {
      set = ((Boolean) val).booleanValue();
    } else {
      try {
        set = Integer.parseInt(val.toString()) != 0;
      } catch (NumberFormatException e) {
        set = Boolean.parseBoolean(val.toString());
      }
    }
    token.setVisible(set);
    MapTool.serverCommand()
        .putToken(MapTool.getFrame().getCurrentZoneRenderer().getZone().getId(), token);
  }

  public void setOwnerOnlyVisible(Token token, Object val) throws ParserException {
    if (!MapTool.getParser().isMacroTrusted()) {
      throw new ParserException(
          I18N.getText("macro.function.general.noPerm", "setOwnerOnlyVisible"));
    }
    boolean set;
    if (val instanceof Integer) {
      set = ((Integer) val).intValue() != 0;
    } else if (val instanceof Boolean) {
      set = ((Boolean) val).booleanValue();
    } else {
      try {
        set = Integer.parseInt(val.toString()) != 0;
      } catch (NumberFormatException e) {
        set = Boolean.parseBoolean(val.toString());
      }
    }
    token.setVisibleOnlyToOwner(set);
    MapTool.serverCommand()
        .putToken(MapTool.getFrame().getCurrentZoneRenderer().getZone().getId(), token);
  }

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> param)
      throws ParserException {
    if (functionName.equals("getVisible")) {
      return getVisible(parser, param);
    } else if (functionName.equals("setOwnerOnlyVisible")) {
      return setOwnerOnlyVisible(parser, param);
    } else if (functionName.equals("getOwnerOnlyVisible")) {
      return getOwnerOnlyVisible(parser, param);
    } else if (functionName.equals("setVisible")) {
      return setVisible(parser, param);
    } else if (functionName.equals("setAlwaysVisible")) {
      return setAlwaysVisible(parser, param);
    } else if (functionName.equals("getAlwaysVisible")) {
      return getAlwaysVisible(parser, param);
    } else {
      throw new ParserException(I18N.getText("macro.function.general.unknownFunction"));
    }
  }

  /**
   * Gets if the token is visible
   *
   * @param parser The parser that called the object.
   * @param args The arguments.
   * @return if the token is visible or not.
   * @throws ParserException if an error occurs.
   */
  private Object getVisible(Parser parser, List<Object> args) throws ParserException {
    Token token;

    if (args.size() == 1) {
      token = FindTokenFunctions.findToken(args.get(0).toString(), null);
      if (token == null) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.unknownToken", "getVisible", args.get(0).toString()));
      }
    } else if (args.size() == 0) {
      MapToolVariableResolver res = (MapToolVariableResolver) parser.getVariableResolver();
      token = res.getTokenInContext();
      if (token == null) {
        throw new ParserException(
            I18N.getText("macro.function.general.noImpersonated", "getVisible"));
      }
    } else {
      throw new ParserException(
          I18N.getText("macro.function.general.tooManyParam", "getVisible", 1, args.size()));
    }
    return getVisible(token);
  }

  /**
   * Sets if the token is visible
   *
   * @param parser The parser that called the object.
   * @param args The arguments.
   * @return the value visible is set to.
   * @throws ParserException if an error occurs.
   */
  private Object setVisible(Parser parser, List<Object> args) throws ParserException {
    Object val;
    Token token;

    if (args.size() == 2) {
      token = FindTokenFunctions.findToken(args.get(1).toString(), null);
      if (token == null) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.unknownToken", "setVisible", args.get(1).toString()));
      }
    } else if (args.size() == 1) {
      MapToolVariableResolver res = (MapToolVariableResolver) parser.getVariableResolver();
      token = res.getTokenInContext();
      if (token == null) {
        throw new ParserException(
            I18N.getText("macro.function.general.noImpersonated", "setVisible"));
      }
    } else if (args.size() == 0) {
      throw new ParserException(
          I18N.getText("macro.function.general.notEnoughParam", "setVisible", 1, args.size()));
    } else {
      throw new ParserException(
          I18N.getText("macro.function.general.tooManyParam", "setVisible", 2, args.size()));
    }
    val = args.get(0);
    setVisible(token, val); // Already calls serverCommand().putToken()
    // MapTool.serverCommand().putToken(MapTool.getFrame().getCurrentZoneRenderer().getZone().getId(), token);
    MapTool.getFrame().getCurrentZoneRenderer().getZone().putToken(token);

    return val;
  }

  private Object setOwnerOnlyVisible(Parser parser, List<Object> args) throws ParserException {
    Object val;
    Token token;

    if (args.size() == 2) {
      token = FindTokenFunctions.findToken(args.get(1).toString(), null);
      if (token == null) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.unknownToken",
                "setOwnerOnlyVisible",
                args.get(1).toString()));
      }
    } else if (args.size() == 1) {
      MapToolVariableResolver res = (MapToolVariableResolver) parser.getVariableResolver();
      token = res.getTokenInContext();
      if (token == null) {
        throw new ParserException(
            I18N.getText("macro.function.general.noImpersonated", "setOwnerOnlyVisible"));
      }
    } else if (args.size() == 0) {
      throw new ParserException(
          I18N.getText(
              "macro.function.general.notEnoughParam", "setOwnerOnlyVisible", 1, args.size()));
    } else {
      throw new ParserException(
          I18N.getText(
              "macro.function.general.tooManyParam", "setOwnerOnlyVisible", 2, args.size()));
    }
    val = args.get(0);
    setOwnerOnlyVisible(token, val); // Already calls serverCommand().putToken()
    MapTool.getFrame().getCurrentZoneRenderer().getZone().putToken(token);
    return val;
  }

  private Object getOwnerOnlyVisible(Parser parser, List<Object> args) throws ParserException {
    Token token;

    if (args.size() == 1) {
      token = FindTokenFunctions.findToken(args.get(0).toString(), null);
      if (token == null) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.unknownToken",
                "getOwnerOnlyVisible",
                args.get(0).toString()));
      }
    } else if (args.size() == 0) {
      MapToolVariableResolver res = (MapToolVariableResolver) parser.getVariableResolver();
      token = res.getTokenInContext();
      if (token == null) {
        throw new ParserException(
            I18N.getText("macro.function.general.noImpersonated", "getOwnerOnlyVisible"));
      }
    } else {
      throw new ParserException(
          I18N.getText(
              "macro.function.general.tooManyParam", "getOwnerOnlyVisible", 1, args.size()));
    }
    return getOwnerOnlyVisible(token);
  }

  public Object getOwnerOnlyVisible(Token token) throws ParserException {
    if (!MapTool.getParser().isMacroTrusted()) {
      throw new ParserException(
          I18N.getText("macro.function.general.noPerm", "getOwnerOnlyVisible"));
    }
    return token.isVisibleOnlyToOwner() ? BigDecimal.ONE : BigDecimal.ZERO;
  }

  /**
   * Sets if the token is always visible or not.
   *
   * @param token the token to set.
   * @param val the value to set the visible flag to.
   * @throws ParserException
   */
  public void setAlwaysVisible(Token token, Object val) throws ParserException {
    if (!MapTool.getParser().isMacroTrusted()) {
      throw new ParserException(I18N.getText("macro.function.general.noPerm", "setAlwaysVisible"));
    }
    boolean set;
    if (val instanceof Integer) {
      set = ((Integer) val).intValue() != 0;
    } else if (val instanceof Boolean) {
      set = ((Boolean) val).booleanValue();
    } else {
      try {
        set = Integer.parseInt(val.toString()) != 0;
      } catch (NumberFormatException e) {
        set = Boolean.parseBoolean(val.toString());
      }
    }
    token.setIsAlwaysVisible(set);
    MapTool.serverCommand()
        .putToken(MapTool.getFrame().getCurrentZoneRenderer().getZone().getId(), token);
  }

  /**
   * Sets if the token is always visible
   *
   * @param parser The parser that called the object.
   * @param args The arguments.
   * @return the value visible is set to.
   * @throws ParserException if an error occurs.
   */
  private Object setAlwaysVisible(Parser parser, List<Object> args) throws ParserException {
    Object val;
    Token token;

    if (args.size() == 2) {
      token = FindTokenFunctions.findToken(args.get(1).toString(), null);
      if (token == null) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.unknownToken", "setAlwaysVisible", args.get(1).toString()));
      }
    } else if (args.size() == 1) {
      MapToolVariableResolver res = (MapToolVariableResolver) parser.getVariableResolver();
      token = res.getTokenInContext();
      if (token == null) {
        throw new ParserException(
            I18N.getText("macro.function.general.noImpersonated", "setAlwaysVisible"));
      }
    } else if (args.size() == 0) {
      throw new ParserException(
          I18N.getText(
              "macro.function.general.notEnoughParam", "setAlwaysVisible", 1, args.size()));
    } else {
      throw new ParserException(
          I18N.getText("macro.function.general.tooManyParam", "setAlwaysVisible", 2, args.size()));
    }
    val = args.get(0);
    setAlwaysVisible(token, val); // Already calls serverCommand().putToken()
    // MapTool.serverCommand().putToken(MapTool.getFrame().getCurrentZoneRenderer().getZone().getId(), token);
    MapTool.getFrame().getCurrentZoneRenderer().getZone().putToken(token);

    return val;
  }

  public boolean getBooleanAlwaysVisible(Token token) throws ParserException {
    if (!MapTool.getParser().isMacroTrusted()) {
      throw new ParserException(I18N.getText("macro.function.general.noPerm", "getAlwaysVisible"));
    }
    boolean ownerOnly = token.isVisibleOnlyToOwner() && !AppUtil.playerOwns(token);
    if (ownerOnly) {
      return false;
    }
    return token.isAlwaysVisible();
  }

  /**
   * Gets if the token is visible.
   *
   * @param token The token to check.
   * @return if the token is visible.
   * @throws ParserException if the player does not have permissions to check.
   */
  public Object getAlwaysVisible(Token token) throws ParserException {
    return getBooleanAlwaysVisible(token) ? BigDecimal.ONE : BigDecimal.ZERO;
  }

  /**
   * Gets if the token is visible
   *
   * @param parser The parser that called the object.
   * @param args The arguments.
   * @return if the token is visible or not.
   * @throws ParserException if an error occurs.
   */
  private Object getAlwaysVisible(Parser parser, List<Object> args) throws ParserException {
    Token token;

    if (args.size() == 1) {
      token = FindTokenFunctions.findToken(args.get(0).toString(), null);
      if (token == null) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.unknownToken", "getAlwaysVisible", args.get(0).toString()));
      }
    } else if (args.size() == 0) {
      MapToolVariableResolver res = (MapToolVariableResolver) parser.getVariableResolver();
      token = res.getTokenInContext();
      if (token == null) {
        throw new ParserException(
            I18N.getText("macro.function.general.noImpersonated", "getAlwaysVisible"));
      }
    } else {
      throw new ParserException(
          I18N.getText("macro.function.general.tooManyParam", "getAlwaysVisible", 1, args.size()));
    }
    return getAlwaysVisible(token);
  }
}
