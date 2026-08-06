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
import static org.mockito.Mockito.mock;

import com.google.gson.*;
import com.vladsch.flexmark.util.html.ui.Color;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.drawing.DrawableColorPaint;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import org.junit.jupiter.api.*;

class FunctionUtilTest {
  private final String functionName = "testFunction";

  @Test
  @Disabled // I don't know how to get these to work
  void untestable() throws ParserException {
    Parser parser = new Parser(true);
    Token mockToken = mock(Token.class);
    VariableResolver variableResolver = new MapToolVariableResolver(mockToken);
    List<Object> parameters = List.of();
    FunctionUtil.experimentalWarning(parser, variableResolver, functionName);
    FunctionUtil.getTokenFromParam(variableResolver, functionName, parameters, 14, 16);
    FunctionUtil.getZoneRendererFromParam(functionName, parameters, 16);
    FunctionUtil.getZoneRenderer(functionName, "Grasslands");

    FunctionUtil.getAssetKeyFromString(mockToken.getImageAssetId().toString());
    FunctionUtil.blockUntrustedMacro(functionName);
  }

  @Test
  @DisplayName("FunctionUtil.checkNumberParam()")
  void checkNumberParam() {
    List<Object> params = List.of("a", "b");
    // max < min
    assertThrows(
        ParserException.class, () -> FunctionUtil.checkNumberParam(functionName, params, 2, 0));
    // length > max
    assertThrows(
        ParserException.class, () -> FunctionUtil.checkNumberParam(functionName, params, 0, 1));
    // length < min
    assertThrows(
        ParserException.class, () -> FunctionUtil.checkNumberParam(functionName, params, 3, 4));
  }

  @Test
  @DisplayName("FunctionUtil.getPaintFromString()")
  void getPaintFromString() {
    assertInstanceOf(
        DrawableColorPaint.class, FunctionUtil.getPaintFromString(Color.black.toString()));
    assertInstanceOf(DrawableColorPaint.class, FunctionUtil.getPaintFromString("black"));
    assertInstanceOf(DrawableColorPaint.class, FunctionUtil.getPaintFromString("#000"));
    assertInstanceOf(DrawableColorPaint.class, FunctionUtil.getPaintFromString("#a0a0a0a0"));
    assertInstanceOf(DrawableColorPaint.class, FunctionUtil.getPaintFromString("0x112233"));
    assertInstanceOf(DrawableColorPaint.class, FunctionUtil.getPaintFromString("0X111222333"));
    assertInstanceOf(
        DrawableColorPaint.class, FunctionUtil.getPaintFromString("0b2333")); // malformed
  }

  //
  // Typed parameters (non-JSON)
  //

  @Test
  @DisplayName("FunctionUtil.getDecimalForBoolean()")
  void getDecimalForBoolean() {
    assertEquals(BigDecimal.ZERO, FunctionUtil.getDecimalForBoolean(false));
    assertEquals(BigDecimal.ONE, FunctionUtil.getDecimalForBoolean(true));
  }

  @Test
  @DisplayName("FunctionUtil.getBooleanValue()")
  void getBooleanValue() {
    List<Object> params =
        List.of(0, " 0 ", false, " FaLse", 1, -2, 3, true, "1 ", " true", "TRuE ");
    for (int i = 0; i < params.size(); i++) {
      if (i < 4) {
        assertFalse(FunctionUtil.getBooleanValue(params.get(i)));
      } else {
        assertTrue(FunctionUtil.getBooleanValue(params.get(i)));
      }
    }
  }

  @Test
  @DisplayName("FunctionUtil.paramAsBoolean()")
  void paramAsBoolean() throws ParserException {
    List<Object> params;

    // Non-numeric string
    assertThrows(
        ParserException.class,
        () -> FunctionUtil.paramAsBoolean(functionName, List.of("3.two"), 0, true));
    assertThrows(
        ParserException.class,
        () -> FunctionUtil.paramAsBoolean(functionName, List.of(emptyString), 0, true));

    // numeric parameters
    params = List.of(0, 1, -2, 3);
    assertEquals(false, FunctionUtil.paramAsBoolean(functionName, params, 0, false));
    assertEquals(true, FunctionUtil.paramAsBoolean(functionName, params, 1, false));
    assertEquals(true, FunctionUtil.paramAsBoolean(functionName, params, 2, false));
    assertEquals(true, FunctionUtil.paramAsBoolean(functionName, params, 3, false));

    // Boolean parameters
    assertEquals(false, FunctionUtil.paramAsBoolean(functionName, List.of(false), 0, true));
    assertEquals(true, FunctionUtil.paramAsBoolean(functionName, List.of(true), 0, true));

    // String parameters
    params = List.of("-0 ", " 1.0 ", "false ", " true");
    assertEquals(false, FunctionUtil.paramAsBoolean(functionName, params, 0, true));
    assertEquals(true, FunctionUtil.paramAsBoolean(functionName, params, 1, true));
    assertEquals(false, FunctionUtil.paramAsBoolean(functionName, params, 2, true));
    assertEquals(true, FunctionUtil.paramAsBoolean(functionName, params, 3, true));
    List<Object> finalParams = params;
    assertThrows(
        ParserException.class,
        () -> FunctionUtil.paramAsBoolean(functionName, finalParams, 0, false));
    assertThrows(
        ParserException.class,
        () -> FunctionUtil.paramAsBoolean(functionName, finalParams, 1, false));
    assertThrows(
        ParserException.class,
        () -> FunctionUtil.paramAsBoolean(functionName, finalParams, 2, false));
    assertThrows(
        ParserException.class,
        () -> FunctionUtil.paramAsBoolean(functionName, finalParams, 3, false));
  }

  @Test
  @DisplayName("FunctionUtil.paramAsString()")
  void paramAsString() throws ParserException {
    assertEquals("NaN", FunctionUtil.paramAsString(functionName, List.of(Double.NaN), 0, true));
    assertThrows(
        ParserException.class,
        () -> FunctionUtil.paramAsString(functionName, List.of(Double.NaN), 0, false));
    assertEquals("true", FunctionUtil.paramAsString(functionName, List.of(true), 0, true));
    assertThrows(
        ParserException.class,
        () -> FunctionUtil.paramAsString(functionName, List.of(true), 0, false));
    assertEquals("true", FunctionUtil.paramAsString(functionName, List.of("true"), 0, true));
    assertEquals(
        "{}", FunctionUtil.paramAsString(functionName, List.of(new JsonObject()), 0, true));
    assertEquals(
        "a string", FunctionUtil.paramAsString(functionName, List.of("a string"), 0, true));
  }

  private final List<Object> numbers =
      List.of(
          Byte.valueOf("-0"),
          -1L,
          2,
          -3.3f,
          4.4d,
          -5e0,
          6.6e0,
          0x00000007,
          0x00000008,
          0b1001,
          Double.NaN);

  private final List<Object> numberStrings =
      List.of(
          "-0 ",
          " -1L",
          " +2 ",
          " -3.3f",
          "4.4d ",
          " -5e0",
          " 6.6e0 ",
          "0x00000007",
          " #8 ",
          " 0b1001",
          "NaN");

  @Test
  @DisplayName("FunctionUtil.paramAsInteger()")
  void paramAsInteger() throws ParserException {
    List<Object> expected = List.of(-0, -1, 2, false, false, -5, false, 7, 8, 9, false);

    for (int i = 0; i < expected.size(); i++) {
      Object expect = expected.get(i);
      if (expect instanceof Boolean) {
        final int index = i;
        assertThrows(
            ParserException.class,
            () -> FunctionUtil.paramAsInteger(functionName, numbers, index, false));
      } else if (expect instanceof Integer result) {
        assertEquals(result, FunctionUtil.paramAsInteger(functionName, numbers, i, false));
        assertEquals(result, FunctionUtil.paramAsInteger(functionName, numberStrings, i, true));
      }
    }
  }

  @Test
  @DisplayName("FunctionUtil.paramAsDouble()")
  void paramAsDouble() throws ParserException {
    List<Object> expected = List.of(0d, -1d, 2d, -3.3d, 4.4d, -5d, 6.6d, 7d, 8d, 9d, false);
    for (int i = 0; i < expected.size(); i++) {
      Object expect = expected.get(i);
      if (expect instanceof Boolean) {
        final int index = i;
        assertThrows(
            ParserException.class,
            () -> FunctionUtil.paramAsDouble(functionName, numbers, index, false));
      } else if (expect instanceof Double result) {
        assertEquals(result, FunctionUtil.paramAsDouble(functionName, numbers, i, false));
        assertEquals(result, FunctionUtil.paramAsDouble(functionName, numberStrings, i, true));
      }
    }
  }

  @Test
  @DisplayName("FunctionUtil.paramAsFloat()")
  void paramAsFloat() throws ParserException {
    List<Object> expected = List.of(0f, -1f, 2f, -3.3f, 4.4f, -5f, 6.6f, 7f, 8f, 9f, false);

    for (int i = 0; i < expected.size(); i++) {
      Object expect = expected.get(i);
      if (expect instanceof Boolean) {
        final int index = i;
        assertThrows(
            ParserException.class,
            () -> FunctionUtil.paramAsFloat(functionName, numbers, index, false));
      } else if (expect instanceof Float result) {
        assertEquals(result, FunctionUtil.paramAsFloat(functionName, numbers, i, false));
        assertEquals(result, FunctionUtil.paramAsFloat(functionName, numberStrings, i, true));
      }
    }
  }

  @Test
  @DisplayName("FunctionUtil.paramAsBigDecimal()")
  void paramAsBigDecimal() throws ParserException {
    List<Object> expected =
        List.of(
            new BigDecimal("-0"),
            new BigDecimal("-1"),
            new BigDecimal("2"),
            new BigDecimal("-3.3"),
            new BigDecimal("4.4"),
            new BigDecimal("-5"),
            new BigDecimal("6.6"),
            new BigDecimal("7"),
            new BigDecimal("8"),
            new BigDecimal("9"),
            false);

    for (int i = 0; i < expected.size(); i++) {
      Object expect = expected.get(i);
      if (expect instanceof Boolean) {
        final int index = i;
        assertThrows(
            ParserException.class,
            () -> FunctionUtil.paramAsBigDecimal(functionName, numbers, index, false));
      } else if (expect instanceof BigDecimal result) {
        assertEquals(
            0, result.compareTo(FunctionUtil.paramAsBigDecimal(functionName, numbers, i, false)));
        assertEquals(result, FunctionUtil.paramAsBigDecimal(functionName, numberStrings, i, true));
      }
    }
  }

  //
  // Typed parameters (JSON)
  //
  private final String emptyString = "";
  private final JsonArray emptyJsonArray = new JsonArray();
  private final JsonObject emptyJsonObject = new JsonObject();
  private final List<String> listOfStrings = List.of("string", "list", "test");
  private final String hash = "#";
  private final String delimJson = "jSoN";
  private final String stringList = "string,list,test";
  private final String stringListHash = "string#list#test";
  private final String strProp = "a=1;B=2";
  private final String strPropHash = "a=1#B=2";
  private final String strPropJson = "a=1;b={}";
  private final String jsonArrayString = "[[{},{}],[]]";
  private final String malformedStrProp = "a=1;b=2;c3";
  private final String malformedObjectString = "{malformed:}";
  private final String malformedArrayString = "['malformed':{";
  private final JsonObject jsonObject = new JsonObject();

  {
    jsonObject.addProperty("a", "1");
    jsonObject.addProperty("B", "2");
  }

  @Test
  @DisplayName("FunctionUtil.paramFromStrPropOrJsonAsJsonObject()")
  void paramFromStrPropOrJsonAsJsonObject() throws ParserException {
    assertEquals(
        jsonObject,
        FunctionUtil.paramFromStrPropOrJsonAsJsonObject(
            functionName, List.of(strProp), 0, emptyString));
    assertEquals(
        jsonObject,
        FunctionUtil.paramFromStrPropOrJsonAsJsonObject(functionName, List.of(strProp), 0, ";"));
    assertEquals(
        jsonObject,
        FunctionUtil.paramFromStrPropOrJsonAsJsonObject(
            functionName, List.of(strPropHash), 0, hash));
    assertEquals(
        jsonObject,
        FunctionUtil.paramFromStrPropOrJsonAsJsonObject(
            functionName, List.of(jsonObject), 0, delimJson));
    assertEquals(
        jsonObject,
        FunctionUtil.paramFromStrPropOrJsonAsJsonObject(
            functionName, List.of(jsonObject), 0, hash));
    assertEquals(
        jsonObject,
        FunctionUtil.paramFromStrPropOrJsonAsJsonObject(
            functionName, List.of(jsonObject), 0, emptyString));
    assertEquals(
        "{\"a\":\"1\",\"b\":\"{}\"}",
        FunctionUtil.paramFromStrPropOrJsonAsJsonObject(
                functionName, List.of(strPropJson), 0, emptyString)
            .toString());
    assertEquals(
        emptyJsonObject,
        FunctionUtil.paramFromStrPropOrJsonAsJsonObject(
            functionName, List.of(emptyJsonObject), 0, delimJson));

    assertThrows(
        ParserException.class,
        () ->
            FunctionUtil.paramFromStrPropOrJsonAsJsonObject(
                functionName, List.of(strPropJson), 0, delimJson));
    assertThrows(
        ParserException.class,
        () ->
            FunctionUtil.paramFromStrPropOrJsonAsJsonObject(
                functionName, List.of(stringList), 0, hash));
    assertThrows(
        ParserException.class,
        () ->
            FunctionUtil.paramFromStrPropOrJsonAsJsonObject(
                functionName, List.of(stringListHash), 0, hash));
    assertThrows(
        ParserException.class,
        () ->
            FunctionUtil.paramFromStrPropOrJsonAsJsonObject(
                functionName, List.of(malformedArrayString), 0, delimJson));
    assertThrows(
        ParserException.class,
        () ->
            FunctionUtil.paramFromStrPropOrJsonAsJsonObject(
                functionName, List.of(malformedObjectString), 0, delimJson));
    assertThrows(
        ParserException.class,
        () ->
            FunctionUtil.paramFromStrPropOrJsonAsJsonObject(
                functionName, List.of(malformedStrProp), 0, delimJson));
  }

  @Test
  @DisplayName("FunctionUtil.paramAsJson()")
  void paramAsJson() throws ParserException {
    assertInstanceOf(
        JsonObject.class, FunctionUtil.paramAsJson(functionName, List.of(emptyJsonObject), 0));
    assertInstanceOf(
        JsonArray.class, FunctionUtil.paramAsJson(functionName, List.of(jsonArrayString), 0));

    assertEquals(
        emptyJsonObject, FunctionUtil.paramAsJson(functionName, List.of(emptyJsonObject), 0));
    assertEquals(
        emptyJsonArray, FunctionUtil.paramAsJson(functionName, List.of(emptyJsonArray), 0));
    assertEquals(
        jsonArrayString,
        FunctionUtil.paramAsJson(functionName, List.of(jsonArrayString), 0).toString());
    assertThrows(
        ParserException.class,
        () -> FunctionUtil.paramAsJson(functionName, List.of(strPropJson), 0));
    assertThrows(
        ParserException.class,
        () -> FunctionUtil.paramAsJson(functionName, List.of(stringList), 0));
    assertThrows(
        ParserException.class,
        () -> FunctionUtil.paramAsJson(functionName, List.of(emptyString), 0));
    assertThrows(
        ParserException.class, () -> FunctionUtil.paramAsJson(functionName, List.of(1), 0));
  }

  @Test
  @DisplayName("FunctionUtil.paramAsJsonObject()")
  void paramAsJsonObject() throws ParserException {
    assertEquals(
        emptyJsonObject, FunctionUtil.paramAsJsonObject(functionName, List.of(emptyJsonObject), 0));
    assertEquals(emptyJsonObject, FunctionUtil.paramAsJsonObject(functionName, List.of("{}"), 0));
    assertEquals(jsonObject, FunctionUtil.paramAsJsonObject(functionName, List.of(jsonObject), 0));
    assertThrows(
        ParserException.class,
        () -> FunctionUtil.paramAsJsonObject(functionName, List.of(malformedObjectString), 0));
    assertThrows(
        ParserException.class,
        () -> FunctionUtil.paramAsJsonObject(functionName, List.of(malformedArrayString), 0));
    assertThrows(
        ParserException.class,
        () -> FunctionUtil.paramAsJsonObject(functionName, List.of(emptyString), 0));
    assertThrows(
        ParserException.class,
        () -> FunctionUtil.paramAsJsonObject(functionName, List.of(jsonArrayString), 0));
    assertThrows(
        ParserException.class,
        () -> FunctionUtil.paramAsJsonObject(functionName, List.of(emptyJsonArray), 0));
    assertThrows(
        ParserException.class,
        () -> FunctionUtil.paramAsJsonObject(functionName, List.of(strProp), 0));
    assertThrows(
        ParserException.class,
        () -> FunctionUtil.paramAsJsonObject(functionName, List.of(strPropJson), 0));
    assertThrows(
        ParserException.class,
        () -> FunctionUtil.paramAsJsonObject(functionName, List.of(stringList), 0));
  }

  @Test
  @DisplayName("FunctionUtil.paramAsJsonArray()")
  void paramAsJsonArray() throws ParserException {
    assertEquals(
        emptyJsonArray, FunctionUtil.paramAsJsonArray(functionName, List.of(emptyJsonArray), 0));
    assertEquals(emptyJsonArray, FunctionUtil.paramAsJsonArray(functionName, List.of("[]"), 0));
    assertEquals(
        jsonArrayString,
        FunctionUtil.paramAsJsonArray(functionName, List.of(jsonArrayString), 0).toString());
    assertThrows(
        ParserException.class,
        () -> FunctionUtil.paramAsJsonArray(functionName, List.of(malformedObjectString), 0));
    assertThrows(
        ParserException.class,
        () -> FunctionUtil.paramAsJsonArray(functionName, List.of(malformedArrayString), 0));
    assertThrows(
        ParserException.class,
        () -> FunctionUtil.paramAsJsonArray(functionName, List.of(emptyJsonObject), 0));
    assertThrows(
        ParserException.class,
        () -> FunctionUtil.paramAsJsonArray(functionName, List.of(emptyString), 0));
    assertThrows(
        ParserException.class,
        () -> FunctionUtil.paramAsJsonArray(functionName, List.of(strProp), 0));
    assertThrows(
        ParserException.class,
        () -> FunctionUtil.paramAsJsonArray(functionName, List.of(strPropJson), 0));
    assertThrows(
        ParserException.class,
        () -> FunctionUtil.paramAsJsonArray(functionName, List.of(stringList), 0));
    assertThrows(
        ParserException.class,
        () -> FunctionUtil.paramAsJsonArray(functionName, List.of(stringListHash), 0));
  }

  @Test
  @DisplayName("FunctionUtil.paramConvertedToJsonArray()")
  void paramConvertedToJsonArray() throws ParserException {
    String pattern = "[\"%s\"]";
    assertEquals(
        emptyJsonArray,
        FunctionUtil.paramConvertedToJsonArray(functionName, List.of(emptyJsonArray), 0));
    assertEquals(
        emptyJsonArray, FunctionUtil.paramConvertedToJsonArray(functionName, List.of("[]"), 0));
    assertEquals(
        jsonArrayString,
        FunctionUtil.paramConvertedToJsonArray(functionName, List.of(jsonArrayString), 0)
            .toString());
    assertEquals(
        emptyJsonArray,
        FunctionUtil.paramConvertedToJsonArray(functionName, List.of(emptyString), 0));
    assertEquals(
        String.format(pattern, malformedObjectString),
        FunctionUtil.paramConvertedToJsonArray(functionName, List.of(malformedObjectString), 0)
            .toString());
    assertEquals(
        String.format(pattern, malformedArrayString),
        FunctionUtil.paramConvertedToJsonArray(functionName, List.of(malformedArrayString), 0)
            .toString());
    assertThrows(
        ParserException.class,
        () -> FunctionUtil.paramConvertedToJsonArray(functionName, List.of(emptyJsonObject), 0));
    assertEquals(
        String.format(pattern, strProp),
        FunctionUtil.paramConvertedToJsonArray(functionName, List.of(strProp), 0).toString());
    assertEquals(
        String.format(pattern, strPropJson),
        FunctionUtil.paramConvertedToJsonArray(functionName, List.of(strPropJson), 0).toString());
    assertEquals(
        String.format(pattern, stringList),
        FunctionUtil.paramConvertedToJsonArray(functionName, List.of(stringList), 0).toString());
    assertEquals(
        String.format(pattern, stringListHash),
        FunctionUtil.paramConvertedToJsonArray(functionName, List.of(stringListHash), 0)
            .toString());
  }

  @Test
  @DisplayName("FunctionUtil.paramConvertedToJson()")
  void paramConvertedToJson() {
    String pattern = "[\"%s\"]";
    assertEquals(
        emptyJsonArray,
        FunctionUtil.paramConvertedToJson(functionName, List.of(emptyJsonArray), 0));
    assertEquals(emptyJsonArray, FunctionUtil.paramConvertedToJson(functionName, List.of("[]"), 0));
    assertEquals(
        jsonArrayString,
        FunctionUtil.paramConvertedToJson(functionName, List.of(jsonArrayString), 0).toString());
    assertEquals(
        emptyJsonArray, FunctionUtil.paramConvertedToJson(functionName, List.of(emptyString), 0));
    assertEquals(
        String.format(pattern, malformedObjectString),
        FunctionUtil.paramConvertedToJson(functionName, List.of(malformedObjectString), 0)
            .toString());
    assertEquals(
        String.format(pattern, malformedArrayString),
        FunctionUtil.paramConvertedToJson(functionName, List.of(malformedArrayString), 0)
            .toString());
    assertEquals(
        emptyJsonObject,
        FunctionUtil.paramConvertedToJson(functionName, List.of(emptyJsonObject), 0));
    assertEquals(
        String.format(pattern, strProp),
        FunctionUtil.paramConvertedToJson(functionName, List.of(strProp), 0).toString());
    assertEquals(
        String.format(pattern, strPropJson),
        FunctionUtil.paramConvertedToJson(functionName, List.of(strPropJson), 0).toString());
    assertEquals(
        String.format(pattern, stringList),
        FunctionUtil.paramConvertedToJson(functionName, List.of(stringList), 0).toString());
    assertEquals(
        String.format(pattern, stringListHash),
        FunctionUtil.paramConvertedToJson(functionName, List.of(stringListHash), 0).toString());
  }

  @Test
  @DisplayName("FunctionUtil.delimitedResult()")
  void delimitedResult() {
    assertEquals("string#list#test", FunctionUtil.delimitedResult(hash, listOfStrings).toString());
    assertEquals(
        "[\"string\",\"list\",\"test\"]",
        FunctionUtil.delimitedResult(delimJson, listOfStrings).toString());
  }

  @Test
  @DisplayName("FunctionUtil.validateKeyNames()")
  void validateKeyNames() {
    Set<String> keyExists = Set.of("B");
    Set<String> keyMissing = Set.of("c");
    assertDoesNotThrow(
        () -> FunctionUtil.validateKeyNames(jsonObject, keyExists, 0, functionName, emptyString));
    assertDoesNotThrow(
        () -> FunctionUtil.validateKeyNames(strPropHash, keyExists, 0, functionName, hash));
    assertThrows(
        ParserException.class,
        () -> FunctionUtil.validateKeyNames(jsonObject, keyMissing, 0, functionName, hash));
  }

  @Test
  @DisplayName("FunctionUtil.jsonWithLowerCaseKeys()")
  void jsonWithLowerCaseKeys() {
    JsonObject joExpect = new JsonObject();
    joExpect.addProperty("a", "1");
    joExpect.addProperty("b", "2");
    assertEquals(joExpect, FunctionUtil.jsonWithLowerCaseKeys(jsonObject));
  }
}
