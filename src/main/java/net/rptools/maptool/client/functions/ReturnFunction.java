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
import net.rptools.maptool.client.functions.exceptions.*;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;
import net.rptools.parser.function.ParameterException;

/**
 * Aborts the current parser evaluation.
 *
 * @author oliver.szymanski
 */
public class ReturnFunction extends AbstractFunction implements DefinesSpecialVariables {
  public ReturnFunction() {
    super(1, 2, "return");
  }

  /** The singleton instance. */
  private static final ReturnFunction instance = new ReturnFunction();

  /**
   * Gets the Input instance.
   *
   * @return the instance.
   */
  public static ReturnFunction getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {

    BigDecimal value = (BigDecimal) parameters.get(0);
    if (value.intValue() == 0) {
      ReturnFunctionException returnException = new ReturnFunctionException(null);
      if (parameters.size() > 1) {
        returnException.setResult(parameters.get(1));
      }
      throw returnException;
    } else return new BigDecimal(value.intValue());
  }

  @Override
  public void checkParameters(String functionName, List<Object> parameters)
      throws ParameterException {
    super.checkParameters(functionName, parameters);

    Object param = parameters.get(0);
    if (!(param instanceof BigDecimal)) {
      Object[] arrobject = new Object[2];
      arrobject[0] = param == null ? "null" : param.getClass().getName();
      arrobject[1] = BigDecimal.class.getName();
      throw new ParameterException(
          String.format("Illegal argument type %s, expecting %s", arrobject));
    }
  }

  @Override
  public String[] getSpecialVariables() {
    return new String[] {"macro.return"};
  }
}
