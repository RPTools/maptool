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

import net.rptools.maptool.model.gamedata.InvalidDataOperation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StringDataValueTest {

  private StringDataValue stringValue1;
  private StringDataValue stringValue2;
  private StringDataValue stringValue3;
  private StringDataValue stringValue4;
  private StringDataValue stringValue5;
  private StringDataValue stringValue6;
  private StringDataValue stringValue7;

  @BeforeEach
  void setUp() {
    stringValue1 = new StringDataValue("test1", "22");
    stringValue2 = new StringDataValue("test2", "0");
    stringValue3 = new StringDataValue("test3", "-2.3");
    stringValue4 = new StringDataValue("test4");
    stringValue5 = new StringDataValue("test5", "test");
    stringValue6 = new StringDataValue("test6", "true");
    stringValue7 = new StringDataValue("test7", "false");
  }

  @Test
  void getName() {
    assertAll(
        () -> assertEquals("test1", stringValue1.getName()),
        () -> assertEquals("test2", stringValue2.getName()),
        () -> assertEquals("test3", stringValue3.getName()),
        () -> assertEquals("test4", stringValue4.getName()),
        () -> assertEquals("test5", stringValue5.getName()),
        () -> assertEquals("test6", stringValue6.getName()),
        () -> assertEquals("test7", stringValue7.getName()));
  }

  @Test
  void getDataType() {
    assertAll(
        () -> assertEquals(DataType.STRING, stringValue1.getDataType()),
        () -> assertEquals(DataType.STRING, stringValue2.getDataType()),
        () -> assertEquals(DataType.STRING, stringValue3.getDataType()),
        () -> assertEquals(DataType.STRING, stringValue4.getDataType()),
        () -> assertEquals(DataType.STRING, stringValue5.getDataType()),
        () -> assertEquals(DataType.STRING, stringValue6.getDataType()),
        () -> assertEquals(DataType.STRING, stringValue7.getDataType()));
  }

  @Test
  void canBeConvertedTo() {
    assertAll(
        () -> assertTrue(stringValue1.canBeConvertedTo(DataType.BOOLEAN)),
        () -> assertTrue(stringValue2.canBeConvertedTo(DataType.BOOLEAN)),
        () -> assertTrue(stringValue3.canBeConvertedTo(DataType.BOOLEAN)),
        () -> assertFalse(stringValue4.canBeConvertedTo(DataType.BOOLEAN)),
        () -> assertFalse(stringValue5.canBeConvertedTo(DataType.BOOLEAN)),
        () -> assertTrue(stringValue6.canBeConvertedTo(DataType.BOOLEAN)),
        () -> assertTrue(stringValue7.canBeConvertedTo(DataType.BOOLEAN)),
        () -> assertTrue(stringValue1.canBeConvertedTo(DataType.LONG)),
        () -> assertTrue(stringValue2.canBeConvertedTo(DataType.LONG)),
        () -> assertTrue(stringValue3.canBeConvertedTo(DataType.LONG)),
        () -> assertFalse(stringValue4.canBeConvertedTo(DataType.LONG)),
        () -> assertFalse(stringValue5.canBeConvertedTo(DataType.LONG)),
        () -> assertFalse(stringValue6.canBeConvertedTo(DataType.LONG)),
        () -> assertFalse(stringValue7.canBeConvertedTo(DataType.LONG)),
        () -> assertTrue(stringValue1.canBeConvertedTo(DataType.DOUBLE)),
        () -> assertTrue(stringValue2.canBeConvertedTo(DataType.DOUBLE)),
        () -> assertTrue(stringValue3.canBeConvertedTo(DataType.DOUBLE)),
        () -> assertFalse(stringValue4.canBeConvertedTo(DataType.DOUBLE)),
        () -> assertFalse(stringValue5.canBeConvertedTo(DataType.DOUBLE)),
        () -> assertFalse(stringValue6.canBeConvertedTo(DataType.DOUBLE)),
        () -> assertFalse(stringValue7.canBeConvertedTo(DataType.DOUBLE)),
        () -> assertTrue(stringValue1.canBeConvertedTo(DataType.STRING)),
        () -> assertTrue(stringValue2.canBeConvertedTo(DataType.STRING)),
        () -> assertTrue(stringValue3.canBeConvertedTo(DataType.STRING)),
        () -> assertFalse(stringValue4.canBeConvertedTo(DataType.STRING)),
        () -> assertTrue(stringValue5.canBeConvertedTo(DataType.STRING)),
        () -> assertTrue(stringValue6.canBeConvertedTo(DataType.STRING)),
        () -> assertTrue(stringValue7.canBeConvertedTo(DataType.STRING)),
        () -> assertTrue(stringValue1.canBeConvertedTo(DataType.JSON_ARRAY)),
        () -> assertTrue(stringValue2.canBeConvertedTo(DataType.JSON_ARRAY)),
        () -> assertTrue(stringValue3.canBeConvertedTo(DataType.JSON_ARRAY)),
        () -> assertFalse(stringValue4.canBeConvertedTo(DataType.JSON_ARRAY)),
        () -> assertTrue(stringValue5.canBeConvertedTo(DataType.JSON_ARRAY)),
        () -> assertTrue(stringValue6.canBeConvertedTo(DataType.JSON_ARRAY)),
        () -> assertTrue(stringValue7.canBeConvertedTo(DataType.JSON_ARRAY)),
        () -> assertFalse(stringValue1.canBeConvertedTo(DataType.JSON_OBJECT)),
        () -> assertFalse(stringValue2.canBeConvertedTo(DataType.JSON_OBJECT)),
        () -> assertFalse(stringValue3.canBeConvertedTo(DataType.JSON_OBJECT)),
        () -> assertFalse(stringValue4.canBeConvertedTo(DataType.JSON_OBJECT)),
        () -> assertFalse(stringValue5.canBeConvertedTo(DataType.JSON_OBJECT)),
        () -> assertFalse(stringValue6.canBeConvertedTo(DataType.JSON_OBJECT)),
        () -> assertFalse(stringValue7.canBeConvertedTo(DataType.JSON_OBJECT)));
  }

  @Test
  void asLong() {
    assertAll(
        () -> assertEquals(22L, stringValue1.asLong()),
        () -> assertEquals(0L, stringValue2.asLong()),
        () -> assertEquals(-2L, stringValue3.asLong()),
        () -> assertThrows(InvalidDataOperation.class, () -> stringValue4.asLong()),
        () -> assertThrows(InvalidDataOperation.class, () -> stringValue5.asLong()),
        () -> assertThrows(InvalidDataOperation.class, () -> stringValue6.asLong()),
        () -> assertThrows(InvalidDataOperation.class, () -> stringValue7.asLong()));
  }

  @Test
  void asDouble() {
    assertAll(
        () -> assertEquals(22.0, stringValue1.asDouble()),
        () -> assertEquals(0.0, stringValue2.asDouble()),
        () -> assertEquals(-2.3, stringValue3.asDouble()),
        () -> assertThrows(InvalidDataOperation.class, () -> stringValue4.asDouble()),
        () -> assertThrows(InvalidDataOperation.class, () -> stringValue5.asDouble()),
        () -> assertThrows(InvalidDataOperation.class, () -> stringValue6.asDouble()),
        () -> assertThrows(InvalidDataOperation.class, () -> stringValue7.asDouble()));
  }

  @Test
  void asString() {
    assertAll(
        () -> assertEquals("22", stringValue1.asString()),
        () -> assertEquals("0", stringValue2.asString()),
        () -> assertEquals("-2.3", stringValue3.asString()),
        () -> assertThrows(InvalidDataOperation.class, () -> stringValue4.asString()),
        () -> assertEquals("test", stringValue5.asString()),
        () -> assertEquals("true", stringValue6.asString()),
        () -> assertEquals("false", stringValue7.asString()));
  }

  @Test
  void asBoolean() {
    assertAll(
        () -> assertTrue(stringValue1.asBoolean()),
        () -> assertFalse(stringValue2.asBoolean()),
        () -> assertTrue(stringValue3.asBoolean()),
        () -> assertThrows(InvalidDataOperation.class, () -> stringValue4.asBoolean()),
        () -> assertThrows(InvalidDataOperation.class, () -> stringValue5.asBoolean()),
        () -> assertTrue(stringValue6.asBoolean()),
        () -> assertFalse(stringValue7.asBoolean()));
  }

  @Test
  void asJsonArray() {
    assertAll(
        () -> assertEquals(1, stringValue1.asJsonArray().size()),
        () -> assertEquals("22", stringValue1.asJsonArray().get(0).getAsString()),
        () -> assertEquals(1, stringValue2.asJsonArray().size()),
        () -> assertEquals("0", stringValue2.asJsonArray().get(0).getAsString()),
        () -> assertEquals(1, stringValue3.asJsonArray().size()),
        () -> assertEquals("-2.3", stringValue3.asJsonArray().get(0).getAsString()),
        () -> assertThrows(InvalidDataOperation.class, () -> stringValue4.asJsonArray()),
        () -> assertEquals("test", stringValue5.asJsonArray().get(0).getAsString()),
        () -> assertEquals("true", stringValue6.asJsonArray().get(0).getAsString()),
        () -> assertEquals("false", stringValue7.asJsonArray().get(0).getAsString()));
  }

  @Test
  void asJsonObject() {
    assertAll(
        () -> assertThrows(InvalidDataOperation.class, () -> stringValue1.asJsonObject()),
        () -> assertThrows(InvalidDataOperation.class, () -> stringValue2.asJsonObject()),
        () -> assertThrows(InvalidDataOperation.class, () -> stringValue3.asJsonObject()),
        () -> assertThrows(InvalidDataOperation.class, () -> stringValue4.asJsonObject()));
  }

  @Test
  void isUndefined() {
    assertAll(
        () -> assertFalse(stringValue1.isUndefined()),
        () -> assertFalse(stringValue2.isUndefined()),
        () -> assertFalse(stringValue3.isUndefined()),
        () -> assertTrue(stringValue4.isUndefined()));
  }
}
