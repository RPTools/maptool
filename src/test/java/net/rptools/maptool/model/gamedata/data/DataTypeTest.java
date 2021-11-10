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
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.rptools.maptool.model.gamedata.InvalidDataOperation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DataTypeTest {

  private LongDataValue longValue;
  private DoubleDataValue doubleValue;
  private BooleanDataValue booleanValue1;
  private BooleanDataValue booleanValue2;
  private StringDataValue stringValue1;
  private StringDataValue stringValue2;
  private StringDataValue stringValue3;
  private StringDataValue stringValue4;
  private StringDataValue stringValue5;
  private JsonArrayDataValue jsonArray1;
  private JsonArrayDataValue jsonArray2;
  private JsonArrayDataValue jsonArray3;
  private JsonObjectDataValue jsonObject1;
  private JsonObjectDataValue jsonObject2;

  @BeforeEach
  void setup() {
    longValue = new LongDataValue("test1", 1234567);
    doubleValue = new DoubleDataValue("test2", 1234567.80);
    booleanValue1 = new BooleanDataValue("test3", true);
    booleanValue2 = new BooleanDataValue("test4", false);
    stringValue1 = new StringDataValue("test5", "test");
    stringValue2 = new StringDataValue("test6", "77");
    stringValue3 = new StringDataValue("test7", "88.77");
    stringValue4 = new StringDataValue("test8", "true");
    stringValue5 = new StringDataValue("test9", "false");
    var jsonA1 = new JsonArray();
    jsonA1.add(10);
    jsonA1.add("blah");
    jsonArray1 = new JsonArrayDataValue("test10", jsonA1);

    var jsonA2 = new JsonArray();
    jsonA2.add(40);
    jsonArray2 = new JsonArrayDataValue("test11", jsonA2);

    var jsonA3 = new JsonArray();
    jsonArray3 = new JsonArrayDataValue("test12", jsonA3);

    var jsonObj1 = new JsonObject();
    jsonObj1.addProperty("test5", 1);
    jsonObject1 = new JsonObjectDataValue("test13", jsonObj1);

    var jsonObj2 = new JsonObject();
    jsonObject2 = new JsonObjectDataValue("test14", jsonObj2);
  }

  @Test
  void convertToString() {

    assertAll(
        () ->
            assertEquals(
                DataType.STRING, DataType.convert(longValue, DataType.STRING).getDataType()),
        () ->
            assertEquals(
                DataType.STRING, DataType.convert(doubleValue, DataType.STRING).getDataType()),
        () ->
            assertEquals(
                DataType.STRING, DataType.convert(booleanValue1, DataType.STRING).getDataType()),
        () ->
            assertEquals(
                DataType.STRING, DataType.convert(booleanValue2, DataType.STRING).getDataType()),
        () ->
            assertEquals(
                DataType.STRING, DataType.convert(stringValue1, DataType.STRING).getDataType()),
        () ->
            assertEquals(
                DataType.STRING, DataType.convert(stringValue2, DataType.STRING).getDataType()),
        () ->
            assertEquals(
                DataType.STRING, DataType.convert(stringValue3, DataType.STRING).getDataType()),
        () ->
            assertEquals(
                DataType.STRING, DataType.convert(stringValue4, DataType.STRING).getDataType()),
        () ->
            assertEquals(
                DataType.STRING, DataType.convert(stringValue5, DataType.STRING).getDataType()),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.convert(jsonArray1, DataType.STRING)),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.convert(jsonArray2, DataType.STRING)),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.convert(jsonArray3, DataType.STRING)),
        () -> assertThrows(InvalidDataOperation.class, () -> DataType.STRING.convert(jsonObject1)),
        () -> assertThrows(InvalidDataOperation.class, () -> DataType.STRING.convert(jsonObject2)),
        () -> assertEquals(DataType.STRING, DataType.STRING.convert(doubleValue).getDataType()),
        () -> assertEquals(DataType.STRING, DataType.STRING.convert(booleanValue1).getDataType()),
        () -> assertEquals(DataType.STRING, DataType.STRING.convert(booleanValue2).getDataType()),
        () -> assertEquals(DataType.STRING, DataType.STRING.convert(stringValue1).getDataType()),
        () -> assertEquals(DataType.STRING, DataType.STRING.convert(stringValue2).getDataType()),
        () -> assertEquals(DataType.STRING, DataType.STRING.convert(stringValue3).getDataType()),
        () -> assertEquals(DataType.STRING, DataType.STRING.convert(stringValue4).getDataType()),
        () -> assertEquals(DataType.STRING, DataType.STRING.convert(stringValue5).getDataType()),
        () -> assertThrows(InvalidDataOperation.class, () -> DataType.STRING.convert(jsonArray1)),
        () -> assertThrows(InvalidDataOperation.class, () -> DataType.STRING.convert(jsonArray2)),
        () -> assertThrows(InvalidDataOperation.class, () -> DataType.STRING.convert(jsonArray3)),
        () -> assertThrows(InvalidDataOperation.class, () -> DataType.STRING.convert(jsonObject1)),
        () -> assertThrows(InvalidDataOperation.class, () -> DataType.STRING.convert(jsonObject2)));
  }

  @Test
  void convertToLong() {
    assertAll(
        () -> assertEquals(DataType.LONG, DataType.convert(longValue, DataType.LONG).getDataType()),
        () ->
            assertEquals(DataType.LONG, DataType.convert(doubleValue, DataType.LONG).getDataType()),
        () ->
            assertEquals(
                DataType.LONG, DataType.convert(booleanValue1, DataType.LONG).getDataType()),
        () ->
            assertEquals(
                DataType.LONG, DataType.convert(booleanValue2, DataType.LONG).getDataType()),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.convert(stringValue1, DataType.LONG)),
        () ->
            assertEquals(
                DataType.LONG, DataType.convert(stringValue2, DataType.LONG).getDataType()),
        () ->
            assertEquals(
                DataType.LONG, DataType.convert(stringValue3, DataType.LONG).getDataType()),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.convert(stringValue4, DataType.LONG)),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.convert(stringValue5, DataType.LONG)),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.convert(jsonArray1, DataType.LONG)),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.convert(jsonArray2, DataType.LONG)),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.convert(jsonArray3, DataType.LONG)),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.convert(jsonObject1, DataType.LONG)),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.convert(jsonObject2, DataType.LONG)),
        () -> assertEquals(DataType.LONG, DataType.LONG.convert(longValue).getDataType()),
        () -> assertEquals(DataType.LONG, DataType.LONG.convert(doubleValue).getDataType()),
        () -> assertEquals(DataType.LONG, DataType.LONG.convert(booleanValue1).getDataType()),
        () -> assertEquals(DataType.LONG, DataType.LONG.convert(booleanValue2).getDataType()),
        () -> assertThrows(InvalidDataOperation.class, () -> DataType.LONG.convert(stringValue1)),
        () -> assertEquals(DataType.LONG, DataType.LONG.convert(stringValue2).getDataType()),
        () -> assertEquals(DataType.LONG, DataType.LONG.convert(stringValue3).getDataType()),
        () -> assertThrows(InvalidDataOperation.class, () -> DataType.LONG.convert(stringValue4)),
        () -> assertThrows(InvalidDataOperation.class, () -> DataType.LONG.convert(stringValue5)),
        () -> assertThrows(InvalidDataOperation.class, () -> DataType.LONG.convert(jsonArray1)),
        () -> assertThrows(InvalidDataOperation.class, () -> DataType.LONG.convert(jsonArray2)),
        () -> assertThrows(InvalidDataOperation.class, () -> DataType.LONG.convert(jsonArray3)),
        () -> assertThrows(InvalidDataOperation.class, () -> DataType.LONG.convert(jsonObject1)),
        () -> assertThrows(InvalidDataOperation.class, () -> DataType.LONG.convert(jsonObject2)));
  }

  @Test
  void convertToDouble() {
    assertAll(
        () ->
            assertEquals(
                DataType.DOUBLE, DataType.convert(longValue, DataType.DOUBLE).getDataType()),
        () ->
            assertEquals(
                DataType.DOUBLE, DataType.convert(doubleValue, DataType.DOUBLE).getDataType()),
        () ->
            assertEquals(
                DataType.DOUBLE, DataType.convert(booleanValue1, DataType.DOUBLE).getDataType()),
        () ->
            assertEquals(
                DataType.DOUBLE, DataType.convert(booleanValue2, DataType.DOUBLE).getDataType()),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.convert(stringValue1, DataType.DOUBLE)),
        () ->
            assertEquals(
                DataType.DOUBLE, DataType.convert(stringValue2, DataType.DOUBLE).getDataType()),
        () ->
            assertEquals(
                DataType.DOUBLE, DataType.convert(stringValue3, DataType.DOUBLE).getDataType()),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.convert(stringValue4, DataType.DOUBLE)),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.convert(stringValue5, DataType.DOUBLE)),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.convert(jsonArray1, DataType.DOUBLE)),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.convert(jsonArray2, DataType.DOUBLE)),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.convert(jsonArray3, DataType.DOUBLE)),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.convert(jsonObject1, DataType.DOUBLE)),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.convert(jsonObject2, DataType.DOUBLE)),
        () -> assertEquals(DataType.DOUBLE, DataType.DOUBLE.convert(longValue).getDataType()),
        () -> assertEquals(DataType.DOUBLE, DataType.DOUBLE.convert(doubleValue).getDataType()),
        () -> assertEquals(DataType.DOUBLE, DataType.DOUBLE.convert(booleanValue1).getDataType()),
        () -> assertEquals(DataType.DOUBLE, DataType.DOUBLE.convert(booleanValue2).getDataType()),
        () -> assertThrows(InvalidDataOperation.class, () -> DataType.DOUBLE.convert(stringValue1)),
        () -> assertEquals(DataType.DOUBLE, DataType.DOUBLE.convert(stringValue2).getDataType()),
        () -> assertEquals(DataType.DOUBLE, DataType.DOUBLE.convert(stringValue3).getDataType()),
        () -> assertThrows(InvalidDataOperation.class, () -> DataType.DOUBLE.convert(stringValue4)),
        () -> assertThrows(InvalidDataOperation.class, () -> DataType.DOUBLE.convert(stringValue5)),
        () -> assertThrows(InvalidDataOperation.class, () -> DataType.DOUBLE.convert(jsonArray1)),
        () -> assertThrows(InvalidDataOperation.class, () -> DataType.DOUBLE.convert(jsonArray2)),
        () -> assertThrows(InvalidDataOperation.class, () -> DataType.DOUBLE.convert(jsonArray3)),
        () -> assertThrows(InvalidDataOperation.class, () -> DataType.DOUBLE.convert(jsonObject1)),
        () -> assertThrows(InvalidDataOperation.class, () -> DataType.DOUBLE.convert(jsonObject2)));
  }

  @Test
  void convertToBoolean() {
    assertAll(
        () ->
            assertEquals(
                DataType.BOOLEAN, DataType.convert(longValue, DataType.BOOLEAN).getDataType()),
        () ->
            assertEquals(
                DataType.BOOLEAN, DataType.convert(doubleValue, DataType.BOOLEAN).getDataType()),
        () ->
            assertEquals(
                DataType.BOOLEAN, DataType.convert(booleanValue1, DataType.BOOLEAN).getDataType()),
        () ->
            assertEquals(
                DataType.BOOLEAN, DataType.convert(booleanValue2, DataType.BOOLEAN).getDataType()),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.convert(stringValue1, DataType.BOOLEAN)),
        () ->
            assertEquals(
                DataType.BOOLEAN, DataType.convert(stringValue2, DataType.BOOLEAN).getDataType()),
        () ->
            assertEquals(
                DataType.BOOLEAN, DataType.convert(stringValue3, DataType.BOOLEAN).getDataType()),
        () ->
            assertEquals(
                DataType.BOOLEAN, DataType.convert(stringValue4, DataType.BOOLEAN).getDataType()),
        () ->
            assertEquals(
                DataType.BOOLEAN, DataType.convert(stringValue5, DataType.BOOLEAN).getDataType()),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.convert(jsonArray1, DataType.BOOLEAN)),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.convert(jsonArray2, DataType.BOOLEAN)),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.convert(jsonArray3, DataType.BOOLEAN)),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.convert(jsonObject1, DataType.BOOLEAN)),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.convert(jsonObject2, DataType.BOOLEAN)));
  }

  @Test
  void convertToJsonArray() {
    assertAll(
        () ->
            assertEquals(
                DataType.JSON_ARRAY,
                DataType.convert(longValue, DataType.JSON_ARRAY).getDataType()),
        () ->
            assertEquals(
                DataType.JSON_ARRAY,
                DataType.convert(doubleValue, DataType.JSON_ARRAY).getDataType()),
        () ->
            assertEquals(
                DataType.JSON_ARRAY,
                DataType.convert(booleanValue1, DataType.JSON_ARRAY).getDataType()),
        () ->
            assertEquals(
                DataType.JSON_ARRAY,
                DataType.convert(booleanValue2, DataType.JSON_ARRAY).getDataType()),
        () ->
            assertEquals(
                DataType.JSON_ARRAY,
                DataType.convert(stringValue1, DataType.JSON_ARRAY).getDataType()),
        () ->
            assertEquals(
                DataType.JSON_ARRAY,
                DataType.convert(stringValue2, DataType.JSON_ARRAY).getDataType()),
        () ->
            assertEquals(
                DataType.JSON_ARRAY,
                DataType.convert(stringValue3, DataType.JSON_ARRAY).getDataType()),
        () ->
            assertEquals(
                DataType.JSON_ARRAY,
                DataType.convert(stringValue4, DataType.JSON_ARRAY).getDataType()),
        () ->
            assertEquals(
                DataType.JSON_ARRAY,
                DataType.convert(stringValue5, DataType.JSON_ARRAY).getDataType()),
        () ->
            assertEquals(
                DataType.JSON_ARRAY,
                DataType.convert(jsonArray1, DataType.JSON_ARRAY).getDataType()),
        () ->
            assertEquals(
                DataType.JSON_ARRAY,
                DataType.convert(jsonArray2, DataType.JSON_ARRAY).getDataType()),
        () ->
            assertEquals(
                DataType.JSON_ARRAY,
                DataType.convert(jsonArray3, DataType.JSON_ARRAY).getDataType()),
        () ->
            assertThrows(
                InvalidDataOperation.class,
                () -> DataType.convert(jsonObject1, DataType.JSON_ARRAY)),
        () ->
            assertThrows(
                InvalidDataOperation.class,
                () -> DataType.convert(jsonObject1, DataType.JSON_ARRAY)),
        () ->
            assertEquals(DataType.JSON_ARRAY, DataType.JSON_ARRAY.convert(longValue).getDataType()),
        () ->
            assertEquals(
                DataType.JSON_ARRAY, DataType.JSON_ARRAY.convert(doubleValue).getDataType()),
        () ->
            assertEquals(
                DataType.JSON_ARRAY, DataType.JSON_ARRAY.convert(booleanValue1).getDataType()),
        () ->
            assertEquals(
                DataType.JSON_ARRAY, DataType.JSON_ARRAY.convert(booleanValue2).getDataType()),
        () ->
            assertEquals(
                DataType.JSON_ARRAY, DataType.JSON_ARRAY.convert(stringValue1).getDataType()),
        () ->
            assertEquals(
                DataType.JSON_ARRAY, DataType.JSON_ARRAY.convert(stringValue2).getDataType()),
        () ->
            assertEquals(
                DataType.JSON_ARRAY, DataType.JSON_ARRAY.convert(stringValue3).getDataType()),
        () ->
            assertEquals(
                DataType.JSON_ARRAY, DataType.JSON_ARRAY.convert(stringValue4).getDataType()),
        () ->
            assertEquals(
                DataType.JSON_ARRAY, DataType.JSON_ARRAY.convert(stringValue5).getDataType()),
        () ->
            assertEquals(
                DataType.JSON_ARRAY, DataType.JSON_ARRAY.convert(jsonArray1).getDataType()),
        () ->
            assertEquals(
                DataType.JSON_ARRAY, DataType.JSON_ARRAY.convert(jsonArray2).getDataType()),
        () ->
            assertEquals(
                DataType.JSON_ARRAY, DataType.JSON_ARRAY.convert(jsonArray3).getDataType()),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.JSON_ARRAY.convert(jsonObject1)),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.JSON_ARRAY.convert(jsonObject1)));
  }

  @Test
  void convertToJsonObject() {
    assertAll(
        () ->
            assertThrows(
                InvalidDataOperation.class,
                () -> DataType.convert(longValue, DataType.JSON_OBJECT)),
        () ->
            assertThrows(
                InvalidDataOperation.class,
                () -> DataType.convert(doubleValue, DataType.JSON_OBJECT)),
        () ->
            assertThrows(
                InvalidDataOperation.class,
                () -> DataType.convert(booleanValue1, DataType.JSON_OBJECT)),
        () ->
            assertThrows(
                InvalidDataOperation.class,
                () -> DataType.convert(booleanValue2, DataType.JSON_OBJECT)),
        () ->
            assertThrows(
                InvalidDataOperation.class,
                () -> DataType.convert(stringValue1, DataType.JSON_OBJECT)),
        () ->
            assertThrows(
                InvalidDataOperation.class,
                () -> DataType.convert(stringValue2, DataType.JSON_OBJECT)),
        () ->
            assertThrows(
                InvalidDataOperation.class,
                () -> DataType.convert(stringValue3, DataType.JSON_OBJECT)),
        () ->
            assertThrows(
                InvalidDataOperation.class,
                () -> DataType.convert(stringValue4, DataType.JSON_OBJECT)),
        () ->
            assertThrows(
                InvalidDataOperation.class,
                () -> DataType.convert(stringValue5, DataType.JSON_OBJECT)),
        () ->
            assertThrows(
                InvalidDataOperation.class,
                () -> DataType.convert(jsonArray1, DataType.JSON_OBJECT)),
        () ->
            assertThrows(
                InvalidDataOperation.class,
                () -> DataType.convert(jsonArray2, DataType.JSON_OBJECT)),
        () ->
            assertThrows(
                InvalidDataOperation.class,
                () -> DataType.convert(jsonArray3, DataType.JSON_OBJECT)),
        () ->
            assertEquals(
                DataType.JSON_OBJECT,
                DataType.convert(jsonObject1, DataType.JSON_OBJECT).getDataType()),
        () ->
            assertEquals(
                DataType.JSON_OBJECT,
                DataType.convert(jsonObject2, DataType.JSON_OBJECT).getDataType()),
        () ->
            assertThrows(InvalidDataOperation.class, () -> DataType.JSON_OBJECT.convert(longValue)),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.JSON_OBJECT.convert(doubleValue)),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.JSON_OBJECT.convert(booleanValue1)),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.JSON_OBJECT.convert(booleanValue2)),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.JSON_OBJECT.convert(stringValue1)),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.JSON_OBJECT.convert(stringValue2)),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.JSON_OBJECT.convert(stringValue3)),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.JSON_OBJECT.convert(stringValue4)),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.JSON_OBJECT.convert(stringValue5)),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.JSON_OBJECT.convert(jsonArray1)),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.JSON_OBJECT.convert(jsonArray2)),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.JSON_OBJECT.convert(jsonArray3)),
        () ->
            assertEquals(
                DataType.JSON_OBJECT, DataType.JSON_OBJECT.convert(jsonObject1).getDataType()),
        () ->
            assertEquals(
                DataType.JSON_OBJECT, DataType.JSON_OBJECT.convert(jsonObject2).getDataType()));
  }

  @Test
  void convertToUndefined() {
    assertAll(
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.convert(longValue, DataType.UNDEFINED)),
        () ->
            assertThrows(
                InvalidDataOperation.class,
                () -> DataType.convert(doubleValue, DataType.UNDEFINED)),
        () ->
            assertThrows(
                InvalidDataOperation.class,
                () -> DataType.convert(booleanValue1, DataType.UNDEFINED)),
        () ->
            assertThrows(
                InvalidDataOperation.class,
                () -> DataType.convert(booleanValue2, DataType.UNDEFINED)),
        () ->
            assertThrows(
                InvalidDataOperation.class,
                () -> DataType.convert(stringValue1, DataType.UNDEFINED)),
        () ->
            assertThrows(
                InvalidDataOperation.class,
                () -> DataType.convert(stringValue2, DataType.UNDEFINED)),
        () ->
            assertThrows(
                InvalidDataOperation.class,
                () -> DataType.convert(stringValue3, DataType.UNDEFINED)),
        () ->
            assertThrows(
                InvalidDataOperation.class,
                () -> DataType.convert(stringValue4, DataType.UNDEFINED)),
        () ->
            assertThrows(
                InvalidDataOperation.class,
                () -> DataType.convert(stringValue5, DataType.UNDEFINED)),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.convert(jsonArray1, DataType.UNDEFINED)),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.convert(jsonArray2, DataType.UNDEFINED)),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.convert(jsonArray3, DataType.UNDEFINED)),
        () ->
            assertThrows(
                InvalidDataOperation.class,
                () -> DataType.convert(jsonObject1, DataType.UNDEFINED)),
        () ->
            assertThrows(
                InvalidDataOperation.class,
                () -> DataType.convert(jsonObject2, DataType.UNDEFINED)),
        () -> assertThrows(InvalidDataOperation.class, () -> DataType.UNDEFINED.convert(longValue)),
        () ->
            assertThrows(InvalidDataOperation.class, () -> DataType.UNDEFINED.convert(doubleValue)),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.UNDEFINED.convert(booleanValue1)),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.UNDEFINED.convert(booleanValue2)),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.UNDEFINED.convert(stringValue1)),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.UNDEFINED.convert(stringValue2)),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.UNDEFINED.convert(stringValue3)),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.UNDEFINED.convert(stringValue4)),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.UNDEFINED.convert(stringValue5)),
        () ->
            assertThrows(InvalidDataOperation.class, () -> DataType.UNDEFINED.convert(jsonArray1)),
        () ->
            assertThrows(InvalidDataOperation.class, () -> DataType.UNDEFINED.convert(jsonArray2)),
        () ->
            assertThrows(InvalidDataOperation.class, () -> DataType.UNDEFINED.convert(jsonArray3)),
        () ->
            assertThrows(InvalidDataOperation.class, () -> DataType.UNDEFINED.convert(jsonObject1)),
        () ->
            assertThrows(
                InvalidDataOperation.class, () -> DataType.UNDEFINED.convert(jsonObject2)));
  }
}
