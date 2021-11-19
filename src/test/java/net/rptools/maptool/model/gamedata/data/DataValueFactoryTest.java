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

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DataValueFactoryTest {

  private Random random;

  @BeforeEach
  void setUp() {
    random = new Random();
  }

  @Test
  void fromLong() {
    for (int i = 0; i < 100; i++) {
      var val = random.nextLong();
      var name = random.nextInt(100) + "test" + random.nextInt(100);

      var value = DataValueFactory.fromLong(name, val);

      assertAll(
          () -> assertEquals(value.getDataType(), DataType.LONG),
          () -> assertEquals(name, value.getName()),
          () -> assertEquals(val, value.asLong()));
    }
  }

  @Test
  void fromString() {
    for (int i = 0; i < 100; i++) {
      var val = random.nextLong() + "--" + random.nextInt(100);
      var name = random.nextInt(100) + "test" + random.nextInt(100);

      var value = DataValueFactory.fromString(name, val);

      assertAll(
          () -> assertEquals(value.getDataType(), DataType.STRING),
          () -> assertEquals(name, value.getName()),
          () -> assertEquals(val, value.asString()));
    }
  }

  @Test
  void fromBoolean() {
    for (int i = 0; i < 100; i++) {
      var val = random.nextBoolean();
      var name = random.nextInt(100) + "test" + random.nextInt(100);

      var value = DataValueFactory.fromBoolean(name, val);

      assertAll(
          () -> assertEquals(value.getDataType(), DataType.BOOLEAN),
          () -> assertEquals(name, value.getName()),
          () -> assertEquals(val, value.asBoolean()));
    }
  }

  @Test
  void fromDouble() {
    for (int i = 0; i < 100; i++) {
      var val = random.nextDouble();
      var name = random.nextInt(100) + "test" + random.nextInt(100);

      var value = DataValueFactory.fromDouble(name, val);

      assertAll(
          () -> assertEquals(value.getDataType(), DataType.DOUBLE),
          () -> assertEquals(name, value.getName()),
          () -> assertEquals(val, value.asDouble()));
    }
  }

  @Test
  void jsonList() {
    var name = random.nextInt(100) + "test" + random.nextInt(100);
    var list = new ArrayList<DataValue>();
    var longVal = random.nextLong();
    var doubleVal = random.nextDouble();
    var boolVal = random.nextBoolean();
    var stringVal = random.nextInt(100) + "test" + random.nextInt(100);

    list.add(DataValueFactory.fromLong("test1", longVal));
    list.add(DataValueFactory.fromDouble("test2", doubleVal));
    list.add(DataValueFactory.fromBoolean("test3", boolVal));
    list.add(DataValueFactory.fromString("test4", stringVal));

    // Test adding multiple types.
    var values1 = DataValueFactory.fromCollection(name, list);
    assertAll(
        () -> assertEquals(values1.getDataType(), DataType.JSON_ARRAY),
        () -> assertEquals(name, values1.getName()),
        () -> assertEquals(list.size(), values1.asJsonArray().size()),
        () -> assertEquals(longVal, values1.asJsonArray().get(0).getAsLong()),
        () -> assertEquals(doubleVal, values1.asJsonArray().get(1).getAsDouble()),
        () -> assertEquals(boolVal, values1.asJsonArray().get(2).getAsBoolean()),
        () -> assertEquals(stringVal, values1.asJsonArray().get(3).getAsString()));

    // Test adding an already existing value does add it to the json array.
    list.add(list.get(0));
    var values2 = DataValueFactory.fromCollection(name, list);
    assertAll(
        () -> assertEquals(values2.getDataType(), DataType.JSON_ARRAY),
        () -> assertEquals(name, values2.getName()),
        () -> assertEquals(list.size(), values2.asJsonArray().size()),
        () -> assertEquals(longVal, values2.asJsonArray().get(0).getAsLong()),
        () -> assertEquals(doubleVal, values2.asJsonArray().get(1).getAsDouble()),
        () -> assertEquals(boolVal, values2.asJsonArray().get(2).getAsBoolean()),
        () -> assertEquals(stringVal, values2.asJsonArray().get(3).getAsString()),
        () -> assertEquals(longVal, values2.asJsonArray().get(4).getAsLong()));

    list.add(values2);
    ;
    var values3 = DataValueFactory.fromCollection(name, list);
    assertAll(
        () -> assertEquals(values3.getDataType(), DataType.JSON_ARRAY),
        () -> assertEquals(name, values3.getName()),
        () -> assertEquals(list.size(), values3.asJsonArray().size()),
        () -> assertEquals(longVal, values3.asJsonArray().get(0).getAsLong()),
        () -> assertEquals(doubleVal, values3.asJsonArray().get(1).getAsDouble()),
        () -> assertEquals(boolVal, values3.asJsonArray().get(2).getAsBoolean()),
        () -> assertEquals(stringVal, values3.asJsonArray().get(3).getAsString()),
        () -> assertEquals(longVal, values3.asJsonArray().get(4).getAsLong()),
        () -> assertEquals(DataType.JSON_ARRAY, values3.getDataType()),
        () -> assertTrue(values3.asJsonArray().get(5).isJsonArray()),
        () -> assertEquals(list.size() - 1, values3.asJsonArray().get(5).getAsJsonArray().size()));

    var values4 = DataValueFactory.fromJsonArray(name, values3.asJsonArray());

    assertAll(
        () -> assertEquals(values4.getDataType(), DataType.JSON_ARRAY),
        () -> assertEquals(values4.asJsonArray().size(), values3.asJsonArray().size()));
  }

  @Test
  void fromJsonObject() {
    var name = random.nextInt(100) + "test" + random.nextInt(100);
    var list = new ArrayList<DataValue>();
    var longVal = random.nextLong();
    var doubleVal = random.nextDouble();
    var boolVal = random.nextBoolean();
    var stringVal = random.nextInt(100) + "test" + random.nextInt(100);

    // Test adding multiple types.
    var jsonObject = new JsonObject();
    jsonObject.addProperty("longV", longVal);
    jsonObject.addProperty("doubleV", doubleVal);
    jsonObject.addProperty("boolV", boolVal);
    jsonObject.addProperty("stringV", stringVal);

    var values1 = DataValueFactory.fromJsonObject(name, jsonObject);
    assertAll(
        () -> assertEquals(values1.getDataType(), DataType.JSON_OBJECT),
        () -> assertEquals(name, values1.getName()),
        () -> assertEquals(longVal, values1.asJsonObject().get("longV").getAsLong()),
        () -> assertEquals(doubleVal, values1.asJsonObject().get("doubleV").getAsDouble()),
        () -> assertEquals(stringVal, values1.asJsonObject().get("stringV").getAsString()),
        () -> assertEquals(boolVal, values1.asJsonObject().get("boolV").getAsBoolean()));
  }

  @Test
  void undefined() {
    var undefined = DataValueFactory.undefined("udef1");
    var undefinedLong = DataValueFactory.undefined("udef2", DataType.LONG);
    var undefinedDouble = DataValueFactory.undefined("udef3", DataType.DOUBLE);
    var undefinedString = DataValueFactory.undefined("udef4", DataType.STRING);
    var undefinedBoolean = DataValueFactory.undefined("udef5", DataType.BOOLEAN);
    var undefinedJsonArray = DataValueFactory.undefined("udef6", DataType.JSON_ARRAY);
    var undefinedJsonObject = DataValueFactory.undefined("udef7", DataType.JSON_OBJECT);

    assertAll(
        (() -> assertEquals(undefined.getDataType(), DataType.UNDEFINED)),
        () -> assertTrue(undefined.isUndefined()),
        () -> assertEquals("udef1", undefined.getName()),
        () -> assertEquals(DataType.LONG, undefinedLong.getDataType()),
        () -> assertTrue(undefinedLong.isUndefined()),
        () -> assertEquals("udef2", undefinedLong.getName()),
        () -> assertEquals(DataType.DOUBLE, undefinedDouble.getDataType()),
        () -> assertTrue(undefinedDouble.isUndefined()),
        () -> assertEquals("udef3", undefinedDouble.getName()),
        () -> assertEquals(DataType.STRING, undefinedString.getDataType()),
        () -> assertTrue(undefinedString.isUndefined()),
        () -> assertEquals("udef4", undefinedString.getName()),
        () -> assertEquals(DataType.BOOLEAN, undefinedBoolean.getDataType()),
        () -> assertTrue(undefinedBoolean.isUndefined()),
        () -> assertEquals("udef5", undefinedBoolean.getName()),
        () -> assertEquals(DataType.JSON_ARRAY, undefinedJsonArray.getDataType()),
        () -> assertTrue(undefinedJsonArray.isUndefined()),
        () -> assertEquals("udef6", undefinedJsonArray.getName()),
        () -> assertEquals(DataType.JSON_OBJECT, undefinedJsonObject.getDataType()),
        () -> assertTrue(undefinedJsonObject.isUndefined()),
        () -> assertEquals("udef7", undefinedJsonObject.getName()));
  }
}
