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
package net.rptools.maptool.client.functions;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.*;
import net.rptools.dicelib.expression.Result;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolLineParser;
import net.rptools.maptool.client.MapToolMacroContext;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.functions.json.JSONMacroFunctions;
import net.rptools.maptool.model.MacroButtonProperties;
import net.rptools.maptool.model.Token;
import net.rptools.parser.ParserException;
import org.junit.jupiter.api.*;

public class GetRolledTest {

  static MapToolLineParser parser;

  private static final String GET_NEW_ROLLS_STR = "getNewRolls()";
  private static final String GET_ALL_ROLLS_STR = "getRolled()";

  private static final String UDF_CONTENTS =
      String.join(
          "\n",
          "[h: priors = getNewRolls()]",
          "[h: 1d20]",
          "[h: rolls_1 = getNewRolls()]",
          "[h: 1d20]",
          "[h: rolls_2 = getNewRolls()]",
          "[r: macro.return = json.set('', 'priors', priors, 'rolls_1', rolls_1, 'rolls_2', rolls_2, 'all_rolls', getRolled())]");

  @BeforeAll
  private static void setUp() {
    parser = MapTool.getParser();
    parser.enterContext("test", "test", true);
  }

  @AfterEach
  private void resetParser() {
    parser.clearRolls();
  }

  private void verifyRolls(String modeExpr, int expectedSize, List<Integer> rolls)
      throws ParserException {
    Result result = parser.parseExpression(modeExpr, false);
    JsonArray arr = (JsonArray) result.getValue();
    assertEquals(expectedSize, rolls.size(), "Wrong number of rolls found");
    assertEquals(rolls.size(), arr.size(), "Number of rolls does not match");
    for (int i = 0; i < expectedSize; i++) {
      assertEquals(rolls.get(i).intValue(), arr.get(i).getAsInt(), "Roll list does not match");
    }
  }

  private void verifyNewRolls(int expectedSize, List<Integer> rolls) throws ParserException {
    verifyRolls(GET_NEW_ROLLS_STR, expectedSize, rolls);
  }

  private void verifyAllRolls(int expectedSize, List<Integer> rolls) throws ParserException {
    verifyRolls(GET_ALL_ROLLS_STR, expectedSize, rolls);
  }

  /**
   * Basic operation test, evaluates some rolls and verifies that getNewRolls() returns only dice
   * rolled since its last invocation, while getRolled() gets all dice rolled in this test.
   */
  @Test
  public void testBasicOperation() throws ParserException {
    List<Integer> mostRecentRolls;
    List<Integer> allRollsSoFar = new ArrayList<>();

    // start with 2 rolls back to back, so both getRolled() and getNewRolls() should contain all
    // dice
    mostRecentRolls = parser.parseExpression("2d6", false).getRolled();
    allRollsSoFar.addAll(mostRecentRolls);
    mostRecentRolls = parser.parseExpression("2d20k1", false).getRolled();
    allRollsSoFar.addAll(mostRecentRolls);
    verifyNewRolls(4, allRollsSoFar);
    verifyAllRolls(4, allRollsSoFar);

    // after this roll getNewRolls() should only have the most recent rolls, getRolled() should have
    // all dice
    mostRecentRolls = parser.parseExpression("3d6", false).getRolled();
    allRollsSoFar.addAll(mostRecentRolls);
    verifyNewRolls(3, mostRecentRolls);
    verifyAllRolls(7, allRollsSoFar);
  }

  /**
   * Creates a UDF that uses getNewRolls() and getRolled() internally, returning a JSON object
   * containing various rolls.
   */
  @Test
  public void testCallsWithinUDF() throws ParserException {
    MacroButtonProperties macro = new MacroButtonProperties(0);
    macro.setLabel("testUDF");
    macro.setCommand(UDF_CONTENTS);

    Token token = new Token();
    token.setName("testUDF");
    token.saveMacro(macro);

    MapToolVariableResolver resolver = new MapToolVariableResolver(token);
    parser.parseExpression(resolver, token, "defineFunction('testUDF', 'testUDF@TOKEN')", false);

    Result result = parser.parseExpression(resolver, token, "testUDF()", false);
    JsonObject json = (JsonObject) result.getValue();
    int roll1 = json.get("rolls_1").getAsInt();
    int roll2 = json.get("rolls_2").getAsInt();
    JsonArray arr = json.get("all_rolls").getAsJsonArray();
    assertEquals(2, arr.size(), "UDF internal getRolled had wrong size");
    assertEquals(roll1, arr.get(0).getAsInt());
    assertEquals(roll2, arr.get(1).getAsInt());
  }

  /**
   * Creates a UDF that rolls some dice internally, and checks that those rolls are included when
   * calling getNewRolls() and getRolled() later in the parent context.
   */
  @Test
  public void testCallsAroundUDF() throws ParserException {
    List<Integer> mostRecentRolls;
    List<Integer> allRollsSoFar = new ArrayList<>();
    MacroButtonProperties macro = new MacroButtonProperties(0);
    macro.setLabel("testUDF");
    macro.setCommand(UDF_CONTENTS);

    Token token = new Token();
    token.setName("testUDF");
    token.saveMacro(macro);

    MapToolVariableResolver resolver = new MapToolVariableResolver(token);
    parser.parseExpression(resolver, token, "defineFunction('testUDF', 'testUDF@TOKEN')", false);

    mostRecentRolls = parser.parseExpression("2d6", false).getRolled();
    allRollsSoFar.addAll(mostRecentRolls);
    verifyNewRolls(2, mostRecentRolls);
    verifyAllRolls(2, allRollsSoFar);

    Result result = parser.parseExpression(resolver, token, "testUDF()", false);
    JsonObject json = (JsonObject) result.getValue();
    assertEquals(
        0,
        json.get("priors").getAsJsonArray().size(),
        "internal getNewRolls() didn't access previous rolls");
    mostRecentRolls = result.getRolled();
    allRollsSoFar.addAll(mostRecentRolls);
    List<Integer> rollsFromJson = new ArrayList<>();
    for (JsonElement elem : json.get("all_rolls").getAsJsonArray())
      rollsFromJson.add(elem.getAsInt());
    assertEquals(
        rollsFromJson.size(),
        allRollsSoFar.size(),
        "result object should have as many rolls as UDF");
    for (int i = 0; i < rollsFromJson.size(); i++) {
      assertEquals(
          rollsFromJson.get(i), allRollsSoFar.get(i), "result object should match UDF rolls");
    }

    verifyNewRolls(0, Collections.emptyList()); // getNewRolls was called inside UDF, no rolls since
    verifyAllRolls(4, allRollsSoFar);
  }

  /**
   * json.objrolls() uses its own parser internally, but the rolls should still propagate up to the
   * parent context
   */
  @Test
  public void testJsonObjRolls() throws ParserException {
    List<Integer> mostRecentRolls;
    List<Integer> allRollsSoFar = new ArrayList<>();

    MapToolVariableResolver resolver = new MapToolVariableResolver(null);
    parser.parseExpression(resolver, null, "vNames = json.append('', 'henchman1')", false);
    parser.parseExpression(
        resolver,
        null,
        "vStats = json.append('', 'Str', 'Dex', 'Con', 'Wis', 'Int', 'Cha')",
        false);
    Result result =
        parser.parseExpression(resolver, null, "json.objrolls(vNames, vStats, '3d6')", false);
    mostRecentRolls = result.getRolled();
    allRollsSoFar.addAll(mostRecentRolls);
    verifyNewRolls(18, mostRecentRolls);
    verifyAllRolls(18, allRollsSoFar);
  }

  /** Tests when rolls are inside a macro called by roll option */
  @Test
  public void testMacroRollOption() throws ParserException {
    MacroButtonProperties macro = new MacroButtonProperties(0);
    macro.setLabel("testMacro");
    macro.setCommand(UDF_CONTENTS);

    Token token = new Token();
    token.setName("testToken");
    token.saveMacro(macro);

    MapToolVariableResolver resolver = new MapToolVariableResolver(token);
    String result =
        parser.parseLine(
            resolver,
            token,
            "[h: 1d6]\n[h, macro('testMacro@TOKEN'):'']\n[r: macro.return]",
            new MapToolMacroContext("test", "test", true));
    JsonObject json =
        JSONMacroFunctions.getInstance().asJsonElement(result.trim()).getAsJsonObject();
    assertEquals(
        1,
        json.get("priors").getAsJsonArray().size(),
        "internal getNewRolls() didn't access previous rolls");
    List<Integer> rollsFromJson = new ArrayList<>();
    for (JsonElement elem : json.get("all_rolls").getAsJsonArray())
      rollsFromJson.add(elem.getAsInt());

    verifyNewRolls(
        0, Collections.emptyList()); // getNewRolls was called inside macro, no rolls since
    verifyAllRolls(3, rollsFromJson); // 1 roll before, 2 rolls inside
  }

  /** ExecMacro shouldn't change roll retrieval - */
  @Test
  public void testExec() throws ParserException {
    String result =
        parser.parseLine(String.format("[h: 3d4]\n[r: execMacro(\"%s\")]", UDF_CONTENTS));
    JsonObject json =
        JSONMacroFunctions.getInstance().asJsonElement(result.trim()).getAsJsonObject();
    assertEquals(
        3,
        json.get("priors").getAsJsonArray().size(),
        "internal getNewRolls() didn't access previous rolls");
    List<Integer> rollsFromJson = new ArrayList<>();
    for (JsonElement elem : json.get("all_rolls").getAsJsonArray())
      rollsFromJson.add(elem.getAsInt());

    verifyNewRolls(
        0, Collections.emptyList()); // getNewRolls was called inside macro, no rolls since
    verifyAllRolls(5, rollsFromJson); // 3 rolls before, 2 rolls inside
  }

  @Test
  public void testEvalMacro() throws ParserException {
    String result =
        parser.parseLine(String.format("[h: 4d6]\n[r: evalMacro(\"%s\")]", UDF_CONTENTS));
    JsonObject json =
        JSONMacroFunctions.getInstance().asJsonElement(result.trim()).getAsJsonObject();
    assertEquals(
        4,
        json.get("priors").getAsJsonArray().size(),
        "internal getNewRolls() didn't access previous rolls");
    List<Integer> rollsFromJson = new ArrayList<>();
    for (JsonElement elem : json.get("all_rolls").getAsJsonArray())
      rollsFromJson.add(elem.getAsInt());

    verifyNewRolls(
        0, Collections.emptyList()); // getNewRolls was called inside macro, no rolls since
    verifyAllRolls(6, rollsFromJson); // 4 rolls before, 2 rolls inside
  }
}
