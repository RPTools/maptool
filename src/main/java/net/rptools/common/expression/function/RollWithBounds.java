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
package net.rptools.common.expression.function;

import java.math.BigDecimal;
import java.util.List;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractNumberFunction;

public class RollWithBounds extends AbstractNumberFunction {

  public RollWithBounds() {
    super(
        3,
        4,
        false,
        "rollSubWithLower",
        "rollWithLower",
        "rollAddWithUpper",
        "rollWithUpper",
        "rollAddWithLower");
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {
    int times = 0;
    int sides = 0;
    int mod = 0;
    int lower = Integer.MIN_VALUE;
    int upper = Integer.MAX_VALUE;

    switch (functionName) {
      case "rollSubWithLower":
        times = ((BigDecimal) parameters.get(0)).intValue();
        sides = ((BigDecimal) parameters.get(1)).intValue();
        mod = -((BigDecimal) parameters.get(2)).intValue();
        lower = ((BigDecimal) parameters.get(3)).intValue();
        break;
      case "rollWithLower":
        times = ((BigDecimal) parameters.get(0)).intValue();
        sides = ((BigDecimal) parameters.get(1)).intValue();
        lower = ((BigDecimal) parameters.get(2)).intValue();
        break;
      case "rollAddWithUpper":
        times = ((BigDecimal) parameters.get(0)).intValue();
        sides = ((BigDecimal) parameters.get(1)).intValue();
        mod = ((BigDecimal) parameters.get(2)).intValue();
        upper = ((BigDecimal) parameters.get(3)).intValue();
        break;
      case "rollWithUpper":
        times = ((BigDecimal) parameters.get(0)).intValue();
        sides = ((BigDecimal) parameters.get(1)).intValue();
        upper = ((BigDecimal) parameters.get(2)).intValue();
        break;
      case "rollAddWithLower":
        times = ((BigDecimal) parameters.get(0)).intValue();
        sides = ((BigDecimal) parameters.get(1)).intValue();
        mod = ((BigDecimal) parameters.get(2)).intValue();
        lower = ((BigDecimal) parameters.get(3)).intValue();
        break;
      default:
        throw new ParserException("Unknown function name: " + functionName);
    }
    return DiceHelper.rollModWithBounds(times, sides, mod, lower, upper);
  }
}
