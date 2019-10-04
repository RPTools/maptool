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

import java.math.BigDecimal;
import java.util.List;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.functions.FindTokenFunctions;
import net.rptools.maptool.client.functions.JSONMacroFunctions;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Token;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.Function;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

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
  private static final String KEY_UNKNOWN_MAP = "macro.function.moveTokenMap.unknownMap";
  private static final String KEY_UNKNOWN_TOKEN = "macro.function.general.unknownToken";
  private static final String KEY_UNKNOWN_TOKEN_ON_MAP = "macro.function.general.unknownTokenOnMap";
  private static final String KEY_NO_IMPERSONATED = "macro.function.general.noImpersonated";
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
   * @param res the variable resolver
   * @param functionName the function name (used for generating exception messages).
   * @param param the parameters for the function
   * @param indexToken the index to find the token at. If -1, use current token instead.
   * @param indexMap the index to find the map name at. If -1, use current map instead.
   * @return the token.
   * @throws ParserException if a token is specified but the macro is not trusted, or the specified
   *     token can not be found, or if no token is specified and no token is impersonated.
   */
  public static Token getTokenFromParam(
      MapToolVariableResolver res,
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
      token = res.getTokenInContext();
      if (token == null) {
        throw new ParserException(I18N.getText(KEY_NO_IMPERSONATED, functionName));
      }
    }
    return token;
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
  public static Object paramAsJson(String functionName, List<Object> parameters, int index)
      throws ParserException {
    Object obj = JSONMacroFunctions.asJSON(parameters.get(index));
    if (!(obj instanceof JSONArray) && !(obj instanceof JSONObject)) {
      throw new ParserException(I18N.getText(KEY_NOT_JSON, functionName, index + 1));
    }
    return obj;
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
  public static JSONObject paramAsJsonObject(
      String functionName, List<Object> parameters, int index) throws ParserException {
    Object obj = JSONMacroFunctions.asJSON(parameters.get(index));
    if (!(obj instanceof JSONObject)) {
      throw new ParserException(I18N.getText(KEY_NOT_JSON_OBJECT, functionName, index + 1));
    } else return (JSONObject) obj;
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
  public static JSONArray paramAsJsonArray(String functionName, List<Object> parameters, int index)
      throws ParserException {
    Object obj = JSONMacroFunctions.asJSON(parameters.get(index));
    if (!(obj instanceof JSONArray)) {
      throw new ParserException(I18N.getText(KEY_NOT_JSON_ARRAY, functionName, index + 1));
    } else return (JSONArray) obj;
  }
}
