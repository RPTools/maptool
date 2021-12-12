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
package net.rptools.maptool.model.gamedata.data;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import net.rptools.maptool.model.gamedata.InvalidDataOperation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JsonObjectDataValueTest {

  private JsonObjectDataValue jsonObjectValue1;
  private JsonObjectDataValue jsonObjectValue2;
  private JsonObjectDataValue jsonObjectValue3;
  private JsonObjectDataValue jsonObjectValue4;

  @BeforeEach
  void setUp() {
    var jsonA1 = new JsonObject();
    jsonA1.addProperty("t1", "test1");
    jsonObjectValue1 = new JsonObjectDataValue("test1", jsonA1);

    var jsonA2 = new JsonObject();
    jsonA2.addProperty("t1", 12);
    jsonA2.addProperty("t3", "test3");
    jsonObjectValue2 = new JsonObjectDataValue("test2", jsonA2);

    var jsonA3 = new JsonObject();
    jsonObjectValue3 = new JsonObjectDataValue("test3", jsonA3);

    jsonObjectValue4 = new JsonObjectDataValue("test4");
  }

  @Test
  void getName() {
    assertAll(
        () -> assertEquals("test1", jsonObjectValue1.getName()),
        () -> assertEquals("test2", jsonObjectValue2.getName()),
        () -> assertEquals("test3", jsonObjectValue3.getName()),
        () -> assertEquals("test4", jsonObjectValue4.getName()));
  }

  @Test
  void getDataType() {
    assertAll(
        () -> assertEquals(DataType.JSON_OBJECT, jsonObjectValue1.getDataType()),
        () -> assertEquals(DataType.JSON_OBJECT, jsonObjectValue2.getDataType()),
        () -> assertEquals(DataType.JSON_OBJECT, jsonObjectValue3.getDataType()),
        () -> assertEquals(DataType.JSON_OBJECT, jsonObjectValue4.getDataType()));
  }

  @Test
  void canBeConvertedTo() {
    assertAll(
        () -> assertFalse(jsonObjectValue1.canBeConvertedTo(DataType.BOOLEAN)),
        () -> assertFalse(jsonObjectValue2.canBeConvertedTo(DataType.BOOLEAN)),
        () -> assertFalse(jsonObjectValue3.canBeConvertedTo(DataType.BOOLEAN)),
        () -> assertFalse(jsonObjectValue4.canBeConvertedTo(DataType.BOOLEAN)),
        () -> assertFalse(jsonObjectValue1.canBeConvertedTo(DataType.LONG)),
        () -> assertFalse(jsonObjectValue2.canBeConvertedTo(DataType.LONG)),
        () -> assertFalse(jsonObjectValue3.canBeConvertedTo(DataType.LONG)),
        () -> assertFalse(jsonObjectValue4.canBeConvertedTo(DataType.LONG)),
        () -> assertFalse(jsonObjectValue1.canBeConvertedTo(DataType.DOUBLE)),
        () -> assertFalse(jsonObjectValue2.canBeConvertedTo(DataType.DOUBLE)),
        () -> assertFalse(jsonObjectValue3.canBeConvertedTo(DataType.DOUBLE)),
        () -> assertFalse(jsonObjectValue4.canBeConvertedTo(DataType.DOUBLE)),
        () -> assertFalse(jsonObjectValue1.canBeConvertedTo(DataType.STRING)),
        () -> assertFalse(jsonObjectValue2.canBeConvertedTo(DataType.STRING)),
        () -> assertFalse(jsonObjectValue3.canBeConvertedTo(DataType.STRING)),
        () -> assertFalse(jsonObjectValue4.canBeConvertedTo(DataType.STRING)),
        () -> assertFalse(jsonObjectValue1.canBeConvertedTo(DataType.JSON_ARRAY)),
        () -> assertFalse(jsonObjectValue2.canBeConvertedTo(DataType.JSON_ARRAY)),
        () -> assertFalse(jsonObjectValue3.canBeConvertedTo(DataType.JSON_ARRAY)),
        () -> assertFalse(jsonObjectValue4.canBeConvertedTo(DataType.JSON_ARRAY)),
        () -> assertTrue(jsonObjectValue1.canBeConvertedTo(DataType.JSON_OBJECT)),
        () -> assertTrue(jsonObjectValue2.canBeConvertedTo(DataType.JSON_OBJECT)),
        () -> assertTrue(jsonObjectValue3.canBeConvertedTo(DataType.JSON_OBJECT)),
        () -> assertFalse(jsonObjectValue4.canBeConvertedTo(DataType.JSON_OBJECT)));
  }

  @Test
  void asLong() {
    assertAll(
        () -> assertThrows(InvalidDataOperation.class, () -> jsonObjectValue1.asLong()),
        () -> assertThrows(InvalidDataOperation.class, () -> jsonObjectValue2.asLong()),
        () -> assertThrows(InvalidDataOperation.class, () -> jsonObjectValue3.asLong()),
        () -> assertThrows(InvalidDataOperation.class, () -> jsonObjectValue4.asLong()));
  }

  @Test
  void asDouble() {
    assertAll(
        () -> assertThrows(InvalidDataOperation.class, () -> jsonObjectValue1.asDouble()),
        () -> assertThrows(InvalidDataOperation.class, () -> jsonObjectValue2.asDouble()),
        () -> assertThrows(InvalidDataOperation.class, () -> jsonObjectValue3.asDouble()),
        () -> assertThrows(InvalidDataOperation.class, () -> jsonObjectValue4.asDouble()));
  }

  @Test
  void asString() {
    assertAll(
        () -> assertThrows(InvalidDataOperation.class, () -> jsonObjectValue1.asString()),
        () -> assertThrows(InvalidDataOperation.class, () -> jsonObjectValue2.asString()),
        () -> assertThrows(InvalidDataOperation.class, () -> jsonObjectValue3.asString()),
        () -> assertThrows(InvalidDataOperation.class, () -> jsonObjectValue4.asString()));
  }

  @Test
  void asBoolean() {
    assertAll(
        () -> assertThrows(InvalidDataOperation.class, () -> jsonObjectValue1.asBoolean()),
        () -> assertThrows(InvalidDataOperation.class, () -> jsonObjectValue2.asBoolean()),
        () -> assertThrows(InvalidDataOperation.class, () -> jsonObjectValue3.asBoolean()),
        () -> assertThrows(InvalidDataOperation.class, () -> jsonObjectValue4.asBoolean()));
  }

  @Test
  void asJsonArray() {
    assertAll(
        () -> assertThrows(InvalidDataOperation.class, () -> jsonObjectValue1.asJsonArray()),
        () -> assertThrows(InvalidDataOperation.class, () -> jsonObjectValue2.asJsonArray()),
        () -> assertThrows(InvalidDataOperation.class, () -> jsonObjectValue3.asJsonArray()),
        () -> assertThrows(InvalidDataOperation.class, () -> jsonObjectValue4.asJsonArray()));
  }

  @Test
  void asJsonObject() {
    assertAll(
        () -> assertEquals(jsonObjectValue1.asJsonObject(), jsonObjectValue1.asJsonObject()),
        () -> assertEquals("test1", jsonObjectValue1.asJsonObject().get("t1").getAsString()),
        () -> assertEquals(12, jsonObjectValue2.asJsonObject().get("t1").getAsLong()),
        () -> assertEquals("test3", jsonObjectValue2.asJsonObject().get("t3").getAsString()),
        () -> assertEquals(0, jsonObjectValue3.asJsonObject().size()),
        () -> assertThrows(InvalidDataOperation.class, () -> jsonObjectValue4.asJsonObject()));
  }

  @Test
  void isUndefined() {
    assertAll(
        () -> assertFalse(jsonObjectValue1.isUndefined()),
        () -> assertFalse(jsonObjectValue2.isUndefined()),
        () -> assertFalse(jsonObjectValue3.isUndefined()),
        () -> assertTrue(jsonObjectValue4.isUndefined()));
  }
}
