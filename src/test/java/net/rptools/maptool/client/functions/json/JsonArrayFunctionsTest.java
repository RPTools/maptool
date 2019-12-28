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
package net.rptools.maptool.client.functions.json;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.rptools.parser.ParserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JsonArrayFunctionsTest {

  private JsonArrayFunctions jsonArrayFunctions;
  private String jsonArrString1 = "[4, 5, 6, 7, 12, 1]";
  private String jsonArrString2 = "[2, 3, 4, 5, 6]";
  private String jsonArrString3 = "[1, 5, 6]";
  private JsonArray jsonArray1;
  private JsonArray jsonArray2;
  private JsonArray jsonArray3;
  private JsonArray jsonEmpty;
  Random random;

  @BeforeEach
  void setup() {
    JsonParser jsonParser = new JsonParser();
    JsonMTSTypeConversion typeConversion = new JsonMTSTypeConversion(jsonParser);
    random = new Random(System.currentTimeMillis());

    jsonArrayFunctions = new JsonArrayFunctions(typeConversion);

    jsonArray1 = jsonParser.parse(jsonArrString1).getAsJsonArray();
    jsonArray2 = jsonParser.parse(jsonArrString2).getAsJsonArray();
    jsonArray3 = jsonParser.parse(jsonArrString3).getAsJsonArray();

    jsonEmpty = new JsonArray();
  }

  @Test
  void fromStringList() {
    var numbers = new ArrayList<String>();
    for (int i = 0; i < 200; i++) {
      numbers.add(Integer.toString(random.nextInt()));
    }

    String[] delim = new String[] {",", ";", ":", " "};

    for (String s : delim) {
      String str = String.join(s, numbers);
      JsonArray jsonArray = jsonArrayFunctions.fromStringList(str, s);

      assertEquals(numbers.size(), jsonArray.size());
      for (int i = 0; i < numbers.size(); i++) {
        assertEquals(
            numbers.get(i), jsonArray.get(i).getAsString(), "Index " + i + " does not match.");
      }
    }
  }

  @Test
  void toStringProp() {
    var numbers = new ArrayList<String>();
    for (int i = 0; i < 200; i++) {
      numbers.add(Integer.toString(random.nextInt()));
    }

    String[] delim = new String[] {",", ";", ":", " "};

    for (String s : delim) {
      StringBuilder sb = new StringBuilder();
      JsonArray arr = new JsonArray();
      for (int i = 0; i < numbers.size(); i++) {
        if (sb.length() > 0) {
          sb.append(s);
        }
        sb.append(i).append("=").append(numbers.get(i));

        arr.add(numbers.get(i));
      }
      String strProp = jsonArrayFunctions.toStringProp(arr, s);
      assertEquals(sb.toString(), strProp);
    }
  }

  @Test
  void coerceToJsonArray() throws ParserException {
    JsonArray arr = jsonArrayFunctions.coerceToJsonArray("hello");
    assertEquals(1, arr.size());
    assertTrue(arr.get(0).isJsonPrimitive());
    assertEquals("hello", arr.get(0).getAsString());

    arr = jsonArrayFunctions.coerceToJsonArray(arr);
    assertEquals(1, arr.size());
    assertTrue(arr.get(0).isJsonPrimitive());
    assertEquals("hello", arr.get(0).getAsString());

    for (int i = 0; i < 100; i++) {
      int rand = random.nextInt();
      arr = jsonArrayFunctions.coerceToJsonArray(rand);
      assertEquals(1, arr.size());
      assertTrue(arr.get(0).isJsonPrimitive());
      assertTrue(arr.get(0).getAsJsonPrimitive().isNumber());
      assertEquals(rand, arr.get(0).getAsInt());
    }
  }

  @Test
  void concatenate() {
    List<Integer> l1 = new ArrayList<>();
    List<Integer> l2 = new ArrayList<>();
    List<Integer> l3 = new ArrayList<>();

    for (int i = 0; i < 100; i++) {
      l1.add(random.nextInt());
      l2.add(random.nextInt());
      l3.add(random.nextInt());
    }

    List<Integer> totalList = new ArrayList<>();
    totalList.addAll(l1);
    totalList.addAll(l2);
    totalList.addAll(l3);

    JsonArray arr1 = new JsonArray();
    JsonArray arr2 = new JsonArray();
    JsonArray arr3 = new JsonArray();
    for (Integer i : l1) {
      arr1.add(i);
    }
    for (Integer i : l2) {
      arr2.add(i);
    }
    for (Integer i : l3) {
      arr3.add(i);
    }

    JsonArray concatArr = jsonArrayFunctions.concatenate(List.of(arr1, arr2, arr3));

    assertEquals(totalList.size(), concatArr.size());
    for (int i = 0; i < totalList.size(); i++) {
      assertEquals(totalList.get(i).intValue(), concatArr.get(i).getAsInt());
    }
  }

  @Test
  void concatenateList() throws ParserException {
    List<Integer> l1 = new ArrayList<>();
    List<Integer> l2 = new ArrayList<>();

    for (int i = 0; i < 400; i++) {
      l1.add(random.nextInt());
      l2.add(random.nextInt());
    }

    List<Integer> totalList = new ArrayList<>();
    totalList.addAll(l1);
    totalList.addAll(l2);

    JsonArray arr1 = new JsonArray();
    for (Integer i : l1) {
      arr1.add(i);
    }

    JsonArray concatArr = jsonArrayFunctions.concatenate(arr1, l2);

    assertEquals(totalList.size(), concatArr.size());
    for (int i = 0; i < totalList.size(); i++) {
      assertEquals(totalList.get(i).intValue(), concatArr.get(i).getAsInt());
    }
  }

  @Test
  void remove() {
    JsonArray arr = new JsonArray();
    List<Integer> list = new ArrayList<>();

    for (int i = 0; i < 1000; i++) {
      int rand = random.nextInt();
      list.add(rand);
      arr.add(rand);
    }

    for (int i = 0; i < 100; i++) {
      int remove = random.nextInt(list.size());
      list.remove(remove);
      arr.remove(remove);

      assertEquals(list.size(), arr.size());

      for (int j = 0; j < list.size(); j++) {
        assertEquals(list.get(j).intValue(), arr.get(j).getAsInt());
      }
    }
  }

  @Test
  void sortAscending() {
    JsonArray arr = new JsonArray();
    List<Integer> list = new ArrayList<>();

    for (int i = 0; i < 1000; i++) {
      int rand = random.nextInt();
      list.add(rand);
      arr.add(rand);
    }

    Collections.sort(list);
    JsonArray arr2 = jsonArrayFunctions.sortAscending(arr);

    assertEquals(list.size(), arr2.size());

    for (int i = 0; i < list.size(); i++) {
      assertEquals(list.get(i).intValue(), arr2.get(i).getAsInt());
    }
  }

  @Test
  void sortDescending() {
    JsonArray arr = new JsonArray();
    List<Integer> list = new ArrayList<>();

    for (int i = 0; i < 1000; i++) {
      int rand = random.nextInt();
      list.add(rand);
      arr.add(rand);
    }

    Collections.sort(list, Collections.reverseOrder());
    JsonArray arr2 = jsonArrayFunctions.sortDescending(arr);

    assertEquals(list.size(), arr2.size());

    for (int i = 0; i < list.size(); i++) {
      assertEquals(list.get(i).intValue(), arr2.get(i).getAsInt());
    }
  }

  @Test
  void sortObjectsAscending() throws ParserException {
    JsonArray arr1 = new JsonArray();
    for (int i = 0; i < 100; i++) {
      JsonObject obj = new JsonObject();
      obj.add("key", new JsonPrimitive(random.nextInt()));
      obj.add("key1", new JsonPrimitive(1));
      arr1.add(obj);
    }

    assertThrows(
        ParserException.class,
        () -> jsonArrayFunctions.sortObjectsAscending(arr1, List.of("not a key")));
    JsonArray arr2 = jsonArrayFunctions.sortObjectsAscending(arr1, List.of("key"));

    for (int i = 1; i < arr2.size(); i++) {
      int val1 = arr2.get(i - 1).getAsJsonObject().get("key").getAsInt();
      int val2 = arr2.get(i).getAsJsonObject().get("key").getAsInt();

      assertTrue(val1 <= val2, val1 + " should be less than / equal to " + val2);
    }

    arr2 = jsonArrayFunctions.sortObjectsAscending(arr1, List.of("key1", "key"));
    for (int i = 1; i < arr2.size(); i++) {
      int val1 = arr2.get(i - 1).getAsJsonObject().get("key").getAsInt();
      int val2 = arr2.get(i).getAsJsonObject().get("key").getAsInt();

      assertTrue(val1 <= val2, val1 + " should be less than / equal to " + val2);
    }
  }

  @Test
  void sorObjectsDescending() {}

  @Test
  void contains() {}

  @Test
  void shuffle() {}

  @Test
  void reverse() {}

  @Test
  void isEmpty() {}

  @Test
  void count() {}

  @Test
  void indexOf() {}

  @Test
  void merge() {}

  @Test
  void unique() {}

  @Test
  void removeAll() {}

  @Test
  void union() {}

  @Test
  void intersection() {}

  @Test
  void difference() {}

  @Test
  void isSubset() {}

  @Test
  void removeFirst() {}

  @Test
  void jsonArrayAsMTScriptList() {}

  @Test
  void parseJsonArray() {}

  @Test
  void jsonArrayToListOfStrings() {}

  @Test
  void shallowCopy() {}
}
