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
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;

/** @author Jay */
public class TokenBarFunction extends AbstractFunction {

  /** Support get and set bar on tokens */
  private TokenBarFunction() {
    super(1, 4, "getBar", "setBar", "isBarVisible", "setBarVisible");
  }

  /** singleton instance of this function */
  private static final TokenBarFunction instance = new TokenBarFunction();

  /** @return singleton instance */
  public static TokenBarFunction getInstance() {
    return instance;
  }

  /**
   * @see net.rptools.parser.function.AbstractFunction#childEvaluate(net.rptools.parser.Parser,
   *     java.lang.String, java.util.List)
   */
  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> parameters)
      throws ParserException {
    String bar = (String) parameters.get(0);
    verifyBar(functionName, bar);

    if (functionName.equals("getBar")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 3);
      Token token = FunctionUtil.getTokenFromParam(parser, functionName, parameters, 1, 2);
      return getValue(token, bar);
    } else if (functionName.equals("setBar")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 2, 4);
      Token token = FunctionUtil.getTokenFromParam(parser, functionName, parameters, 2, 3);
      return setValue(token, bar, parameters.get(1));
    } else if (functionName.equals("isBarVisible")) {
      FunctionUtil.checkNumberParam(functionName, parameters, 1, 3);
      Token token = FunctionUtil.getTokenFromParam(parser, functionName, parameters, 1, 2);
      return isVisible(token, bar);
    } else { // setBarVisible
      FunctionUtil.checkNumberParam(functionName, parameters, 2, 4);
      boolean visible = FunctionUtil.paramAsBoolean(functionName, parameters, 1, true);
      Token token = FunctionUtil.getTokenFromParam(parser, functionName, parameters, 2, 3);
      return setVisible(token, bar, visible);
    }
  }

  /**
   * Get the value for the bar.
   *
   * @param token Get the value from this token
   * @param bar For this bar
   * @return A {@link BigDecimal} value, or an empty string "" if bar is not visible
   */
  public static Object getValue(Token token, String bar) {
    Object value = token.getState(bar);
    return value != null ? value : "";
  }

  /**
   * @param token Set the value in this token
   * @param bar For this bar
   * @param value New value for the bar. Will be converted into a {@link BigDecimal} before setting
   * @return The {@link BigDecimal} value that was actually set.
   */
  public static Object setValue(Token token, String bar, Object value) {
    BigDecimal val = getBigDecimalValue(value);
    MapTool.serverCommand().updateTokenProperty(token, Token.Update.setState, bar, value);
    return val;
  }

  /**
   * @param token Get the value of this token
   * @param bar For this bar
   * @return If the bar visible or not
   */
  public static BigDecimal isVisible(Token token, String bar) {
    return token.getState(bar) == null ? BigDecimal.ZERO : BigDecimal.ONE;
  }

  /**
   * @param token Set the value of this token
   * @param bar For this bar
   * @param show Should this bar be visible
   * @return If the bar visible or not
   */
  public static BigDecimal setVisible(Token token, String bar, boolean show) {
    BigDecimal value = show ? BigDecimal.ONE : null;
    MapTool.serverCommand().updateTokenProperty(token, Token.Update.setState, bar, value);
    return show ? BigDecimal.ONE : BigDecimal.ZERO;
  }

  /**
   * Convert the passed object into a big decimal value.
   *
   * @param value The value to be converted
   * @return The {@link BigDecimal} version of the value. The <code>null</code> value and strings
   *     that can't be converted to numbers return {@link BigDecimal#ZERO}
   */
  public static BigDecimal getBigDecimalValue(Object value) {
    BigDecimal val = null;
    if (value instanceof BigDecimal) {
      val = (BigDecimal) value;
    } else if (value == null) {
      val = BigDecimal.ZERO;
    } else {
      try {
        val = new BigDecimal(value.toString());
      } catch (NumberFormatException e) {
        val = BigDecimal.ZERO;
      } // endtry
    } // endif
    return val;
  }

  /**
   * @param functionName the name of the function
   * @param bar the name of the bar
   * @throws ParserException if the bar doesn't exist
   */
  private static void verifyBar(String functionName, String bar) throws ParserException {
    if (!MapTool.getCampaign().getTokenBarsMap().containsKey(bar)) {
      throw new ParserException(
          I18N.getText("macro.function.tokenBarFunction.unknownBar", functionName, bar));
    }
  }
}
