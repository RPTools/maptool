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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;

public class TokenImage extends AbstractFunction {

  /** Singleton instance. */
  private static final TokenImage instance = new TokenImage();

  private static final Pattern assetRE = Pattern.compile("asset://([^-]+)");

  enum imageType {
    TOKEN_IMAGE(0),
    TOKEN_PORTRAIT(1),
    TOKEN_HANDOUT(2);

    int value;

    imageType(int v) {
      value = v;
    }

    final int getValue() {
      return value;
    }
  };

  public static final String SET_IMAGE = "setImage";
  public static final String SET_PORTRAIT = "setTokenPortrait";
  public static final String SET_HANDOUT = "setTokenHandout";

  private TokenImage() {
    super(
        0,
        3,
        "getTokenImage",
        "getTokenPortrait",
        "getTokenHandout",
        "setTokenImage",
        "setTokenPortrait",
        "setTokenHandout",
        "getImage",
        "setTokenOpacity",
        "getTokenOpacity");
  }

  /**
   * Gets the TokenImage instance.
   *
   * @return the instance.
   */
  public static TokenImage getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> args)
      throws ParserException {
    Token token;
    Zone zone;
    MapToolVariableResolver resolver = (MapToolVariableResolver) parser.getVariableResolver();

    if (functionName.equals("setTokenOpacity")) {
      if (!MapTool.getParser().isMacroPathTrusted())
        throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));

      FunctionUtil.checkNumberParam(functionName, args, 1, 3);
      String opacityValue = args.get(0).toString();
      token = FunctionUtil.getTokenFromParam(resolver, functionName, args, 1, 2);
      zone = token.getZoneRenderer().getZone();

      float newOpacity = token.setTokenOpacity(Float.parseFloat(opacityValue));
      MapTool.serverCommand().putToken(zone.getId(), token);
      return newOpacity;
    }

    if (functionName.equals("getTokenOpacity")) {
      if (!MapTool.getParser().isMacroPathTrusted())
        throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));

      FunctionUtil.checkNumberParam(functionName, args, 0, 2);
      token = FunctionUtil.getTokenFromParam(resolver, functionName, args, 0, 1);

      return token.getTokenOpacity();
    }

    if (functionName.equals("setTokenImage")) {
      FunctionUtil.checkNumberParam(functionName, args, 1, 3);

      String assetName = args.get(0).toString();
      token = FunctionUtil.getTokenFromParam(resolver, functionName, args, 1, 2);
      zone = token.getZoneRenderer().getZone();

      setImage(token, assetName);
      return "";
    }

    if (functionName.equals("setTokenPortrait")) {
      FunctionUtil.checkNumberParam(functionName, args, 1, 3);

      String assetName = args.get(0).toString();
      token = FunctionUtil.getTokenFromParam(resolver, functionName, args, 1, 2);
      zone = token.getZoneRenderer().getZone();

      setPortrait(token, assetName);
      return "";
    }

    if (functionName.equals("setTokenHandout")) {
      FunctionUtil.checkNumberParam(functionName, args, 1, 3);

      String assetName = args.get(0).toString();
      token = FunctionUtil.getTokenFromParam(resolver, functionName, args, 1, 2);
      zone = token.getZoneRenderer().getZone();

      setHandout(token, assetName);
      return "";
    }

    /** getImage, getTokenImage, getTokenPortrait, or getTokenHandout */
    int indexSize = -1; // by default, no size added to asset id
    if (functionName.equals("getImage")) {
      FunctionUtil.checkNumberParam(functionName, args, 1, 2);

      token = findImageToken(args.get(0).toString(), "getImage");

      // Lee: people want a blank instead of an error
      if (token == null) return "";

      if (args.size() > 1) {
        indexSize = 1;
      }
    } else { // getTokenImage, getTokenPortrait, or getTokenHandout

      FunctionUtil.checkNumberParam(functionName, args, 0, 3);

      if (args.size() > 0) {
        indexSize = 0;
      }

      token = FunctionUtil.getTokenFromParam(resolver, functionName, args, 1, 2);
    }

    StringBuilder assetId = new StringBuilder("asset://");
    if (functionName.equals("getTokenImage")) {
      if (token.getImageAssetId() == null) {
        return "";
      }
      assetId.append(token.getImageAssetId().toString());
    } else if (functionName.equals("getTokenPortrait")) {
      if (token.getPortraitImage() == null) {
        return "";
      }
      assetId.append(token.getPortraitImage().toString());
    } else if (functionName.equals("getImage")) {
      if (token.getImageAssetId() == null) {
        return "";
      }
      assetId.append(token.getImageAssetId().toString());
    } else { // getTokenHandout, or different capitalization
      if (token.getCharsheetImage() == null) {
        return "";
      }
      assetId.append(token.getCharsheetImage().toString());
    }

    if (indexSize >= 0
        && !"".equals(args.get(indexSize).toString())) { // if size parameter entered and not ""
      if (args.get(indexSize) instanceof BigDecimal) {
        assetId.append("-");
        BigDecimal size = (BigDecimal) args.get(indexSize);
        // Constrain it slightly, so its greater than 1
        int i = Math.max(size.intValue(), 1);
        assetId.append(i);
      } else {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.argumentTypeInvalid",
                functionName,
                indexSize + 1,
                args.get(indexSize).toString()));
      }
    }

    return assetId.toString();
  }

  private String typeOf(Object ob) {
    // TODO Auto-generated method stub
    return null;
  }

  private static void assignImage(Token token, String assetName, imageType type, String func)
      throws ParserException {
    Matcher m = assetRE.matcher(assetName);

    String assetId;
    if (m.matches()) {
      assetId = m.group(1);
    } else if (assetName.toLowerCase().startsWith("image:")) {
      assetId = findImageToken(assetName, func).getImageAssetId().toString();
    } else {
      throw new ParserException(
          I18N.getText("macro.function.general.argumentTypeInvalid", func, 1, assetName));
    }
    switch (type) {
      case TOKEN_IMAGE:
        token.setImageAsset(null, new MD5Key(assetId));
        break;
      case TOKEN_PORTRAIT:
        token.setPortraitImage(new MD5Key(assetId));
        break;
      case TOKEN_HANDOUT:
        token.setCharsheetImage(new MD5Key(assetId));
        break;
      default:
        throw new IllegalArgumentException("unknown image type " + type);
    }
    MapTool.serverCommand().putToken(token.getZoneRenderer().getZone().getId(), token);
  }

  public static void setImage(Token token, String assetName) throws ParserException {
    assignImage(token, assetName, imageType.TOKEN_IMAGE, SET_IMAGE);
  }

  public static void setPortrait(Token token, String assetName) throws ParserException {
    assignImage(token, assetName, imageType.TOKEN_PORTRAIT, SET_PORTRAIT);
  }

  public static void setHandout(Token token, String assetName) throws ParserException {
    assignImage(token, assetName, imageType.TOKEN_HANDOUT, SET_HANDOUT);
  }

  public static Token findImageToken(final String name, String functionName)
      throws ParserException {
    Token imageToken = null;
    if (name != null && name.length() > 0) {
      List<ZoneRenderer> zrenderers = MapTool.getFrame().getZoneRenderers();
      for (ZoneRenderer zr : zrenderers) {
        List<Token> tokenList =
            zr.getZone()
                .getTokensFiltered(
                    new Zone.Filter() {
                      public boolean matchToken(Token t) {
                        return t.getName().equalsIgnoreCase(name);
                      }
                    });
        for (Token token : tokenList) {
          // If we are not the GM and the token is not visible to players then we don't
          // let them get functions from it.
          if (!MapTool.getPlayer().isGM() && !token.isVisible()) {
            // Lee: as requested, handling this as "" instead of an error
            // throw new ParserException(I18N.getText("macro.function.general.unknownToken",
            // functionName, name));
            return null;
          }
          if (imageToken != null) {
            // Lee: returning first found instead.
            // throw new ParserException("Duplicate " + name + " tokens");
            return imageToken;
          }
          imageToken = token;
        }
      }
      return imageToken;
    }

    // Lee: for the final "" return
    return null;
    // throw new ParserException(I18N.getText("macro.function.general.unknownToken", functionName,
    // name));
  }
}
