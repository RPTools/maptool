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
 * <https://www.gnu.org/licenses/> and specifically the Affero license
 * text at <https://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.model;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.macro.MacroLocationFactory;
import net.rptools.maptool.model.permissions.Permissions;
import net.rptools.maptool.server.proto.TokenPropertyDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TokenPropertiesTest {

  MapToolVariableResolver variableResolver;
  List<TokenProperty> propsList;
  Token testToken;

  MacroLocationFactory factory;

  @BeforeEach
  public void setUp() {
    propsList = new ArrayList<>();
    propsList.add(new TokenProperty("prop1", null, null, true, false, Permissions.NONE, "10"));
    propsList.add(
        new TokenProperty("prop2", null, null, false, false, Permissions.ALL, "{prop2=prop1}"));
    propsList.add(
        new TokenProperty(
            "jsonObj1",
            null,
            null,
            false,
            false,
            Permissions.ALL,
            "{\"sampleKey\": 5, \"otherKey\": \"theValue\"}"));
    propsList.add(
        new TokenProperty(
            "jsonObj2", null, null, false, false, Permissions.ALL, "{\"prop3\"=other}"));
    propsList.add(
        new TokenProperty("jsonObj3", null, null, false, false, Permissions.ALL, "{prop3:other}"));
    propsList.add(
        new TokenProperty("jsonArr1", null, null, false, false, Permissions.ALL, "[4, 3]"));
    propsList.add(
        new TokenProperty("plainStr1", null, null, false, false, Permissions.ALL, "justAString"));
    propsList.add(
        new TokenProperty(
            "badJson", null, null, false, false, Permissions.ALL, "{\"a\": 1}{\"b\": 2}"));
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
    factory = MacroLocationFactory.getInstance();
    var loc = factory.createTokenLocation("test", testToken);
    MapTool.getParser().enterContext("test", loc, true);
  }

  /**
   * Simple test to make sure constructors don't throw exceptions, and the copy constructor fields
   * are the same
   */
  @Test
  public void testConstructors() {
    assertDoesNotThrow(() -> new TokenProperty());
    assertDoesNotThrow(() -> new TokenProperty("Attribute"));
    assertDoesNotThrow(() -> new TokenProperty("attribute", "ATT", "Attribute", Permissions.ALL));
    assertDoesNotThrow(
        () ->
            new TokenProperty(
                "attribute", "ATT", "Attribute", null, true, Permissions.ALL, "value"));
    assertDoesNotThrow(
        () ->
            new TokenProperty(
                "attribute", "ATT", "Attribute", true, null, Permissions.ALL, "value"));
    assertDoesNotThrow(
        () ->
            new TokenProperty(
                "attribute", "ATT", "Attribute", true, false, Permissions.ALL, "value"));

    TokenProperty prop =
        new TokenProperty("attribute", "ATT", "Attribute", true, true, Permissions.ALL, "value");
    TokenProperty property = new TokenProperty(prop);
    assertTrue(
        Objects.equals(prop.getName(), property.getName())
            && Objects.equals(prop.getShortName(), property.getShortName())
            && Objects.equals(prop.getDisplayName(), property.getDisplayName())
            && Objects.equals(prop.getDefaultValue(), property.getDefaultValue())
            && Objects.equals(prop.getEditorEditPermission(), property.getEditorEditPermission())
            && Objects.equals(prop.getEditorViewPermission(), property.getEditorViewPermission())
            && Objects.equals(
                prop.getStatSheetViewPermission(), property.getStatSheetViewPermission()));
  }

  /** Test inferred Permissions are present and correct */
  @Test
  void testPermissions() {
    // constructor providing null values for permissions
    TokenProperty prop =
        new TokenProperty("attribute", "ATT", "Attribute", null, null, null, "value");
    assertEquals(TokenProperty.EDITOR_VIEW_DEFAULT, prop.getEditorViewPermission());
    assertEquals(TokenProperty.EDITOR_EDIT_DEFAULT, prop.getEditorEditPermission());
    assertEquals(TokenProperty.VIEW_STATSHEET_DEFAULT, prop.getStatSheetViewPermission());

    // player view/edit allowed, sheet visibility non-null
    prop =
        new TokenProperty(
            "attribute", "ATT", "Attribute", true, true, Permissions.OPPONENT_ONLY, "value");
    assertEquals(Permissions.OWNER, prop.getEditorViewPermission());
    assertEquals(Permissions.OWNER, prop.getEditorEditPermission());
    assertEquals(Permissions.OPPONENT_ONLY, prop.getStatSheetViewPermission());

    // player view/edit disallowed
    prop =
        new TokenProperty(
            "attribute", "ATT", "Attribute", false, false, Permissions.OPPONENT_ONLY, "value");
    assertEquals(Permissions.GM, prop.getEditorViewPermission());
    assertEquals(Permissions.GM, prop.getEditorEditPermission());
  }

  /** Test DTO output matches input */
  @Test
  public void testDto() {
    assertDoesNotThrow(
        () -> {
          TokenProperty prop =
              new TokenProperty(
                  "attribute", "ATT", "Attribute", true, true, Permissions.ALL, "value");
          TokenPropertyDto dto = prop.toDto();
          TokenProperty property = TokenProperty.fromDto(dto);
          assertTrue(
              Objects.equals(prop.getName(), property.getName())
                  && Objects.equals(prop.getShortName(), property.getShortName())
                  && Objects.equals(prop.getDisplayName(), property.getDisplayName())
                  && Objects.equals(prop.getDefaultValue(), property.getDefaultValue())
                  && Objects.equals(
                      prop.getEditorEditPermission(), property.getEditorEditPermission())
                  && Objects.equals(
                      prop.getEditorViewPermission(), property.getEditorViewPermission())
                  && Objects.equals(
                      prop.getStatSheetViewPermission(), property.getStatSheetViewPermission()));
        });
  }

  /** Tests the copy constructor used in readResolve() for legacy field conversion to Permissions */
  @Test
  public void testLegacyCompatible() {
    TokenProperty prop = new TokenProperty("attribute", "ATT", "Attribute", Permissions.ALL);
    TokenProperty property = new TokenProperty();
    String[] oldFields = {"highPriority", "ownerOnly", "gmOnly"};
    String[] newFields = {
      "editorViewPermission", "editorEditPermission", "statSheetViewPermission"
    };
    try {
      // nullify the new
      for (String fieldName : newFields) {
        Field field = prop.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(prop, null);
      }
      // set the old
      for (String fieldName : oldFields) {
        Field field = prop.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(prop, true);
        property = new TokenProperty(prop);
        switch (fieldName) {
          case "highPriority" ->
              assertEquals(Permissions.ALL, property.getStatSheetViewPermission());
          case "ownerOnly" -> {
            assertEquals(Permissions.OWNER, property.getStatSheetViewPermission());
            field.set(prop, false);
          }
          case "gmOnly" -> assertEquals(Permissions.GM, property.getStatSheetViewPermission());
        }
      }
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
    assertEquals(TokenProperty.EDITOR_EDIT_DEFAULT, property.getEditorEditPermission());
    assertEquals(TokenProperty.EDITOR_VIEW_DEFAULT, property.getEditorViewPermission());
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
    assertInstanceOf(String.class, val);
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
