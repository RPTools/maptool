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
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.AssetManager;
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
        "getAssetProperties",
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

    if (functionName.equals("setTokenOpacity")) {
      if (!MapTool.getParser().isMacroTrusted())
        throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));

      FunctionUtil.checkNumberParam(functionName, args, 1, 3);
      String strOpacity = args.get(0).toString();
      FunctionUtil.paramAsFloat(functionName, args, 0, true);
      token = FunctionUtil.getTokenFromParam(parser, functionName, args, 1, 2);

      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setTokenOpacity, strOpacity);
      return token.getTokenOpacity();
    }

    if (functionName.equals("getTokenOpacity")) {
      if (!MapTool.getParser().isMacroTrusted())
        throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));

      FunctionUtil.checkNumberParam(functionName, args, 0, 2);
      token = FunctionUtil.getTokenFromParam(parser, functionName, args, 0, 1);

      return token.getTokenOpacity();
    }

    if (functionName.equals("setTokenImage")) {
      FunctionUtil.checkNumberParam(functionName, args, 1, 3);

      String assetName = args.get(0).toString();
      token = FunctionUtil.getTokenFromParam(parser, functionName, args, 1, 2);

      setImage(token, assetName);
      return "";
    }

    if (functionName.equals("setTokenPortrait")) {
      FunctionUtil.checkNumberParam(functionName, args, 1, 3);

      String assetName = args.get(0).toString();
      token = FunctionUtil.getTokenFromParam(parser, functionName, args, 1, 2);

      setPortrait(token, assetName);
      return "";
    }

    if (functionName.equals("setTokenHandout")) {
      FunctionUtil.checkNumberParam(functionName, args, 1, 3);

      String assetName = args.get(0).toString();
      token = FunctionUtil.getTokenFromParam(parser, functionName, args, 1, 2);

      setHandout(token, assetName);
      return "";
    }

    if (functionName.equalsIgnoreCase("getAssetProperties")) {
      FunctionUtil.checkNumberParam(functionName, args, 1, 1);
      MD5Key key = getMD5Key(args.get(0).toString(), functionName);
      Asset asset = AssetManager.getAsset(key);
      if (asset == null) {
        return "";
      } else {
        return asset.getProperties();
      }
    }

    /* getImage, getTokenImage, getTokenPortrait, or getTokenHandout */
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

      token = FunctionUtil.getTokenFromParam(parser, functionName, args, 1, 2);
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

  /**
   * Get the MD5Key corresponding to an asset.
   *
   * @param assetName either an assetId or the name of an image token.
   * @param functionName the name of the function, to display the exception message.
   * @return the MD5Key associated with the asset.
   * @throws ParserException if assetName not found or assetName doesn't
   */
  public static MD5Key getMD5Key(String assetName, String functionName) throws ParserException {
    Matcher m = assetRE.matcher(assetName);

    String assetId;
    if (m.matches()) {
      assetId = m.group(1);
    } else if (assetName.toLowerCase().startsWith("image:")) {
      Token imageToken = findImageToken(assetName, functionName);
      if (imageToken == null) {
        throw new ParserException(
            I18N.getText("macro.function.general.unknownToken", functionName, assetName));
      }
      assetId = imageToken.getImageAssetId().toString();
    } else {
      throw new ParserException(
          I18N.getText("macro.function.general.argumentTypeInvalid", functionName, 1, assetName));
    }
    return new MD5Key(assetId);
  }

  private static void setImage(Token token, String assetName) throws ParserException {
    MD5Key md5key = getMD5Key(assetName, SET_IMAGE);
    MapTool.serverCommand().updateTokenProperty(token, Token.Update.setImageAsset, null, md5key);
  }

  private static void setPortrait(Token token, String assetName) throws ParserException {
    MD5Key md5key = getMD5Key(assetName, SET_PORTRAIT);
    MapTool.serverCommand().updateTokenProperty(token, Token.Update.setPortraitImage, md5key);
  }

  private static void setHandout(Token token, String assetName) throws ParserException {
    MD5Key md5key = getMD5Key(assetName, SET_HANDOUT);
    MapTool.serverCommand().updateTokenProperty(token, Token.Update.setCharsheetImage, md5key);
  }

  private static Token findImageToken(final String name, String functionName)
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
