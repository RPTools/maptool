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
package net.rptools.dicelib.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import net.rptools.parser.ParserException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * An alternative test suite that evaluates dice expressions against specific known roll sequences.
 * Uses {@link RunDataMockForTesting}.
 */
public class ExpressionParserWithMockRollsTest {
  /**
   * Helper method to initialize the RunData with the given rolls
   *
   * @param rolls the sequence of rolls that should be used
   */
  private void setUpMockRunData(int[] rolls) {
    RunDataMockForTesting mockRD = new RunDataMockForTesting(new Result(""), rolls);
    RunData.setCurrent(mockRD);
  }

  /** Clear the RunData after each test, in case we leave a mock instance as Current. */
  @AfterEach
  public void clearRunData() {
    RunData.setCurrent(null);
  }

  @Test
  public void testEvaluate_ShadowRun4NonGlich25() throws ParserException {
    int[] rolls = {2, 5};
    setUpMockRunData(rolls);
    Result result = new ExpressionParser().evaluate("2sr4");
    assertEquals("Hits: 1 Ones: 0  Results: 2 5 ", result.getValue());
  }

  @Test
  public void testEvaluate_ShadowRun4GremlinGlich25() throws ParserException {
    int[] rolls = {2, 5};
    setUpMockRunData(rolls);
    Result result = new ExpressionParser().evaluate("2sr4g1");
    assertEquals("Hits: 1 Ones: 0 *Gremlin Glitch*  Results: 2 5 ", result.getValue());
  }

  @Test
  public void testEvaluate_ShadowRun4CriticalGremlinGlich22() throws ParserException {
    int[] rolls = {2, 2};
    setUpMockRunData(rolls);
    Result result = new ExpressionParser().evaluate("2sr4g1");
    assertEquals("Hits: 0 Ones: 0 *Critical Gremlin Glitch*  Results: 2 2 ", result.getValue());
  }

  @Test
  public void testEvaluate_ShadowRun4Glitch15() throws ParserException {
    int[] rolls = {1, 5};
    setUpMockRunData(rolls);
    Result result = new ExpressionParser().evaluate("2sr4");
    assertEquals("Hits: 1 Ones: 1 *Glitch*  Results: 1 5 ", result.getValue());
  }

  @Test
  public void testEvaluate_ShadowRun4CriticalGlitch12() throws ParserException {
    int[] rolls = {1, 2};
    setUpMockRunData(rolls);
    Result result = new ExpressionParser().evaluate("2sr4");
    assertEquals("Hits: 0 Ones: 1 *Critical Glitch*  Results: 1 2 ", result.getValue());
  }

  @Test
  public void testEvaluate_ShadowRun4CriticalGlitch11() throws ParserException {
    int[] rolls = {1, 1};
    setUpMockRunData(rolls);
    Result result = new ExpressionParser().evaluate("2sr4");
    assertEquals("Hits: 0 Ones: 2 *Critical Glitch*  Results: 1 1 ", result.getValue());
  }

  @Test
  public void testEvaluate_ShadowRun5NonGlich25() throws ParserException {
    int[] rolls = {2, 5};
    setUpMockRunData(rolls);
    Result result = new ExpressionParser().evaluate("2sr5");
    assertEquals("Hits: 1 Ones: 0  Results: 2 5 ", result.getValue());
  }

  @Test
  public void testEvaluate_ShadowRun5NonGlitch15() throws ParserException {
    int[] rolls = {1, 5};
    setUpMockRunData(rolls);
    Result result = new ExpressionParser().evaluate("2sr5");
    assertEquals("Hits: 1 Ones: 1  Results: 1 5 ", result.getValue());
  }

  @Test
  public void testEvaluate_ShadowRun5CriticalGlitch11() throws ParserException {
    int[] rolls = {1, 1};
    setUpMockRunData(rolls);
    Result result = new ExpressionParser().evaluate("2sr5");
    assertEquals("Hits: 0 Ones: 2 *Critical Glitch*  Results: 1 1 ", result.getValue());
  }

  @Test
  public void testEvaluate_ShadowRun5CriticalGremlinGlitch12() throws ParserException {
    int[] rolls = {1, 2};
    setUpMockRunData(rolls);
    Result result = new ExpressionParser().evaluate("2sr5g1");
    assertEquals("Hits: 0 Ones: 1 *Critical Gremlin Glitch*  Results: 1 2 ", result.getValue());
  }

  @Test
  public void testEvaluate_ShadowRun5GremlinGlitch15() throws ParserException {
    int[] rolls = {1, 5};
    setUpMockRunData(rolls);
    Result result = new ExpressionParser().evaluate("2sr5g1");
    assertEquals("Hits: 1 Ones: 1 *Gremlin Glitch*  Results: 1 5 ", result.getValue());
  }

  @Test
  public void testEvaluate_ShadowRun5CriticalNonGremlinGlitch11() throws ParserException {
    int[] rolls = {1, 1};
    setUpMockRunData(rolls);
    Result result = new ExpressionParser().evaluate("2sr5g1");
    // This one would have glitched even without gremlins, thus, don't show the gremlin mark
    assertEquals("Hits: 0 Ones: 2 *Critical Glitch*  Results: 1 1 ", result.getValue());
  }

  @Test
  public void testEvaluate_ShadowRun5Glitch61Exploding1() throws ParserException {
    int[] rolls = {6, 1, 1};
    setUpMockRunData(rolls);
    Result result = new ExpressionParser().evaluate("2sr5e");
    assertEquals("Hits: 1 Ones: 2 *Glitch*  Results: 6 1 1 ", result.getValue());
  }

  @Test
  public void testEvaluate_ShadowRun5Glitch66Exploding6611() throws ParserException {
    int[] rolls = {6, 6, 6, 6, 1, 1};
    setUpMockRunData(rolls);
    Result result = new ExpressionParser().evaluate("2sr5e");
    // this is still a glitch.
    assertEquals("Hits: 4 Ones: 2 *Glitch*  Results: 6 6 6 6 1 1 ", result.getValue());
  }

  @Test
  public void testEvaluate_ExplodeWithMockRunData() throws ParserException {
    int[] rolls = {3, 6, 6, 2}; // explode both sixes
    setUpMockRunData(rolls);
    Result result = new ExpressionParser().evaluate("2d6e");
    assertEquals(new BigDecimal(17), result.getValue());
  }

  @Test
  public void testEvaluate_DropWithMockRunData() throws ParserException {
    int[] rolls = {6, 2, 5, 4, 1, 6}; // drop 2 and 1
    setUpMockRunData(rolls);
    Result result = new ExpressionParser().evaluate("6d6d2");
    assertEquals(new BigDecimal(21), result.getValue());
  }

  @Test
  public void testEvaluate_KeepWithMockRunData() throws ParserException {
    int[] rolls = {6, 2, 5, 4, 1, 6}; // keep the sixes and the 5
    setUpMockRunData(rolls);
    Result result = new ExpressionParser().evaluate("6d6k3");
    assertEquals(new BigDecimal(17), result.getValue());
  }

  @Test
  public void testEvaluate_KeepLowestWithMockRunData() throws ParserException {
    int[] rolls = {6, 2, 5, 4, 1, 6}; // keep the 1 and 2
    setUpMockRunData(rolls);
    Result result = new ExpressionParser().evaluate("6d6kl2");
    assertEquals(new BigDecimal(3), result.getValue());
  }

  @Test
  public void testEvaluate_RerollOnceAndKeepWithMockRunData() throws ParserException {
    int[] rolls = {4, 2, 1}; // the 2 will be re-rolled, and the 1 will be kept
    RunDataMockForTesting mockRD = new RunDataMockForTesting(new Result(""), rolls);
    RunData.setCurrent(mockRD);
    Result result = new ExpressionParser().evaluate("2d6rk3");
    assertEquals(new BigDecimal(5), result.getValue());
  }

  @Test
  public void testEvaluate_RerollOnceAndChooseWithMockRunData() throws ParserException {
    int[] rolls = {4, 2, 1}; // the 2 will be re-rolled, but still kept as it is higher than the 1
    RunDataMockForTesting mockRD = new RunDataMockForTesting(new Result(""), rolls);
    RunData.setCurrent(mockRD);
    Result result = new ExpressionParser().evaluate("2d6rc3");
    assertEquals(new BigDecimal(6), result.getValue());
  }

  @Test
  public void testEvaluate_CountSuccessesWithMockRunData() throws ParserException {
    int[] rolls = {6, 2, 5, 4, 1, 6}; // count the 5 and 6s
    setUpMockRunData(rolls);
    Result result = new ExpressionParser().evaluate("6d6s5");
    assertEquals(new BigDecimal(3), result.getValue());
  }

  @Test
  public void testEvaluate_ExplodingSuccessesWithMockRunData() throws ParserException {
    int[] rolls = {5, 4, 6, 1, 6, 6, 5};
    setUpMockRunData(rolls);
    Result result = new ExpressionParser().evaluate("4d6es8");
    assertEquals("Dice: 5, 4, 7, 17, Successes: 1", result.getValue());
  }

  @Test
  public void testEvaluate_OpenWithMockRunData() throws ParserException {
    int[] rolls = {5, 6, 4, 6, 6, 2, 3};
    setUpMockRunData(rolls);
    Result result = new ExpressionParser().evaluate("4d6o");
    assertEquals("Dice: 5, 10, 14, 3, Maximum: 14", result.getValue());
  }

  @Test
  public void testEvaluate_DropHighWithMockRunData() throws ParserException {
    int[] rolls = {6, 2, 5, 4, 1, 6}; // drop 6s and 5
    setUpMockRunData(rolls);
    Result result = new ExpressionParser().evaluate("6d6dh3");
    assertEquals(new BigDecimal(7), result.getValue());
  }

  @Test
  public void testEvaluate_KeepLowWithMockRunData() throws ParserException {
    int[] rolls = {6, 2, 5, 4, 1, 6}; // keep the 1 and 2
    setUpMockRunData(rolls);
    Result result = new ExpressionParser().evaluate("6d6kl2");
    assertEquals(new BigDecimal(3), result.getValue());
  }

  @Test
  public void testEvaluate_FudgeRollWithMockRunData() throws ParserException {
    int[] rolls = {1, 2, 2, 3}; // fudge dice are weird - they're shifted d3s to equal -1, 0, 1
    setUpMockRunData(rolls);
    Result result = new ExpressionParser().evaluate("4df");
    assertEquals(BigDecimal.ZERO, result.getValue());
  }

  @Test
  public void testEvaluate_RollWithUpperWithMockRunData() throws ParserException {
    int[] rolls = {6, 16, 18, 15, 14}; // 16, 18 bounded to 15
    setUpMockRunData(rolls);
    Result result = new ExpressionParser().evaluate("5d20u15");
    assertEquals(new BigDecimal(65), result.getValue());
  }

  @Test
  public void testEvaluate_RollWithLowerWithMockRunData() throws ParserException {
    int[] rolls = {13, 7, 6, 8, 2}; // 6, 2 bounded to 7
    setUpMockRunData(rolls);
    Result result = new ExpressionParser().evaluate("5d20l7");
    assertEquals(new BigDecimal(42), result.getValue());
  }

  @Test
  public void testEvaluate_RollAddWithUpperWithMockRunData() throws ParserException {
    int[] rolls = {17, 4, 20, 15, 16}; // adds to 22, 9, 25, 20, 21. Then 22, 25 bounded to 21.
    setUpMockRunData(rolls);
    Result result = new ExpressionParser().evaluate("5d20a5u21");
    assertEquals(new BigDecimal(92), result.getValue());
  }

  @Test
  public void testEvaluate_RollAddWithLowerWithMockRunData() throws ParserException {
    int[] rolls = {13, 6, 7, 4, 8}; // adds to 16, 9, 10, 7, 11. Then 9, 7 bounded to 10.
    setUpMockRunData(rolls);
    Result result = new ExpressionParser().evaluate("5d20a3l10");
    assertEquals(new BigDecimal(57), result.getValue());
  }

  @Test
  public void testEvaluate_RollSubWithUpperWithMockRunData() throws ParserException {
    int[] rolls = {16, 7, 19, 15, 17}; // subs to 12, 3, 15, 11, 13. Then 15, 13 bounded to 12.
    setUpMockRunData(rolls);
    Result result = new ExpressionParser().evaluate("5d20s4u12");
    assertEquals(new BigDecimal(50), result.getValue());
  }

  @Test
  public void testEvaluate_RollSubWithLowerWithMockRunData() throws ParserException {
    int[] rolls = {13, 12, 18, 5, 11}; // subs to 6, 5, 11, -2, 4. Then -2, 4 bounded to 5.
    setUpMockRunData(rolls);
    Result result = new ExpressionParser().evaluate("5d20s7l5");
    assertEquals(new BigDecimal(32), result.getValue());
  }

  @Test
  public void testEvaluate_arsMagicaStress() throws ParserException {
    int[] rolls = {3, 7, 5, 2, 3, 2, 8, 9, 4, 7};
    setUpMockRunData(rolls);
    for (int i = 0; i < rolls.length; i++) {
      Result result = new ExpressionParser().evaluate("ans2");
      assertEquals(BigDecimal.valueOf(rolls[i]), result.getValue());
    }

    setUpMockRunData(rolls);
    for (int i = 0; i < rolls.length; i++) {
      Result result = new ExpressionParser().evaluate("as2");
      assertEquals(Integer.toString(rolls[i]), result.getValue());
    }

    int[] bonus = {1, 3, 0, -4, 5, -2, -1, 4};
    for (int x = 0; x < bonus.length; x++) {
      setUpMockRunData(rolls);
      for (int i = 0; i < rolls.length; i++) {
        String bonusStr = bonus[x] < 0 ? Integer.toString(bonus[x]) : "+" + bonus[x];
        Result result = new ExpressionParser().evaluate("ans2b#" + bonusStr);
        assertEquals(BigDecimal.valueOf(Math.max(rolls[i] + bonus[x], 0)), result.getValue());
      }

      setUpMockRunData(rolls);
      for (int i = 0; i < rolls.length; i++) {
        String bonusStr = bonus[x] < 0 ? Integer.toString(bonus[x]) : "+" + bonus[x];
        Result result = new ExpressionParser().evaluate("as2b#" + bonusStr);
        assertEquals(Integer.toString(Math.max(rolls[i] + bonus[x], 0)), result.getValue());
      }
    }
  }

  @Test
  public void testEvaluate_arsMagicaStressNoBotch() throws ParserException {
    int[] rolls = {10, 3, 2};
    setUpMockRunData(rolls);
    Result result = new ExpressionParser().evaluate("ans2");
    assertEquals(BigDecimal.valueOf(0), result.getValue());

    setUpMockRunData(rolls);
    result = new ExpressionParser().evaluate("as2");
    assertEquals(Integer.toString(0), result.getValue());

    int[] bonus = {1, 3, 0, -4, 5, -2, -1, 4};
    for (int x = 0; x < bonus.length; x++) {
      setUpMockRunData(rolls);
      String bonusStr = bonus[x] < 0 ? Integer.toString(bonus[x]) : "+" + bonus[x];
      result = new ExpressionParser().evaluate("ans2b#" + bonusStr);
      assertEquals(BigDecimal.valueOf(Math.max(0, bonus[x])), result.getValue());
    }

    for (int x = 0; x < bonus.length; x++) {
      setUpMockRunData(rolls);
      String bonusStr = bonus[x] < 0 ? Integer.toString(bonus[x]) : "+" + bonus[x];
      result = new ExpressionParser().evaluate("as2b#" + bonusStr);
      assertEquals(Integer.toString(Math.max(0, bonus[x])), result.getValue());
    }

    rolls = new int[] {10, 1, 1};
    setUpMockRunData(rolls);
    result = new ExpressionParser().evaluate("ans2");
    assertEquals(BigDecimal.valueOf(0), result.getValue());

    setUpMockRunData(rolls);
    result = new ExpressionParser().evaluate("as2");
    assertEquals(Integer.toString(0), result.getValue());

    for (int x = 0; x < bonus.length; x++) {
      setUpMockRunData(rolls);
      String bonusStr = bonus[x] < 0 ? Integer.toString(bonus[x]) : "+" + bonus[x];
      result = new ExpressionParser().evaluate("ans2b#" + bonusStr);
      assertEquals(BigDecimal.valueOf(Math.max(0, bonus[x])), result.getValue());
    }

    for (int x = 0; x < bonus.length; x++) {
      setUpMockRunData(rolls);
      String bonusStr = bonus[x] < 0 ? Integer.toString(bonus[x]) : "+" + bonus[x];
      result = new ExpressionParser().evaluate("as2b#" + bonusStr);
      assertEquals(Integer.toString(Math.max(0, bonus[x])), result.getValue());
    }
  }

  @Test
  public void testEvaluate_arsMagicaStressBotch() throws ParserException {
    int[] rolls = {10, 10, 1};
    setUpMockRunData(rolls);
    Result result = new ExpressionParser().evaluate("ans2");
    assertEquals(BigDecimal.valueOf(-1), result.getValue());

    setUpMockRunData(rolls);
    result = new ExpressionParser().evaluate("as2");
    assertEquals("0 (1 botch)", result.getValue());

    rolls = new int[] {10, 10, 10, 10};
    setUpMockRunData(rolls);
    result = new ExpressionParser().evaluate("ans2");
    assertEquals(BigDecimal.valueOf(-2), result.getValue());

    setUpMockRunData(rolls);
    result = new ExpressionParser().evaluate("as2");
    assertEquals("0 (2 botches)", result.getValue());

    int[] bonus = {1, 3, 0, -4, 5, -2, -1, 4};
    for (int x = 0; x < bonus.length; x++) {
      setUpMockRunData(rolls);
      String bonusStr = bonus[x] < 0 ? Integer.toString(bonus[x]) : "+" + bonus[x];
      result = new ExpressionParser().evaluate("ans2b#" + bonusStr);
      assertEquals(BigDecimal.valueOf(-2), result.getValue());
    }

    for (int x = 0; x < bonus.length; x++) {
      setUpMockRunData(rolls);
      String bonusStr = bonus[x] < 0 ? Integer.toString(bonus[x]) : "+" + bonus[x];
      result = new ExpressionParser().evaluate("as2b#" + bonusStr);
      assertEquals("0 (2 botches)", result.getValue());
    }

    for (int x = 0; x < bonus.length; x++) {
      setUpMockRunData(rolls);
      String bonusStr = bonus[x] < 0 ? Integer.toString(bonus[x]) : "+" + bonus[x];
      result = new ExpressionParser().evaluate("ans3b#" + bonusStr);
      assertEquals(BigDecimal.valueOf(-3), result.getValue());
    }

    for (int x = 0; x < bonus.length; x++) {
      setUpMockRunData(rolls);
      String bonusStr = bonus[x] < 0 ? Integer.toString(bonus[x]) : "+" + bonus[x];
      result = new ExpressionParser().evaluate("as3b#" + bonusStr);
      assertEquals("0 (3 botches)", result.getValue());
    }
  }
}
