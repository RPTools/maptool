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
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractNumberFunction;

/**
 * roll(range) or roll(times, range)
 *
 * <p>Generate a random number form 1 to <code>sides</code>, <code>times</code> number of times. If
 * <code>times</code> is not supplied it defaults to 1.
 *
 * <p>Example: roll(4, 6) = 4d6
 */
public class Roll extends AbstractNumberFunction {

  public Roll() {
    super(1, 2, false, "d", "roll", "dice");
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters) {
    int n = 0;

    int times = 1;
    if (parameters.size() == 2) times = ((BigDecimal) parameters.get(n++)).intValue();

    int sides = ((BigDecimal) parameters.get(n++)).intValue();

    return new BigDecimal(DiceHelper.rollDice(times, sides));
  }
}
