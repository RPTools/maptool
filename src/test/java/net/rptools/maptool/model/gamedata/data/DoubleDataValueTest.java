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

class DoubleDataValueTest {

  private DoubleDataValue longValue1;
  private DoubleDataValue longValue2;
  private DoubleDataValue longValue3;
  private DoubleDataValue longValue4;

  @BeforeEach
  void setUp() {
    longValue1 = new DoubleDataValue("test1", 22);
    longValue2 = new DoubleDataValue("test2", 0);
    longValue3 = new DoubleDataValue("test3", -2);
    longValue4 = new DoubleDataValue("test4");
  }

  @Test
  void getName() {
    assertAll(
        () -> assertEquals("test1", longValue1.getName()),
        () -> assertEquals("test2", longValue2.getName()),
        () -> assertEquals("test3", longValue3.getName()),
        () -> assertEquals("test4", longValue4.getName()));
  }

  @Test
  void getDataType() {
    assertAll(
        () -> assertEquals(DataType.DOUBLE, longValue1.getDataType()),
        () -> assertEquals(DataType.DOUBLE, longValue2.getDataType()),
        () -> assertEquals(DataType.DOUBLE, longValue3.getDataType()),
        () -> assertEquals(DataType.DOUBLE, longValue4.getDataType()));
  }

  @Test
  void canBeConvertedTo() {
    assertAll(
        () -> assertTrue(longValue1.canBeConvertedTo(DataType.BOOLEAN)),
        () -> assertTrue(longValue2.canBeConvertedTo(DataType.BOOLEAN)),
        () -> assertTrue(longValue3.canBeConvertedTo(DataType.BOOLEAN)),
        () -> assertFalse(longValue4.canBeConvertedTo(DataType.BOOLEAN)),
        () -> assertTrue(longValue1.canBeConvertedTo(DataType.LONG)),
        () -> assertTrue(longValue2.canBeConvertedTo(DataType.LONG)),
        () -> assertTrue(longValue3.canBeConvertedTo(DataType.LONG)),
        () -> assertFalse(longValue4.canBeConvertedTo(DataType.LONG)),
        () -> assertTrue(longValue1.canBeConvertedTo(DataType.DOUBLE)),
        () -> assertTrue(longValue2.canBeConvertedTo(DataType.DOUBLE)),
        () -> assertTrue(longValue3.canBeConvertedTo(DataType.DOUBLE)),
        () -> assertFalse(longValue4.canBeConvertedTo(DataType.DOUBLE)),
        () -> assertTrue(longValue1.canBeConvertedTo(DataType.STRING)),
        () -> assertTrue(longValue2.canBeConvertedTo(DataType.STRING)),
        () -> assertTrue(longValue3.canBeConvertedTo(DataType.STRING)),
        () -> assertFalse(longValue4.canBeConvertedTo(DataType.STRING)),
        () -> assertTrue(longValue1.canBeConvertedTo(DataType.JSON_ARRAY)),
        () -> assertTrue(longValue2.canBeConvertedTo(DataType.JSON_ARRAY)),
        () -> assertTrue(longValue3.canBeConvertedTo(DataType.JSON_ARRAY)),
        () -> assertFalse(longValue4.canBeConvertedTo(DataType.JSON_ARRAY)),
        () -> assertFalse(longValue1.canBeConvertedTo(DataType.JSON_OBJECT)),
        () -> assertFalse(longValue2.canBeConvertedTo(DataType.JSON_OBJECT)),
        () -> assertFalse(longValue3.canBeConvertedTo(DataType.JSON_OBJECT)),
        () -> assertFalse(longValue4.canBeConvertedTo(DataType.JSON_OBJECT)));
  }

  @Test
  void asLong() {
    assertAll(
        () -> assertEquals(22.0, longValue1.asDouble()),
        () -> assertEquals(0.0, longValue2.asDouble()),
        () -> assertEquals(-2.0, longValue3.asDouble()),
        () -> assertThrows(InvalidDataOperation.class, () -> longValue4.asDouble()));
  }

  @Test
  void asDouble() {
    assertAll(
        () -> assertEquals(22.0, longValue1.asDouble()),
        () -> assertEquals(0.0, longValue2.asDouble()),
        () -> assertEquals(-2.0, longValue3.asDouble()),
        () -> assertThrows(InvalidDataOperation.class, () -> longValue4.asDouble()));
  }

  @Test
  void asString() {
    assertAll(
        () -> assertEquals("22.0", longValue1.asString()),
        () -> assertEquals("0.0", longValue2.asString()),
        () -> assertEquals("-2.0", longValue3.asString()),
        () -> assertThrows(InvalidDataOperation.class, () -> longValue4.asString()));
  }

  @Test
  void asBoolean() {
    assertAll(
        () -> assertTrue(longValue1.asBoolean()),
        () -> assertFalse(longValue2.asBoolean()),
        () -> assertTrue(longValue3.asBoolean()),
        () -> assertThrows(InvalidDataOperation.class, () -> longValue4.asBoolean()));
  }

  @Test
  void asJsonArray() {
    assertAll(
        () -> assertEquals(1, longValue1.asJsonArray().size()),
        () -> assertEquals(22.0, longValue1.asJsonArray().get(0).getAsDouble()),
        () -> assertEquals(1, longValue2.asJsonArray().size()),
        () -> assertEquals(0.0, longValue2.asJsonArray().get(0).getAsDouble()),
        () -> assertEquals(1, longValue3.asJsonArray().size()),
        () -> assertEquals(-2.0, longValue3.asJsonArray().get(0).getAsDouble()),
        () -> assertThrows(InvalidDataOperation.class, () -> longValue4.asJsonArray()));
  }

  @Test
  void asJsonObject() {
    assertAll(
        () -> assertThrows(InvalidDataOperation.class, () -> longValue1.asJsonObject()),
        () -> assertThrows(InvalidDataOperation.class, () -> longValue2.asJsonObject()),
        () -> assertThrows(InvalidDataOperation.class, () -> longValue3.asJsonObject()),
        () -> assertThrows(InvalidDataOperation.class, () -> longValue4.asJsonObject()));
  }

  @Test
  void isUndefined() {
    assertAll(
        () -> assertFalse(longValue1.isUndefined()),
        () -> assertFalse(longValue2.isUndefined()),
        () -> assertFalse(longValue3.isUndefined()),
        () -> assertTrue(longValue4.isUndefined()));
  }
}
