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

import com.google.gson.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolUtil;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.functions.FindTokenFunctions;
import net.rptools.maptool.client.functions.StringFunctions;
import net.rptools.maptool.client.functions.json.JSONMacroFunctions;
import net.rptools.maptool.client.ui.theme.ThemeSupport;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.*;
import net.rptools.maptool.model.drawing.DrawableColorPaint;
import net.rptools.maptool.model.drawing.DrawablePaint;
import net.rptools.maptool.model.drawing.DrawableTexturePaint;
import net.rptools.parser.Parser;
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
  private static final MathContext MATH_CONTEXT = new MathContext(16, RoundingMode.HALF_EVEN);

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
  private static final String KEY_EXPERIMENTAL = "macro.function.general.experimentalWarning";
  private static final List<String> EXPERIMENTAL_WARNINGS = new ArrayList<>();

  public static void experimentalWarning(
      Parser parser, VariableResolver resolver, String functionName) {
    if (!EXPERIMENTAL_WARNINGS.contains(functionName)) {
      EXPERIMENTAL_WARNINGS.add(functionName);
      String messageText = I18N.getText(KEY_EXPERIMENTAL, functionName);
      String white =
          String.format("#%06X", UIManager.getColor("Panel.background").getRGB() & 0x00FFFFFF);
      String textColour =
          String.format("#%06X", UIManager.getColor("Panel.foreground").getRGB() & 0x00FFFFFF);
      String red = ThemeSupport.getThemeColorHexString(ThemeSupport.ThemeColor.RED);
      String imageText;
      try {
        imageText =
            String.format(
                "<img vspace=2 hspace=4 src=\"%s\" height=\"40\" width=\"40\"/>",
                FunctionUtil.class
                    .getResource("/net/rptools/maptool/client/image/warning.svg")
                    .toURI()
                    .toURL());
      } catch (MalformedURLException | URISyntaxException ignored) {
        imageText = String.format("<font size=7 color=\"%s\">&#9888;</font>", red);
      }
      String html =
          String.format(
              """
                                            <table border=0 width=100%% cellspacing=3 cellpadding=0 style="background:%s;"><tr><td>
                                            <table style="background: %s; color: %s"><tr valign=middle><td height="44px" width="48px">%s</td>
                                            <td>%s</td></tr></table>
                                            </td></tr></table>
                                            """,
              red, white, textColour, imageText, messageText);
      MapTool.addGlobalMessage(html, List.of("self"));
    }
  }

  /**
   * Collects results into a string list or JSON array.
   *
   * @param delim The delimiter to use for the string list, or "json" to create a JSON array.
   * @param results The results to combine into a list.
   * @return The string list of all results, or a JSON array of the same.
   */
  public static Object delimitedResult(String delim, List<String> results) {
    if ("json".equalsIgnoreCase(delim)) {
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
   * the token as an optional argument.
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
   * Gets the ZoneRender from the specified index or returns the current ZoneRender. This method
   * will check the list size before trying to retrieve the token so it is safe to use for functions
   * that have the map as an optional argument.
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
   * Return the BigDecimal value of a parameter. Throws a <code>ParserException</code> if the
   * parameter can't be converted to BigDecimal.
   *
   * @param functionName this is used in the exception message
   * @param parameters the list of parameters
   * @param index the index of the parameter to return as BigDecimal
   * @param allowString should text that can be converted to BigDecimal be allowed?
   * @return the BigDecimal value of the parameter
   * @throws ParserException when unable to convert to BigDecimal, or if disallowed text
   */
  public static BigDecimal paramAsBigDecimal(
      String functionName, List<Object> parameters, int index, boolean allowString)
      throws ParserException {
    return new BigDecimal(paramAsNumber(functionName, parameters, index, allowString).toString());
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
   * @throws ParserException when unable to convert to BigDecimal, or if disallowed text
   */
  public static Boolean paramAsBoolean(
      String functionName, List<Object> parameters, int index, boolean allowString)
      throws ParserException {
    Object parameter = parameters.get(index);
    if (parameter instanceof Boolean b) {
      // already a boolean, return it.
      return b;
    } else if (allowString && parameter instanceof String string) {
      // if it is a string of "true/false" return it as boolean
      if (string.trim().equalsIgnoreCase("false")) {
        return false;
      } else if (string.trim().equalsIgnoreCase("true")) {
        return true;
      }
    }
    try {
      if (!allowString && parameter instanceof String) {
        throw new NumberFormatException("String");
      }
      BigDecimal val = new BigDecimal(parameter.toString().trim());
      return !val.equals(BigDecimal.ZERO); // true if any value except zero
    } catch (NumberFormatException ne) {
      throw new ParserException(
          I18N.getText(KEY_NOT_NUMBER, functionName, index + 1, parameter.toString()));
    }
  }

  /**
   * Helper function for the various getParam as numeric type. Converts numbers of various types and
   * formats, including scientific, hex, octal, binary and their string representations. Allows for
   * suffixes that should not appear in macro code; i.e. 1f, 2d, 3L. Only thing it doesn't cope with
   * is returning -0, which hopefully nobody is counting on.
   *
   * @param functionName for error messages
   * @param parameters containing values
   * @param index of value to return
   * @param allowString if string representation of number is allowed
   * @return number value as a Number
   * @throws ParserException if value is not a valid number.
   */
  private static Number paramAsNumber(
      String functionName, List<Object> parameters, int index, boolean allowString)
      throws ParserException {
    Object parameter = parameters.get(index);

    if (!allowString && parameter instanceof String string) {
      throw new ParserException(I18N.getText(KEY_NOT_NUMBER, functionName, index + 1, string));
    }
    String nString;
    if (parameter instanceof Number number) {
      if (Double.isNaN(number.doubleValue()) || Float.isNaN(number.floatValue())) {
        throw new ParserException(
            I18N.getText(KEY_NOT_NUMBER, functionName, index + 1, Double.NaN));
      } else if (number.doubleValue() == 0) {
        return 0;
      }
      // this seems stupid, but it is the only way to get consistent results between floats, doubles
      // and ints out of BigDecimal
      nString = number.toString();
    } else {
      nString = parameter.toString().trim().toLowerCase();
      if (nString.equals("nan")) {
        throw new ParserException(
            I18N.getText(KEY_NOT_NUMBER, functionName, index + 1, Double.NaN));
      }
    }
    if (nString.endsWith("d") || nString.endsWith("f") || nString.endsWith("l")) {
      nString = nString.substring(0, nString.length() - 1);
    }
    if (nString.startsWith("#") || nString.startsWith("0x") || nString.startsWith("0b")) {
      try {
        if (nString.startsWith("0b")) {
          return Integer.parseUnsignedInt(nString.substring(2), 2);
        } else {
          return Integer.decode(nString);
        }
      } catch (NumberFormatException nfe) {
        throw new ParserException(
            I18N.getText(KEY_NOT_NUMBER, functionName, index + 1, parameter.toString()));
      }
    }
    try {
      return new BigDecimal(nString, MATH_CONTEXT);
    } catch (NumberFormatException nfe) {
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
   * @throws ParserException if the parameter can't be converted to Integer, or if disallowed text
   */
  public static int paramAsInteger(
      String functionName, List<Object> parameters, int index, boolean allowString)
      throws ParserException {

    Number number = paramAsNumber(functionName, parameters, index, allowString);
    if (number.intValue() == number.doubleValue()) {
      return number.intValue();
    } else {
      throw new ParserException(
          I18N.getText(KEY_NOT_INT, functionName, index + 1, parameters.get(index).toString()));
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
   * @throws ParserException when cannot be converted to Double, or disallowed text
   */
  public static double paramAsDouble(
      String functionName, List<Object> parameters, int index, boolean allowString)
      throws ParserException {
    return paramAsNumber(functionName, parameters, index, allowString).doubleValue();
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
   * @throws ParserException when unable to convert to Float, or if disallowed text
   */
  public static float paramAsFloat(
      String functionName, List<Object> parameters, int index, boolean allowString)
      throws ParserException {
    return paramAsNumber(functionName, parameters, index, allowString).floatValue();
  }

  /**
   * Return the jsonObject value of a parameter supplied as a JSON Object or StringPropList.<br>
   * Throws a <code>ParserException</code> if the parameter can't be converted to a JSON.
   *
   * @param functionName this is used in the exception message
   * @param parameters the list of parameters
   * @param index the index of the parameter to return as JSON
   * @param delimiter the delimiter if known
   * @return the parameter as a jsonObject
   * @throws ParserException if the parameter can't be converted to jsonObject or jsonArray
   */
  public static JsonObject paramFromStrPropOrJsonAsJsonObject(
      String functionName, List<Object> parameters, int index, String delimiter)
      throws ParserException {

    Object arg = parameters.get(index);
    if (arg instanceof JsonObject jo) {
      return jo;
    }

    if (delimiter.equalsIgnoreCase("json")) {
      return paramAsJsonObject(functionName, parameters, index);
    }
    delimiter = delimiter.isEmpty() ? ";" : delimiter;
    // just in case it is a JSON Object despite the delimiter
    Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
    try {
      JsonElement jsonElement = gson.toJsonTree(arg);
      if (jsonElement.isJsonObject()) {
        return jsonElement.getAsJsonObject();
      }
    } catch (JsonParseException ignored) {
    }
    String strProp = arg.toString();
    String[] kvPairs = strProp.split(delimiter);
    JsonObject jsonObject = new JsonObject();
    for (String pair : kvPairs) {
      String[] kv = pair.split("=");
      if (kv.length != 2) {
        throw new ParserException(
            I18N.getText("macro.function.general.unableToParse", functionName, index));
      }
      String key = kv[0];
      String value = kv[1];
      if ((value.contains("{") && value.contains("}"))
          || (value.contains("[") && value.contains("]"))) {
        try {
          JsonElement je = gson.toJsonTree(value);
          jsonObject.add(key, je);
        } catch (JsonParseException ignored) {
          jsonObject.addProperty(key, value);
        }
      } else {
        jsonObject.addProperty(key, value);
      }
    }
    return jsonObject;
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
   * Return the jsonObject or jsonArray value of a parameter. Throws a <code>ParserException</code>
   * if the parameter can't be converted to a JSON.
   *
   * @param functionName this is used in the exception message
   * @param parameters the list of parameters
   * @param index the index of the parameter to return as JSON
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

  public static void validateKeyNames(
      Object paramObject,
      Set<String> requiredKeys,
      int index,
      String functionName,
      String delimiter)
      throws ParserException {
    JsonObject jsonObject;
    if (paramObject.getClass().isAssignableFrom(JsonObject.class)) {
      jsonObject = (JsonObject) paramObject;
    } else {
      jsonObject =
          JSONMacroFunctions.getInstance()
              .getJsonObjectFunctions()
              .fromStrProp((String) paramObject, delimiter);
    }
    Set<String> keySet = jsonObject.keySet();
    for (String key : requiredKeys) {
      if (!keySet.contains(key)) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.missingKey",
                functionName,
                index,
                key,
                String.join(", ", requiredKeys)));
      }
    }
  }

  public static JsonObject jsonWithLowerCaseKeys(JsonObject jsonObject) {
    JsonObject jObj = new JsonObject();
    Set<String> keys = jsonObject.keySet();
    for (String key : keys) {
      jObj.add(key.toLowerCase(), jsonObject.get(key));
    }
    return jObj;
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
   * Return the jsonObject or jsonArray value of a parameter. If the parameter can't be converted to
   * a JSON, an empty string results in an empty JSON array, otherwise a JSON array containing the
   * argument will be returned.
   *
   * @param functionName this is used in the exception message
   * @param parameters the list of parameters
   * @param index the index of the parameter to return as JSON
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
   * Return the jsonElement value of a parameter. Where the value is not a JSON, returns an array
   * containing the value.
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
      if (!val.toString().isEmpty()) {
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
   * Convert an object into a boolean value. Never returns an error.
   *
   * @param value Convert this object. Must be {@link Boolean}, {@link BigDecimal}, or will have its
   *     string value be converted to one of those types.
   * @return The boolean value of the object
   */
  public static boolean getBooleanValue(Object value) {
    boolean returnValue = false;
    switch (value) {
      case Boolean b -> returnValue = b;
      case Number number -> returnValue = number.doubleValue() != 0;
      case String string -> {
        string = string.trim();
        if (string.equalsIgnoreCase("false") || string.equalsIgnoreCase("true")) {
          returnValue = string.equalsIgnoreCase("true");
        } else {
          try {
            returnValue = !new BigDecimal(string).equals(BigDecimal.ZERO);
          } catch (NumberFormatException e) {
            returnValue = Boolean.parseBoolean(value.toString());
          }
        }
      }
      case null, default -> {}
    }
    return returnValue;
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
   * Parses a string into either a Colour Paint or Texture Paint.
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
