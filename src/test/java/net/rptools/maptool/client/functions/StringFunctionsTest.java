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
package net.rptools.maptool.client.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import net.rptools.parser.ParserException;
import org.junit.jupiter.api.Test;

public class StringFunctionsTest {
  private final StringFunctions funcs = StringFunctions.getInstance();
  private final String capitalize = "capitalize";

  private Object submitStringFunction(String functionName, Object... params)
      throws ParserException {
    return funcs.childEvaluate(null, null, functionName, Arrays.asList(params));
  }

  @Test
  public void testCapitalize() throws ParserException {
    String string1 = "i'll do it. no you won't. like you'd know. i've seen you 1st.";
    String cap1WithSymbolBoundaries =
        "I'Ll Do It. No You Won'T. Like You'D Know. I'Ve Seen You 1St.";
    String cap1WithWhitespaceBoundaries =
        "I'll Do It. No You Won't. Like You'd Know. I've Seen You 1st.";
    String string2 = "but o'shea works";
    String cap2WithSymbolBoundaries = "But O'Shea Works";
    String cap2WithWhiteSpaceBoundaries = "But O'shea Works";

    assertEquals(
        cap1WithSymbolBoundaries, submitStringFunction(capitalize, string1, BigDecimal.ONE));
    assertEquals(
        cap1WithWhitespaceBoundaries, submitStringFunction(capitalize, string1, BigDecimal.ZERO));
    assertEquals(
        cap1WithSymbolBoundaries,
        submitStringFunction(capitalize, string1),
        "use numbers and symbols as boundaries by default");

    assertEquals(
        cap2WithSymbolBoundaries, submitStringFunction(capitalize, string2, BigDecimal.ONE));
    assertEquals(
        cap2WithWhiteSpaceBoundaries, submitStringFunction(capitalize, string2, BigDecimal.ZERO));
    assertEquals(
        cap2WithSymbolBoundaries,
        submitStringFunction(capitalize, string2),
        "use numbers and symbols as boundaries by default");
  }

  @Test
  public void testIsNumber() throws ParserException {
    String isNumber = "isNumber";

    List<String> numbers = List.of("4", "4.3", "-4.3", "04");
    // strings that the parser doesn't consider numeric or doesn't interpret correctly should be
    // considered NOT numbers
    List<String> notNumbers = List.of("", "control", "1E3", "1,000");

    for (String s : numbers) {
      assertEquals(
          BigDecimal.ONE,
          submitStringFunction(isNumber, s),
          "Expection [" + s + "] to be recognized as a number");
    }
    for (String s : notNumbers) {
      assertEquals(
          BigDecimal.ZERO,
          submitStringFunction(isNumber, s),
          "Expecting [" + s + "] to be non-numeric");
    }
  }
}
