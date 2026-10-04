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
 * <https://www.gnu.org/licenses/> and specifically the Affero license
 * text at <https://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.client.functions;

import static net.rptools.maptool.client.functions.TokenImage.getMD5Key;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.AssetManager;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tika.mime.MediaType;

/**
 * Base64 functions to encode/decode strings.
 *
 * <p>Functions take a string parameter and returns a string.
 *
 * <p>RESTful API's often use Base64 encoding in the payload for file contents and as such may be
 * useful to compliment the RESTful macro functions. GitHub API is one such example.
 */
public class Base64Functions extends AbstractFunction {
  private static final Logger log = LogManager.getLogger(Base64Functions.class);

  private static final Base64Functions instance = new Base64Functions();

  private Base64Functions() {
    super(1, 2, "base64.encode", "base64.encode.asset", "base64.decode");
  }

  public static Base64Functions getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {

    if (functionName.equalsIgnoreCase("base64.encode")) {
      checkParameters(functionName, parameters, 1, 1);
      return base64Encode(functionName, parameters);
    } else if (functionName.equalsIgnoreCase("base64.encode.asset")) {
      checkParameters(functionName, parameters, 1, 2);
      return base64EncodeAsset(functionName, parameters);
    } else if (functionName.equalsIgnoreCase("base64.decode")) {
      checkParameters(functionName, parameters, 1, 1);
      return base64Decode(functionName, parameters);
    } else {
      throw new ParserException(
          I18N.getText("macro.function.general.unknownFunction", functionName));
    }
  }

  /**
   * Encodes passed in string to Base64
   *
   * @param functionName the name of the function
   * @param parameters a list, with the message as the first element
   * @return Base64 encoded string
   */
  private Object base64Encode(String functionName, List<Object> parameters) {
    byte[] message = parameters.get(0).toString().getBytes(StandardCharsets.UTF_8);

    return Base64.getEncoder().encodeToString(message);
  }

  /**
   * Encodes the passed in asset (via the asset reference) to Base64, with the option to include the
   * data URI prefix (data scheme name, MIME type, and base64 encoding for http usage, default =
   * {@code false}.
   *
   * @param functionName the name of the function
   * @param parameters a list, with the asset ref as the first element and (optional) data URI
   *     prefix as the second element
   * @return Base64 encoded string with or without the data URI prefix
   * @throws ParserException the parser exception
   */
  private Object base64EncodeAsset(String functionName, List<Object> parameters)
      throws ParserException {
    MD5Key key = getMD5Key(parameters.getFirst().toString(), functionName);
    boolean includeDataURIPrefix =
        parameters.size() > 1 ? FunctionUtil.getBooleanValue(parameters.get(1)) : false;

    Asset asset = AssetManager.getAsset(key);
    if (asset == null) {
      // asset not available
      return "";
    }

    Asset.Type assetType = asset.getType();
    String assetSubType = asset.getExtension();
    if (assetType == Asset.Type.IMAGE) {
      byte[] rawByteData;
      rawByteData = asset.getData();
      String base64Data = Base64.getEncoder().encodeToString(rawByteData);
      if (includeDataURIPrefix && !assetSubType.isBlank()) {
        MediaType mediaType = MediaType.image(assetSubType);
        return "data:" + mediaType.toString() + ";base64," + base64Data;
      }
      return base64Data;
    } else {
      // asset type not currently handled
      throw new ParserException(
          I18N.getText(
              "macro.function.base64.encodeAssetTypeUnhandled",
              functionName,
              key.toString(),
              assetType.toString()));
    }
  }

  /**
   * Decodes a passed in string from Base64
   *
   * @param functionName the name of the function
   * @param parameters a list of parameters with string to decode as first element.
   * @return String decoded from a Base64 encoded string
   */
  private Object base64Decode(String functionName, List<Object> parameters) {
    byte[] decoded = Base64.getDecoder().decode(parameters.get(0).toString());

    return new String(decoded, StandardCharsets.UTF_8);
  }

  /**
   * @param functionName the name of the function
   * @param parameters passed into the function call
   * @param min number of parameters required
   * @param max number of parameters required
   * @throws ParserException the parser exception
   */
  private void checkParameters(String functionName, List<Object> parameters, int min, int max)
      throws ParserException {

    if (min == max) {
      if (parameters.size() != max)
        throw new ParserException(
            I18N.getText(
                "macro.function.general.wrongNumParam", functionName, max, parameters.size()));

    } else {
      if (parameters.size() < min)
        throw new ParserException(
            I18N.getText(
                "macro.function.general.notEnoughParam", functionName, min, parameters.size()));

      if (parameters.size() > max)
        throw new ParserException(
            I18N.getText(
                "macro.function.general.tooManyParam", functionName, max, parameters.size()));
    }
  }
}
