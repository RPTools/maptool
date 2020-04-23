package net.rptools.common.expression;

import junit.framework.TestCase;
import net.rptools.parser.ParserException;

import java.math.BigDecimal;

/**
 * An alternative test suite that evaluates dice expressions against specific known roll sequences.  Uses {@link RunDataMockForTesting}.
 */
public class ExpressionParserWithMockRollsTest extends TestCase {
    /**
     * Helper method to initialize the RunData with the given rolls
     *
     * @param rolls the sequence of rolls that should be used
     */
    private void setUpMockRunData(int[] rolls) {
        RunDataMockForTesting mockRD = new RunDataMockForTesting(new Result(""), rolls);
        RunData.setCurrent(mockRD);
    }

    public void testEvaluate_ExplodeWithMockRunData() throws ParserException {
        int[] rolls = {3, 6, 6, 2}; // explode both sixes
        setUpMockRunData(rolls);
        Result result = new ExpressionParser().evaluate("2d6e");
        assertEquals(new BigDecimal(17), result.getValue());
    }

    public void testEvaluate_DropWithMockRunData() throws ParserException {
        int[] rolls = {6, 2, 5, 4, 1, 6}; // drop 2 and 1
        setUpMockRunData(rolls);
        Result result = new ExpressionParser().evaluate("6d6d2");
        assertEquals(new BigDecimal(21), result.getValue());
    }

    public void testEvaluate_KeepWithMockRunData() throws ParserException {
        int[] rolls = {6, 2, 5, 4, 1, 6}; // keep the sixes and the 5
        setUpMockRunData(rolls);
        Result result = new ExpressionParser().evaluate("6d6k3");
        assertEquals(new BigDecimal(17), result.getValue());
    }

    public void testEvaluate_CountSuccessesWithMockRunData() throws ParserException {
        int[] rolls = {6, 2, 5, 4, 1, 6}; // count the 5 and 6s
        setUpMockRunData(rolls);
        Result result = new ExpressionParser().evaluate("6d6s5");
        assertEquals(new BigDecimal(3), result.getValue());
    }

    public void testEvaluate_ExplodingSuccessesWithMockRunData() throws ParserException {
        int[] rolls = {5, 4, 6, 1, 6, 6, 5};
        setUpMockRunData(rolls);
        Result result = new ExpressionParser().evaluate("4d6es8");
        assertEquals("Dice: 5, 4, 7, 17, Successes: 1", result.getValue());
    }

    public void testEvaluate_OpenWithMockRunData() throws ParserException {
        int[] rolls = {5, 6, 4, 6, 6, 2, 3};
        setUpMockRunData(rolls);
        Result result = new ExpressionParser().evaluate("4d6o");
        assertEquals("Dice: 5, 10, 14, 3, Maximum: 14", result.getValue());
    }

    public void testEvaluate_DropHighWithMockRunData() throws ParserException {
        int[] rolls = {6, 2, 5, 4, 1, 6}; // drop 6s and 5
        setUpMockRunData(rolls);
        Result result = new ExpressionParser().evaluate("6d6dh3");
        assertEquals(new BigDecimal(7), result.getValue());
    }

    public void testEvaluate_KeepLowWithMockRunData() throws ParserException {
        int[] rolls = {6, 2, 5, 4, 1, 6}; // keep the 1 and 2
        setUpMockRunData(rolls);
        Result result = new ExpressionParser().evaluate("6d6kl2");
        assertEquals(new BigDecimal(3), result.getValue());
    }

    public void testEvaluate_FudgeRollWithMockRunData() throws ParserException {
        int[] rolls = {1, 2, 2, 3}; // fudge dice are weird - they're shifted d3s to equal -1, 0, 1
        setUpMockRunData(rolls);
        Result result = new ExpressionParser().evaluate("4df");
        assertEquals(BigDecimal.ZERO, result.getValue());
    }
}
