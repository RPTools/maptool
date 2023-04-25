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
import junit.framework.TestCase;
import net.rptools.common.expression.RunData;
import net.rptools.parser.Expression;
import net.rptools.parser.MapVariableResolver;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.EvaluationException;
import net.rptools.parser.function.ParameterException;

public class RollTest extends TestCase {
  public void testEvaluateRoll() throws ParserException, EvaluationException, ParameterException {
    Parser p = new Parser();
    p.addFunction(new Roll());

    try {
      RunData.setCurrent(new RunData(null));

      Expression xp = p.parseExpression("roll(6)");

      for (int i = 0; i < 1000; i++) {
        long result = ((BigDecimal) xp.evaluate()).longValueExact();

        assertTrue(result >= 1 && result <= 6);
      }
    } finally {
      RunData.setCurrent(null);
    }
  }

  public void testIsNonDeterministic()
      throws ParserException, EvaluationException, ParameterException {
    Parser p = new Parser();
    p.addFunction(new Roll());

    try {
      RunData.setCurrent(new RunData(null));

      Expression xp = p.parseExpression("roll(10, 6) + 10");
      Expression dxp = xp.getDeterministicExpression(new MapVariableResolver());

      assertTrue(dxp.format().matches("\\d+ \\+ 10"));
    } finally {
      RunData.setCurrent(null);
    }
  }
}
