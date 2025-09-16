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
import net.rptools.lib.StringUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolUtil;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Token;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;

public class TokenHaloFunctions extends AbstractFunction {
  private static final TokenHaloFunctions instance = new TokenHaloFunctions();

  private TokenHaloFunctions() {
    super(
        0,
        3,
        "getHalo",
        "setHalo",
        "getHaloColor",
        "setHaloColor",
        "getHaloShape",
        "setHaloShape",
        "getHaloStyle",
        "setHaloStyle");
  }

  /**
   * Gets the singleton Halo instance.
   *
   * @return the Halo instance.
   */
  public static TokenHaloFunctions getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> args)
      throws ParserException {

    if (functionName.equalsIgnoreCase("getHalo")) {
      return getHaloColor((MapToolVariableResolver) resolver, args);
    } else if (functionName.equalsIgnoreCase("setHalo")) {
      return setHaloColor((MapToolVariableResolver) resolver, args);
    } else if (functionName.equalsIgnoreCase("getHaloColor")) {
      return getHaloColor((MapToolVariableResolver) resolver, args);
    } else if (functionName.equalsIgnoreCase("setHaloColor")) {
      return setHaloColor((MapToolVariableResolver) resolver, args);
    } else if (functionName.equalsIgnoreCase("getHaloShape")) {
      return getHaloShape((MapToolVariableResolver) resolver, args);
    } else if (functionName.equalsIgnoreCase("setHaloShape")) {
      return setHaloShape((MapToolVariableResolver) resolver, args);
    } else if (functionName.equalsIgnoreCase("getHaloStyle")) {
      return getHaloStyle((MapToolVariableResolver) resolver, args);
    } else if (functionName.equalsIgnoreCase("setHaloStyle")) {
      return setHaloStyle((MapToolVariableResolver) resolver, args);
    }
    throw new ParserException(I18N.getText("macro.function.general.unknownFunction", functionName));
  }

  /**
   * Gets the halo color for the token.
   *
   * @param token the token to get the halo color for.
   * @return the halo color.
   */
  public static Object getHaloColor(Token token) {
    var haloColor = token.getHaloColor();
    if (haloColor != null) {
      return "#" + Integer.toHexString(haloColor.getRGB()).substring(2);
    } else {
      return "None";
    }
  }

  /**
   * Sets the halo color of the token.
   *
   * @param token the token to set halo color of.
   * @param value the value to set.
   */
  public static void setHaloColor(Token token, Object value) {
    Color haloColor;
    if (value instanceof Color) {
      haloColor = (Color) value;
    } else if (value instanceof BigDecimal) {
      haloColor = new Color(((BigDecimal) value).intValue());
    } else {
      String col = value.toString();
      if (StringUtil.isEmpty(col)
          || col.equalsIgnoreCase("none")
          || col.equalsIgnoreCase("default")) {
        haloColor = null;
      } else {
        String hex = col;
        Color color = MapToolUtil.getColor(hex);
        haloColor = color;
      }
    }
    var cmd = MapTool.serverCommand();

    if (haloColor != null) {
      cmd.updateTokenProperty(token, Token.Update.setHaloColor, haloColor.getRGB());
    } else {
      cmd.updateTokenProperty(token, Token.Update.setHaloColor);
    }
  }

  /**
   * Gets the halo color of the token.
   *
   * @param args The arguments.
   * @return the halo color.
   * @throws ParserException if an error occurs.
   */
  private Object getHaloColor(MapToolVariableResolver resolver, List<Object> args)
      throws ParserException {
    Token token;

    if (args.size() == 1) {
      if (!MapTool.getParser().isMacroTrusted()) {
        throw new ParserException(
            I18N.getText("macro.function.general.noPermOther", "getHaloColor"));
      }
      token = FindTokenFunctions.findToken(args.get(0).toString(), null);
      if (token == null) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.unknownToken", "getHaloColor", args.get(0).toString()));
      }
    } else if (args.size() == 0) {
      token = resolver.getTokenInContext();
      if (token == null) {
        throw new ParserException(
            I18N.getText("macro.function.general.noImpersonated", "getHaloColor"));
      }
    } else {
      throw new ParserException(
          I18N.getText("macro.function.general.tooManyParam", "getHaloColor", 1, args.size()));
    }
    return getHaloColor(token);
  }

  /**
   * Sets the halo of the token.
   *
   * @param args The arguments.
   * @return the halo color.
   * @throws ParserException if an error occurs.
   */
  private Object setHaloColor(MapToolVariableResolver resolver, List<Object> args)
      throws ParserException {

    Token token;
    Object value = args.get(0);

    switch (args.size()) {
      case 0:
        throw new ParserException(
            I18N.getText("macro.function.general.notEnoughParam", "setHaloColor", 1, args.size()));
      default:
        throw new ParserException(
            I18N.getText("macro.function.general.tooManyParam", "setHaloColor", 2, args.size()));
      case 1:
        token = resolver.getTokenInContext();
        if (token == null) {
          throw new ParserException(
              I18N.getText("macro.function.general.noImpersonated", "setHaloColor"));
        }
        break;
      case 2:
        if (!MapTool.getParser().isMacroTrusted()) {
          throw new ParserException(
              I18N.getText("macro.function.general.noPermOther", "setHaloColor"));
        }
        token = FindTokenFunctions.findToken(args.get(1).toString(), null);
        if (token == null) {
          throw new ParserException(
              I18N.getText(
                  "macro.function.general.unknownToken", "setHaloColor", args.get(1).toString()));
        }
    }
    setHaloColor(token, value);
    return value;
  }

  /**
   * Gets the halo shape for the token.
   *
   * @param token the token to get the halo shape for.
   * @return the halo shape.
   */
  public static Object getHaloShape(Token token) {
    Token.HaloShape haloShape = token.getHaloShape();
    if (haloShape != null) {
      return haloShape;
    } else {
      return Token.HaloShape.GRID;
    }
  }

  /**
   * Sets the halo shape of the token.
   *
   * @param token the token to set halo shape of.
   * @param value the value to set.
   */
  public static void setHaloShape(Token token, Object value) {
    Token.HaloShape haloShape;
    if (value instanceof Token.HaloShape) {
      haloShape = (Token.HaloShape) value;
    } else {
      String shape = value.toString();
      if (StringUtil.isEmpty(shape) || shape.equalsIgnoreCase("default")) {
        haloShape = Token.HaloShape.GRID;
      } else {
        try {
          haloShape =
              Token.HaloShape.valueOf(value.toString().toUpperCase().trim().replace(" ", "_"));
        } catch (IllegalArgumentException iae) {
          haloShape = Token.HaloShape.GRID;
        }
      }
    }
    MapTool.serverCommand().updateTokenProperty(token, Token.Update.setHaloShape, haloShape.name());
  }

  /**
   * Gets the halo shape of the token.
   *
   * @param args The arguments.
   * @return the halo shape.
   * @throws ParserException if an error occurs.
   */
  private Object getHaloShape(MapToolVariableResolver resolver, List<Object> args)
      throws ParserException {
    Token token;

    if (args.size() == 1) {
      if (!MapTool.getParser().isMacroTrusted()) {
        throw new ParserException(
            I18N.getText("macro.function.general.noPermOther", "getHaloShape"));
      }
      token = FindTokenFunctions.findToken(args.get(0).toString(), null);
      if (token == null) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.unknownToken", "getHaloShape", args.get(0).toString()));
      }
    } else if (args.size() == 0) {
      token = resolver.getTokenInContext();
      if (token == null) {
        throw new ParserException(
            I18N.getText("macro.function.general.noImpersonated", "getHaloShape"));
      }
    } else {
      throw new ParserException(
          I18N.getText("macro.function.general.tooManyParam", "getHaloShape", 1, args.size()));
    }
    return getHaloShape(token);
  }

  /**
   * Sets the halo shape of the token.
   *
   * @param args The arguments.
   * @return the halo shape.
   * @throws ParserException if an error occurs.
   */
  private Object setHaloShape(MapToolVariableResolver resolver, List<Object> args)
      throws ParserException {

    Token token;
    Object value = args.get(0);

    switch (args.size()) {
      case 0:
        throw new ParserException(
            I18N.getText("macro.function.general.notEnoughParam", "setHaloShape", 1, args.size()));
      default:
        throw new ParserException(
            I18N.getText("macro.function.general.tooManyParam", "setHaloShape", 2, args.size()));
      case 1:
        token = resolver.getTokenInContext();
        if (token == null) {
          throw new ParserException(
              I18N.getText("macro.function.general.noImpersonated", "setHaloShape"));
        }
        break;
      case 2:
        if (!MapTool.getParser().isMacroTrusted()) {
          throw new ParserException(
              I18N.getText("macro.function.general.noPermOther", "setHaloShape"));
        }
        token = FindTokenFunctions.findToken(args.get(1).toString(), null);
        if (token == null) {
          throw new ParserException(
              I18N.getText(
                  "macro.function.general.unknownToken", "setHaloShape", args.get(1).toString()));
        }
    }
    setHaloShape(token, value);
    return token.getHaloShape().name();
  }

  /**
   * Gets the halo style of the token.
   *
   * @param token the token to get the halo style for.
   * @return the halo style.
   */
  public static Object getHaloStyle(Token token) {
    Token.HaloStyle haloStyle = token.getHaloStyle();
    if (haloStyle != null) {
      return haloStyle;
    } else {
      return Token.HaloStyle.SOLID;
    }
  }

  /**
   * Sets the halo style of the token.
   *
   * @param token the token to set halo style of.
   * @param value the value to set.
   */
  public static void setHaloStyle(Token token, Object value) {
    Token.HaloStyle haloStyle;
    if (value instanceof Token.HaloStyle) {
      haloStyle = (Token.HaloStyle) value;
    } else {
      String style = value.toString();
      if (StringUtil.isEmpty(style) || style.equalsIgnoreCase("default")) {
        haloStyle = Token.HaloStyle.SOLID;
      } else {
        try {
          haloStyle =
              Token.HaloStyle.valueOf(value.toString().toUpperCase().trim().replace(" ", "_"));
        } catch (IllegalArgumentException iae) {
          haloStyle = Token.HaloStyle.SOLID;
        }
      }
    }
    MapTool.serverCommand().updateTokenProperty(token, Token.Update.setHaloStyle, haloStyle.name());
  }

  /**
   * Gets the halo style of the token.
   *
   * @param args The arguments.
   * @return the halo style.
   * @throws ParserException if an error occurs.
   */
  private Object getHaloStyle(MapToolVariableResolver resolver, List<Object> args)
      throws ParserException {
    Token token;

    if (args.size() == 1) {
      if (!MapTool.getParser().isMacroTrusted()) {
        throw new ParserException(
            I18N.getText("macro.function.general.noPermOther", "getHaloStyle"));
      }
      token = FindTokenFunctions.findToken(args.get(0).toString(), null);
      if (token == null) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.unknownToken", "getHaloStyle", args.get(0).toString()));
      }
    } else if (args.size() == 0) {
      token = resolver.getTokenInContext();
      if (token == null) {
        throw new ParserException(
            I18N.getText("macro.function.general.noImpersonated", "getHaloStyle"));
      }
    } else {
      throw new ParserException(
          I18N.getText("macro.function.general.tooManyParam", "getHaloStyle", 1, args.size()));
    }
    return getHaloStyle(token);
  }

  /**
   * Sets the halo style of the token.
   *
   * @param args The arguments.
   * @return the halo style.
   * @throws ParserException if an error occurs.
   */
  private Object setHaloStyle(MapToolVariableResolver resolver, List<Object> args)
      throws ParserException {

    Token token;
    Object value = args.get(0);

    switch (args.size()) {
      case 0:
        throw new ParserException(
            I18N.getText("macro.function.general.notEnoughParam", "setHaloStyle", 1, args.size()));
      default:
        throw new ParserException(
            I18N.getText("macro.function.general.tooManyParam", "setHaloStyle", 2, args.size()));
      case 1:
        token = resolver.getTokenInContext();
        if (token == null) {
          throw new ParserException(
              I18N.getText("macro.function.general.noImpersonated", "setHaloStyle"));
        }
        break;
      case 2:
        if (!MapTool.getParser().isMacroTrusted()) {
          throw new ParserException(
              I18N.getText("macro.function.general.noPermOther", "setHaloStyle"));
        }
        token = FindTokenFunctions.findToken(args.get(1).toString(), null);
        if (token == null) {
          throw new ParserException(
              I18N.getText(
                  "macro.function.general.unknownToken", "setHaloStyle", args.get(1).toString()));
        }
    }
    setHaloStyle(token, value);
    return token.getHaloStyle().name();
  }
}
