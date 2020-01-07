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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.rptools.parser.ParserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JsonObjectFunctionsTest {

  private JsonObjectFunctions jsonObjectFunctions;
  private String jsonObjString1 = "{A:1, b:2, c:null, d:true, e:false, f:\"hello\"}";
  private String jsonObjString2 = "{A:1, b:2, c:null, d:true, e:false, f:\"world\"}";
  private String jsonObjString3 = "{test: 123, thisIsATest: 'testing' }";
  private JsonObject jsonObject1;
  private JsonObject jsonObject2;
  private JsonObject jsonObject3;
  private JsonObject jsonEmpty;
  Random random;

  private String getRandomKey(JsonObject jsonObject) {
    int numKeys = jsonObject.keySet().size();
    String[] keyArray = jsonObject.keySet().toArray(new String[numKeys]);
    return keyArray[random.nextInt(numKeys)];
  }

  @BeforeEach
  void setup() {
    JsonParser jsonParser = new JsonParser();
    JsonMTSTypeConversion typeConversion = new JsonMTSTypeConversion(jsonParser);
    random = new Random(System.currentTimeMillis());

    jsonObjectFunctions = new JsonObjectFunctions(typeConversion);

    jsonObject1 = jsonParser.parse(jsonObjString1).getAsJsonObject();
    jsonObject2 = jsonParser.parse(jsonObjString2).getAsJsonObject();
    jsonObject3 = jsonParser.parse(jsonObjString3).getAsJsonObject();

    jsonEmpty = new JsonObject();
  }

  @Test
  void fromStrProp() throws ParserException {
    JsonObject jsonObject = jsonObjectFunctions.fromStrProp("A=1,b=7,t=3,f=hello", ",");
    assertEquals(4, jsonObject.keySet().size(), "JsonObject has 4 keys");
    assertTrue(jsonObject.has("A"), "JsonObject has key 'A'");
    JsonElement jsonElement = jsonObject.get("A");
    assertTrue(jsonElement.isJsonPrimitive(), "Value for 'A' is a primitive");
    assertTrue(jsonElement.getAsJsonPrimitive().isNumber(), "Value for 'A' is a number");
    assertEquals(1, jsonElement.getAsInt(), "Value for 'A' is 1");

    assertTrue(jsonObject.has("b"), "JsonObject has key 'b'");
    jsonElement = jsonObject.get("b");
    assertTrue(jsonElement.isJsonPrimitive(), "Value for 'b' is a primitive");
    assertTrue(jsonElement.getAsJsonPrimitive().isNumber(), "Value for 'b' is a number");
    assertEquals(7, jsonElement.getAsInt(), "Value for 'b' is 7");

    assertTrue(jsonObject.has("t"), "JsonObject has key 't'");
    jsonElement = jsonObject.get("t");
    assertTrue(jsonElement.isJsonPrimitive(), "Value for 't' is a primitive");
    assertTrue(jsonElement.getAsJsonPrimitive().isNumber(), "Value for 't' is a number");
    assertEquals(3, jsonElement.getAsInt(), "Value for 't' is 3");

    assertTrue(jsonObject.has("f"), "JsonObject has key 'f'");
    jsonElement = jsonObject.get("f");
    assertTrue(jsonElement.isJsonPrimitive(), "Value for 'f' is a primitive");
    assertTrue(jsonElement.getAsJsonPrimitive().isString(), "Value for 'f' is a String");
    assertEquals("hello", jsonElement.getAsString(), "Value for 'f' is hello");

    jsonObject = jsonObjectFunctions.fromStrProp("A=1:b=7:t=3:f=hello", ":");
    assertEquals(4, jsonObject.keySet().size(), "JsonObject has 4 keys");
    assertTrue(jsonObject.has("A"), "JsonObject has key 'A'");
    jsonElement = jsonObject.get("A");
    assertTrue(jsonElement.isJsonPrimitive(), "Value for 'A' is a primitive");
    assertTrue(jsonElement.getAsJsonPrimitive().isNumber(), "Value for 'A' is a number");
    assertEquals(1, jsonElement.getAsInt(), "Value for 'A' is 1");

    assertTrue(jsonObject.has("b"), "JsonObject has key 'b'");
    jsonElement = jsonObject.get("b");
    assertTrue(jsonElement.isJsonPrimitive(), "Value for 'b' is a primitive");
    assertTrue(jsonElement.getAsJsonPrimitive().isNumber(), "Value for 'b' is a number");
    assertEquals(7, jsonElement.getAsInt(), "Value for 'b' is 7");

    assertTrue(jsonObject.has("t"), "JsonObject has key 't'");
    jsonElement = jsonObject.get("t");
    assertTrue(jsonElement.isJsonPrimitive(), "Value for 't' is a primitive");
    assertTrue(jsonElement.getAsJsonPrimitive().isNumber(), "Value for 't' is a number");
    assertEquals(3, jsonElement.getAsInt(), "Value for 't' is 3");

    assertTrue(jsonObject.has("f"), "JsonObject has key 'f'");
    jsonElement = jsonObject.get("f");
    assertTrue(jsonElement.isJsonPrimitive(), "Value for 'f' is a primitive");
    assertTrue(jsonElement.getAsJsonPrimitive().isString(), "Value for 'f' is a String");
    assertEquals("hello", jsonElement.getAsString(), "Value for 'f' is hello");

    jsonObject = jsonObjectFunctions.fromStrProp("A=1,b=7,t=3,f=hello", ";");
    assertEquals(1, jsonObject.keySet().size());
    assertTrue(jsonObject.has("A"), "JsonObject has keu 'A'");
    jsonElement = jsonObject.get("A");
    assertTrue(jsonElement.getAsJsonPrimitive().isString(), "Value for 'A' is a String.");
    assertEquals("1,b=7,t=3,f=hello", jsonElement.getAsString());
  }

  @Test
  void toStrProp() throws ParserException {
    String strProp = jsonObjectFunctions.toStringProp(jsonObject1, ",");
    assertEquals("A=1,b=2,c=null,d=true,e=false,f=hello", strProp);
    strProp = jsonObjectFunctions.toStringProp(jsonObject1, ";");
    assertEquals("A=1;b=2;c=null;d=true;e=false;f=hello", strProp);
    strProp = jsonObjectFunctions.toStringProp(jsonObject1, ":");
    assertEquals("A=1:b=2:c=null:d=true:e=false:f=hello", strProp);
  }

  @Test
  void remove() {
    JsonObject cloned = jsonObject1.deepCopy();

    for (int i = 0; i < 20; i++) {
      String key = getRandomKey(cloned);
      JsonObject jsonObj = jsonObjectFunctions.remove(cloned, key);
      assertNotSame(cloned, jsonObj, "remove should return new copy");
      assertEquals(jsonObject1, cloned, "remove should not modify passed in object");
      assertTrue(cloned.has(key), "remove should not modify passed in object");
      assertNotEquals(cloned, jsonObj, "remove should not return same result");
      assertFalse(jsonObj.has(key), "Key '" + key + "' should have been removed");

      String key1 = getRandomKey(jsonObj);
      JsonObject jsonObj2 = jsonObjectFunctions.remove(jsonObj, key1);
      assertNotSame(jsonObj, jsonObj2, "remove should return new copy");
      assertTrue(cloned.has(key), "remove should not modify passed in object");
      assertTrue(cloned.has(key1), "remove should not modify passed in object");
      assertTrue(jsonObj.has(key1), "remove should not modify passed in object");
      assertNotEquals(jsonObj, jsonObj2, "remove should not return same result");
      assertFalse(jsonObj2.has(key), "Key '" + key + "' should have been removed");
      assertFalse(jsonObj2.has(key1), "Key '" + key1 + "' should have been removed");
    }
  }

  @Test
  void removeEverything() {

    for (int i = 0; i < 10; i++) {
      JsonObject jobj = jsonObject1;

      while (jobj.keySet().size() > 0) {
        String key = getRandomKey(jobj);
        JsonObject jobj2 = jsonObjectFunctions.remove(jobj, key);
        assertNotSame(jobj, jobj2, "remove should return new copy");
        assertTrue(jobj.has(key), "remove should not modify passed in object");
        assertFalse(jobj2.has(key), "Key '" + key + "' should have been removed");

        jobj = jobj2;
      }

      assertEquals(0, jobj.keySet().size(), "All keys should be removed");
      assertEquals("{}", jobj.toString(), "The JsonObject should be an empty object");
      assertEquals(jsonEmpty, jobj, "The JsonObject should be an empty object");
      assertFalse(jobj.isJsonNull(), "Should be an empty object not null");
    }
  }

  @Test
  void removeNonExisting() {
    JsonObject jobj = jsonObject1;
    for (int i = 0; i < 100; i++) {
      String key = "NOT_THERE_" + i;
      jobj = jsonObjectFunctions.remove(jobj, key);
      assertEquals(jsonObject1, jobj, "Nothing should have been removed");
    }
  }

  @Test
  void contains() {
    for (int i = 0; i < 100; i++) {
      String key = getRandomKey(jsonObject1);
      assertTrue(jsonObjectFunctions.contains(jsonObject1, key));
      assertFalse(jsonObjectFunctions.contains(jsonObject1, key + "__" + i));
    }
  }

  @Test
  void isEmpty() {
    assertFalse(jsonObjectFunctions.isEmpty(jsonObject1));
    assertTrue(jsonObjectFunctions.isEmpty(jsonEmpty));
  }

  @Test
  void merge() {
    JsonObject result = jsonObjectFunctions.merge(List.of(jsonObject1, jsonEmpty));
    assertEquals(jsonObject1, result, "merge with empty object");
    result = jsonObjectFunctions.merge(List.of(jsonObject2, jsonEmpty));
    assertEquals(jsonObject2, result, "merge with empty object");
    result = jsonObjectFunctions.merge(List.of(jsonObject3, jsonEmpty));
    assertEquals(jsonObject3, result, "merge with empty object");

    result = jsonObjectFunctions.merge(List.of(jsonObject1, jsonObject2));
    assertNotEquals(jsonObject1, result);

    assertTrue(result.keySet().containsAll(jsonObject1.keySet()));
    assertTrue(result.keySet().containsAll(jsonObject2.keySet()));

    for (String key : jsonObject2.keySet()) {
      assertEquals(jsonObject2.get(key), result.get(key));
    }

    Set<String> keys = new HashSet<>(jsonObject1.keySet());
    keys.removeAll(jsonObject2.keySet());

    for (String key : keys) {
      assertEquals(jsonObject1.get(key), result.get(key));
    }

    assertTrue(result.keySet().containsAll(jsonObject1.keySet()));
    assertTrue(result.keySet().containsAll(jsonObject2.keySet()));

    for (String key : jsonObject2.keySet()) {
      assertEquals(jsonObject2.get(key), result.get(key));
    }

    result = jsonObjectFunctions.merge(List.of(jsonObject1, jsonObject3));
    keys = new HashSet<>(jsonObject1.keySet());
    keys.removeAll(jsonObject3.keySet());

    for (String key : keys) {
      assertEquals(jsonObject1.get(key), result.get(key));
    }

    assertTrue(result.keySet().containsAll(jsonObject1.keySet()));
    assertTrue(result.keySet().containsAll(jsonObject3.keySet()));
  }

  @Test
  void removeAll() {
    JsonObject jobj = new JsonObject();
    jobj.addProperty("test1", "This is a test");
    jobj.addProperty("test2", "This is another test");
    jobj.addProperty("test3", "Yet another test");
    jobj.addProperty("test4", "Another test?");
    jobj.addProperty("test5", "This is getting monotonous");

    JsonObject remove1 = new JsonObject();
    remove1.addProperty("test3", "doesn't matter");
    JsonObject remove2 = new JsonObject();
    remove2.addProperty("test2", "doesn't matter");
    remove2.addProperty("test4", "doesn't matter");

    JsonObject result = jsonObjectFunctions.removeAll(jobj, List.of(remove1));

    assertEquals(4, result.size());
    assertFalse(result.has("test3"));

    result = jsonObjectFunctions.removeAll(jobj, List.of(remove1, remove2));

    assertEquals(2, result.size());
    assertFalse(result.has("test2"));
    assertFalse(result.has("test3"));
    assertFalse(result.has("test4"));
  }

  @Test
  void shallowCopy() {
    JsonObject jobj = new JsonObject();
    jobj.addProperty("test1", "This is a test");
    jobj.addProperty("test2", "This is another test");
    jobj.addProperty("test3", "Yet another test");
    jobj.addProperty("test4", "Another test?");
    jobj.addProperty("test5", "This is getting monotonous");

    JsonObject job2 = new JsonObject();
    job2.addProperty("hello", "world");

    jobj.add("hello", job2);

    JsonObject copy = jsonObjectFunctions.shallowCopy(jobj);

    assertEquals(jobj.size(), copy.size());

    for (String key : jobj.keySet()) {
      assertSame(jobj.get(key), copy.get(key));
    }
  }
}
