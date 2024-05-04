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
package net.rptools.maptool.model;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TokenPropertiesTest {

  MapToolVariableResolver variableResolver;
  List<TokenProperty> propsList;
  Token testToken;

  @BeforeEach
  public void setUp() {
    propsList = new ArrayList<>();
    propsList.add(new TokenProperty("prop1", null, true, false, false, "10"));
    propsList.add(new TokenProperty("prop2", null, true, false, false, "{prop2=prop1}"));
    propsList.add(
        new TokenProperty(
            "jsonObj1",
            null,
            true,
            false,
            false,
            "{\"sampleKey\": 5, \"otherKey\": \"theValue\"}"));
    propsList.add(new TokenProperty("jsonObj2", null, true, false, false, "{\"prop3\"=other}"));
    propsList.add(new TokenProperty("jsonObj3", null, true, false, false, "{prop3:other}"));
    propsList.add(new TokenProperty("jsonArr1", null, true, false, false, "[4, 3]"));
    propsList.add(new TokenProperty("plainStr1", null, true, false, false, "justAString"));
    propsList.add(new TokenProperty("badJson", null, true, false, false, "{\"a\": 1}{\"b\": 2}"));
    MapTool.getCampaign().putTokenType("testType", propsList);

    testToken = new Token();
    testToken.setPropertyType("testType");

    variableResolver =
        new MapToolVariableResolver(testToken) {
          @Override
          protected void updateTokenProperty(
              Token tokenToUpdate, String varToUpdate, String valueToSet) {
            tokenToUpdate.setProperty(varToUpdate, valueToSet);
          }
        };

    MapTool.getParser().enterContext("test", "testToken", true);
  }

  /**
   * Tests evaluation of a properly formed JSON object. This should be a much faster test than the
   * {@link #testJSONObjectLenient()} counterpart, since the initial strict evaluation should avoid
   * the need to try MT parsing.
   */
  @Test
  public void testJSONObjectStrict() {
    Object val = testToken.getEvaluatedProperty(variableResolver, "jsonObj1");
    JsonObject json = (JsonObject) val;
    assertAll(
        () -> assertTrue(json.isJsonObject()),
        () -> assertEquals(5, json.get("sampleKey").getAsInt()),
        () -> assertEquals("theValue", json.get("otherKey").getAsString()));
  }

  /**
   * Tests evaluation of a malformed, but unfortunately acceptable to Gson JSON object. This is
   * expected to be slower than {@link #testJSONObjectStrict()}, because the initial strict check
   * should NOT identify this as a JSON object. The full MT parser needs to be applied first, before
   * we begrudgingly allow the lenient Gson evaluator to call this a JSON object.
   */
  @Test
  public void testJSONObjectLenient() {
    Object jsonObj2 = testToken.getEvaluatedProperty(variableResolver, "jsonObj2");
    Object jsonObj3 = testToken.getEvaluatedProperty(variableResolver, "jsonObj3");

    JsonObject expected = new JsonObject();
    expected.addProperty("prop3", "other");

    assertEquals(expected, jsonObj2);
    assertEquals(expected, jsonObj3);
  }

  /**
   * Tests that the <code>{prop2=prop1}</code> assignment syntax is properly supported. This means
   * it should NOT be evaluated (leniently) as a JSON object, but instead executed as MTScript.
   */
  @Test
  public void testAssignmentInDefaultValue() {
    assertEquals("10", testToken.getEvaluatedProperty(variableResolver, "prop2"));

    // the point of the assignment is that after the first access, prop2 should no longer be
    // dependent on prop1
    testToken.setProperty("prop1", "newValue");
    assertEquals("10", testToken.getEvaluatedProperty(variableResolver, "prop2"));
  }

  @Test
  public void testJsonArray() {
    JsonElement elem = (JsonElement) testToken.getEvaluatedProperty(variableResolver, "jsonArr1");
    assertTrue(elem.isJsonArray());
  }

  @Test
  public void testPlainStr() {
    Object val = testToken.getEvaluatedProperty(variableResolver, "plainStr1");
    assertTrue(val instanceof String);
    assertEquals("justAString", val);
  }

  @Test
  public void testUnknownProperty() {
    assertEquals("", testToken.getEvaluatedProperty(variableResolver, "unknownProp"));
  }

  @Test
  public void testBadJsonReturnsAsString() {
    assertEquals(
        "{\"a\": 1}{\"b\": 2}", testToken.getEvaluatedProperty(variableResolver, "badJson"));
  }
}
