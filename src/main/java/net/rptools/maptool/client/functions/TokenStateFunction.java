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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.ui.token.BooleanTokenOverlay;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Token;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;
import net.sf.json.JSONArray;

public class TokenStateFunction extends AbstractFunction {

  /** The value for setting all states. */
  public static final String ALL_STATES = "ALL";

  /** The singleton instance. */
  private static final TokenStateFunction instance = new TokenStateFunction();

  /**
   * Gets the singleton instance of the state.
   *
   * @return the instance.
   */
  public static TokenStateFunction getInstance() {
    return instance;
  }

  private TokenStateFunction() {
    super(0, 4, "getState", "setState", "setAllStates", "getTokenStates");
  }

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> args)
      throws ParserException {
    MapToolVariableResolver resolver = (MapToolVariableResolver) parser.getVariableResolver();

    if (functionName.equals("setAllStates")) {
      checkNumberOfParameters(functionName, args, 1, 3);
      Boolean val = getBooleanFromValue(args.get(0));
      Token token = getTokenFromParam(resolver, functionName, args, 1, 2);
      MapTool.serverCommand().updateTokenProperty(token, functionName, val);
      return val ? BigDecimal.valueOf(1) : BigDecimal.valueOf(0);
    } else if (functionName.equals("getState")) {
      checkNumberOfParameters(functionName, args, 1, 3);
      String stateName = args.get(0).toString();
      Token token = getTokenFromParam(resolver, functionName, args, 1, 2);
      return getState(token, stateName);
    } else if (functionName.equals("setState")) {
      checkNumberOfParameters(functionName, args, 2, 4);
      String stateName = args.get(0).toString();
      Object value = args.get(1);
      Token token = getTokenFromParam(resolver, functionName, args, 2, 3);
      return setState(token, stateName, value);
    } else if (functionName.equals("getTokenStates")) {
      checkNumberOfParameters(functionName, args, 0, 2);
      return getTokenStates(parser, args);
    } else {
      throw new ParserException(
          I18N.getText("macro.function.general.unknownFunction", functionName));
    }
  }

  /**
   * Gets the state of the specified token.
   *
   * @param token The token.
   * @param stateName the name of the state to get.
   * @return the value of the state.
   * @throws ParserException if the state is unknown.
   */
  public Object getState(Token token, String stateName) throws ParserException {
    return getBooleanTokenState(token, stateName) ? BigDecimal.valueOf(1) : BigDecimal.valueOf(0);
  }

  /**
   * Gets the boolean value of the tokens state.
   *
   * @param token The token to get the state of.
   * @param stateName The name of the state to get.
   * @return the value of the state.
   * @throws ParserException if an error occurs.
   */
  public boolean getBooleanTokenState(Token token, String stateName) throws ParserException {
    if (!MapTool.getCampaign().getTokenStatesMap().containsKey(stateName)) {
      throw new ParserException(
          I18N.getText("macro.function.tokenStateFunctions.unknownState", stateName));
    }
    Object val = token.getState(stateName);
    return (getBooleanFromValue(val));
  }

  /**
   * Sets the state of the specified token.
   *
   * @param token The token to set.
   * @param stateName the name of the state or {@link #ALL_STATES}
   * @param value the value to set it to.
   * @return the value of the state.
   * @throws ParserException if the state is unknown.
   */
  public BigDecimal setState(Token token, String stateName, Object val) throws ParserException {
    boolean set = getBooleanFromValue(val);
    if (stateName.equals(ALL_STATES)) {
      MapTool.serverCommand().updateTokenProperty(token, "setAllStates", set);
    } else {
      if (!MapTool.getCampaign().getTokenStatesMap().containsKey(stateName)) {
        throw new ParserException(
            I18N.getText("macro.function.tokenStateFunctions.unknownState", stateName));
      }
      MapTool.serverCommand().updateTokenProperty(token, "setState", stateName, set);
    }
    return set ? BigDecimal.valueOf(1) : BigDecimal.valueOf(0);
  }

  /**
   * Gets the boolean value of an object
   *
   * @param Object the object to get the value from
   */
  private boolean getBooleanFromValue(Object value) {
    if (value == null) { // If state does not exist then it can't be set ;)
      return false;
    }
    if (value instanceof Integer) {
      return ((Integer) value).intValue() != 0;
    } else if (value instanceof Boolean) {
      return ((Boolean) value).booleanValue();
    } else {
      try {
        return Integer.parseInt(value.toString()) != 0;
      } catch (NumberFormatException e) {
        return Boolean.parseBoolean(value.toString());
      }
    }
  }

  /**
   * Gets a list of the valid token states.
   *
   * @param parser The parser.
   * @param args The arguments.
   * @return A string with the states.
   */
  private String getTokenStates(Parser parser, List<Object> args) {
    String delim = args.size() > 0 ? args.get(0).toString() : ",";
    Set<String> stateNames;

    if (args.size() > 1) {
      String group = (String) args.get(1);
      Map<String, BooleanTokenOverlay> states = MapTool.getCampaign().getTokenStatesMap();
      stateNames = new HashSet<String>();
      for (BooleanTokenOverlay bto : states.values()) {
        if (group.equals(bto.getGroup())) {
          stateNames.add(bto.getName());
        }
      }
    } else {
      stateNames = MapTool.getCampaign().getTokenStatesMap().keySet();
    }

    StringBuilder sb = new StringBuilder();
    if ("json".equals(delim)) {
      return JSONArray.fromObject(stateNames).toString();
    } else {
      for (String s : stateNames) {
        if (sb.length() > 0) {
          sb.append(delim);
        }
        sb.append(s);
      }
      return sb.toString();
    }
  }

  /**
   * Checks that the number of objects in the list <code>parameters</code> is within given bounds
   * (inclusive). Throws a <code>ParserException</code> if the check fails.
   *
   * @param functionName this is used in the exception message
   * @param parameters a list of parameters
   * @param min the minimum amount of parameters (inclusive)
   * @param max the maximum amount of parameters (inclusive)
   * @throws ParserException if there were more or less parameters than allowed
   */
  private void checkNumberOfParameters(
      String functionName, List<Object> parameters, int min, int max) throws ParserException {
    int numberOfParameters = parameters.size();
    if (numberOfParameters < min) {
      throw new ParserException(
          I18N.getText(
              "macro.function.general.notEnoughParam", functionName, min, numberOfParameters));
    } else if (numberOfParameters > max) {
      throw new ParserException(
          I18N.getText(
              "macro.function.general.tooManyParam", functionName, max, numberOfParameters));
    }
  }

  /**
   * Gets the token from the specified index or returns the token in context. This method will check
   * the list size before trying to retrieve the token so it is safe to use for functions that have
   * the token as a optional argument.
   *
   * @param res the variable resolver
   * @param functionName The function name (used for generating exception messages).
   * @param param The parameters for the function.
   * @param indexToken The index to find the token at.
   * @param indexMap The index to find the map name at. If -1, use current map instead.
   * @return the token.
   * @throws ParserException if a token is specified but the macro is not trusted, or the specified
   *     token can not be found, or if no token is specified and no token is impersonated.
   */
  private Token getTokenFromParam(
      MapToolVariableResolver res,
      String functionName,
      List<Object> param,
      int indexToken,
      int indexMap)
      throws ParserException {

    String mapName =
        indexMap >= 0 && param.size() > indexMap ? param.get(indexMap).toString() : null;
    Token token;
    if (param.size() > indexToken) {
      if (!MapTool.getParser().isMacroTrusted()) {
        throw new ParserException(I18N.getText("macro.function.general.noPermOther", functionName));
      }
      token = FindTokenFunctions.findToken(param.get(indexToken).toString(), mapName);
      if (token == null) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.unknownToken",
                functionName,
                param.get(indexToken).toString()));
      }
    } else {
      token = res.getTokenInContext();
      if (token == null) {
        throw new ParserException(
            I18N.getText("macro.function.general.noImpersonated", functionName));
      }
    }
    return token;
  }
}
