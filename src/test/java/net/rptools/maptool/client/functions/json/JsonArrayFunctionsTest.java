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
  void sortObjectsDescending() throws ParserException {
    JsonArray arr1 = new JsonArray();
    for (int i = 0; i < 100; i++) {
      JsonObject obj = new JsonObject();
      obj.add("key", new JsonPrimitive(random.nextInt()));
      obj.add("key1", new JsonPrimitive(1));
      arr1.add(obj);
    }

    assertThrows(
        ParserException.class,
        () -> jsonArrayFunctions.sortObjectsDescending(arr1, List.of("not a key")));
    JsonArray arr2 = jsonArrayFunctions.sortObjectsDescending(arr1, List.of("key"));

    for (int i = 1; i < arr2.size(); i++) {
      int val1 = arr2.get(i - 1).getAsJsonObject().get("key").getAsInt();
      int val2 = arr2.get(i).getAsJsonObject().get("key").getAsInt();

      assertTrue(val1 >= val2, val1 + " should be less than / equal to " + val2);
    }

    arr2 = jsonArrayFunctions.sortObjectsDescending(arr1, List.of("key1", "key"));
    for (int i = 1; i < arr2.size(); i++) {
      int val1 = arr2.get(i - 1).getAsJsonObject().get("key").getAsInt();
      int val2 = arr2.get(i).getAsJsonObject().get("key").getAsInt();

      assertTrue(val1 >= val2, val1 + " should be less than / equal to " + val2);
    }
  }

  @Test
  void contains() throws ParserException {
    var intList = new ArrayList<Integer>();
    JsonArray jarr = new JsonArray();
    for (int i = 0; i < 100; i++) {
      int rnd = new Random().nextInt();
      intList.add(rnd);
      jarr.add(rnd);
    }
    for (Integer i : intList) {
      assertTrue(jsonArrayFunctions.contains(jarr, i));
    }

    for (int i = 0; i < 1000; i++) {
      int rnd = random.nextInt();
      assertEquals(intList.contains(rnd), jsonArrayFunctions.contains(jarr, rnd));
    }
  }

  @Test
  void shuffle() {
    JsonArray jarr = new JsonArray();
    for (int i = 0; i < 1000; i++) {
      jarr.add(random.nextInt());
    }

    // be prepared to run shuffle a few times as its possible (although unlikely that a shuffle
    // could return everything in the same order.
    JsonArray res = null;
    for (int i = 0; i < 100; i++) {
      res = jsonArrayFunctions.shuffle(jarr);
      if (!res.equals(jarr)) {
        break;
      }
    }
    assertEquals(jarr.size(), res.size());

    for (int i = 0; i < res.size(); i++) {
      assertTrue(res.contains(jarr.get(i)));
    }

    assertNotEquals(jarr, res);
  }

  @Test
  void reverse() {
    var intList = new ArrayList<Integer>();
    JsonArray jarr = new JsonArray();
    for (int i = 0; i < 100; i++) {
      int rnd = random.nextInt();
      intList.add(rnd);
      jarr.add(rnd);
    }

    Collections.reverse(intList);
    JsonArray rev = jsonArrayFunctions.reverse(jarr);
    assertEquals(jarr.size(), rev.size());

    for (int i = 0; i < intList.size(); i++) {
      assertEquals(intList.get(i).intValue(), rev.get(i).getAsJsonPrimitive().getAsInt());
    }

    rev = jsonArrayFunctions.reverse(rev);
    assertNotSame(jarr, rev);
    assertEquals(jarr, rev);
  }

  @Test
  void isEmpty() {
    JsonArray jarr = new JsonArray();
    assertTrue(jsonArrayFunctions.isEmpty(jarr));
    jarr.add(1);
    assertFalse(jsonArrayFunctions.isEmpty(jarr));
    jarr.remove(0);
    assertTrue(jsonArrayFunctions.isEmpty(jarr));
    assertTrue(jsonArrayFunctions.isEmpty(JsonArrayFunctions.EMPTY_JSON_ARRAY));
  }

  @Test
  void count() {
    JsonArray jarr = new JsonArray();
    for (int i = 0; i < 10; i++) {
      jarr.add("test1");
    }
    for (int i = 0; i < 15; i++) {
      jarr.add("test2");
    }
    for (int i = 0; i < 17; i++) {
      jarr.add("test1");
    }

    JsonPrimitive jeleTest1 = new JsonPrimitive("test1");
    JsonPrimitive jeleTest2 = new JsonPrimitive("test2");
    assertEquals(27, jsonArrayFunctions.count(jarr, jeleTest1, 0));
    assertEquals(17, jsonArrayFunctions.count(jarr, jeleTest1, 10));
    assertEquals(15, jsonArrayFunctions.count(jarr, jeleTest2, 0));
    assertEquals(1, jsonArrayFunctions.count(jarr, jeleTest2, 24));
    assertEquals(0, jsonArrayFunctions.count(jarr, jeleTest2, 25));
  }

  @Test
  void indexOf() {
    var intList = new ArrayList<Integer>();
    JsonArray jarr = new JsonArray();
    for (int i = 0; i < 100; i++) {
      intList.add(i);
      jarr.add(i);
    }

    for (int i = 0; i < 100; i++) {
      JsonPrimitive value = new JsonPrimitive(intList.get(i));
      assertEquals(i, jsonArrayFunctions.indexOf(jarr, value, 0));
    }

    for (int i = 1; i < 100; i++) {
      JsonPrimitive value = new JsonPrimitive(intList.get(i));
      assertEquals(i, jsonArrayFunctions.indexOf(jarr, value, i - 1));
    }

    for (int i = 0; i < 99; i++) {
      JsonPrimitive value = new JsonPrimitive(intList.get(i));
      assertEquals(-1, jsonArrayFunctions.indexOf(jarr, value, i + 1));
    }

    JsonArray jarr2 = new JsonArray();
    jarr2.add("Test1");
    jarr2.add("Test2");
    jarr2.add("Test3");
    jarr2.add("Test1");

    JsonPrimitive jeleTest1 = new JsonPrimitive("Test1");
    assertEquals(0, jsonArrayFunctions.indexOf(jarr2, jeleTest1, 0));
    for (int i = 1; i < jarr2.size(); i++) {
      assertEquals(3, jsonArrayFunctions.indexOf(jarr2, jeleTest1, i));
    }
  }

  @Test
  void merge() {
    JsonArray expected = new JsonArray();
    JsonArray jarr1 = new JsonArray();
    for (int i = 0; i < 100; i++) {
      expected.add(i);
      jarr1.add(i);
    }

    JsonArray jarr2 = new JsonArray();
    for (int i = 0; i < 100; i++) {
      expected.add(i);
      jarr2.add(i);
    }

    JsonArray jarr3 = new JsonArray();
    for (int i = 0; i < 100; i++) {
      expected.add(i);
      jarr3.add(i);
    }

    List<JsonArray> list = List.of(jarr1, jarr2, jarr3);
    JsonArray res = jsonArrayFunctions.merge(list);

    assertEquals(expected, res);
  }

  @Test
  void unique() {
    JsonArray jarr1 = new JsonArray();
    jarr1.add("Test1");
    jarr1.add("Test2");
    jarr1.add("Test3");
    jarr1.add("Test1");

    JsonArray res = jsonArrayFunctions.unique(jarr1);
    assertEquals(3, res.size());

    JsonArray jarr2 = new JsonArray();
    jarr2.add("Test1");
    jarr2.add("Test2");
    jarr2.add("Test3");
    jarr2.add("Test1");
    jarr2.add("Test2");
    jarr2.add("Test2");

    res = jsonArrayFunctions.unique(jarr1);
    assertEquals(3, res.size());
  }

  @Test
  void removeAll() {
    JsonArray jarr = new JsonArray();
    List<String> list = new ArrayList<String>();
    list.add("Test1");
    list.add("Test2");
    list.add("Test3");
    list.add("Test1");
    list.add("Test2");
    list.add("Test2");

    for (String s : list) {
      jarr.add(s);
    }

    JsonArray jarr1 = new JsonArray();
    jarr1.add("Test1");

    JsonArray jarr2 = new JsonArray();
    jarr2.add("Test2");

    JsonArray jarr3 = new JsonArray();
    jarr3.add("Test3");

    JsonArray jarr4 = new JsonArray();
    jarr4.add("Test4");

    JsonArray jarr5 = new JsonArray();
    jarr5.add("Test2");
    jarr5.add("Test3");

    JsonArray res = jsonArrayFunctions.removeAll(jarr, List.of(jarr1));
    List<String> expected = new ArrayList<>(list);
    expected.removeAll(List.of("Test1"));
    assertEquals(expected.size(), res.size());
    for (int i = 0; i < expected.size(); i++) {
      assertEquals(expected.get(i), res.get(i).getAsString());
    }

    res = jsonArrayFunctions.removeAll(jarr, List.of(jarr2));
    expected = new ArrayList<>(list);
    expected.removeAll(List.of("Test2"));
    assertEquals(expected.size(), res.size());
    for (int i = 0; i < expected.size(); i++) {
      assertEquals(expected.get(i), res.get(i).getAsString());
    }

    res = jsonArrayFunctions.removeAll(jarr, List.of(jarr3));
    expected = new ArrayList<>(list);
    expected.removeAll(List.of("Test3"));
    assertEquals(expected.size(), res.size());
    for (int i = 0; i < expected.size(); i++) {
      assertEquals(expected.get(i), res.get(i).getAsString());
    }

    res = jsonArrayFunctions.removeAll(jarr, List.of(jarr4));
    expected = new ArrayList<>(list);
    expected.removeAll(List.of("Test4"));
    assertEquals(expected.size(), res.size());
    for (int i = 0; i < expected.size(); i++) {
      assertEquals(expected.get(i), res.get(i).getAsString());
    }

    res = jsonArrayFunctions.removeAll(jarr, List.of(jarr5));
    expected = new ArrayList<>(list);
    expected.removeAll(List.of("Test2", "Test3"));
    assertEquals(expected.size(), res.size());
    for (int i = 0; i < expected.size(); i++) {
      assertEquals(expected.get(i), res.get(i).getAsString());
    }
  }

  @Test
  void union() {
    JsonArray jarr1 = new JsonArray();
    jarr1.add("Test1");
    jarr1.add("Test2");
    jarr1.add("Test2");

    JsonArray jarr2 = new JsonArray();
    jarr2.add("Test3");
    jarr2.add("Test4");

    JsonArray jarr3 = new JsonArray();
    jarr3.add("Test1");
    jarr3.add("Test4");

    JsonObject jobj1 = new JsonObject();
    jobj1.addProperty("Test5", "aaa");
    jobj1.addProperty("Test3", "aaa");
    jobj1.addProperty("Test1", "aaa");

    JsonObject jobj2 = new JsonObject();
    jobj2.addProperty("Test5", "aaa");
    jobj2.addProperty("Test3", "aaa");
    jobj2.addProperty("Test6", "aaa");

    JsonArray res = jsonArrayFunctions.union(List.of(jarr1, jarr2));
    assertEquals(4, res.size());
    assertTrue(res.contains(new JsonPrimitive("Test1")));
    assertTrue(res.contains(new JsonPrimitive("Test2")));
    assertTrue(res.contains(new JsonPrimitive("Test3")));
    assertTrue(res.contains(new JsonPrimitive("Test4")));

    res = jsonArrayFunctions.union(List.of(jarr1, jarr2, jarr3));
    assertEquals(4, res.size());
    assertTrue(res.contains(new JsonPrimitive("Test1")));
    assertTrue(res.contains(new JsonPrimitive("Test2")));
    assertTrue(res.contains(new JsonPrimitive("Test3")));
    assertTrue(res.contains(new JsonPrimitive("Test4")));

    res = jsonArrayFunctions.union(List.of(jarr1, jobj1));
    assertEquals(4, res.size());
    assertTrue(res.contains(new JsonPrimitive("Test1")));
    assertTrue(res.contains(new JsonPrimitive("Test2")));
    assertTrue(res.contains(new JsonPrimitive("Test3")));
    assertTrue(res.contains(new JsonPrimitive("Test5")));

    res = jsonArrayFunctions.union(List.of(jarr1, jobj1, jobj2));
    assertEquals(5, res.size());
    assertTrue(res.contains(new JsonPrimitive("Test1")));
    assertTrue(res.contains(new JsonPrimitive("Test2")));
    assertTrue(res.contains(new JsonPrimitive("Test3")));
    assertTrue(res.contains(new JsonPrimitive("Test5")));
    assertTrue(res.contains(new JsonPrimitive("Test6")));
  }

  @Test
  void intersection() {
    JsonArray jarr1 = new JsonArray();
    jarr1.add("Test1");
    jarr1.add("Test2");
    jarr1.add("Test2");
    jarr1.add("Test6");

    JsonArray jarr2 = new JsonArray();
    jarr2.add("Test3");
    jarr2.add("Test4");

    JsonArray jarr3 = new JsonArray();
    jarr3.add("Test1");
    jarr3.add("Test4");

    JsonObject jobj1 = new JsonObject();
    jobj1.addProperty("Test5", "aaa");
    jobj1.addProperty("Test3", "aaa");
    jobj1.addProperty("Test1", "aaa");

    JsonObject jobj2 = new JsonObject();
    jobj2.addProperty("Test5", "aaa");
    jobj2.addProperty("Test3", "aaa");
    jobj2.addProperty("Test6", "aaa");

    JsonArray res = jsonArrayFunctions.intersection(List.of(jarr1, jarr2));
    assertEquals(0, res.size());

    res = jsonArrayFunctions.intersection(List.of(jarr1, jarr3));
    assertEquals(1, res.size());
    assertTrue(res.contains(new JsonPrimitive("Test1")));

    res = jsonArrayFunctions.intersection(List.of(jarr1, jobj1));
    assertEquals(1, res.size());
    assertTrue(res.contains(new JsonPrimitive("Test1")));

    res = jsonArrayFunctions.intersection(List.of(jarr1, jobj2));
    assertEquals(1, res.size());
    assertTrue(res.contains(new JsonPrimitive("Test6")));

    res = jsonArrayFunctions.intersection(List.of(jarr3, jobj2));
    assertEquals(0, res.size());
  }

  @Test
  void difference() {
    JsonArray jarr1 = new JsonArray();
    jarr1.add("Test1");
    jarr1.add("Test2");
    jarr1.add("Test2");
    jarr1.add("Test6");

    JsonArray jarr2 = new JsonArray();
    jarr2.add("Test3");
    jarr2.add("Test4");

    JsonArray jarr3 = new JsonArray();
    jarr3.add("Test1");
    jarr3.add("Test4");

    JsonObject jobj1 = new JsonObject();
    jobj1.addProperty("Test5", "aaa");
    jobj1.addProperty("Test3", "aaa");
    jobj1.addProperty("Test1", "aaa");

    JsonObject jobj2 = new JsonObject();
    jobj2.addProperty("Test5", "aaa");
    jobj2.addProperty("Test3", "aaa");
    jobj2.addProperty("Test6", "aaa");

    JsonArray res = jsonArrayFunctions.difference(List.of(jarr1, jarr2));
    assertEquals(3, res.size());
    assertTrue(res.contains(new JsonPrimitive("Test1")));
    assertTrue(res.contains(new JsonPrimitive("Test2")));
    assertTrue(res.contains(new JsonPrimitive("Test6")));

    res = jsonArrayFunctions.difference(List.of(jarr1, jarr3));
    assertEquals(2, res.size());
    assertTrue(res.contains(new JsonPrimitive("Test2")));
    assertTrue(res.contains(new JsonPrimitive("Test6")));

    res = jsonArrayFunctions.difference(List.of(jarr1, jobj1));
    assertEquals(2, res.size());
    assertTrue(res.contains(new JsonPrimitive("Test2")));
    assertTrue(res.contains(new JsonPrimitive("Test6")));

    res = jsonArrayFunctions.difference(List.of(jarr1, jobj2));
    assertEquals(2, res.size());
    assertTrue(res.contains(new JsonPrimitive("Test1")));
    assertTrue(res.contains(new JsonPrimitive("Test2")));
  }

  @Test
  void isSubset() {
    JsonArray jarr1 = new JsonArray();
    jarr1.add("Test1");
    jarr1.add("Test2");
    jarr1.add("Test2");
    jarr1.add("Test6");

    JsonArray jarr2 = new JsonArray();
    jarr2.add("Test3");
    jarr2.add("Test4");

    JsonArray jarr3 = new JsonArray();
    jarr3.add("Test1");
    jarr3.add("Test6");

    JsonArray jarr4 = new JsonArray();
    jarr4.add("Test1");
    jarr4.add("Test1");
    jarr4.add("Test2");
    jarr4.add("Test6");

    JsonObject jobj1 = new JsonObject();
    jobj1.add("Test2", new JsonPrimitive("aaa"));
    jobj1.add("Test2", new JsonPrimitive("aaa"));

    JsonObject jobj2 = new JsonObject();
    jobj2.add("Test1", new JsonPrimitive("aaa"));
    jobj2.add("Test2", new JsonPrimitive("aaa"));

    assertFalse(jsonArrayFunctions.isSubset(List.of(jarr1, jarr2)));
    assertTrue(jsonArrayFunctions.isSubset(List.of(jarr1, jarr4)));
    assertTrue(jsonArrayFunctions.isSubset(List.of(jarr4, jarr1)));
    assertTrue(jsonArrayFunctions.isSubset(List.of(jarr4, jarr3)));
    assertFalse(jsonArrayFunctions.isSubset(List.of(jarr3, jarr4)));
    assertTrue(jsonArrayFunctions.isSubset(List.of(jarr1, jarr3, jarr4)));
    assertTrue(jsonArrayFunctions.isSubset(List.of(jarr1, jobj1)));
    assertTrue(jsonArrayFunctions.isSubset(List.of(jarr1, jobj1, jobj2)));
    assertTrue(jsonArrayFunctions.isSubset(List.of(jobj2, jobj1)));
  }

  @Test
  void removeFirst() {
    JsonArray jarr1 = new JsonArray();
    jarr1.add("Test1");
    jarr1.add("Test2");
    jarr1.add("Test2");
    jarr1.add("Test6");

    JsonArray jarr2 = new JsonArray();
    jarr2.add("Test3");
    jarr2.add("Test4");

    JsonArray jarr3 = new JsonArray();
    jarr3.add("Test2");
    jarr3.add("Test6");

    JsonArray jarr4 = new JsonArray();
    jarr4.add("Test2");
    jarr4.add("Test1");
    jarr4.add("Test2");
    jarr4.add("Test6");

    JsonArray res = jsonArrayFunctions.removeFirst(jarr1, jarr3);
    assertEquals(2, res.size());
    assertEquals("Test1", res.get(0).getAsString());
    assertEquals("Test2", res.get(1).getAsString());

    res = jsonArrayFunctions.removeFirst(jarr1, jarr2);
    assertEquals(jarr1, res);

    res = jsonArrayFunctions.removeFirst(jarr1, jarr4);
    assertEquals(0, res.size());
  }
}
