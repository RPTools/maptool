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
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import net.rptools.parser.ParserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MathFunctionsTest {

  private final String jsonArrString1 = "[4, 5, 6, 7, 12, 1]";
  private final String jsonArrString2 = "[2, 3, 4, 5, 6]";
  private final String jsonArrString3 = "[1.5, -5, 6]";
  private final String listString1 = "4,5,6,7,12,1";
  private final String listString2 = "2ZZZ3ZZZ4ZZZ5ZZZ6";
  private final String listString2_delim = "ZZZ";
  private final String listString3 = "1.5, -5, 6";
  MathFunctions mathFunctions;

  @BeforeEach
  void setup() {
    mathFunctions = MathFunctions.getInstance();
  }

  /** Helper to call childEvaluate */
  private Object eval(String functionName, Object... params) throws ParserException {
    return mathFunctions.childEvaluate(null, null, functionName, Arrays.asList(params));
  }

  @Test
  void testArraySum() throws ParserException {
    assertEquals(BigDecimal.valueOf(35), eval("math.arraySum", jsonArrString1));
    assertEquals(BigDecimal.valueOf(20), eval("math.arraySum", jsonArrString2));
    assertEquals(BigDecimal.valueOf(2.5), eval("math.arraySum", jsonArrString3));
  }

  @Test
  void testListSum() throws ParserException {
    assertEquals(BigDecimal.valueOf(35), eval("math.listSum", listString1));
    assertEquals(BigDecimal.valueOf(20), eval("math.listSum", listString2, listString2_delim));
    assertEquals(BigDecimal.valueOf(2.5), eval("math.listSum", listString3));
  }

  @Test
  void testArrayProduct() throws ParserException {
    assertEquals(BigDecimal.valueOf(10080), eval("math.arrayProduct", jsonArrString1));
    assertEquals(BigDecimal.valueOf(720), eval("math.arrayProduct", jsonArrString2));
    // verifies sign, magnitude, and precision:
    assertEquals(BigDecimal.valueOf(-45.0), eval("math.arrayProduct", jsonArrString3));
  }

  @Test
  void testListProduct() throws ParserException {
    assertEquals(BigDecimal.valueOf(10080), eval("math.listProduct", listString1));
    assertEquals(BigDecimal.valueOf(720), eval("math.listProduct", listString2, listString2_delim));
    // verifies sign, magnitude, and precision:
    assertEquals(BigDecimal.valueOf(-45.0), eval("math.listProduct", listString3));
  }

  @Test
  void testArrayMin() throws ParserException {
    assertEquals(BigDecimal.valueOf(1), eval("math.arrayMin", jsonArrString1));
    assertEquals(BigDecimal.valueOf(2), eval("math.arrayMin", jsonArrString2));
    assertEquals(BigDecimal.valueOf(-5), eval("math.arrayMin", jsonArrString3));
  }

  @Test
  void testListMin() throws ParserException {
    assertEquals(BigDecimal.valueOf(1), eval("math.listMin", listString1));
    assertEquals(BigDecimal.valueOf(2), eval("math.listMin", listString2, listString2_delim));
    assertEquals(BigDecimal.valueOf(-5), eval("math.listMin", listString3));
  }

  @Test
  void testArrayMax() throws ParserException {
    assertEquals(BigDecimal.valueOf(12), eval("math.arrayMax", jsonArrString1));
    assertEquals(BigDecimal.valueOf(6), eval("math.arrayMax", jsonArrString2));
    assertEquals(BigDecimal.valueOf(6), eval("math.arrayMax", jsonArrString3));
  }

  @Test
  void testListMax() throws ParserException {
    assertEquals(BigDecimal.valueOf(12), eval("math.listMax", listString1));
    assertEquals(BigDecimal.valueOf(6), eval("math.listMax", listString2, listString2_delim));
    assertEquals(BigDecimal.valueOf(6), eval("math.listMax", listString3));
  }

  @Test
  void testArrayMean() throws ParserException {
    assertEquals(
        BigDecimal.valueOf(5.833),
        ((BigDecimal) eval("math.arrayMean", jsonArrString1)).setScale(3, RoundingMode.HALF_UP));
    assertEquals(BigDecimal.valueOf(4), eval("math.arrayMean", jsonArrString2));
    assertEquals(
        BigDecimal.valueOf(0.833),
        ((BigDecimal) eval("math.arrayMean", jsonArrString3)).setScale(3, RoundingMode.HALF_UP));
  }

  @Test
  void testListMean() throws ParserException {
    assertEquals(
        BigDecimal.valueOf(5.833),
        ((BigDecimal) eval("math.listMean", listString1)).setScale(3, RoundingMode.HALF_UP));
    assertEquals(BigDecimal.valueOf(4), eval("math.listMean", listString2, listString2_delim));
    assertEquals(
        BigDecimal.valueOf(0.833),
        ((BigDecimal) eval("math.listMean", listString3)).setScale(3, RoundingMode.HALF_UP));
  }

  @Test
  void testArrayMedian() throws ParserException {
    assertEquals(BigDecimal.valueOf(5.5), eval("math.arrayMedian", jsonArrString1));
    assertEquals(BigDecimal.valueOf(4), eval("math.arrayMedian", jsonArrString2));
    assertEquals(BigDecimal.valueOf(1.5), eval("math.arrayMedian", jsonArrString3));
  }

  @Test
  void testListMedian() throws ParserException {
    assertEquals(BigDecimal.valueOf(5.5), eval("math.listMedian", listString1));
    assertEquals(BigDecimal.valueOf(4), eval("math.listMedian", listString2, listString2_delim));
    assertEquals(BigDecimal.valueOf(1.5), eval("math.listMedian", listString3));
  }

  @Test
  void testArrayOps_InvalidParams() throws ParserException {
    try {
      eval("math.arraySum");
      fail("Expected ParserException for too few arguments");
    } catch (ParserException e) {
      // test passes, expected condition
    }

    try {
      eval("math.arrayProduct", "[1]", "extraParam");
      fail("Expected ParserException for too many arguments");
    } catch (ParserException e) {
      // test passes, expected condition
    }

    try {
      eval("math.arrayMin", "[]");
      fail("Expected ParserException for empty array");
    } catch (ParserException e) {
      // test passes, expected condition
    }

    try {
      eval("math.arrayMean", "['some string']");
      fail("Expected NumberFormatException for non-numeric element");
    } catch (NumberFormatException e) {
      // test passes, expected condition
    }
  }

  @Test
  void testListOps_InvalidParams() throws ParserException {
    try {
      eval("math.listMedian");
      fail("Expected ParserException for too few arguments");
    } catch (ParserException e) {
      // test passes, expected condition
    }

    try {
      eval("math.listSum", "1", "delim", "extraParam");
      fail("Expected ParserException for too many arguments");
    } catch (ParserException e) {
      // test passes, expected condition
    }

    try {
      eval("math.listProduct", "");
      fail("Expected ParserException for empty list");
    } catch (ParserException e) {
      // test passes, expected condition
    }

    try {
      eval("math.listMax", "some,strings");
      fail("Expected NumberFormatException for non-numeric element");
    } catch (NumberFormatException e) {
      // test passes, expected condition
    }
  }
}
