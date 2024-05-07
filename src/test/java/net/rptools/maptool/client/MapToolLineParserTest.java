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
package net.rptools.maptool.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;

import java.math.BigDecimal;
import java.util.Collections;
import net.rptools.dicelib.expression.Result;
import net.rptools.maptool.model.CampaignFactory;
import net.rptools.maptool.model.MacroButtonProperties;
import net.rptools.maptool.model.Token;
import net.rptools.parser.ParserException;
import org.junit.jupiter.api.Test;

public class MapToolLineParserTest {

  private static final MapToolLineParser parser = MapTool.getParser();

  private Result parseExpression(
      String expression,
      boolean makeDeterministic,
      Token tokenInContext,
      MapToolVariableResolver resolver)
      throws ParserException {
    return parser.parseExpression(
        resolver == null ? new MapToolVariableResolver(null) : resolver,
        tokenInContext,
        expression,
        makeDeterministic);
  }

  private void assertEqualsIgnoreSpaces(String expected, String actual) {
    assertEquals(expected.replaceAll(" ", ""), actual.replaceAll(" ", ""));
  }

  private void assertMatches(String expected, String actual) {
    expected = expected.replace("(", "\\(").replace(")", "\\)");
    assertLinesMatch(Collections.singletonList(expected), Collections.singletonList(actual));
  }

  private String parseLine(String line, Token tokenInContext, MapToolVariableResolver resolver)
      throws ParserException {
    MapToolMacroContext ctx = new MapToolMacroContext("test", line, true);
    return parser.parseLine(
        resolver == null ? new MapToolVariableResolver(tokenInContext) : resolver,
        tokenInContext,
        line,
        ctx);
  }

  @Test
  public void testMacros() throws ParserException {

    // no branch
    assertEquals("a var gives 1", parseLine("a var gives [r: a = 1]", null, null));

    // branch type if
    assertEquals(
        "a condition leads to 1",
        parseLine("a condition leads to [r: if(1 == 1, 1, 0)]", null, null));
    assertEquals(
        "a d10 roll is always a No Critical Hit",
        parseLine(
            "a d10 roll[h: d20roll = 1d10] is always a [r,if(d20roll == 20): output = \"Critical Hit\"; output = \"No Critical Hit\"]",
            null,
            null));
    assertEquals(
        "a hidden evaluation gives nothing",
        parseLine("a hidden evaluation gives nothing[h: if(1 == 1, 1, 0)]", null, null));
    // expanded rolls contain MessagePanel's ASCII control characters that mark of roll information
    // (line 182)
    assertMatches(
        "expanded roll shows ...if(1 == 1, 1, 0) = 1.",
        parseLine("expanded roll shows [e: if(1 == 1, 1, 0)]", null, null));

    // branch type count loop
    assertEquals(
        "a loop yields hit, hit, hit",
        parseLine("a loop yields [r, count(3): \"hit\" ]", null, null));

    // branch type switch
    assertEquals(
        "switched is 1",
        parseLine(
            "switched is [h:a=1][r,switch(a): case 0: 0; case 1: 1; case 2: 2; default: -1]",
            null,
            null));

    // eval macro
    assertEquals("got evaluated", parseLine("got [r: evalMacro('[r:\"evaluated\"]')]", null, null));

    // macro
    MacroButtonProperties macro = new MacroButtonProperties(0);
    macro.setLabel("testMacro");
    macro.setCommand("[r:macro.args]");

    Token token = new Token();
    token.saveMacro(macro);

    assertEquals(
        "hello world", parseLine("hello [MACRO(\"testMacro@TOKEN\"): \"world\"]", token, null));
  }

  @Test
  public void testMacroChangesTokenProperty() throws ParserException {
    // We need the campaign to have the "Strength" property defined.
    MapTool.getClient().setCampaign(CampaignFactory.createBasicCampaign());

    Token token = new Token();
    token.setProperty("Strength", "1");

    MapToolVariableResolver res =
        new MapToolVariableResolver(token) {
          @Override
          protected void updateTokenProperty(
              Token tokenToUpdate, String varToUpdate, String valueToSet) {
            // variable resolver will try to send a server command which will fail in unittest
            // catch the event and do not delegate to super implementation. Fake a set here instead
            assertEquals(token, tokenToUpdate);
            assertEquals("1", token.getProperty("Strength"));
            assertEquals("Strength", varToUpdate);
            assertEquals("6", valueToSet);
            token.setProperty("Strength", "6");
          }
        };

    Result result = parseExpression("Strength = 6", false, token, res);

    assertEquals(new BigDecimal(6), result.getValue());
    assertEquals("6", token.getProperty("Strength"));
  }

  @Test
  public void testRollBranching() throws ParserException {

    // no branch
    String macro = "[r: 1]";
    assertEquals("1", parseLine(macro, null, null));

    // no branch code
    macro = "[r, code: {  1 }]";
    assertEquals("1", parseLine(macro, null, null));

    // if
    macro = "[r, if(1 == 1): \"true\"; \"false\"]";
    assertEquals("true", parseLine(macro, null, null));

    // if CODE
    macro =
        "[r, if(1 == 1), code: {"
            + "  [r:\"still\"] [r:\"true\"]"
            + "};"
            + "{"
            + "  [\"always false\"]"
            + "}]";
    assertEquals("still true", parseLine(macro, null, null));

    // switch
    MapToolVariableResolver res = new MapToolVariableResolver(null);
    res.setVariable("a", "2");
    macro =
        "[r, switch(a):"
            + "case 1: \"one\";"
            + "case 2: \"two\";"
            + "case 3: \"three\";"
            + "default: \"default\"]";
    assertEquals("two", parseLine(macro, null, res));
    res.setVariable("a", "100");
    macro =
        "[r, switch(a):"
            + "case 1: \"one\";"
            + "case 2: \"two\";"
            + "case 3: \"three\";"
            + "default: \"default\"]";
    assertEquals("default", parseLine(macro, null, res));

    // switch CODE
    res.setVariable("a", "3");
    macro =
        "[h, switch(a), code:"
            + "case 1: { [a = \"one\"] };"
            + "case 2: { [a = \"two\"] };"
            + "case 3: { [a = \"three\"] };"
            + "default: { [a = \"default\"] }]";
    parseLine(macro, null, res);
    assertEquals("three", res.getVariable("a"));
  }

  @Test
  public void testConditional() throws ParserException {

    // if , deterministic
    String ifcondition = "if(1==1,\"match\",\"no match\")";
    Result result = parseExpression(ifcondition, true, null, null);
    assertEquals(result.getValue(), "match");
    assertEquals(result.getDetailExpression(), "match");

    // if, non deterministic
    ifcondition = "if(1==1,\"match\",\"no match\")";
    result = parseExpression(ifcondition, false, null, null);
    assertEquals("match", result.getValue());
    assertEqualsIgnoreSpaces(ifcondition, result.getDetailExpression());

    // if, deterministic
    ifcondition = "if(1<2,\"match\",\"no match\")";
    result = parseExpression(ifcondition, true, null, null);
    assertEquals("match", result.getValue());
    assertEquals("match", result.getDetailExpression());

    // if, non deterministic, the result is equal to deterministic but detailed expression is not
    // resolved to a deterministic value
    ifcondition = "if(1<2,\"match\",\"no match\")";
    result = parseExpression(ifcondition, false, null, null);
    assertEquals("match", result.getValue());
    assertEqualsIgnoreSpaces(ifcondition, result.getDetailExpression());
  }

  @Test
  public void testValue() throws ParserException {

    MapToolVariableResolver resolver = new MapToolVariableResolver(null);

    // "match" + "this", deterministic
    Result result = parseExpression("\"match\"+\"this\"", true, null, resolver);
    assertEquals("matchthis", result.getValue());
    assertEquals("\"match\" + \"this\"", result.getDetailExpression());

    // "match" + "this", non deterministic
    result = parseExpression("\"match\"+\"this\"", false, null, resolver);
    assertEquals("matchthis", result.getValue());
    assertEquals("\"match\" + \"this\"", result.getDetailExpression());
  }

  @Test
  public void testExpression() throws ParserException {

    MapToolVariableResolver resolver = new MapToolVariableResolver(null);

    // a = 1, deterministic
    Result result = parseExpression("a = 1", true, null, resolver);
    assertEquals(result.getValue(), BigDecimal.ONE);
    assertEquals(resolver.getVariable("a"), BigDecimal.ONE);
    assertEquals(result.getDetailExpression(), "a = 1");

    // a = a * 10, deterministic
    result = parseExpression("a = a * 10", true, null, resolver);
    assertEquals(result.getValue(), BigDecimal.TEN);
    assertEquals(resolver.getVariable("a"), BigDecimal.TEN);
    assertEquals(result.getDetailExpression(), "a = (1 * 10)");

    // a = 1, non-deterministic
    result = parseExpression("a = 1", false, null, resolver);
    assertEquals(result.getValue(), BigDecimal.ONE);
    assertEquals(resolver.getVariable("a"), BigDecimal.ONE);
    assertEquals(result.getDetailExpression(), "a = 1");

    // a = a * 10
    result = parseExpression("a = a * 10", false, null, resolver);
    assertEquals(result.getValue(), BigDecimal.TEN);
    assertEquals(resolver.getVariable("a"), BigDecimal.TEN);
    assertEquals(result.getDetailExpression(), "a = (a * 10)");
  }
}
