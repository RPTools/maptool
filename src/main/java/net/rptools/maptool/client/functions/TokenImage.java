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

import com.google.gson.JsonObject;
import com.jidesoft.utils.Base64;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import javax.imageio.ImageIO;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.AssetManager;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.util.AssetResolver;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.maptool.util.ImageManager;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;

public class TokenImage extends AbstractFunction {

  /** Singleton instance. */
  private static final TokenImage instance = new TokenImage();

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
  public static final String FILE_HEADER_WEBP = "RIFF";
  public static final String FILE_HEADER_JPG = "ÿØÿà";
  public static final String FILE_HEADER_PNG = "‰PNG";

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
        "getTokenOpacity",
        "createAsset");
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
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> args)
      throws ParserException {
    Token token;

    if (functionName.equalsIgnoreCase("setTokenOpacity")) {
      if (!MapTool.getParser().isMacroTrusted())
        throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));

      FunctionUtil.checkNumberParam(functionName, args, 1, 3);
      String strOpacity = args.get(0).toString();
      FunctionUtil.paramAsFloat(functionName, args, 0, true);
      token = FunctionUtil.getTokenFromParam(resolver, functionName, args, 1, 2);

      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setTokenOpacity, strOpacity);
      return token.getTokenOpacity();
    }

    if (functionName.equalsIgnoreCase("getTokenOpacity")) {
      if (!MapTool.getParser().isMacroTrusted())
        throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));

      FunctionUtil.checkNumberParam(functionName, args, 0, 2);
      token = FunctionUtil.getTokenFromParam(resolver, functionName, args, 0, 1);

      return token.getTokenOpacity();
    }

    if (functionName.equalsIgnoreCase("setTokenImage")) {
      FunctionUtil.checkNumberParam(functionName, args, 1, 3);

      String assetName = args.get(0).toString();
      token = FunctionUtil.getTokenFromParam(resolver, functionName, args, 1, 2);

      setImage(token, assetName);
      return "";
    }

    if (functionName.equalsIgnoreCase("setTokenPortrait")) {
      FunctionUtil.checkNumberParam(functionName, args, 1, 3);

      String assetName = args.get(0).toString();
      token = FunctionUtil.getTokenFromParam(resolver, functionName, args, 1, 2);

      setPortrait(token, assetName);
      return "";
    }

    if (functionName.equalsIgnoreCase("setTokenHandout")) {
      FunctionUtil.checkNumberParam(functionName, args, 1, 3);

      String assetName = args.get(0).toString();
      token = FunctionUtil.getTokenFromParam(resolver, functionName, args, 1, 2);

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
        JsonObject properties = new JsonObject();
        properties.addProperty("type", asset.getType().toString().toLowerCase());
        properties.addProperty("subtype", asset.getExtension());
        properties.addProperty("id", asset.getMD5Key().toString());
        properties.addProperty("name", asset.getName());

        Image img =
            ImageManager.getImageAndWait(
                asset.getMD5Key()); // wait until loaded, so width/height are correct
        String status = "loaded";
        if (img == ImageManager.BROKEN_IMAGE) {
          status = "broken";
        } else if (img == ImageManager.TRANSFERING_IMAGE) {
          status = "transferring";
        }
        properties.addProperty("status", status);
        properties.addProperty("width", img.getWidth(null));
        properties.addProperty("height", img.getHeight(null));
        return properties;
      }
    }

    StringBuilder assetId = new StringBuilder("asset://");
    if (functionName.equalsIgnoreCase("createAsset")) {
      FunctionUtil.checkNumberParam(functionName, args, 2, 2);
      String imageName = args.get(0).toString();
      String imageString = args.get(1).toString();
      if (imageName.isEmpty() || imageString.isEmpty()) {
        throw new ParserException(
            I18N.getText("macro.function.general.paramCannotBeEmpty", functionName));
      } else if (imageString.length() > 8) {
        Asset asset;
        if (imageString.toLowerCase().startsWith("https://")
            && (imageString.toLowerCase().endsWith(".jpg")
                || imageString.toLowerCase().endsWith(".png")
                || imageString.toLowerCase().endsWith(".webp"))) {
          try {
            URI uri = new URI(imageString);
            URL url = uri.toURL();
            BufferedImage imageRAW = ImageIO.read(url);
            asset = Asset.createImageAsset(imageName, imageRAW);
          } catch (URISyntaxException | MalformedURLException | IllegalArgumentException e) {
            throw new ParserException(
                I18N.getText("macro.function.input.illegalArgumentType", imageString));
          } catch (IOException e1) {
            throw new ParserException(I18N.getText("macro.function.html5.invalidURI", imageString));
          }
        } else {
          byte[] imageBytes = Base64.decode(imageString);
          String imageCheck;
          try {
            imageCheck = new String(imageBytes, 0, 4);
          } catch (Exception e) {
            throw new ParserException(I18N.getText("dragdrop.unsupportedType", functionName));
          }
          if (imageCheck.equals(FILE_HEADER_WEBP)
              || imageCheck.equals(FILE_HEADER_JPG)
              || imageCheck.equals(FILE_HEADER_PNG)) {
            asset = Asset.createImageAsset(imageName, imageBytes);
          } else {
            throw new ParserException(I18N.getText("dragdrop.unsupportedType", functionName));
          }
        }
        AssetManager.putAsset(asset);
        assetId.append(asset.getMD5Key().toString());
        return assetId;
      } else {
        throw new ParserException(
            I18N.getText("macro.function.general.wrongParamType", functionName));
      }
    }

    /* getImage, getTokenImage, getTokenPortrait, or getTokenHandout */
    int indexSize = -1; // by default, no size added to asset id
    if (functionName.equalsIgnoreCase("getImage")) {
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

    if (functionName.equalsIgnoreCase("getTokenImage")) {
      if (token.getImageAssetId() == null) {
        return "";
      }
      assetId.append(token.getImageAssetId().toString());
    } else if (functionName.equalsIgnoreCase("getTokenPortrait")) {
      if (token.getPortraitImage() == null) {
        return "";
      }
      assetId.append(token.getPortraitImage().toString());
    } else if (functionName.equalsIgnoreCase("getImage")) {
      if (token.getImageAssetId() == null) {
        return "";
      }
      assetId.append(token.getImageAssetId().toString());
    } else if ("getTokenHandout"
        .equalsIgnoreCase(functionName)) { // getTokenHandout, or different capitalization
      if (token.getCharsheetImage() == null) {
        return "";
      }
      assetId.append(token.getCharsheetImage().toString());
    } else {
      throw new ParserException(
          I18N.getText("macro.function.general.unknownFunction", functionName));
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
    String assetId = null;
    if (assetName.toLowerCase().startsWith("image:")) {
      Token imageToken = findImageToken(assetName, functionName);
      if (imageToken == null) {
        throw new ParserException(
            I18N.getText("macro.function.general.unknownToken", functionName, assetName));
      }
      assetId = imageToken.getImageAssetId().toString();
    } else {
      var assetKey = new AssetResolver().getAssetKey(assetName);
      if (assetKey.isPresent()) {
        assetId = assetKey.get().toString();
      }
    }
    if (assetId == null) {
      throw new ParserException(
          I18N.getText("macro.function.general.argumentTypeInvalid", functionName, 1, assetName));
    } else {
      return new MD5Key(assetId);
    }
  }

  private static void setImage(Token token, String assetName) throws ParserException {
    MD5Key md5key = getMD5Key(assetName, SET_IMAGE);
    MapTool.serverCommand()
        .updateTokenProperty(token, Token.Update.setImageAsset, (String) null, md5key.toString());
  }

  private static void setPortrait(Token token, String assetName) throws ParserException {
    var md5key = "".equals(assetName) ? "" : getMD5Key(assetName, SET_PORTRAIT).toString();
    MapTool.serverCommand().updateTokenProperty(token, Token.Update.setPortraitImage, md5key);
  }

  private static void setHandout(Token token, String assetName) throws ParserException {
    var md5key = "".equals(assetName) ? "" : getMD5Key(assetName, SET_HANDOUT).toString();
    MapTool.serverCommand().updateTokenProperty(token, Token.Update.setCharsheetImage, md5key);
  }

  private static Token findImageToken(final String name, String functionName) {
    Token imageToken = null;
    if (name != null && name.length() > 0) {
      List<ZoneRenderer> zrenderers = MapTool.getFrame().getZoneRenderers();
      for (ZoneRenderer zr : zrenderers) {
        List<Token> tokenList =
            zr.getZone().getTokensFiltered(t -> t.getName().equalsIgnoreCase(name));
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
