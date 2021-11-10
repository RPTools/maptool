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

import com.google.gson.JsonArray;
import net.rptools.maptool.model.gamedata.InvalidDataOperation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JsonArrayDataValueTest {

  private JsonArrayDataValue jsonArrayValue1;
  private JsonArrayDataValue jsonArrayValue2;
  private JsonArrayDataValue jsonArrayValue3;
  private JsonArrayDataValue jsonArrayValue4;

  @BeforeEach
  void setUp() {
    var jsonA1 = new JsonArray();
    jsonA1.add(5);
    jsonArrayValue1 = new JsonArrayDataValue("test1", jsonA1);

    var jsonA2 = new JsonArray();
    jsonA2.add("test");
    jsonArrayValue2 = new JsonArrayDataValue("test2", jsonA2);

    var jsonA3 = new JsonArray();
    jsonArrayValue3 = new JsonArrayDataValue("test3", jsonA3);

    jsonArrayValue4 = new JsonArrayDataValue("test4");
  }

  @Test
  void getName() {
    assertAll(
        () -> assertEquals("test1", jsonArrayValue1.getName()),
        () -> assertEquals("test2", jsonArrayValue2.getName()),
        () -> assertEquals("test3", jsonArrayValue3.getName()),
        () -> assertEquals("test4", jsonArrayValue4.getName()));
  }

  @Test
  void getDataType() {
    assertAll(
        () -> assertEquals(DataType.JSON_ARRAY, jsonArrayValue1.getDataType()),
        () -> assertEquals(DataType.JSON_ARRAY, jsonArrayValue2.getDataType()),
        () -> assertEquals(DataType.JSON_ARRAY, jsonArrayValue3.getDataType()),
        () -> assertEquals(DataType.JSON_ARRAY, jsonArrayValue4.getDataType()));
  }

  @Test
  void canBeConvertedTo() {
    assertAll(
        () -> assertFalse(jsonArrayValue1.canBeConvertedTo(DataType.BOOLEAN)),
        () -> assertFalse(jsonArrayValue2.canBeConvertedTo(DataType.BOOLEAN)),
        () -> assertFalse(jsonArrayValue3.canBeConvertedTo(DataType.BOOLEAN)),
        () -> assertFalse(jsonArrayValue4.canBeConvertedTo(DataType.BOOLEAN)),
        () -> assertFalse(jsonArrayValue1.canBeConvertedTo(DataType.LONG)),
        () -> assertFalse(jsonArrayValue2.canBeConvertedTo(DataType.LONG)),
        () -> assertFalse(jsonArrayValue3.canBeConvertedTo(DataType.LONG)),
        () -> assertFalse(jsonArrayValue4.canBeConvertedTo(DataType.LONG)),
        () -> assertFalse(jsonArrayValue1.canBeConvertedTo(DataType.DOUBLE)),
        () -> assertFalse(jsonArrayValue2.canBeConvertedTo(DataType.DOUBLE)),
        () -> assertFalse(jsonArrayValue3.canBeConvertedTo(DataType.DOUBLE)),
        () -> assertFalse(jsonArrayValue4.canBeConvertedTo(DataType.DOUBLE)),
        () -> assertFalse(jsonArrayValue1.canBeConvertedTo(DataType.STRING)),
        () -> assertFalse(jsonArrayValue2.canBeConvertedTo(DataType.STRING)),
        () -> assertFalse(jsonArrayValue3.canBeConvertedTo(DataType.STRING)),
        () -> assertFalse(jsonArrayValue4.canBeConvertedTo(DataType.STRING)),
        () -> assertTrue(jsonArrayValue1.canBeConvertedTo(DataType.JSON_ARRAY)),
        () -> assertTrue(jsonArrayValue2.canBeConvertedTo(DataType.JSON_ARRAY)),
        () -> assertTrue(jsonArrayValue3.canBeConvertedTo(DataType.JSON_ARRAY)),
        () -> assertFalse(jsonArrayValue4.canBeConvertedTo(DataType.JSON_ARRAY)),
        () -> assertFalse(jsonArrayValue1.canBeConvertedTo(DataType.JSON_OBJECT)),
        () -> assertFalse(jsonArrayValue2.canBeConvertedTo(DataType.JSON_OBJECT)),
        () -> assertFalse(jsonArrayValue3.canBeConvertedTo(DataType.JSON_OBJECT)),
        () -> assertFalse(jsonArrayValue4.canBeConvertedTo(DataType.JSON_OBJECT)));
  }

  @Test
  void asLong() {
    assertAll(
        () -> assertThrows(InvalidDataOperation.class, () -> jsonArrayValue1.asLong()),
        () -> assertThrows(InvalidDataOperation.class, () -> jsonArrayValue2.asLong()),
        () -> assertThrows(InvalidDataOperation.class, () -> jsonArrayValue3.asLong()),
        () -> assertThrows(InvalidDataOperation.class, () -> jsonArrayValue4.asLong()));
  }

  @Test
  void asDouble() {
    assertAll(
        () -> assertThrows(InvalidDataOperation.class, () -> jsonArrayValue1.asDouble()),
        () -> assertThrows(InvalidDataOperation.class, () -> jsonArrayValue2.asDouble()),
        () -> assertThrows(InvalidDataOperation.class, () -> jsonArrayValue3.asDouble()),
        () -> assertThrows(InvalidDataOperation.class, () -> jsonArrayValue4.asDouble()));
  }

  @Test
  void asString() {
    assertAll(
        () -> assertThrows(InvalidDataOperation.class, () -> jsonArrayValue1.asString()),
        () -> assertThrows(InvalidDataOperation.class, () -> jsonArrayValue2.asString()),
        () -> assertThrows(InvalidDataOperation.class, () -> jsonArrayValue3.asString()),
        () -> assertThrows(InvalidDataOperation.class, () -> jsonArrayValue4.asString()));
  }

  @Test
  void asBoolean() {
    assertAll(
        () -> assertThrows(InvalidDataOperation.class, () -> jsonArrayValue1.asBoolean()),
        () -> assertThrows(InvalidDataOperation.class, () -> jsonArrayValue2.asBoolean()),
        () -> assertThrows(InvalidDataOperation.class, () -> jsonArrayValue3.asBoolean()),
        () -> assertThrows(InvalidDataOperation.class, () -> jsonArrayValue4.asBoolean()));
  }

  @Test
  void asJsonArray() {
    assertAll(
        () -> assertEquals(1, jsonArrayValue1.asJsonArray().size()),
        () -> assertEquals(5L, jsonArrayValue1.asJsonArray().get(0).getAsLong()),
        () -> assertEquals(1, jsonArrayValue2.asJsonArray().size()),
        () -> assertEquals("test", jsonArrayValue2.asJsonArray().get(0).getAsString()),
        () -> assertEquals(0, jsonArrayValue3.asJsonArray().size()),
        () -> assertThrows(InvalidDataOperation.class, () -> jsonArrayValue4.asJsonArray()));
  }

  @Test
  void asJsonObject() {
    assertAll(
        () -> assertThrows(InvalidDataOperation.class, () -> jsonArrayValue1.asJsonObject()),
        () -> assertThrows(InvalidDataOperation.class, () -> jsonArrayValue2.asJsonObject()),
        () -> assertThrows(InvalidDataOperation.class, () -> jsonArrayValue3.asJsonObject()),
        () -> assertThrows(InvalidDataOperation.class, () -> jsonArrayValue4.asJsonObject()));
  }

  @Test
  void isUndefined() {
    assertAll(
        () -> assertFalse(jsonArrayValue1.isUndefined()),
        () -> assertFalse(jsonArrayValue2.isUndefined()),
        () -> assertFalse(jsonArrayValue3.isUndefined()),
        () -> assertTrue(jsonArrayValue4.isUndefined()));
  }
}
