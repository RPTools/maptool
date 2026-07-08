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
package net.rptools.maptool.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import org.junit.jupiter.api.Test;

public class HandlebarsHelperTest {
  private final TemplateLoader templateLoader =
      new ClassPathTemplateLoader("/net/rptools/maptool/util/handlebars/");
  private final Handlebars hb = HandlebarsUtil.getHandlebarsInstance(templateLoader);
  private final Map<Object, Object> mathsContextData =
      new HashMap<>() {
        {
          put("num1", 1);
          put("num2", "-1");
          put("arr1", new Object[] {0.5, "1", 2, "3.5"});
        }
      };
  private final Context mathsContext = Context.newBuilder(mathsContextData).build();

  /* Maths function names
  abs
  add
  div
  divide
  max
  min
  mod
  multiply
  pow
  sqrt
  subtract
  */

  private final BiFunction<String, String, String> applyFunction =
      (helper, values) -> {
        try {
          Template template = hb.compileInline(String.format("{{%s %s}}", helper, values));
          return template.apply(mathsContext);
        } catch (IOException ex) {
          return null;
        }
      };

  @Test
  public void testMathsConstants() {
    assertEquals("3.141592653589793", applyFunction.apply("pi", ""));
    assertEquals("6.283185307179586", applyFunction.apply("tau", ""));
  }

  @Test
  public void testMathsHelpersOneArg() {
    assertEquals("1", applyFunction.apply("abs", "1"));
    assertEquals("1.1", applyFunction.apply("abs", "-1.1"));

    assertEquals("1", applyFunction.apply("add", "1"));
    assertEquals("-1", applyFunction.apply("add", "-1"));
    assertEquals("NaN", applyFunction.apply("div", "1"));
    assertEquals("1", applyFunction.apply("divide", "1"));

    assertEquals("1", applyFunction.apply("max", "1"));
    assertEquals("1", applyFunction.apply("min", "1"));
    assertEquals("NaN", applyFunction.apply("mod", "1"));
    assertEquals("1", applyFunction.apply("multiply", "1"));
    assertEquals("NaN", applyFunction.apply("pow", "1"));

    assertEquals("2", applyFunction.apply("sqrt", "4"));
    assertEquals("NaN", applyFunction.apply("sqrt", "-1"));
    assertEquals("1", applyFunction.apply("subtract", "1"));
  }

  @Test
  public void testMathsHelpersContextVariables() {
    assertEquals(
        new BigDecimal("1").doubleValue(),
        new BigDecimal(applyFunction.apply("abs", "num1")).doubleValue());
    assertEquals(
        new BigDecimal("1").doubleValue(),
        new BigDecimal(applyFunction.apply("abs", "num2")).doubleValue());
    assertEquals("NaN", applyFunction.apply("abs", "arr1"));
    assertEquals(
        new BigDecimal("1").doubleValue(),
        new BigDecimal(applyFunction.apply("add", "num1")).doubleValue());
    assertEquals(
        new BigDecimal("-1").doubleValue(),
        new BigDecimal(applyFunction.apply("add", "num2")).doubleValue());
    assertEquals(
        new BigDecimal("7").doubleValue(),
        new BigDecimal(applyFunction.apply("add", "arr1")).doubleValue());
    assertEquals("NaN", applyFunction.apply("div", "num1"));
    assertEquals("NaN", applyFunction.apply("div", "num2"));
    assertEquals(
        new BigDecimal("0").doubleValue(),
        new BigDecimal(applyFunction.apply("div", "arr1")).doubleValue());
    assertEquals(
        new BigDecimal("1").doubleValue(),
        new BigDecimal(applyFunction.apply("divide", "num1")).doubleValue());
    assertEquals(
        new BigDecimal("-1").doubleValue(),
        new BigDecimal(applyFunction.apply("divide", "num2")).doubleValue());
    assertEquals(
        new BigDecimal("0.07142857142857142").doubleValue(),
        new BigDecimal(applyFunction.apply("divide", "arr1")).doubleValue());
    assertEquals(
        new BigDecimal("1").doubleValue(),
        new BigDecimal(applyFunction.apply("max", "num1")).doubleValue());
    assertEquals(
        new BigDecimal("-1").doubleValue(),
        new BigDecimal(applyFunction.apply("max", "num2")).doubleValue());
    assertEquals(
        new BigDecimal("3.5").doubleValue(),
        new BigDecimal(applyFunction.apply("max", "arr1")).doubleValue());
    assertEquals(
        new BigDecimal("1").doubleValue(),
        new BigDecimal(applyFunction.apply("min", "num1")).doubleValue());
    assertEquals(
        new BigDecimal("-1").doubleValue(),
        new BigDecimal(applyFunction.apply("min", "num2")).doubleValue());
    assertEquals(
        new BigDecimal("0.5").doubleValue(),
        new BigDecimal(applyFunction.apply("min", "arr1")).doubleValue());
    assertEquals("NaN", applyFunction.apply("mod", "num1"));
    assertEquals("NaN", applyFunction.apply("mod", "num2"));
    assertEquals(
        new BigDecimal("0").doubleValue(),
        new BigDecimal(applyFunction.apply("mod", "arr1")).doubleValue());
    assertEquals(
        new BigDecimal("1").doubleValue(),
        new BigDecimal(applyFunction.apply("multiply", "num1")).doubleValue());
    assertEquals(
        new BigDecimal("-1").doubleValue(),
        new BigDecimal(applyFunction.apply("multiply", "num2")).doubleValue());
    assertEquals(
        new BigDecimal("3.5").doubleValue(),
        new BigDecimal(applyFunction.apply("multiply", "arr1")).doubleValue());
    assertEquals("NaN", applyFunction.apply("pow", "num1"));
    assertEquals("NaN", applyFunction.apply("pow", "num2"));
    assertEquals(
        new BigDecimal("1").doubleValue(),
        new BigDecimal(applyFunction.apply("pow", "arr1")).doubleValue());
    assertEquals(
        new BigDecimal("1").doubleValue(),
        new BigDecimal(applyFunction.apply("sqrt", "num1")).doubleValue());
    assertEquals("NaN", applyFunction.apply("sqrt", "num2"));
    assertEquals("NaN", applyFunction.apply("sqrt", "arr1"));
    assertEquals(
        new BigDecimal("1").doubleValue(),
        new BigDecimal(applyFunction.apply("subtract", "num1")).doubleValue());
    assertEquals(
        new BigDecimal("-1").doubleValue(),
        new BigDecimal(applyFunction.apply("subtract", "num2")).doubleValue());
    assertEquals(
        new BigDecimal("0").doubleValue(),
        new BigDecimal(applyFunction.apply("subtract", "arr1")).doubleValue());
  }

  @Test
  public void testMathsHelpersMixedArgs() {
    assertEquals(
        new BigDecimal("7").doubleValue(),
        new BigDecimal(applyFunction.apply("add", "-2 2 \"[3,-3]\" num1 num2 arr1 x=5 y=-5"))
            .doubleValue());
  }

  @Test
  public void testMathsHelpersNested() {
    String complex =
        """
                multiply 0.05
                    (add
                        (subtract
                            (multiply (pi) 2)
                            (tau)
                        )
                    "0.1e1")
                    (mod
                        (add
                            (div
                                (divide 9 3 2)
                            1)
                            (pow
                                (abs
                                    (subtract 0
                                        (sqrt 16)
                                    )
                                )
                                2
                            )
                            (max arr1)
                            (min arr1)
                        )
                    20
                    )
                """;
    assertEquals(
        new BigDecimal("1").doubleValue(),
        new BigDecimal(applyFunction.apply(complex, "")).doubleValue());
  }

  private final Map<Object, Object> helperContextData =
      new HashMap<>() {
        {
          put(
              "id",
              new HashMap<Object, Object>() {
                {
                  put("firstName", "First");
                  put("lastName", "Last");
                }
              });
        }
      };
  private final Context helperContext = Context.newBuilder(helperContextData).build();

  @Test
  public void embeddedHelperTest() {
    String expected =
        """
            <script id="user-hbs" type="text/x-handlebars">
            <tr><td>{{firstName}}</td><td>{{lastName}}</td></tr>
            </script>""";
    try {
      String templateText =
          templateLoader.sourceAt("embeddedHelperTest").content(StandardCharsets.UTF_8);
      Template template = hb.compileInline(templateText);
      assertEquals(expected, template.apply(helperContext));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void includeHelperTest() {
    String expected = "<tr><td>First</td><td>Last</td></tr>";
    try {
      String templateText =
          templateLoader.sourceAt("includeHelperTest").content(StandardCharsets.UTF_8);
      Template template = hb.compileInline(templateText);
      assertEquals(expected, template.apply(helperContext));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void jsonHelperTest() {
    String expected = "{\"id\":{\"firstName\":\"First\",\"lastName\":\"Last\"}}";
    try {
      String templateText =
          templateLoader.sourceAt("jsonHelperTest").content(StandardCharsets.UTF_8);
      Template template = hb.compileInline(templateText);
      assertEquals(expected, template.apply(helperContext));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void partialHelperTest() {
    String expected = "<tr><td>First</td><td>Last</td></tr>";
    try {
      String templateText =
          templateLoader.sourceAt("partialHelperTest").content(StandardCharsets.UTF_8);
      Template template = hb.compileInline(templateText);
      assertEquals(expected, template.apply(helperContext));
      template = hb.compileInline("{{#*inline \"myPartial\"}}success{{/inline}}{{> myPartial}}");
      assertEquals("success", template.apply(helperContext));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
