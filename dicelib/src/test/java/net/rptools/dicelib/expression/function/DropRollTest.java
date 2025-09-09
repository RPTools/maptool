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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import net.rptools.dicelib.expression.RunData;
import net.rptools.parser.Expression;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.EvaluationException;
import net.rptools.parser.function.ParameterException;
import org.junit.jupiter.api.Test;

public class DropRollTest {
  @Test
  public void testEvaluateRoll() throws ParserException, EvaluationException, ParameterException {

    Parser p = new Parser();
    p.addFunction(new DropRoll());

    try {
      RunData.setCurrent(new RunData(null));
      RunData.setSeed(10423L);

      Expression xp = p.parseExpression("drop(4,6,1)");

      long result = ((BigDecimal) xp.evaluate()).longValueExact();

      assertEquals(12L, result);
    } finally {
      RunData.setCurrent(null);
    }
  }
}
