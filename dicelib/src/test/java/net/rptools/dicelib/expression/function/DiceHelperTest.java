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
import static org.junit.jupiter.api.Assertions.fail;

import net.rptools.dicelib.expression.RunData;
import net.rptools.parser.function.EvaluationException;
import org.junit.jupiter.api.Test;

public class DiceHelperTest {
  @Test
  public void testRollDice() throws Exception {
    RunData.setCurrent(new RunData(null));
    RunData.setSeed(102312L);

    assertEquals(42, DiceHelper.rollDice(10, 6));
  }

  @Test
  public void testKeepDice() throws Exception {
    RunData.setCurrent(new RunData(null));
    RunData.setSeed(102312L);

    assertEquals(28, DiceHelper.keepDice(10, 6, 5));
  }

  @Test
  public void testDropDice() throws Exception {
    RunData.setCurrent(new RunData(null));
    RunData.setSeed(102312L);

    assertEquals(28, DiceHelper.dropDice(10, 6, 5));
  }

  @Test
  public void testRerollDice() throws Exception {
    RunData.setCurrent(new RunData(null));
    RunData.setSeed(102312L);

    assertEquals(50, DiceHelper.rerollDice(10, 6, 2));
  }

  @Test
  public void testExplodeDice() throws Exception {
    RunData.setCurrent(new RunData(null));
    RunData.setSeed(102312L);

    assertEquals(23, DiceHelper.explodeDice(4, 6, -1));
  }

  @Test
  public void testExplodeDice_Exception() throws Exception {
    try {
      RunData.setCurrent(new RunData(null));
      RunData.setSeed(102312L);

      assertEquals(23, DiceHelper.explodeDice(4, 1, -1));
      fail("Expected EvaluationException");
    } catch (EvaluationException e) {
    }
  }
}
