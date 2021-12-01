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

import static org.junit.jupiter.api.Assertions.*;

import net.rptools.maptool.model.gamedata.InvalidDataOperation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BooleanDataValueTest {

  private BooleanDataValue booleanValue1;
  private BooleanDataValue booleanValue2;
  private BooleanDataValue booleanValue3;

  @BeforeEach
  void setUp() {
    booleanValue1 = new BooleanDataValue("test1", true);
    booleanValue2 = new BooleanDataValue("test2", false);
    booleanValue3 = new BooleanDataValue("test3");
  }

  @Test
  void getName() {
    assertAll(
        () -> assertEquals("test1", booleanValue1.getName()),
        () -> assertEquals("test2", booleanValue2.getName()),
        () -> assertEquals("test3", booleanValue3.getName()));
  }

  @Test
  void getDataType() {
    assertAll(
        () -> assertEquals(DataType.BOOLEAN, booleanValue1.getDataType()),
        () -> assertEquals(DataType.BOOLEAN, booleanValue2.getDataType()),
        () -> assertEquals(DataType.BOOLEAN, booleanValue3.getDataType()));
  }

  @Test
  void canBeConvertedTo() {
    assertAll(
        () -> assertTrue(booleanValue1.canBeConvertedTo(DataType.BOOLEAN)),
        () -> assertTrue(booleanValue2.canBeConvertedTo(DataType.BOOLEAN)),
        () -> assertFalse(booleanValue3.canBeConvertedTo(DataType.BOOLEAN)),
        () -> assertTrue(booleanValue1.canBeConvertedTo(DataType.LONG)),
        () -> assertTrue(booleanValue2.canBeConvertedTo(DataType.LONG)),
        () -> assertFalse(booleanValue3.canBeConvertedTo(DataType.LONG)),
        () -> assertTrue(booleanValue1.canBeConvertedTo(DataType.DOUBLE)),
        () -> assertTrue(booleanValue2.canBeConvertedTo(DataType.DOUBLE)),
        () -> assertFalse(booleanValue3.canBeConvertedTo(DataType.DOUBLE)),
        () -> assertTrue(booleanValue1.canBeConvertedTo(DataType.STRING)),
        () -> assertTrue(booleanValue2.canBeConvertedTo(DataType.STRING)),
        () -> assertFalse(booleanValue3.canBeConvertedTo(DataType.STRING)),
        () -> assertTrue(booleanValue1.canBeConvertedTo(DataType.JSON_ARRAY)),
        () -> assertTrue(booleanValue2.canBeConvertedTo(DataType.JSON_ARRAY)),
        () -> assertFalse(booleanValue3.canBeConvertedTo(DataType.JSON_ARRAY)),
        () -> assertFalse(booleanValue1.canBeConvertedTo(DataType.JSON_OBJECT)),
        () -> assertFalse(booleanValue2.canBeConvertedTo(DataType.JSON_OBJECT)),
        () -> assertFalse(booleanValue3.canBeConvertedTo(DataType.JSON_OBJECT)));
  }

  @Test
  void asLong() {
    assertAll(
        () -> assertEquals(1L, booleanValue1.asLong()),
        () -> assertEquals(0L, booleanValue2.asLong()),
        () -> assertThrows(InvalidDataOperation.class, () -> booleanValue3.asLong()));
  }

  @Test
  void asDouble() {
    assertAll(
        () -> assertEquals(1.0, booleanValue1.asDouble()),
        () -> assertEquals(0.0, booleanValue2.asDouble()),
        () -> assertThrows(InvalidDataOperation.class, () -> booleanValue3.asDouble()));
  }

  @Test
  void asString() {
    assertAll(
        () -> assertEquals("true", booleanValue1.asString()),
        () -> assertEquals("false", booleanValue2.asString()),
        () -> assertThrows(InvalidDataOperation.class, () -> booleanValue3.asString()));
  }

  @Test
  void asBoolean() {
    assertAll(
        () -> assertTrue(booleanValue1.asBoolean()),
        () -> assertFalse(booleanValue2.asBoolean()),
        () -> assertThrows(InvalidDataOperation.class, () -> booleanValue3.asBoolean()));
  }

  @Test
  void asJsonArray() {
    assertAll(
        () -> assertEquals(1, booleanValue1.asJsonArray().size()),
        () -> assertTrue(booleanValue1.asJsonArray().get(0).getAsBoolean()),
        () -> assertEquals(1, booleanValue2.asJsonArray().size()),
        () -> assertFalse(booleanValue2.asJsonArray().get(0).getAsBoolean()),
        () -> assertThrows(InvalidDataOperation.class, () -> booleanValue3.asJsonArray()));
  }

  @Test
  void asJsonObject() {
    assertAll(
        () -> assertThrows(InvalidDataOperation.class, () -> booleanValue1.asJsonObject()),
        () -> assertThrows(InvalidDataOperation.class, () -> booleanValue2.asJsonObject()),
        () -> assertThrows(InvalidDataOperation.class, () -> booleanValue3.asJsonObject()));
  }

  @Test
  void isUndefined() {
    assertAll(
        () -> assertFalse(booleanValue1.isUndefined()),
        () -> assertFalse(booleanValue2.isUndefined()),
        () -> assertTrue(booleanValue3.isUndefined()));
  }
}
