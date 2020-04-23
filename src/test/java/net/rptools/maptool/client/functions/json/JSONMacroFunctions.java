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

import com.jayway.jsonpath.JsonPath;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import net.rptools.maptool.client.functions.JSONMacroFunctionsOld;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.util.Assert;

class TestJSONMacroFunctions {
  // a list of json objects ready to be used in tests
  public static final HashMap<String, JSONObject> o_samples = new HashMap<String, JSONObject>();
  public static final HashMap<String, JSONArray> a_samples = new HashMap<String, JSONArray>();
  // MT uses bigdecimal 0 and 1 as value for boolean operations
  public static final BigDecimal _true = new BigDecimal(1);
  public static final BigDecimal _false = new BigDecimal(0);
  private static final Parser parser = new Parser();

  /** An alias to childEvaluate. */
  Object run(String function_name, Object json, List<Object> keys) throws ParserException {
    List<Object> fparams = new ArrayList<Object>();
    fparams.add(json);
    fparams.addAll(keys);
    return JSONMacroFunctionsOld.getInstance().childEvaluate(parser, function_name, fparams);
  }

  /**
   * An alias to childEvaluate.
   *
   * @param functionName the name of the function
   * @param objs the arguments of the function
   * @return the output of the function
   * @throws ParserException if the macro function throws a parser exception
   */
  Object run(String functionName, Object... objs) throws ParserException {
    List<Object> params = new ArrayList<>(Arrays.asList(objs));
    return JSONMacroFunctionsOld.getInstance().childEvaluate(parser, functionName, params);
  }

  // @BeforeAll
  static void setUpOnce() {
    InputStream is = TestJSONMacroFunctions.class.getResourceAsStream("json_macro.json");
    JSONObject json_from_file = null;
    if (is != null) {
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
      json_from_file =
          JSONObject.fromObject(reader.lines().collect(Collectors.joining(System.lineSeparator())));
    }

    if (json_from_file != null) {
      for (Object k : json_from_file.keySet()) {
        Object value = json_from_file.get(k);
        if (value instanceof JSONObject) {
          o_samples.put((String) k, (JSONObject) value);
        }
        if (value instanceof JSONArray) {
          a_samples.put((String) k, (JSONArray) value);
        }
      }
    }
  }

  private static Object sanitize(Object obj) {
    // MT always use BigDecimal for the json type "Number"
    // the Java lib may use Integer or Double, sanitize the java data9
    // to be ready for comparisons
    if (obj instanceof Integer) {
      return new BigDecimal((Integer) obj);
    }
    if (obj instanceof Double) {
      // what a drag, round up double and strip trailing zeros to avoid test failing
      // like "12.15000" does not equal "12.15", there may be a better solution
      return (new BigDecimal((Double) obj)).setScale(5, RoundingMode.HALF_UP).stripTrailingZeros();
    }
    return obj;
  }

  // java to mt json object
  // take a json java object and use MT fonctions to build an identical json object (hopefully)
  private static JSONObject j2m(JSONObject o) {
    return o; // TODO
  }

  // asJSON works on complex json type (objects and arrays)
  // jsonify works on simple types (number, strings, booleans)
  // this function merges the 2 functionality in one.
  private static Object jsonify(Object mt_value) {
    if (mt_value instanceof String) {
      String trimmed = ((String) mt_value).trim();
      // complex json data types
      if (trimmed.startsWith("[") || trimmed.startsWith("{")) {
        return JSONMacroFunctionsOld.asJSON(mt_value);
      }
      // simple json data types
      return JSONMacroFunctionsOld.jsonify(mt_value);
    }
    return mt_value;
  }

  // @Test
  // @DisplayName("JSONGet testing.")
  void testJSONGet() throws ParserException {
    // test the Maptool json.get impl. against the Java JSON library get implementation
    for (String k : o_samples.keySet()) {
      JSONObject jobj = o_samples.get(k);
      for (Object field : jobj.keySet()) {
        Object java_value = jobj.get(field);
        Object mt_value = run("json.get", jobj, field);
        // while both may use different data types, their string representation must match
        assertEquals(java_value.toString(), mt_value.toString());
        // however calling jsonify on MT values should make it match the internal java type
        // there's a limitation of MT not able to encode properly "true", "false", and "null"
        String[] blacklist = {"true", "false", "null"};
        if (!Arrays.stream(blacklist).anyMatch(java_value::equals)) {
          assertEquals(sanitize(java_value), jsonify(mt_value));
        }
      }
    }
  }

  // @Test
  // @DisplayName("JSONSet testing.")
  void testJSONSet() throws ParserException {
    // test the Maptool json.get impl. against the Java JSON library set implementation
    for (String k : o_samples.keySet()) {
      JSONObject src = o_samples.get(k);
      JSONObject dst = new JSONObject();
      // iterate over all src pairs k,v and use MT json.set to build a new, yet identical object
      for (Object field : src.keySet()) {
        Object java_value = src.get(field);
        dst = (JSONObject) run("json.set", dst, field, java_value);
      }
      // eventually they have to match
      assertEquals(src, dst);
    }
  }

  // @Test
  // @DisplayName("JSONEmpty testing.")
  void testJSONEmpty() throws ParserException {
    BigDecimal _true = new BigDecimal(1);
    BigDecimal _false = new BigDecimal(0);
    // explicit use of test fixtures
    assertEquals(_true, (BigDecimal) run("json.isEmpty", o_samples.get("o_empty")));
    assertEquals(_false, (BigDecimal) run("json.isEmpty", o_samples.get("o_nested")));
    assertEquals(_true, (BigDecimal) run("json.isEmpty", a_samples.get("a_empty")));
    assertEquals(_false, (BigDecimal) run("json.isEmpty", a_samples.get("a_nested")));

    // using json.set("", "key", "v") should not yield an empty object
    assertEquals(_false, (BigDecimal) run("json.isEmpty", run("json.set", "", "a_key", "a_value")));
    // adding and removing a key from an already empty json object should yield an empty object
    assertEquals(
        _true,
        (BigDecimal)
            run(
                "json.isEmpty",
                run("json.remove", run("json.set", "", "a_key", "a_value"), "a_key")));
    // iterate over all fixtures objects and test the MT impl. against the java impl.
    for (String k : o_samples.keySet()) {
      JSONObject jobj = o_samples.get(k);
      // match MT impl. against java library
      assertEquals(jobj.isEmpty() ? _true : _false, (BigDecimal) run("json.isEmpty", jobj));
    }
  }

  // @DisplayName("JSONLength testing.")
  void testJSONLength() throws ParserException {
    for (JSONArray java_array : a_samples.values()) {
      JSONArray mt_array = JSONArray.fromObject("[]");
      for (Object elem : java_array) {
        mt_array = (JSONArray) run("json.append", mt_array, elem);
      }
      assertEquals(sanitize(java_array.size()), run("json.length", mt_array));
    }
  }

  /** JSON example from https://github.com/json-path/JsonPath/ */
  private static final String JSON =
      "{ \"store\": {"
          + "    \"book\": [ "
          + "      { \"category\": \"reference\","
          + "        \"author\": \"Nigel Rees\","
          + "        \"title\": \"Sayings of the Century\","
          + "        \"price\": 8.95"
          + "      },\n"
          + "      { \"category\": \"fiction\","
          + "        \"author\": \"Evelyn Waugh\","
          + "        \"title\": \"Sword of Honour\","
          + "        \"price\": 12.99"
          + "      },"
          + "      { \"category\": \"fiction\","
          + "        \"author\": \"Herman Melville\","
          + "        \"title\": \"Moby Dick\","
          + "        \"isbn\": \"0-553-21311-3\","
          + "        \"price\": 8.99"
          + "      },\n"
          + "      { \"category\": \"fiction\","
          + "        \"author\": \"J. R. R. Tolkien\","
          + "        \"title\": \"The Lord of the Rings\","
          + "        \"isbn\": \"0-395-19395-8\","
          + "        \"price\": 22.99"
          + "      }\n"
          + "    ],\n"
          + "    \"bicycle\": {"
          + "      \"color\": \"red\","
          + "      \"price\": 19.95"
          + "    }\n"
          + "  }, \"expensive\": 10"
          + "}";

  /** Path examples from https://github.com/json-path/JsonPath. */
  private static final String[] PATHS = {
    "$.store.book[*].author",
    "$..author",
    "$.store.*",
    "$.store..price",
    "$..book[2]",
    "$..book[-2]",
    "$..book[0,1]",
    "$..book[:2]",
    "$..book[1:2]",
    "$..book[-2:]",
    "$..book[2:]",
    "$..book[?(@.isbn)]",
    "$.store.book[?(@.price < 10)]",
    "$..book[?(@.price <= $['expensive'])]",
    "$..book[?(@.author =~ /.*REES/i)]",
    "$..*",
    "$..book.length()"
  };

  /** Tests the JSON and paths from https://github.com/json-path/JsonPath. */
  @Test
  void testJsonPathRead() {
    String fName = "json.path.read";
    String msg = "Incorrect output for path %s";
    String excMsg = "Parser exception '%s' for path %s.";
    for (String path : PATHS) {
      try {
        String mtOutput = run(fName, JSON, path).toString();
        String jwOutput = JsonPath.read(JSON, path).toString();
        Assert.equals(jwOutput, mtOutput, String.format(msg, path));
      } catch (ParserException e) {
        fail(String.format(excMsg, e.getLocalizedMessage(), path));
      }
    }
  }

  /** Test json.difference() when Strings are used as arguments (fixed bug #1177). */
  @Test
  void testJsonDifferenceWithStrings() throws ParserException {
    Assert.equals("[]", run("json.difference", "test", "test").toString());
  }
  /** Test json.isEmpty() when String is used as argument (fixed bug #1167). */
  @Test
  void testJsonIsEmptyWithString() throws ParserException {
    Assert.equals(BigDecimal.ZERO, run("json.isEmpty", "a"));
  }
  /** Test json.set() when empty String is used as argument (fixed bug #1151). */
  @Test
  void testJsonSetWithEmptyString() throws ParserException {
    Assert.equals("{\"field\":\"\"}", run("json.set", "{}", "field", "").toString());
  }
  /** Test json.get() returning empty string if field doesn't exist (fixed bug #1121). */
  @Test
  void testJsonGetMissingField() throws ParserException {
    Assert.equals("", run("json.get", "{}", "field"));
  }
  /** Test json.get() when String is used as argument (fixed bug #1144). */
  @Test
  void testJsonGetWithString() throws ParserException {
    Assert.equals("test", run("json.get", "test", 0));
  }
  /** Test json.append() when number is surrounded by white spaces (fixed bug #1139). */
  @Test
  void testJsonAppendWhiteSpaces() throws ParserException {
    Assert.equals("[\" 0 \"]", run("json.append", "[]", " 0 ").toString());
  }
  /** Test json.toList() when quotes are in the json used as argument (fixed bug #1143). */
  @Test
  void testJsonToListWithQuotes() throws ParserException {
    Assert.equals("\"", run("json.toList", "[\"\\\"\"]").toString());
  }
  /** Test json.type() with a json array (fixed bug #1125). */
  @Test
  void testJsonTypeArray() throws ParserException {
    Assert.equals("ARRAY", run("json.type", "[]"));
  }
}
