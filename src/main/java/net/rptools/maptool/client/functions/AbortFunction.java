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
import net.rptools.maptool.language.I18N;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractNumberFunction;

/**
 * Aborts the current parser evaluation.
 *
 * @author knizia.fan
 */
public class AbortFunction extends AbstractNumberFunction implements DefinesSpecialVariables {
  public AbortFunction() {
    super(1, 1, "abort");
  }

  /** The singleton instance. */
  private static final AbortFunction instance = new AbortFunction();

  /**
   * Gets the Input instance.
   *
   * @return the instance.
   */
  public static AbortFunction getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> parameters)
      throws ParserException {
    BigDecimal value = (BigDecimal) parameters.get(0);
    if (value.intValue() == 0)
      throw new AbortFunctionException(
          I18N.getText("macro.function.abortFunction.message", "Abort()"));
    else return new BigDecimal(value.intValue());
  }

  /**
   * Exception type thrown by abort() function. Semantics are to silently halt the current
   * execution.
   */
  public static class AbortFunctionException extends ParserException {
    public AbortFunctionException(Throwable cause) {
      super(cause);
    }

    public AbortFunctionException(String msg) {
      super(msg);
    }
  }

  @Override
  public String[] getSpecialVariables() {
    return new String[] {"macro.catchAbort"};
  }
}
