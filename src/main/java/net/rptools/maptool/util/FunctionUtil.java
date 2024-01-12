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
package net.rptools.maptool.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.math.BigDecimal;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolUtil;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.functions.FindTokenFunctions;
import net.rptools.maptool.client.functions.StringFunctions;
import net.rptools.maptool.client.functions.json.JSONMacroFunctions;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.InvalidGUIDException;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.drawing.DrawableColorPaint;
import net.rptools.maptool.model.drawing.DrawablePaint;
import net.rptools.maptool.model.drawing.DrawableTexturePaint;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.Function;

/**
 * Provides static methods to help handle macro functions.
 *
 * @author Merudo
 * @since 1.5.5
 */
public class FunctionUtil {
  private static final String KEY_WRONG_NUM_PARAM = "macro.function.general.wrongNumParam";
  private static final String KEY_NOT_ENOUGH_PARAM = "macro.function.general.notEnoughParam";
  private static final String KEY_TOO_MANY_PARAM = "macro.function.general.tooManyParam";

  private static final String KEY_NOT_INT = "macro.function.general.argumentTypeI";
  private static final String KEY_NOT_JSON = "macro.function.general.argumentTypeJ";
  private static final String KEY_NOT_JSON_ARRAY = "macro.function.general.argumentTypeA";
  private static final String KEY_NOT_JSON_OBJECT = "macro.function.general.argumentTypeO";
  private static final String KEY_NOT_NUMBER = "macro.function.general.argumentTypeN";
  private static final String KEY_NOT_STRING = "macro.function.general.argumentTypeS";

  private static final String KEY_NO_PERM = "macro.function.general.noPermOther";
  private static final String KEY_NO_CURRENT_MAP = "macro.function.map.none";
  private static final String KEY_UNKNOWN_MAP = "macro.function.moveTokenMap.unknownMap";
  private static final String KEY_UNKNOWN_TOKEN = "macro.function.general.unknownToken";
  private static final String KEY_UNKNOWN_TOKEN_ON_MAP = "macro.function.general.unknownTokenOnMap";
  private static final String KEY_NO_IMPERSONATED = "macro.function.general.noImpersonated";

  /**
   * Collects results into a string list or JSON array.
   *
   * @param delim The delimiter to use for the string list, or "json" to create a JSON array.
   * @param results The results to combine into a list.
   * @return The string list of all results, or a JSON array of the same.
   */
  public static Object delimitedResult(String delim, List<String> results) {
    if ("json".equals(delim)) {
      JsonArray jarr = new JsonArray();
      results.forEach(m -> jarr.add(new JsonPrimitive(m)));
      return jarr;
    } else {
      return StringFunctions.getInstance().join(results, delim);
    }
  }

  /**
   * Checks if the number of <code>parameters</code> is within given bounds (inclusive). Throws a
   * <code>ParserException</code> if the check fails.
   *
   * @param functionName this is used in the exception message
   * @param parameters the list of parameters
   * @param min the minimum amount of parameters (inclusive)
   * @param max the maximum amount of parameters (inclusive). If -1, skip this check.
   * @throws ParserException if there were more or less parameters than allowed
   */
  public static void checkNumberParam(
      String functionName, List<Object> parameters, int min, int max) throws ParserException {
    int size = parameters.size();

    if (min == max) {
      if (size != max)
        throw new ParserException(I18N.getText(KEY_WRONG_NUM_PARAM, functionName, max, size));
    } else {
      if (size < min)
        throw new ParserException(I18N.getText(KEY_NOT_ENOUGH_PARAM, functionName, min, size));
      if (size > max && max != Function.UNLIMITED_PARAMETERS)
        throw new ParserException(I18N.getText(KEY_TOO_MANY_PARAM, functionName, max, size));
    }
  }

  /**
   * Gets the token from the specified index or returns the token in context. This method will check
   * the list size before trying to retrieve the token so it is safe to use for functions that have
   * the token as a optional argument.
   *
   * @param functionName the function name (used for generating exception messages).
   * @param param the parameters for the function
   * @param indexToken the index to find the token at. If -1, use current token instead.
   * @param indexMap the index to find the map name or ID at. If -1, use current map instead.
   * @return the token.
   * @throws ParserException if a token is specified but the macro is not trusted, or the specified
   *     token can not be found, or if no token is specified and no token is impersonated.
   */
  public static Token getTokenFromParam(
      VariableResolver resolver,
      String functionName,
      List<Object> param,
      int indexToken,
      int indexMap)
      throws ParserException {

    int size = param.size();
    String id = indexToken >= 0 && size > indexToken ? param.get(indexToken).toString() : null;
    String map = indexMap >= 0 && size > indexMap ? param.get(indexMap).toString() : null;
    Token token;
    if (id != null) {
      if (!MapTool.getParser().isMacroTrusted()) {
        throw new ParserException(I18N.getText(KEY_NO_PERM, functionName));
      }
      token = FindTokenFunctions.findToken(id, map);
      if (token == null) {
        if (map == null) {
          throw new ParserException(I18N.getText(KEY_UNKNOWN_TOKEN, functionName, id));
        } else if (MapTool.getFrame().getZoneRenderer(map) == null) {
          throw new ParserException(I18N.getText(KEY_UNKNOWN_MAP, functionName, map));
        } else {
          throw new ParserException(I18N.getText(KEY_UNKNOWN_TOKEN_ON_MAP, functionName, id, map));
        }
      }
    } else {
      token = ((MapToolVariableResolver) resolver).getTokenInContext();
      if (token == null) {
        throw new ParserException(I18N.getText(KEY_NO_IMPERSONATED, functionName));
      }
    }
    return token;
  }

  /**
   * Gets the ZoneRender with the given name, throwing a ParserException if it does not exist.
   *
   * @param functionName the function name (used for generating exception messages).
   * @param map the name or ID of the map
   * @return the ZoneRenderer.
   * @throws ParserException if the map cannot be found
   */
  public static @Nonnull ZoneRenderer getZoneRenderer(String functionName, String map)
      throws ParserException {
    if (!GUID.isNotGUID(map)) {
      try {
        final var zr = MapTool.getFrame().getZoneRenderer(GUID.valueOf(map));
        if (zr != null) {
          return zr;
        }
      } catch (InvalidGUIDException ignored) {
        // Wasn't a GUID after all. Fall back to looking up by name.
      }
    }

    ZoneRenderer zoneRenderer = MapTool.getFrame().getZoneRenderer(map);
    if (zoneRenderer == null) {
      throw new ParserException(I18N.getText(KEY_UNKNOWN_MAP, functionName, map));
    }
    return zoneRenderer;
  }

  /**
   * Gets the ZoneRender from the specified index or returns the current ZoneRender. This method
   * will check the list size before trying to retrieve the token so it is safe to use for functions
   * that have the map as a optional argument.
   *
   * @param functionName the function name (used for generating exception messages).
   * @param param the parameters for the function
   * @param indexMap the index to find the map name or ID at. If -1, use current map instead.
   * @return the ZoneRenderer.
   * @throws ParserException if the map cannot be found
   */
  public static @Nonnull ZoneRenderer getZoneRendererFromParam(
      String functionName, List<Object> param, int indexMap) throws ParserException {

    String map = indexMap >= 0 && param.size() > indexMap ? param.get(indexMap).toString() : null;

    ZoneRenderer zoneRenderer;
    if (map == null) {
      zoneRenderer = MapTool.getFrame().getCurrentZoneRenderer();
      if (zoneRenderer == null) {
        throw new ParserException(I18N.getText("macro.function.map.none", functionName));
      }
    } else {
      zoneRenderer = getZoneRenderer(functionName, map);
    }

    return zoneRenderer;
  }

  /**
   * Return the BigDecimal value of a parameter. Throws a <code>ParserException</code> if the
   * parameter can't be converted to BigDecimal.
   *
   * @param functionName this is used in the exception message
   * @param parameters the list of parameters
   * @param index the index of the parameter to return as BigDecimal
   * @param allowString should text that can be converted to BigDecimal be allowed?
   * @return the BigDecimal value of the parameter
   * @throws ParserException if can't be converted to BigDecimal, or if disallowed text
   */
  public static BigDecimal paramAsBigDecimal(
      String functionName, List<Object> parameters, int index, boolean allowString)
      throws ParserException {
    Object parameter = parameters.get(index);
    if (parameter instanceof BigDecimal) return (BigDecimal) parameter;
    try {
      if (!allowString && parameter instanceof String) throw new NumberFormatException("String");
      return (new BigDecimal(parameter.toString()));
    } catch (NumberFormatException ne) {
      throw new ParserException(
          I18N.getText(KEY_NOT_NUMBER, functionName, index + 1, parameter.toString()));
    }
  }

  /**
   * Return the String value of a parameter.
   *
   * @param functionName this is used in the exception message
   * @param parameters the list of parameters
   * @param index the index of the parameter to return as String
   * @param allowNumber should numbers be allowed?
   * @return the parameter as a string
   * @throws ParserException if the parameter is disallowed number
   */
  public static String paramAsString(
      String functionName, List<Object> parameters, int index, boolean allowNumber)
      throws ParserException {
    Object parameter = parameters.get(index);
    if (!allowNumber && !(parameter instanceof String)) {
      throw new ParserException(I18N.getText(KEY_NOT_STRING, functionName, parameter.toString()));
    }
    return parameter.toString();
  }

  /**
   * Return the Boolean value of a parameter. Throws a <code>ParserException</code> if the parameter
   * isn't a BigDecimal.
   *
   * @param functionName this is used in the exception message
   * @param parameters the list of parameters
   * @param index the index of the parameter to return as Boolean
   * @param allowString should text parameters be allowed
   * @return the parameter as a Boolean
   * @throws ParserException if can't be converted to BigDecimal, or if disallowed text
   */
  public static Boolean paramAsBoolean(
      String functionName, List<Object> parameters, int index, boolean allowString)
      throws ParserException {
    Object parameter = parameters.get(index);
    try {
      if (!allowString && parameter instanceof String) throw new NumberFormatException("String");
      BigDecimal val = new BigDecimal(parameter.toString());
      return !val.equals(BigDecimal.ZERO); // true if any value except zero
    } catch (NumberFormatException ne) {
      throw new ParserException(
          I18N.getText(KEY_NOT_NUMBER, functionName, index + 1, parameter.toString()));
    }
  }

  /**
   * Return the Integer value of a parameter. Throws a <code>ParserException</code> if the parameter
   * can't be converted to integer
   *
   * @param functionName this is used in the exception message
   * @param parameters the list of parameters
   * @param index the index of the parameter to return as integer
   * @param allowString should text be allowed as parameter
   * @return the parameter as an integer
   * @throws ParserException if the parameter can't be converted to BigDecimal, or if disallowed
   *     text
   */
  public static Integer paramAsInteger(
      String functionName, List<Object> parameters, int index, boolean allowString)
      throws ParserException {
    Object parameter = parameters.get(index);
    try {
      if (!allowString && parameter instanceof String) throw new NumberFormatException("String");
      return Integer.valueOf(parameter.toString());
    } catch (NumberFormatException ne) {
      throw new ParserException(
          I18N.getText(KEY_NOT_INT, functionName, index + 1, parameter.toString()));
    }
  }

  /**
   * Return the Double value of a parameter. Throws a <code>ParserException</code> if the parameter
   * can't be converted to Double.
   *
   * @param functionName this is used in the exception message
   * @param parameters the list of parameters
   * @param index the index of the parameter to return as Double
   * @param allowString should text be allowed
   * @return the parameter as a Double
   * @throws ParserException if can't be converted to Double, or disallowed text
   */
  public static Double paramAsDouble(
      String functionName, List<Object> parameters, int index, boolean allowString)
      throws ParserException {
    Object parameter = parameters.get(index);
    try {
      if (!allowString && parameter instanceof String) throw new NumberFormatException("String");
      return (Double.valueOf(parameter.toString()));
    } catch (NumberFormatException ne) {
      throw new ParserException(
          I18N.getText(KEY_NOT_NUMBER, functionName, index + 1, parameter.toString()));
    }
  }

  /**
   * Return the Float value of a parameter. Throws a <code>ParserException</code> if the parameter
   * can't be converted to Float.
   *
   * @param functionName this is used in the exception message
   * @param parameters the list of parameters
   * @param index the index of the parameter to return as Float
   * @param allowString should text be allowed
   * @return the parameter as a Float
   * @throws ParserException if can't be converted to Float, or if disallowed text
   */
  public static Float paramAsFloat(
      String functionName, List<Object> parameters, int index, boolean allowString)
      throws ParserException {
    Object parameter = parameters.get(index);
    try {
      if (!allowString && parameter instanceof String) throw new NumberFormatException("String");
      return (Float.valueOf(parameter.toString()));
    } catch (NumberFormatException ne) {
      throw new ParserException(
          I18N.getText(KEY_NOT_NUMBER, functionName, index + 1, parameter.toString()));
    }
  }

  /**
   * Return the jsonObject or jsonArray value of a parameter. Throws a <code>ParserException</code>
   * if the parameter can't be converted to a json.
   *
   * @param functionName this is used in the exception message
   * @param parameters the list of parameters
   * @param index the index of the parameter to return as Json
   * @return the parameter as a jsonObject or jsonArray
   * @throws ParserException if the parameter can't be converted to jsonObject or jsonArray
   */
  public static JsonElement paramAsJson(String functionName, List<Object> parameters, int index)
      throws ParserException {
    JsonElement jsonElement = JSONMacroFunctions.getInstance().asJsonElement(parameters.get(index));
    if (!jsonElement.isJsonObject() && !jsonElement.isJsonArray()) {
      throw new ParserException(I18N.getText(KEY_NOT_JSON, functionName, index + 1));
    }
    return jsonElement;
  }

  /**
   * Return the jsonObject value of a parameter. Throws a <code>ParserException</code> if the
   * parameter can't be converted to a jsonObject.
   *
   * @param functionName this is used in the exception message
   * @param parameters the list of parameters
   * @param index the index of the parameter to return as jsonObject
   * @return the parameter as a jsonObject
   * @throws ParserException if the parameter can't be converted to jsonObject
   */
  public static JsonObject paramAsJsonObject(
      String functionName, List<Object> parameters, int index) throws ParserException {
    JsonElement jsonElement = paramAsJson(functionName, parameters, index);
    if (!jsonElement.isJsonObject()) {
      throw new ParserException(I18N.getText(KEY_NOT_JSON_OBJECT, functionName, index + 1));
    }

    return jsonElement.getAsJsonObject();
  }

  /**
   * Return the jsonArray value of a parameter. Throws a <code>ParserException</code> if the
   * parameter can't be converted to a jsonArray.
   *
   * @param functionName this is used in the exception message
   * @param parameters the list of parameters
   * @param index the index of the parameter to return as jsonArray
   * @return the parameter as a jsonArray
   * @throws ParserException if the parameter can't be converted to jsonArray
   */
  public static JsonArray paramAsJsonArray(String functionName, List<Object> parameters, int index)
      throws ParserException {
    JsonElement jsonElement = paramAsJson(functionName, parameters, index);
    if (!jsonElement.isJsonArray()) {
      throw new ParserException(I18N.getText(KEY_NOT_JSON_ARRAY, functionName, index + 1));
    }

    return jsonElement.getAsJsonArray();
  }

  /**
   * Return the jsonElement value of a parameter. Throws a <code>ParserException</code> if the
   * parameter can't be converted to a jsonArray.
   *
   * @param functionName this is used in the exception message
   * @param parameters the list of parameters
   * @param index the index of the parameter to return as jsonArray
   * @return the parameter as a jsonArray
   */
  public static JsonElement paramConvertedToJson(
      String functionName, List<Object> parameters, int index) {
    try {
      return paramAsJson(functionName, parameters, index);
    } catch (ParserException e) {
      JsonArray json = new JsonArray();
      Object val = parameters.get(index);
      if (val.toString().length() > 0) {
        if (val instanceof Number) {
          json.add((Number) val);
        } else {
          json.add(val.toString());
        }
      }

      return json;
    }
  }

  /**
   * Return the jsonObject or jsonArray value of a parameter. if the parameter can't be converted to
   * a json. Then an empty json array will be returned if its an empty string, otherwise a a
   * JsonArray containing the argument will be returned.
   *
   * @param functionName this is used in the exception message
   * @param parameters the list of parameters
   * @param index the index of the parameter to return as Json
   * @return the parameter as a jsonObject or jsonArray
   * @throws ParserException if the parameter can't be converted to jsonObject or jsonArray
   */
  public static JsonArray paramConvertedToJsonArray(
      String functionName, List<Object> parameters, int index) throws ParserException {
    JsonElement json = paramConvertedToJson(functionName, parameters, index);
    if (!json.isJsonArray()) {
      throw new ParserException(I18N.getText(KEY_NOT_JSON_ARRAY, functionName, index + 1));
    } else {
      return json.getAsJsonArray();
    }
  }

  /**
   * Convert an object into a boolean value. Never returns an error.
   *
   * @param value Convert this object. Must be {@link Boolean}, {@link BigDecimal}, or will have its
   *     string value be converted to one of those types.
   * @return The boolean value of the object
   */
  public static boolean getBooleanValue(Object value) {
    boolean set = false;
    if (value instanceof Boolean) {
      set = (Boolean) value;
    } else if (value instanceof Number) {
      set = ((Number) value).doubleValue() != 0;
    } else if (value == null) {
      set = false;
    } else {
      try {
        set = !new BigDecimal(value.toString()).equals(BigDecimal.ZERO);
      } catch (NumberFormatException e) {
        set = Boolean.parseBoolean(value.toString());
      } // endif
    } // endif
    return set;
  }

  /**
   * Get our standard BigDecimal representation (1 or 0) of a boolean value.
   *
   * @param b the boolean value
   * @return {@link BigDecimal#ONE} if true, {@link BigDecimal#ZERO} if false
   */
  public static BigDecimal getDecimalForBoolean(boolean b) {
    return b ? BigDecimal.ONE : BigDecimal.ZERO;
  }

  /**
   * Parses a string into either a Color Paint or Texture Paint.
   *
   * @param paint String containing the paint description.
   * @return Pen DrawableTexturePaint or DrawableColorPaint.
   */
  public static DrawablePaint getPaintFromString(String paint) {
    if (paint.toLowerCase().startsWith("asset://")) {
      String id = paint.substring("asset://".length());
      return new DrawableTexturePaint(new MD5Key(id));
    } else if (paint.length() == 32) {
      return new DrawableTexturePaint(new MD5Key(paint));
    } else {
      return new DrawableColorPaint(MapToolUtil.getColor(paint));
    }
  }

  /**
   * Parses a string as an asset URL.
   *
   * @param assetUrlOrId String containing the asset ID (ID), asset URL (asset://ID), or addon
   *     URL(lib://PATH).
   * @return The MD5 key present in {@code assetUrlOrId}, or null.
   */
  public static @Nullable MD5Key getAssetKeyFromString(String assetUrlOrId) {
    String id = null;
    if (assetUrlOrId.toLowerCase().startsWith("asset://")) {
      id = assetUrlOrId.substring("asset://".length());
    } else if (assetUrlOrId.toLowerCase().startsWith("lib://")) {
      var assetKey = new AssetResolver().getAssetKey(assetUrlOrId);
      if (assetKey.isPresent()) {
        id = assetKey.get().toString();
      }
    } else if (assetUrlOrId.toLowerCase().startsWith("image:")) {
      for (ZoneRenderer z : MapTool.getFrame().getZoneRenderers()) {
        Token t = z.getZone().getTokenByName(assetUrlOrId);
        if (t != null) {
          id = t.getImageAssetId().toString();
        }
      }
    } else if (assetUrlOrId.length() == 32) {
      id = assetUrlOrId;
    }

    if (id == null) {
      return null;
    }

    return new MD5Key(id);
  }

  /**
   * Throw an exception if the macro isn't trusted.
   *
   * @param functionName the name of the function.
   * @throws ParserException if the macro isn't trusted.
   */
  public static void blockUntrustedMacro(String functionName) throws ParserException {
    if (!MapTool.getParser().isMacroTrusted()) {
      throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));
    }
  }
}
