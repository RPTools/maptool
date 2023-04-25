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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.rptools.parser.MapVariableResolver;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.EvaluationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ExpressionParserTest {

  /**
   * Make sure these tests aren't using mock RunData instances that might still be Current.
   * Safeguard against possible problematic interleaving if tests get run in parallel.
   */
  @BeforeEach
  public void setUp() {
    RunData.setCurrent(null);
  }

  @Test
  public void testEvaluate() throws ParserException {
    Result result = new ExpressionParser().evaluate("100+4d1*10");

    assertNotNull(result);
    assertEquals("100+4d1*10", result.getExpression());
    assertEquals("100 + 4 * 10", result.getDetailExpression());
    assertEquals(new BigDecimal(140), (BigDecimal) result.getValue());
  }

  @Test
  public void testEvaluate_Explode() throws ParserException {
    RunData.setSeed(10423L);
    Result result = new ExpressionParser().evaluate("100+10d6e+1");

    assertEquals(new BigDecimal(164), result.getValue());
  }

  @Test
  public void testEvaluate_Drop() throws ParserException {
    RunData.setSeed(10423L);
    Result result = new ExpressionParser().evaluate("100+10d6d2+1");

    assertEquals(new BigDecimal(138), result.getValue());
  }

  @Test
  public void testEvaluate_Keep() throws ParserException {
    RunData.setSeed(10423L);
    Result result = new ExpressionParser().evaluate("100+10d6k8+1");

    assertEquals(new BigDecimal(138), result.getValue());
  }

  @Test
  public void testEvaluate_Keep_error_tooManyDice() throws ParserException {
    try {
      Result result = new ExpressionParser().evaluate("2d6k4");
      fail("Expected EvaluationException from trying to keep too many dice");
    } catch (EvaluationException expected) {
      // test passes if expected exception is produced
    }
  }

  @Test
  public void testEvaluate_KeepLowest_error_tooManyDice() throws ParserException {
    try {
      Result result = new ExpressionParser().evaluate("2d6kl4");
      fail("Expected EvaluationException from trying to keep too many dice");
    } catch (EvaluationException expected) {
      // test passes if expected exception is produced
    }
  }

  @Test
  public void testEvaluate_RerollOnceAndKeep() throws ParserException {
    RunData.setSeed(10423L);
    Result result = new ExpressionParser().evaluate("20d10rk5");
    // the sequence of rolls produced includes an instance of a 4 being replaced by a 3
    assertEquals(new BigDecimal(121), result.getValue());
  }

  @Test
  public void testEvaluate_RerollOnceAndChoose() throws ParserException {
    RunData.setSeed(10423L);
    Result result = new ExpressionParser().evaluate("20d10rc5");
    // in rerollAndChoose mode, the 4 gets preserved instead of being replaced by the 3
    assertEquals(new BigDecimal(122), result.getValue());
  }

  @Test
  public void testEvaluate_CountSuccess() throws ParserException {
    RunData.setSeed(10423L);
    Result result = new ExpressionParser().evaluate("100+10d6s4+1");

    assertEquals(new BigDecimal(109), result.getValue());
  }

  @Test
  public void testEvaluate_ExplodingSuccess() throws ParserException {
    RunData.setSeed(10423L);
    Result result = new ExpressionParser().evaluate("10d4es6");
    assertEquals("10d4es6", result.getExpression());
    assertEquals("explodingSuccess(10, 4, 6)", result.getDetailExpression());
    assertEquals("Dice: 1, 2, 2, 1, 2, 7, 1, 7, 2, 3, Successes: 2", result.getValue());
    RunData.setSeed(10423L);

    result = new ExpressionParser().evaluate("10es9");
    assertEquals("10es9", result.getExpression());
    assertEquals("explodingSuccess(10, 6, 9)", result.getDetailExpression());
    assertEquals("Dice: 4, 4, 4, 3, 16, 5, 1, 4, 14, 8, Successes: 2", result.getValue());
  }

  @Test
  public void testEvaluate_OpenTest() throws ParserException {
    RunData.setSeed(10423L);
    Result result = new ExpressionParser().evaluate("10d4o");
    assertEquals("10d4o", result.getExpression());
    assertEquals("openTest(10, 4)", result.getDetailExpression());
    assertEquals("Dice: 1, 2, 2, 1, 2, 7, 1, 7, 2, 3, Maximum: 7", result.getValue());

    RunData.setSeed(10423L);
    result = new ExpressionParser().evaluate("10o");
    assertEquals("10o", result.getExpression());
    assertEquals("openTest(10, 6)", result.getDetailExpression());
    assertEquals("Dice: 4, 4, 4, 3, 16, 5, 1, 4, 14, 8, Maximum: 16", result.getValue());
  }

  @Test
  public void testEvaluate_SR4Success() throws ParserException {
    RunData.setSeed(10523L);
    Result result = new ExpressionParser().evaluate("5sr4");
    assertEquals("5sr4", result.getExpression());
    assertEquals("sr4(5)", result.getDetailExpression());
    assertEquals("Hits: 1 Ones: 1  Results: 3 1 4 6 3 ", result.getValue());
  }

  @Test
  public void testEvaluate_SR4GremlinSuccess() throws ParserException {
    RunData.setSeed(10523L);
    Result result = new ExpressionParser().evaluate("5sr4g2");
    assertEquals("5sr4g2", result.getExpression());
    assertEquals("sr4(5, 2)", result.getDetailExpression());
    assertEquals("Hits: 1 Ones: 1 *Gremlin Glitch*  Results: 3 1 4 6 3 ", result.getValue());
  }

  @Test
  public void testEvaluate_SR4ExplodingSuccess() throws ParserException {
    RunData.setSeed(10523L);
    Result result = new ExpressionParser().evaluate("5sr4e");
    assertEquals("5sr4e", result.getExpression());
    assertEquals("sr4e(5)", result.getDetailExpression());
    assertEquals("Hits: 1 Ones: 2  Results: 3 1 4 6 3 1 ", result.getValue());
  }

  @Test
  public void testEvaluate_SR4ExplodingGremlinSuccess() throws ParserException {
    RunData.setSeed(10523L);
    Result result = new ExpressionParser().evaluate("5sr4eg2");
    assertEquals("5sr4eg2", result.getExpression());
    assertEquals("sr4e(5, 2)", result.getDetailExpression());
    assertEquals("Hits: 1 Ones: 2 *Gremlin Glitch*  Results: 3 1 4 6 3 1 ", result.getValue());
  }

  @Test
  public void testEvaluate_SR5Success() throws ParserException {
    RunData.setSeed(10523L);
    Result result = new ExpressionParser().evaluate("5sr5");
    assertEquals("5sr5", result.getExpression());
    assertEquals("sr5(5)", result.getDetailExpression());
    assertEquals("Hits: 1 Ones: 1  Results: 3 1 4 6 3 ", result.getValue());
  }

  @Test
  public void testEvaluate_SR5GremlinSuccess() throws ParserException {
    RunData.setSeed(10523L);
    Result result = new ExpressionParser().evaluate("5sr5g2");
    assertEquals("5sr5g2", result.getExpression());
    assertEquals("sr5(5, 2)", result.getDetailExpression());
    assertEquals("Hits: 1 Ones: 1 *Gremlin Glitch*  Results: 3 1 4 6 3 ", result.getValue());
  }

  @Test
  public void testEvaluate_SR5ExplodingSuccess() throws ParserException {
    RunData.setSeed(10523L);
    Result result = new ExpressionParser().evaluate("5sr5e");
    assertEquals("5sr5e", result.getExpression());
    assertEquals("sr5e(5)", result.getDetailExpression());
    assertEquals("Hits: 1 Ones: 2  Results: 3 1 4 6 3 1 ", result.getValue());
  }

  @Test
  public void testEvaluate_SR5ExplodingGremlinSuccess() throws ParserException {
    RunData.setSeed(10523L);
    Result result = new ExpressionParser().evaluate("5sr5eg2");
    assertEquals("5sr5eg2", result.getExpression());
    assertEquals("sr5e(5, 2)", result.getDetailExpression());
    assertEquals("Hits: 1 Ones: 2 *Gremlin Glitch*  Results: 3 1 4 6 3 1 ", result.getValue());
  }

  @Test
  public void testEvaluate_HeroRoll() throws ParserException {
    RunData.setSeed(10423L);
    ExpressionParser parser = new ExpressionParser();
    VariableResolver resolver = new MapVariableResolver();

    Result result = parser.evaluate("4.5d6h", resolver);
    assertEquals(new BigDecimal(18), result.getValue());

    result = parser.evaluate("4.5d6b", resolver);
    assertEquals(new BigDecimal(5), result.getValue());

    RunData.setSeed(10423L);
    parser = new ExpressionParser();
    resolver = new MapVariableResolver();

    result = parser.evaluate("4d6h", resolver);
    assertEquals(new BigDecimal(15), result.getValue());

    result = parser.evaluate("4d6b", resolver);
    assertEquals(new BigDecimal(4), result.getValue());
  }

  @Test
  private VariableResolver initVar(String name, Object value) throws ParserException {
    VariableResolver result = new MapVariableResolver();
    result.setVariable(name, value);
    return result;
  }

  @Test
  public void testEvaluate_FudgeRoll() throws ParserException {
    RunData.setSeed(10423L);
    ExpressionParser parser = new ExpressionParser();

    Result result = parser.evaluate("dF");
    assertEquals(new BigDecimal(-1), result.getValue());

    result = parser.evaluate("4df");
    assertEquals(new BigDecimal(0), result.getValue());

    // Don't parse df in the middle of things
    result = parser.evaluate("asdfg", initVar("asdfg", new BigDecimal(10)));
    assertEquals(new BigDecimal(10), result.getValue());
  }

  @Test
  public void testEvaluate_UbiquityRoll() throws ParserException {
    RunData.setSeed(10423L);
    ExpressionParser parser = new ExpressionParser();

    Result result = parser.evaluate("dU");
    assertEquals(new BigDecimal(0), result.getValue());

    result = parser.evaluate("10du");
    assertEquals(new BigDecimal(4), result.getValue());

    // Don't parse a uf in the middle of other things
    result = parser.evaluate("asufg", initVar("asufg", new BigDecimal(10)));
    assertEquals(new BigDecimal(10), result.getValue());
  }

  @Test
  public void testEvaluate_ColorHex() throws ParserException {
    RunData.setSeed(10423L);
    ExpressionParser parser = new ExpressionParser();

    Result result = parser.evaluate("#FF0000");
    assertEquals(new BigDecimal(new BigInteger("FF0000", 16)), result.getValue());

    result = parser.evaluate("#00FF0000");
    assertEquals(new BigDecimal(new BigInteger("FF0000", 16)), result.getValue());

    result = parser.evaluate("#FF0");
    assertEquals(new BigDecimal(new BigInteger("FFFF00", 16)), result.getValue());
  }

  @Test
  public void testEvaluate_If() throws ParserException {
    ExpressionParser parser = new ExpressionParser();

    evaluateExpression(parser, "if(10 > 2, 10, 2)", new BigDecimal(10));
    evaluateExpression(parser, "if(10 < 2, 10, 2)", new BigDecimal(2));
    evaluateStringExpression(parser, "if(10 < 2, 's1', 's2')", "s2");
    evaluateStringExpression(parser, "if(10 > 2, 's1', 's2')", "s1");
  }

  @Test
  public void testEvaluate_Multiline() throws ParserException {
    RunData.setSeed(10423L);
    ExpressionParser parser = new ExpressionParser();

    evaluateExpression(parser, "10 + \r\n d6 + \n 2", new BigDecimal(16));

    String s = "10 + // Constant expression\n" + "2 + // Another bit\n" + "d20 // The roll\n";

    evaluateExpression(parser, s, new BigDecimal(26));
  }

  @Test
  public void testMultilineRegex() {
    String str1 = "one two three";
    String str2 = "one two\nthree";

    Pattern p1 = Pattern.compile("^one(.*)three$");
    Pattern p2 = Pattern.compile("one(.*)three", Pattern.MULTILINE);

    Matcher m1 = p1.matcher(str1);
    Matcher m2 = p2.matcher(str2);

    System.out.println(m1.matches());
    System.out.println(m2.matches());
  }

  @Test
  public void testNoTransformInStrings() throws ParserException {
    ExpressionParser parser = new ExpressionParser();

    evaluateStringExpression(parser, "'10' + 'd10'", "10d10");
  }

  @Test
  public void testVariableRegexOverlaps() throws ParserException {
    ExpressionParser parser = new ExpressionParser();
    Result result = parser.evaluate("food10 + 10", initVar("food10", new BigDecimal(10)));
    assertEquals(new BigDecimal(20), result.getValue());
  }

  @Test
  public void testNonDetailedExpression() throws ParserException {
    ExpressionParser parser = new ExpressionParser();

    int[] flattenings = new int[] {0};

    MapVariableResolver resolver = new MapVariableResolver();
    resolver.setVariable(
        "anumber",
        new BigDecimal(3) {
          @Override
          public String toString() {
            flattenings[0]++;
            return super.toString();
          }
        });

    // one evaluation with detailed expression (the default)
    Result result = parser.evaluate("anumber + 1", resolver, true);
    assertEquals("anumber + 1", result.getExpression());
    assertEquals("3 + 1", result.getDetailExpression());
    assertEquals(new BigDecimal(4), result.getValue());

    // one evaluation without detailed expression (this makes dicelib not go through a deterministic
    // expression)
    result = parser.evaluate("anumber + 1", resolver, false);
    assertEquals("anumber + 1", result.getExpression());
    assertEquals("anumber + 1", result.getDetailExpression());
    assertEquals(new BigDecimal(4), result.getValue());

    // expecting one flattening only for the former, not for the latter
    // this makes json arrays not being flattened/coerced on every macro
    // line *unless* the result is printed out
    assertEquals(flattenings[0], 1);
  }

  @Test
  private void evaluateExpression(ExpressionParser p, String expression, BigDecimal answer)
      throws ParserException {
    Result result = p.evaluate(expression);
    assertEquals(
        0,
        answer.compareTo((BigDecimal) result.getValue()),
        String.format(
            "%s evaluated incorrectly expected <%s> but was <%s>",
            expression, answer, result.getValue()));
  }

  private void evaluateStringExpression(ExpressionParser p, String expression, String answer)
      throws ParserException {
    Result result = p.evaluate(expression);

    assertEquals(answer, result.getValue());
  }
}
