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
package net.rptools.dicelib.expression.function;

import java.math.BigDecimal;
import java.util.List;
import net.rptools.parser.Parser;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractNumberFunction;
import net.rptools.parser.function.EvaluationException;

public class ExplodingSuccessDice extends AbstractNumberFunction {

  public ExplodingSuccessDice() {
    super(3, 4, true, "explodingSuccess");
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws EvaluationException {
    int n = 0;
    int times = ((BigDecimal) parameters.get(n++)).intValue();
    int sides = ((BigDecimal) parameters.get(n++)).intValue();
    int target = ((BigDecimal) parameters.get(n++)).intValue();
    int limit = -1;
    if (parameters.size() > 3) {
      limit = ((BigDecimal) parameters.get(n++)).intValue();
    }

    return DiceHelper.explodingSuccessDice(times, sides, target, limit);
  }
}
