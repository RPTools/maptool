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
import net.rptools.parser.function.EvaluationException;

/**
 * Will re-roll dice under a given threshold once, and optionally choose the higher of the two
 * results.
 *
 * <p>Differs from {@link RerollDice} in that the new results are allowed to be below the
 * lowerBound.
 */
public class RerollDiceOnce extends AbstractNumberFunction {

  public RerollDiceOnce() {
    super(3, 4, false, "rerollOnce");
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws EvaluationException {
    int n = 0;
    int times = ((BigDecimal) parameters.get(n++)).intValue();
    int sides = ((BigDecimal) parameters.get(n++)).intValue();
    int lowerBound = ((BigDecimal) parameters.get(n++)).intValue();
    boolean chooseHigher = false;
    if (parameters.size() > n)
      chooseHigher =
          !BigDecimal.ZERO.equals(parameters.get(n)); // as with If, anything other than 0 is truthy

    return new BigDecimal(DiceHelper.rerollDiceOnce(times, sides, lowerBound, chooseHigher));
  }
}
