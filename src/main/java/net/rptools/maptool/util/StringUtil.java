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

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** @author Tylere */
public class StringUtil {
  private static NumberFormat nf = NumberFormat.getNumberInstance();

  public static String formatDecimal(double value) {
    String result1;
    result1 = nf.format(value); // On a separate line to allow for breakpoints
    return result1;
  }

  /**
   * Returns <code>text</code> converted to a double-precision value, or the value of <code>def
   * </code> if the string cannot be converted. This method is locale-aware.
   *
   * @param text string to convert to a number
   * @param def default value to use if a ParseException is thrown
   * @return the result
   */
  public static Double parseDecimal(String text, Double def) {
    if (text == null) return def;
    try {
      return parseDecimal(text);
    } catch (ParseException e) {
      return def;
    }
  }

  /**
   * Returns <code>text</code> converted to a double-precision value or throws an exception. This
   * method is locale-aware.
   *
   * @param text string to convert to a number
   * @return the result
   */
  public static Double parseDecimal(String text) throws ParseException {
    double def = 0.0;
    if (text == null) return def;
    def = nf.parse(text).doubleValue();
    // System.out.println("Decimal: Input string is >>"+text+"<< and parsing produces "+newValue);
    return def;
  }

  /**
   * Returns <code>text</code> converted to an integer value, or the value of <code>def</code> if
   * the string cannot be converted. This method is locale-aware (which doesn't mean much for
   * integers).
   *
   * @param text string to convert to a number
   * @param def default value to use if a ParseException is thrown
   * @return the result
   */
  public static Integer parseInteger(String text, Integer def) {
    if (text == null) return def;
    try {
      return parseInteger(text);
    } catch (ParseException e) {
      return def;
    }
  }

  /**
   * Returns <code>text</code> converted to an integer value or throws an exception. This method is
   * locale-aware (which doesn't mean much for integers).
   *
   * @param text string to convert to a number
   * @return the result
   */
  public static Integer parseInteger(String text) throws ParseException {
    int def = 0;
    if (text == null) return def;
    def = nf.parse(text).intValue();
    // System.out.println("Integer: Input string is >>"+text+"<< and parsing produces "+newValue);
    return def;
  }

  /**
   * Returns <code>text</code> converted to a Boolean value, or the value of <code>def</code> if the
   * string cannot be converted. This method returns <code>Boolean.TRUE</code> if the string
   * provided is not <code>null</code> and is "true" using a case-insensitive comparison, or if it
   * is parseable as an integer and represents a non-zero value.
   *
   * @param text string to convert to a Boolean
   * @param def default value to use if a ParseException is thrown
   * @return the result
   */
  public static Boolean parseBoolean(String text, Boolean def) {
    if (text == null) return def;
    try {
      return parseBoolean(text);
    } catch (ParseException e) {
      return def;
    }
  }

  /**
   * Returns <code>text</code> converted to a Boolean value or throws an exception. This method
   * returns <code>Boolean.TRUE</code> if the string provided is not <code>null</code> and is "true"
   * using a case-insensitive comparison or represents a non-zero value as an integer.
   *
   * @param text string to convert to a Boolean
   * @return the result
   */
  public static Boolean parseBoolean(String text) throws ParseException {
    Boolean def = Boolean.FALSE;
    if (text != null) {
      text = text.toLowerCase();
      if (text.equals("true")) return Boolean.TRUE;
      else if (text.equals("false")) return Boolean.FALSE;
      def = parseInteger(text) != 0;
    }
    return def;
  }

  public static String wrapText(String string, int wrapLength, int startPosition, String wrapChar) {
    StringBuilder wrappedString = new StringBuilder();
    String subString;
    int newlinePos;
    int length = string.length();

    if (length - startPosition <= wrapLength) {
      return string;
    }
    while (length - startPosition > wrapLength) {
      // look ahead one char (wrapLength + 1) in case it is a space or newline
      subString = string.substring(startPosition, startPosition + wrapLength + 1);
      // restart if newline character is found
      newlinePos = subString.lastIndexOf(wrapChar);
      if (newlinePos == -1) {
        // if there's no line break, then find the first space to break the line
        newlinePos = subString.lastIndexOf(" ");
        if (newlinePos == -1) {
          // if there are no spaces, then force the line break within the word.
          newlinePos = wrapLength - 1; // -1 because of 0 start point of position
        }
      }
      wrappedString.append(subString.substring(0, newlinePos));
      wrappedString.append(wrapChar);
      startPosition += newlinePos + 1;
    }
    // add the remainder of the string
    wrappedString.append(string.substring(startPosition));
    return wrappedString.toString();
  }

  /**
   * Gets copy of <b>string</b> wrapped with '\n' character a wraplength or the nearest space
   * between words.
   *
   * @param string The multiline string to be wrapped
   * @param wrapLength the number of characters before wrapping
   */
  public static String wrapText(String string, int wrapLength) {
    return wrapText(string, wrapLength, 0, "\n");
  }

  /**
   * Whether the string is null or all whitespace chars (This should use {@link String#isEmpty()}
   * but that's new to Java 6 and we're trying to stay compatible with Java 5 if possible.)
   */
  public static boolean isEmpty(String string) {
    return string == null || string.trim().length() == 0;
  }

  public static int countOccurances(String source, String str) {
    int count = 0;
    int index = 0;
    while ((index = source.indexOf(str, index)) >= 0) {
      count++;
      index += str.length();
    }
    return count;
  }

  public static List<String> getWords(String line) {
    List<String> list = new ArrayList<String>();
    while (line != null && line.trim().length() > 0) {
      line = line.trim();
      // System.out.println("'" + line + "'");
      List<String> split = splitNextWord(line);

      String nextWord = split.get(0);
      line = split.get(1);
      if (nextWord == null) {
        continue;
      }
      list.add(nextWord);
    }
    return list;
  }

  public static String getFirstWord(String line) {
    List<String> split = splitNextWord(line);
    return split != null ? split.get(0) : null;
  }

  public static String findMatch(String pattern, List<String> stringList) {
    for (String listValue : stringList) {
      String upperValue = listValue.toUpperCase();
      if (upperValue.startsWith(pattern.toUpperCase())) {
        return listValue;
      }
    }
    return "";
  }

  public static List<String> splitNextWord(String line) {
    line = line.trim();
    if (line.length() == 0) {
      return null;
    }
    StringBuilder builder = new StringBuilder();
    boolean quoted = line.charAt(0) == '"';

    int start = quoted ? 1 : 0;
    int end = start;
    for (; end < line.length(); end++) {
      char c = line.charAt(end);
      if (quoted) {
        if (c == '"') {
          break;
        }
      } else {
        if (Character.isWhitespace(c)) {
          break;
        }
      }
      builder.append(c);
    }
    return Arrays.asList(
        new String[] {
          line.substring(start, end), line.substring(Math.min(end + 1, line.length()))
        });
  }
}
