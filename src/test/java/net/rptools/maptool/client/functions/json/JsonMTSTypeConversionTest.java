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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.math.BigDecimal;
import java.util.Random;
import net.rptools.parser.ParserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JsonMTSTypeConversionTest {

  JsonMTSTypeConversion typeConversion;
  Random random;

  @BeforeEach
  void setup() {
    JsonParser jsonParser = new JsonParser();
    typeConversion = new JsonMTSTypeConversion(jsonParser);
    random = new Random(System.currentTimeMillis());
  }

  @Test
  void asScriptType() {
    assertEquals(
        "null", typeConversion.asScriptType(null), "Null type should convert to and empty string");
    assertEquals(
        "true",
        typeConversion.asScriptType(new JsonPrimitive(true)),
        "true should convert to string \"true\"");
    assertEquals(
        "false",
        typeConversion.asScriptType(new JsonPrimitive(false)),
        "true should convert to string \"true\"");
    for (int i = 0; i < 100; i++) {
      int ir = random.nextInt();
      assertEquals(BigDecimal.valueOf(ir), typeConversion.asScriptType(new JsonPrimitive(ir)));
      double id = random.nextDouble();
      assertEquals(BigDecimal.valueOf(id), typeConversion.asScriptType(new JsonPrimitive(id)));
    }
    JsonArray jsonArray = new JsonArray();
    assertEquals(jsonArray, typeConversion.asScriptType(jsonArray));
    jsonArray.add(22);
    assertEquals(jsonArray, typeConversion.asScriptType(jsonArray));
    jsonArray.add("test");
    assertEquals(jsonArray, typeConversion.asScriptType(jsonArray));
    jsonArray.add(new JsonArray());
    assertEquals(jsonArray, typeConversion.asScriptType(jsonArray));
    jsonArray.add(new JsonObject());
    assertEquals(jsonArray, typeConversion.asScriptType(jsonArray));

    JsonObject jsonObject = new JsonObject();
    assertEquals(jsonObject, typeConversion.asScriptType(jsonObject));
    jsonObject.add("test", new JsonPrimitive("test1"));
    assertEquals(jsonObject, typeConversion.asScriptType(jsonObject));
    jsonObject.add("test1", new JsonPrimitive(22));
    assertEquals(jsonObject, typeConversion.asScriptType(jsonObject));
    jsonObject.add("test2", jsonArray);
    assertEquals(jsonObject, typeConversion.asScriptType(jsonObject));
    jsonObject.add("test3", new JsonObject());
    assertEquals(jsonObject, typeConversion.asScriptType(jsonObject));
  }

  @Test
  void asJsonElement() throws ParserException {
    JsonObject jsonObject = new JsonObject();
    assertEquals(jsonObject, typeConversion.asJsonElement(jsonObject.toString()));
    jsonObject.add("test", new JsonPrimitive("test1"));
    assertEquals(jsonObject, typeConversion.asJsonElement(jsonObject.toString()));

    JsonArray jsonArray = new JsonArray();
    assertEquals(jsonArray, typeConversion.asJsonElement(jsonArray.toString()));
    jsonArray.add(42);
    assertEquals(jsonArray, typeConversion.asJsonElement(jsonArray.toString()));
    jsonArray.add("something");
    assertEquals(jsonArray, typeConversion.asJsonElement(jsonArray.toString()));
    jsonArray.add(new JsonArray());
    assertEquals(jsonArray, typeConversion.asJsonElement(jsonArray.toString()));
    jsonArray.add(jsonObject);
    assertEquals(jsonArray, typeConversion.asJsonElement(jsonArray.toString()));

    JsonPrimitive jsonPrimitive;
    JsonPrimitive jsonStringPrimitive;
    for (int i = 0; i < 100; i++) {
      int ir = random.nextInt();
      jsonPrimitive = new JsonPrimitive(ir);
      jsonStringPrimitive = new JsonPrimitive(Integer.toString(ir));
      assertEquals(jsonPrimitive, typeConversion.asJsonElement(jsonPrimitive));
      assertEquals(jsonStringPrimitive, typeConversion.asJsonElement(jsonStringPrimitive));
      double id = random.nextDouble();
      jsonPrimitive = new JsonPrimitive(id);
      jsonStringPrimitive = new JsonPrimitive(Double.toString(id));
      assertEquals(jsonPrimitive, typeConversion.asJsonElement(jsonPrimitive));
      assertEquals(jsonStringPrimitive, typeConversion.asJsonElement(jsonStringPrimitive));
    }
  }

  @Test
  void asClonedJsonElement() throws ParserException {
    JsonObject jsonObject = new JsonObject();
    JsonElement jsonElement = typeConversion.asClonedJsonElement(jsonObject.toString());
    assertEquals(jsonObject, jsonElement);
    assertNotSame(jsonObject, jsonElement);

    jsonObject.add("test", new JsonPrimitive("test1"));
    jsonElement = typeConversion.asClonedJsonElement(jsonObject.toString());
    assertEquals(jsonObject, jsonElement);
    assertNotSame(jsonObject, jsonElement);

    JsonArray jsonArray = new JsonArray();
    jsonElement = typeConversion.asClonedJsonElement(jsonArray.toString());
    assertEquals(jsonArray, jsonElement);
    assertNotSame(jsonObject, jsonElement);

    jsonArray.add(42);
    jsonElement = typeConversion.asClonedJsonElement(jsonArray.toString());
    assertEquals(jsonArray, jsonElement);
    assertNotSame(jsonArray, jsonElement);

    jsonArray.add("something");
    jsonElement = typeConversion.asClonedJsonElement(jsonArray.toString());
    assertEquals(jsonArray, jsonElement);
    assertNotSame(jsonArray, jsonElement);

    jsonArray.add(new JsonArray());
    jsonElement = typeConversion.asClonedJsonElement(jsonArray.toString());
    assertEquals(jsonArray, jsonElement);
    assertNotSame(jsonArray, jsonElement);

    jsonArray.add(jsonObject);
    jsonElement = typeConversion.asClonedJsonElement(jsonArray.toString());
    assertEquals(jsonArray, jsonElement);
    assertNotSame(jsonArray, jsonElement);

    JsonPrimitive jsonPrimitive;
    for (int i = 0; i < 100; i++) {
      int ir = random.nextInt();
      jsonPrimitive = new JsonPrimitive(ir);
      jsonElement = typeConversion.asClonedJsonElement(jsonPrimitive);
      assertEquals(jsonPrimitive, jsonElement);

      jsonPrimitive = new JsonPrimitive(Integer.toString(ir));
      jsonElement = typeConversion.asClonedJsonElement(jsonPrimitive);
      assertEquals(jsonPrimitive, jsonElement);

      double id = random.nextDouble();
      jsonPrimitive = new JsonPrimitive(id);

      jsonPrimitive = new JsonPrimitive(Double.toString(id));
      jsonElement = typeConversion.asClonedJsonElement(jsonPrimitive);
      assertEquals(jsonPrimitive, jsonElement);
    }
  }

  @Test
  void convertPrimitiveFromString() {
    JsonElement jsonElement = typeConversion.convertPrimitiveFromString("");
    assertSame(JsonMTSTypeConversion.EMPTY_STRING_ELEMENT, jsonElement);

    for (int i = 0; i < 100; i++) {
      int r = random.nextInt();
      String rString = Integer.toString(r);

      JsonPrimitive jsonPrimitive = typeConversion.convertPrimitiveFromString(rString);
      assertTrue(jsonPrimitive.isNumber());
      assertFalse(jsonPrimitive.isString());
    }

    JsonPrimitive jsonPrimitive = typeConversion.convertPrimitiveFromString("test");
    assertTrue(jsonPrimitive.isString());
    assertFalse(jsonPrimitive.isNumber());

    jsonPrimitive = typeConversion.convertPrimitiveFromString("[1]");
    assertTrue(jsonPrimitive.isString());
    assertFalse(jsonPrimitive.isNumber());

    jsonPrimitive = typeConversion.convertPrimitiveFromString("{a:1, b:2}");
    assertTrue(jsonPrimitive.isString());
    assertFalse(jsonPrimitive.isNumber());
  }
}
