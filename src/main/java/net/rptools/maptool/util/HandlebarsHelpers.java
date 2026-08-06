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

import static org.apache.commons.lang3.Validate.notNull;

import com.github.jknack.handlebars.*;
import com.github.jknack.handlebars.helper.ConditionalHelpers;
import com.github.jknack.handlebars.helper.EmbeddedHelper;
import com.github.jknack.handlebars.helper.LogHelper;
import com.github.jknack.handlebars.helper.StringHelpers;
import com.github.jknack.handlebars.helper.ext.AssignHelper;
import com.github.jknack.handlebars.helper.ext.IncludeHelper;
import com.github.jknack.handlebars.helper.ext.NumberHelper;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HandlebarsHelpers {
  private static final Logger log = LoggerFactory.getLogger(HandlebarsHelpers.class);

  static Handlebars registerHelpers(Handlebars handlebars) {
    StringHelpers.register(handlebars);
    MoreStringHelpers.register(handlebars);
    Arrays.stream(ConditionalHelpers.values()).forEach(h -> handlebars.registerHelper(h.name(), h));
    handlebars.registerHelper("json", Jackson2Helper.INSTANCE);
    NumberHelper.register(handlebars);
    handlebars.registerHelper(EmbeddedHelper.NAME, EmbeddedHelper.INSTANCE);
    handlebars.registerHelper(AssignHelper.NAME, AssignHelper.INSTANCE);
    handlebars.registerHelper(IncludeHelper.NAME, IncludeHelper.INSTANCE);
    handlebars.registerHelper(MarkdownHelper.NAME, MarkdownHelper.INSTANCE);
    handlebars.registerHelper(Base64EncodeHelper.NAME, Base64EncodeHelper.INSTANCE);
    handlebars.registerHelper(HBLogger.NAME, HBLogger.INSTANCE);
    Arrays.stream(HandlebarsHelpers.MathsHelpers.values())
        .forEach(h -> handlebars.registerHelper(h.name(), h));

    return handlebars;
  }

  static class Base64EncodeHelper implements Helper<Object> {
    /** A singleton instance of this helper. */
    public static final Helper<Object> INSTANCE = new Base64EncodeHelper();

    /** The helper's name. */
    public static final String NAME = "base64Encode";

    /**
     * Turns the textual form of the value into a base64-encoded string. For example:
     *
     * <pre>
     * &lt;script type="application/json;base64" id="jsonProperty"&gt;
     *   {{ base64Encode properties[0].value }}
     * &lt;/script&gt;
     * &lt;script type="application/javascript"&gt;
     * const jsonProperty = JSON.parse(atob(document.getElementById("jsonProperty").innerText));
     * &lt;/script&gt;
     * </pre>
     */
    @SuppressWarnings("SpellCheckingInspection")
    @Override
    public Object apply(final Object context, final Options options) {
      if (context == null || context instanceof String s && s.isBlank()) {
        return "";
      } else {
        byte[] message = context.toString().getBytes(StandardCharsets.UTF_8);
        return new Handlebars.SafeString(Base64.getUrlEncoder().encodeToString(message));
      }
    }
  }

  public enum MathsHelpers implements Helper<Object> {
    add {
      @Override
      public Object apply(final Object a, final Options options) throws IOException {
        try {
          List<BigDecimal> numbers = numberList(a, options);
          checkOperandCount(1, -1, numbers);
          BigDecimal result = numbers.removeLast();
          while (!numbers.isEmpty()) {
            result = result.add(numbers.removeLast(), MATH_CONTEXT);
          }
          return result.toPlainString();
        } catch (NumberFormatException | ArithmeticException e) {
          log.debug("Function \"add\" - {}", e.getLocalizedMessage(), e);
          return "NaN";
        }
      }
    },
    subtract {
      @Override
      public Object apply(final Object a, final Options options) throws IOException {
        try {
          List<BigDecimal> numbers = numberList(a, options);
          checkOperandCount(1, -1, numbers);
          BigDecimal result = numbers.removeLast();
          while (!numbers.isEmpty()) {
            result = result.subtract(numbers.removeLast(), MATH_CONTEXT);
          }
          return result.toPlainString();
        } catch (NumberFormatException | ArithmeticException e) {
          log.debug("Function \"subtract\" - {}", e.getLocalizedMessage(), e);
          return "NaN";
        }
      }
    },
    multiply {
      @Override
      public Object apply(final Object a, final Options options) throws IOException {
        try {
          List<BigDecimal> numbers = numberList(a, options).reversed();
          checkOperandCount(1, -1, numbers);
          BigDecimal result = numbers.removeLast();
          while (!numbers.isEmpty()) {
            result = result.multiply(numbers.removeLast(), MATH_CONTEXT);
          }
          return result.toPlainString();
        } catch (NumberFormatException | ArithmeticException e) {
          log.debug("Function \"multiply\" - {}", e.getLocalizedMessage(), e);
          return "NaN";
        }
      }
    },
    divide {
      @Override
      public Object apply(final Object a, final Options options) throws IOException {
        try {
          List<BigDecimal> numbers = numberList(a, options).reversed();
          checkOperandCount(1, -1, numbers);
          BigDecimal result = numbers.removeLast();
          while (!numbers.isEmpty()) {
            result = result.divide(numbers.removeLast(), MATH_CONTEXT);
          }
          return result.toPlainString();
        } catch (NumberFormatException | ArithmeticException e) {
          log.debug("Function \"divide\" - {}", e.getLocalizedMessage(), e);
          return "NaN";
        }
      }
    },
    max {
      @Override
      public Object apply(final Object a, final Options options) throws IOException {
        try {
          List<BigDecimal> numbers = numberList(a, options);
          checkOperandCount(1, -1, numbers);
          BigDecimal result = numbers.removeLast();
          while (!numbers.isEmpty()) {
            result = result.max(numbers.removeLast());
          }
          return result.toPlainString();
        } catch (NumberFormatException | ArithmeticException e) {
          log.debug("Function \"max\" - {}", e.getLocalizedMessage(), e);
          return "NaN";
        }
      }
    },
    min {
      @Override
      public Object apply(final Object a, final Options options) throws IOException {
        try {
          List<BigDecimal> numbers = numberList(a, options);
          checkOperandCount(1, -1, numbers);
          BigDecimal result = numbers.removeLast();
          while (!numbers.isEmpty()) {
            result = result.min(numbers.removeLast());
          }
          return result.toPlainString();
        } catch (NumberFormatException | ArithmeticException e) {
          log.debug("Function \"min\" - {}", e.getLocalizedMessage(), e);
          return "NaN";
        }
      }
    },
    mod {
      @Override
      public Object apply(final Object a, final Options options) throws IOException {
        try {
          List<BigDecimal> numbers = numberList(a, options);
          checkOperandCount(2, -1, numbers);
          BigDecimal result = numbers.removeLast();
          while (!numbers.isEmpty()) {
            result = result.remainder(numbers.removeLast(), MATH_CONTEXT);
          }
          return result.toPlainString();
        } catch (NumberFormatException | ArithmeticException e) {
          log.debug("Function \"mod\" - {}", e.getLocalizedMessage(), e);
          return "NaN";
        }
      }
    },
    div {
      @Override
      public Object apply(final Object a, final Options options) throws IOException {
        try {
          List<BigDecimal> numbers = numberList(a, options).reversed();
          checkOperandCount(2, -1, numbers);
          BigDecimal result = numbers.removeLast();
          while (!numbers.isEmpty()) {
            result = result.divideToIntegralValue(numbers.removeLast());
          }
          return result.toPlainString();
        } catch (NumberFormatException | ArithmeticException e) {
          log.debug("Function \"div\" - {}", e.getLocalizedMessage(), e);
          return "NaN";
        }
      }
    },
    pow {
      @Override
      public Object apply(final Object a, final Options options) throws IOException {
        try {
          List<BigDecimal> numbers = numberList(a, options);
          checkOperandCount(2, -1, numbers);
          BigDecimal result = numbers.removeLast();
          while (!numbers.isEmpty()) {
            result = result.pow(numbers.removeLast().intValue(), MATH_CONTEXT);
          }
          return result.toPlainString();
        } catch (NumberFormatException | ArithmeticException e) {
          log.debug("Function \"pow\" - {}", e.getLocalizedMessage(), e);
          return "NaN";
        }
      }
    },
    abs {
      @Override
      public Object apply(final Object a, final Options options) throws IOException {
        try {
          List<BigDecimal> numbers = numberList(a, options);
          checkOperandCount(1, 1, numbers);
          return numbers.removeFirst().abs().toPlainString();
        } catch (NumberFormatException | ArithmeticException e) {
          log.debug(e.getLocalizedMessage(), e);
          return "NaN";
        }
      }
    },
    sqrt {
      @Override
      public Object apply(final Object a, final Options options) throws IOException {
        try {
          List<BigDecimal> numbers = numberList(a, options);
          checkOperandCount(1, 1, numbers);
          return new BigDecimal(a.toString()).sqrt(MATH_CONTEXT).toString();
        } catch (NumberFormatException | ArithmeticException e) {
          log.debug(e.getLocalizedMessage(), e);
          return "NaN";
        }
      }
    },
    /** 2 x Pi */
    tau {
      @Override
      public Object apply(final Object a, final Options options) throws IOException {
        return String.valueOf(Math.TAU);
      }
    },
    pi {
      @Override
      public Object apply(final Object a, final Options options) throws IOException {
        return String.valueOf(Math.PI);
      }
    },
    ;
    private static final MathContext MATH_CONTEXT = new MathContext(16, RoundingMode.HALF_EVEN);

    /**
     * Validate the correct number of operands in list.
     *
     * @param minRequired minimum required numberList to preform operation
     * @param maxAllowed maximum required numberList to preform operation
     * @param operands list of BigDecimal to check
     * @throws ArithmeticException if operand list is out of bounds
     */
    void checkOperandCount(int minRequired, int maxAllowed, List<BigDecimal> operands)
        throws ArithmeticException {
      if (maxAllowed > -1 && operands.size() > maxAllowed) {
        throw new ArithmeticException("Too many operands.");
      }
      if (operands.size() < minRequired) {
        throw new ArithmeticException("Not enough operands.");
      }
    }

    /** Convert passed object to BigDecimal */
    private static final Function<Object, BigDecimal> toBigDecimal =
        o -> {
          try {
            return new BigDecimal(String.valueOf(o), MATH_CONTEXT);
          } catch (NumberFormatException ignored) {
            return BigDecimal.valueOf(Double.NaN);
          }
        };

    /**
     * Compose passed values into a list of BigDecimal
     *
     * @param args list of object/object[]
     * @return List of BigDecimal
     */
    List<BigDecimal> numberList(Object... args) {
      // ("num1", 1)("num2", "-1")("arr1", new Object[]{0.5, "1", 2, "3.5"});
      List<BigDecimal> values = new LinkedList<>();
      for (Object o : args) {
        switch (o) {
          case String[] strings -> {
            for (String string : strings) {
              values.add(toBigDecimal.apply(string));
            }
          }
          case String string -> {
            string = string.strip();

            if (string.startsWith("[") && string.contains("]")) {
              /* deal with literal arrays */
              values.addAll(
                  numberList((Object) string.substring(1, string.indexOf("]")).split(",")));
            } else if (string.contains(" ")) {
              /* split space delimited values */
              values.addAll(numberList(String.join(" ", string)));
            } else {
              values.add(toBigDecimal.apply(string));
            }
          }
          case Object[] objectArray -> {
            /* contents of options.params */
            for (var object : objectArray) {
              values.add(toBigDecimal.apply(object));
            }
          }
          case Options options -> {
            /* if named arguments exist, try converting them to BigDecimal */
            for (String key : options.hash.keySet()) {
              try {
                BigDecimal bd = new BigDecimal(options.hash(key).toString());
                values.add(bd);
              } catch (NumberFormatException ignored) {
              }
            }
            /* add values from parameter array */
            for (Object param : options.params) {
              values.addAll(numberList(param));
            }
          }
          case null -> {}
          default ->
              /* hooray, it's just a single thing to convert */
              values.add(toBigDecimal.apply(o));
        }
      }
      return values;
    }
  }

  @SuppressWarnings("LoggerInitializedWithForeignClass")
  public static class HBLogger extends LogHelper {
    private static final Logger log = LoggerFactory.getLogger(HandlebarsHelpers.class);

    /** A singleton instance of this helper. */
    public static final Helper<Object> INSTANCE = new HBLogger();

    /** The helper's name. */
    public static final String NAME = "log";

    @Override
    public Object apply(Object context, Options options) throws IOException {
      StringBuilder sb = new StringBuilder();
      String level = options.hash("level", "info");
      TagType tagType = options.tagType;
      if (tagType.inline()) {
        sb.append(context);
        for (int i = 0; i < options.params.length; i++) {
          sb.append(" ").append((Object) options.param(i));
        }
      } else {
        sb.append(options.fn());
      }

      switch (level) {
        case "error":
          log.error(sb.toString().trim());
          break;
        case "debug":
          log.debug(sb.toString().trim());
          break;
        case "warn":
          log.warn(sb.toString().trim());
          break;
        case "trace":
          log.trace(sb.toString().trim());
          break;
        default:
          log.info(sb.toString().trim());
      }
      return null;
    }
  }

  public static class MarkdownHelper implements Helper<Object> {
    private static final MutableDataSet MD_OPTIONS = new MutableDataSet();
    private static final Parser PARSER = Parser.builder(MD_OPTIONS).build();
    private static final HtmlRenderer HTML_RENDERER = HtmlRenderer.builder(MD_OPTIONS).build();

    /** A singleton version of {@link MarkdownHelper}. */
    public static final Helper<Object> INSTANCE = new MarkdownHelper();

    /** The helper's name. */
    public static final String NAME = "markdown";

    @Override
    public Object apply(final Object context, final Options options) throws IOException {
      if (options.isFalsy(context)) {
        return "";
      }
      String markdown = context.toString();
      Node document = PARSER.parse(markdown);
      return new Handlebars.SafeString(HTML_RENDERER.render(document));
    }
  }

  public enum MoreStringHelpers implements Helper<Object> {
    contains {
      @Override
      public Object apply(final Object value, final Options options) throws IOException {
        try {
          return value.toString().contains(options.param(1, ""));
        } catch (Exception e) {
          log.debug("Function \"contains\" - {}", e.getLocalizedMessage(), e);
          return "";
        }
      }
    },
    containsIgnoreCase {
      @Override
      public Object apply(final Object value, final Options options) throws IOException {
        try {
          return value.toString().toLowerCase().contains(options.param(1, "").toLowerCase());
        } catch (Exception e) {
          log.debug("Function \"containsIgnoreCase\" - {}", e.getLocalizedMessage(), e);
          return "";
        }
      }
    },
    equalsIgnoreCase {
      @Override
      public Object apply(final Object value, final Options options) throws IOException {
        try {
          return value.toString().equalsIgnoreCase(options.param(1, ""));
        } catch (Exception e) {
          log.debug("Function \"equalsIgnoreCase\" - {}", e.getLocalizedMessage(), e);
          return "";
        }
      }
    },
    ;

    /**
     * Register the helper in a handlebars instance.
     *
     * @param handlebars A handlebars object. Required.
     */
    public void registerHelper(final Handlebars handlebars) {
      notNull(handlebars, "The handlebars is required.");
      handlebars.registerHelper(name(), this);
    }

    /**
     * Register all the text helpers.
     *
     * @param handlebars The helper's owner. Required.
     */
    public static void register(final Handlebars handlebars) {
      notNull(handlebars, "A handlebars object is required.");
      MoreStringHelpers[] helpers = values();
      for (MoreStringHelpers helper : helpers) {
        helper.registerHelper(handlebars);
      }
    }
  }
}
