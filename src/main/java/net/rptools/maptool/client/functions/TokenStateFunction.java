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

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.token.BooleanTokenOverlay;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;

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
    if (functionName.equals("setAllStates")) {
      FunctionUtil.checkNumberParam(functionName, args, 1, 3);
      Boolean val = getBooleanFromValue(args.get(0));
      Token token = FunctionUtil.getTokenFromParam(parser, functionName, args, 1, 2);
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setAllStates, val);
      return val ? BigDecimal.valueOf(1) : BigDecimal.valueOf(0);
    } else if (functionName.equals("getState")) {
      FunctionUtil.checkNumberParam(functionName, args, 1, 3);
      String stateName = args.get(0).toString();
      Token token = FunctionUtil.getTokenFromParam(parser, functionName, args, 1, 2);
      return getState(token, stateName);
    } else if (functionName.equals("setState")) {
      FunctionUtil.checkNumberParam(functionName, args, 2, 4);
      String stateName = args.get(0).toString();
      Object value = args.get(1);
      Token token = FunctionUtil.getTokenFromParam(parser, functionName, args, 2, 3);
      return setState(token, stateName, value);
    } else if (functionName.equals("getTokenStates")) {
      FunctionUtil.checkNumberParam(functionName, args, 0, 4);
      String delim = args.size() > 0 ? args.get(0).toString() : ",";
      String group = args.size() > 1 ? args.get(1).toString() : "*";
      Token token =
          args.size() > 2 ? FunctionUtil.getTokenFromParam(parser, functionName, args, 2, 3) : null;
      return getTokenStates(delim, group, token);
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
  public static Object getState(Token token, String stateName) throws ParserException {
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
  public static boolean getBooleanTokenState(Token token, String stateName) throws ParserException {
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
   * @param val the value to set it to.
   * @return the value of the state.
   * @throws ParserException if the state is unknown.
   */
  public static BigDecimal setState(Token token, String stateName, Object val)
      throws ParserException {
    boolean set = getBooleanFromValue(val);
    if (stateName.equals(ALL_STATES)) {
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setAllStates, set);
    } else {
      if (!MapTool.getCampaign().getTokenStatesMap().containsKey(stateName)) {
        throw new ParserException(
            I18N.getText("macro.function.tokenStateFunctions.unknownState", stateName));
      }
      MapTool.serverCommand().updateTokenProperty(token, Token.Update.setState, stateName, set);
    }
    return set ? BigDecimal.valueOf(1) : BigDecimal.valueOf(0);
  }

  /**
   * Gets the boolean value of an object
   *
   * @param value the object to get the value from
   */
  private static boolean getBooleanFromValue(Object value) {
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
   * Gets a list of the token states, either from the Campaign or from a token.
   *
   * @param delim The delimiter to use to return the list.
   * @param group The group to get the states of. If "*" returns states of all groups.
   * @param token The token to get the states of. If null, get the Campaign states instead.
   * @return A string with the states.
   */
  private String getTokenStates(String delim, String group, Token token) {
    Set<String> stateNames;

    if ("*".equals(group)) { // get all Campaign states
      stateNames = MapTool.getCampaign().getTokenStatesMap().keySet();
    } else {
      Map<String, BooleanTokenOverlay> states = MapTool.getCampaign().getTokenStatesMap();
      stateNames = new HashSet<String>();
      for (BooleanTokenOverlay bto : states.values()) {
        // return states of the group that matches
        if (group.equals(bto.getGroup())) stateNames.add(bto.getName());
      }
    }

    if (token != null) {
      // only keep states set to true on token
      Set<String> tokenStates = token.getStatePropertyNames(true);
      tokenStates.retainAll(stateNames);
      stateNames = tokenStates;
    }

    StringBuilder sb = new StringBuilder();
    if ("json".equals(delim)) {
      JsonArray jarr = new JsonArray();
      stateNames.forEach(s -> jarr.add(new JsonPrimitive(s)));
      return jarr.toString();
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
}
