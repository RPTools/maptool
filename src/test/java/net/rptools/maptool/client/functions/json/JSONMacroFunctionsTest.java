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

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class JSONMacroFunctionsTest {
  private JSONMacroFunctions jsonMacroFunctions;

  @BeforeEach
  void setup() {
    jsonMacroFunctions = JSONMacroFunctions.getInstance();
  }

  @ParameterizedTest
  @MethodSource("indentSource")
  void testIndent(JsonElement input, int indent, String expectedOutput) {
    expectedOutput = expectedOutput.trim();

    String indented = jsonMacroFunctions.jsonIndent(input, indent);
    indented = indented.trim();

    assertEquals(expectedOutput, indented);
  }

  static List<Arguments> indentSource() {
    var simpleObject = new JsonObject();
    simpleObject.addProperty("a", 1);
    simpleObject.addProperty("b", "2");
    simpleObject.addProperty("c", true);

    var simpleArray = new JsonArray();
    simpleArray.add(1);
    simpleArray.add("2");
    simpleArray.add(true);

    var complexObject = new JsonObject();
    complexObject.addProperty("x", 1);
    complexObject.addProperty("y", "2");
    var complexObjectSubObject = new JsonArray();
    complexObjectSubObject.add(false);
    complexObjectSubObject.add("1");
    complexObjectSubObject.add(2);
    complexObject.add("z", complexObjectSubObject);

    return List.of(
        Arguments.argumentSet(
            "Simple object, indent = 2",
            simpleObject,
            2,
            """
            {
              "a": 1,
              "b": "2",
              "c": true
            }
            """),
        Arguments.argumentSet(
            "Simple object, indent = 4",
            simpleObject,
            4,
            """
            {
                "a": 1,
                "b": "2",
                "c": true
            }
            """),
        Arguments.argumentSet(
            "Simple array, indent = 2",
            simpleArray,
            2,
            """
                [
                  1,
                  "2",
                  true
                ]
                """),
        Arguments.argumentSet(
            "Simple array, indent = 4",
            simpleArray,
            4,
            """
                [
                    1,
                    "2",
                    true
                ]
                """),
        Arguments.argumentSet(
            "Complex object, indent = 2",
            complexObject,
            2,
            """
            {
              "x": 1,
              "y": "2",
              "z": [
                false,
                "1",
                2
              ]
            }
            """));
  }
}
