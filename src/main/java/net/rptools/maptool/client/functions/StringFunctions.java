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

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.IllegalFormatConversionException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import net.rptools.maptool.language.I18N;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;

public class StringFunctions extends AbstractFunction {
  private int matchNo = 0;
  private final Pattern strFormatVariable = Pattern.compile("%\\{([^}]+)\\}");

  private static final StringFunctions instance = new StringFunctions();

  private StringFunctions() {
    super(
        1,
        UNLIMITED_PARAMETERS,
        "replace",
        "stringToList",
        "substring",
        "length",
        "upper",
        "lower",
        "indexOf",
        "lastIndexOf",
        "trim",
        "strformat",
        "matches",
        "string",
        "number",
        "isNumber",
        "strfind",
        "getFindCount",
        "getGroup",
        "getGroupStart",
        "getGroupEnd",
        "getGroupCount",
        "encode",
        "decode",
        "startsWith",
        "endsWith",
        "capitalize");
  }

  public static StringFunctions getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> parameters)
      throws ParserException {
    try {
      if (functionName.equals("replace")) {
        if (parameters.size() < 3) {
          throw new ParserException(
              I18N.getText(
                  "macro.function.general.notEnoughParam", functionName, 3, parameters.size()));
        }
        if (parameters.size() > 3) {
          if (!(parameters.get(3) instanceof BigDecimal)) {
            throw new ParserException(
                I18N.getText(
                    "macro.function.general.argumentTypeN",
                    functionName,
                    4,
                    parameters.get(3).toString()));
          }
          return replace(
              parameters.get(0).toString(),
              parameters.get(1).toString(),
              parameters.get(2).toString(),
              ((BigDecimal) parameters.get(3)).intValue());
        } else {
          return replace(
              parameters.get(0).toString(),
              parameters.get(1).toString(),
              parameters.get(2).toString());
        }
      }
      if (functionName.equals("stringToList")) {
        if (parameters.size() < 2) {
          throw new ParserException(
              I18N.getText(
                  "macro.function.general.notEnoughParam", functionName, 2, parameters.size()));
        }
        String delim;
        if (parameters.size() > 2) {
          delim = parameters.get(2).toString();
        } else {
          delim = ",";
        }
        return stringToList(parameters.get(0).toString(), parameters.get(1).toString(), delim);
      }
      if (functionName.equals("substring")) {
        if (parameters.size() < 2) {
          throw new ParserException(
              I18N.getText(
                  "macro.function.general.notEnoughParam", functionName, 2, parameters.size()));
        }
        if (!(parameters.get(1) instanceof BigDecimal)) {
          throw new ParserException(
              I18N.getText(
                  "macro.function.general.argumentTypeN",
                  functionName,
                  2,
                  parameters.get(1).toString()));
        }
        int end;
        if (parameters.size() > 2) {
          if (!(parameters.get(2) instanceof BigDecimal)) {
            throw new ParserException(
                I18N.getText(
                    "macro.function.general.argumentTypeN",
                    functionName,
                    3,
                    parameters.get(2).toString()));
          }
          end = ((BigDecimal) parameters.get(2)).intValue();
        } else {
          end = parameters.get(0).toString().length();
        }
        return parameters
            .get(0)
            .toString()
            .substring(((BigDecimal) parameters.get(1)).intValue(), end);
      }
      if (functionName.equals("length")) {
        return BigDecimal.valueOf(parameters.get(0).toString().length());
      }
      if (functionName.equals("upper")) {
        if (parameters.size() > 1) {
          String str = parameters.get(0).toString();
          try {
            int len = Integer.parseInt(parameters.get(1).toString());
            len = Math.min(len, str.length());
            return str.substring(0, len).toUpperCase() + str.substring(len);
          } catch (NumberFormatException nfe) {
            throw new ParserException(
                I18N.getText(
                    "macro.function.general.argumentTypeN",
                    functionName,
                    2,
                    parameters.get(1).toString()));
          }
        } else {
          return parameters.get(0).toString().toUpperCase();
        }
      }
      if (functionName.equals("lower")) {
        if (parameters.size() > 1) {
          String str = parameters.get(0).toString();
          try {
            int len = Integer.parseInt(parameters.get(1).toString());
            len = Math.min(len, str.length());
            return str.substring(0, len).toLowerCase() + str.substring(len);
          } catch (NumberFormatException nfe) {
            throw new ParserException(
                I18N.getText(
                    "macro.function.general.argumentTypeN",
                    functionName,
                    2,
                    parameters.get(1).toString()));
          }
        } else {
          return parameters.get(0).toString().toLowerCase();
        }
      }
      if (functionName.equals("indexOf")) {
        if (parameters.size() < 2) {
          throw new ParserException(
              I18N.getText(
                  "macro.function.general.notEnoughParam", functionName, 2, parameters.size()));
        }
        int from = 0;
        if (parameters.size() > 2) {
          if (!(parameters.get(2) instanceof BigDecimal)) {
            throw new ParserException(
                I18N.getText(
                    "macro.function.general.argumentTypeN",
                    functionName,
                    3,
                    parameters.get(2).toString()));
          }
          from = ((BigDecimal) parameters.get(2)).intValue();
        } else {
          from = 0;
        }
        return BigDecimal.valueOf(
            parameters.get(0).toString().indexOf(parameters.get(1).toString(), from));
      }
      if (functionName.equals("lastIndexOf")) {
        if (parameters.size() < 2) {
          throw new ParserException(
              I18N.getText(
                  "macro.function.general.notEnoughParam", functionName, 2, parameters.size()));
        }
        return BigDecimal.valueOf(
            parameters.get(0).toString().lastIndexOf(parameters.get(1).toString()));
      }
      if (functionName.equals("trim")) {
        return parameters.get(0).toString().trim();
      }
      if (functionName.equals("strformat")) {
        int size = parameters.size();
        if (size > 1) {
          return format(
              parameters.get(0).toString(),
              parser.getVariableResolver(),
              parameters.subList(1, size));
        } else {
          return format(parameters.get(0).toString(), parser.getVariableResolver(), null);
        }
      }
      if (functionName.equals("matches")) {
        if (parameters.size() < 2) {
          throw new ParserException(
              I18N.getText(
                  "macro.function.general.notEnoughParam", functionName, 2, parameters.size()));
        }
        if (parameters.get(0).toString().matches(parameters.get(1).toString())) {
          return BigDecimal.valueOf(1);
        } else {
          return BigDecimal.valueOf(0);
        }
      }
    } catch (PatternSyntaxException e) {
      throw new ParserException(e.getMessage());
    }
    if (functionName.equals("string")) {
      return parameters.get(0).toString();
    }
    if (functionName.equals("number")) {
      if (parameters.get(0).toString().trim().isEmpty()) return BigDecimal.ZERO;
      try {
        return BigDecimal.valueOf(Integer.parseInt(parameters.get(0).toString()));
      } catch (NumberFormatException e) {
        // Do nothing as we will try it as a double
      }
      try {
        return BigDecimal.valueOf(Double.parseDouble(parameters.get(0).toString()));
      } catch (NumberFormatException e) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.argumentTypeN",
                functionName,
                1,
                parameters.get(0).toString()));
      }
    }
    if (functionName.equals("isNumber")) {
      try {
        BigDecimal.valueOf(Integer.parseInt(parameters.get(0).toString()));
        return BigDecimal.ONE;
      } catch (NumberFormatException e) {
        // Do nothing as we will try it as a double
      }
      try {
        BigDecimal.valueOf(Double.parseDouble(parameters.get(0).toString()));
        return BigDecimal.ONE;
      } catch (NumberFormatException e) {
        return BigDecimal.ZERO;
      }
    }
    if (functionName.equals("strfind")) {
      if (parameters.size() < 2) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.notEnoughParam", functionName, 2, parameters.size()));
      }
      return stringFind(
          parser.getVariableResolver(), parameters.get(0).toString(), parameters.get(1).toString());
    }
    if (functionName.equals("getGroupCount")) {
      if (parameters.size() < 1) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.notEnoughParam", functionName, 1, parameters.size()));
      }
      VariableResolver resolver = parser.getVariableResolver();
      StringBuilder sb = new StringBuilder();
      sb.append("match.").append(parameters.get(0)).append(".groupCount");
      return resolver.getVariable(sb.toString());
    }
    if (functionName.startsWith("getGroup")) {
      if (parameters.size() < 3) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.notEnoughParam", functionName, 3, parameters.size()));
      }
      VariableResolver resolver = parser.getVariableResolver();
      StringBuilder sb = new StringBuilder();
      sb.append("match.").append(parameters.get(0));
      sb.append(".m").append(parameters.get(1));
      sb.append(".group").append(parameters.get(2));
      if (functionName.equals("getGroupStart")) {
        sb.append(".start");
      } else if (functionName.equals("getGroupEnd")) {
        sb.append(".end");
      }
      return resolver.getVariable(sb.toString());
    }
    if (functionName.equals("getFindCount")) {
      if (parameters.size() < 1) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.notEnoughParam", functionName, 1, parameters.size()));
      }
      VariableResolver resolver = parser.getVariableResolver();
      StringBuilder sb = new StringBuilder();
      sb.append("match.").append(parameters.get(0)).append(".matchCount");
      return resolver.getVariable(sb.toString());
    }
    if (functionName.equals("encode")) {
      if (parameters.size() < 1) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.notEnoughParam", functionName, 1, parameters.size()));
      }
      String encoded;
      try {
        // Shouldn't this use '&#59;' like
        // net.rptools.maptool.client.functions.MacroLinkFunction.argsToStrPropList(String) does?
        encoded = parameters.get(0).toString().replaceAll(";", "&semi;");
        encoded = URLEncoder.encode(encoded, "utf-8");
      } catch (UnsupportedEncodingException e) {
        throw new ParserException(e);
      }
      return encoded;
    }
    if (functionName.equals("decode")) {
      if (parameters.size() < 1) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.notEnoughParam", functionName, 1, parameters.size()));
      }
      String decoded;
      try {
        // Shouldn't this use '&#59;' like
        // net.rptools.maptool.client.functions.MacroLinkFunction.argsToStrPropList(String) does?
        decoded = URLDecoder.decode(parameters.get(0).toString(), "utf-8");
        decoded = decoded.replaceAll("&semi;", ";");
      } catch (UnsupportedEncodingException e) {
        throw new ParserException(e);
      }
      return decoded;
    }
    if (functionName.equals("startsWith")) {
      if (parameters.size() < 2) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.notEnoughParam", functionName, 2, parameters.size()));
      }
      return parameters.get(0).toString().startsWith(parameters.get(1).toString())
          ? BigDecimal.ONE
          : BigDecimal.ZERO;
    }
    if (functionName.equals("endsWith")) {
      if (parameters.size() < 2) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.notEnoughParam", functionName, 2, parameters.size()));
      }
      return parameters.get(0).toString().endsWith(parameters.get(1).toString())
          ? BigDecimal.ONE
          : BigDecimal.ZERO;
    }
    if (functionName.equals("capitalize")) {
      if (parameters.size() < 1) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.notEnoughParam", functionName, 1, parameters.size()));
      }
      return capitalize(parameters.get(0).toString());
    }
    // should never happen
    throw new ParserException(functionName + "(): Unknown function.");
  }

  /**
   * This method returns a version of the passed in string where all the first letters of words are
   * title case.
   *
   * @param str The string converted to title case.
   * @return The string converted to title case.
   */
  private String capitalize(String str) {
    Pattern pattern = Pattern.compile("(\\p{IsAlphabetic}+)");
    Matcher matcher = pattern.matcher(str);

    StringBuffer result = new StringBuffer();
    while (matcher.find()) {
      String word = matcher.group();
      matcher.appendReplacement(result, Character.toTitleCase(word.charAt(0)) + word.substring(1));
    }

    matcher.appendTail(result);

    return result.toString();
  }

  /**
   * Formats a string using the String.format() rules, as well as replacing any values in %{} with
   * the contents of the variable.
   *
   * @param string The string to format.
   * @param resolver The variable resolver used to resolve variables within %{}.
   * @param args The arguments for formating options.
   * @return the formated string.
   * @throws ParserException
   */
  public String format(String string, VariableResolver resolver, List<Object> args)
      throws ParserException {
    StringBuffer sb = new StringBuffer();
    // First replace all variables
    Matcher m = strFormatVariable.matcher(string);
    while (m.find()) {
      try {
        m.appendReplacement(sb, resolver.getVariable(m.group(1)).toString());
      } catch (NullPointerException npe) {
        // FJE Added catch block so that NPE leaves original format intact in the output string
        // m.appendReplacement(sb, m.group(1).toString());
      }
    }
    m.appendTail(sb);

    if (args == null) {
      return sb.toString();
    }

    Object[] argArray = args.toArray();

    // Change all integers in BigDecimal to BigIntegers so formating specifiers work correctly.
    for (int i = 0; i < argArray.length; i++) {
      if (argArray[i] instanceof BigDecimal) {
        BigDecimal bd = (BigDecimal) argArray[i];
        if (bd.scale() < 1) {
          argArray[i] = bd.toBigInteger();
        }
      }
    }

    try {
      return String.format(sb.toString(), argArray);
    } catch (IllegalFormatConversionException e) {
      throw new ParserException(e.getMessage());
    }
  }

  public String sanitize(String input) {
    return input.replaceAll("\u00ab|\u00bb|&#171;|&#187;|&laquo;|&raquo;|\036|\037", "");
  }

  /**
   * Splits up a string based on a pattern and returns s string delimited list.
   *
   * @param string The string to split up.
   * @param pattern The pattern used to split the string.
   * @param delim The delimiter that is used in the resulting output string.
   * @return the delimited list.
   */
  public String stringToList(String string, String pattern, String delim) {
    String[] parts = string.split(pattern);
    return join(parts, delim);
  }

  /**
   * Joins the array together as a string with a default delimiter of ','.
   *
   * @param array The array to join.
   * @return the resulting string.
   */
  public String join(String[] array) {
    return join(array, ",");
  }

  /**
   * Joins the array together as a string with a default delimiter of ','.
   *
   * @param list The list to join.
   * @return the resulting string.
   */
  public String join(List<String> list) {
    return join(list, ",");
  }

  /**
   * Joins the array together as a string with a default delimiter of ','.
   *
   * @param list The array to join.
   * @param delim the delimiter of the list
   * @return the resulting string.
   */
  public String join(List<String> list, String delim) {
    String[] array = new String[list.size()];
    list.toArray(array);
    return join(array, delim);
  }

  /**
   * Joins the array together as a string with the specified delimiter.
   *
   * @param array The array to join.
   * @param delim The delimiter to use between elements.
   * @return the resulting string.
   */
  public String join(String[] array, String delim) {
    StringBuilder sb = new StringBuilder();
    for (String s : array) {
      if (sb.length() > 0) {
        sb.append(delim);
      }
      sb.append(s);
    }
    return sb.toString();
  }

  /**
   * Replaces a pattern in a string a certain number of times. The pattern and replacement strings
   * follow the same rules as in the repalceFirst() and replaceAll() methods in String.
   *
   * @param string The string to do the replacement on.
   * @param pattern The pattern to replace.
   * @param replacement The value to replace the pattern with.
   * @param times The number of times to perform the replacement.
   * @return the modified version of the string.
   */
  public String replace(String string, String pattern, String replacement, int times) {
    if (times < 0) {
      return replace(string, pattern, replacement);
    }

    StringBuffer sb = new StringBuffer();
    Matcher m = Pattern.compile(pattern).matcher(string);
    while (m.find()) {
      if (times < 1) {
        break;
      }
      m.appendReplacement(sb, replacement);
      times--;
    }
    m.appendTail(sb);
    return sb.toString();
  }

  /**
   * Replaces all the occurrences of a pattern in a string. The pattern and replacement strings
   * follow the same rules as in the repalceFirst() and replaceAll() methods in String.
   *
   * @param string The string to do the replacement on.
   * @param pattern The pattern to replace.
   * @param replacement The value to replace the pattern with.
   * @return the modified version of the string.
   */
  public String replace(String string, String pattern, String replacement) {
    return string.replaceAll(pattern, replacement);
  }

  /**
   * Matches the pattern against the input string and set variables in the resolver with the capture
   * groups
   *
   * @param resolver The variable resolver to set the variables in.
   * @param str The string to match the pattern against.
   * @param pattern The pattern to match.
   * @return The number of matches that were found
   * @throws ParserException
   *     <p>Variables that are set in the variable resolver. match.groupCount = The number of
   *     capture groups in the pattern. {matchNo} is a sequence used to differentiate different
   *     calls to strfind match.{matchNo}.matchCount = The number of matches found.
   *     match.{matchNo}.m{M}.group{G} = The matching string for Match {M} and Group number {G}.
   *     match.{matchNo}.m{M}.group{G}.start = The start of Group number {G} in Match Number {M}
   *     match.{matchNo}.m{M}.group{G}.end = The end of Group number {G} in Match Number {M}
   */
  public BigDecimal stringFind(VariableResolver resolver, String str, String pattern)
      throws ParserException {
    Pattern p = Pattern.compile(pattern);
    Matcher m = p.matcher(str);
    int found = 0;

    int matchId = nextMatchNo();
    resolver.setVariable("match." + matchId + ".groupCount", m.groupCount());
    while (m.find()) {
      found++;
      for (int i = 1; i < m.groupCount() + 1; i++) {
        resolver.setVariable(
            "match." + matchId + ".m" + found + ".group" + i, m.group(i) == null ? "" : m.group(i));
        resolver.setVariable(
            "match." + matchId + ".m" + found + ".group" + i + ".start", m.start(i));
        resolver.setVariable("match." + matchId + ".m" + found + ".group" + i + ".end", m.end(i));
      }
      resolver.setVariable(
          "match." + matchId + ".m" + found + ".group0", m.group() == null ? "" : m.group());
      resolver.setVariable("match." + matchId + ".m" + found + ".group0.start", m.start());
      resolver.setVariable("match." + matchId + ".m" + found + ".group0.end", m.end());
    }
    resolver.setVariable("match." + matchId + ".matchCount", found);
    return BigDecimal.valueOf(matchId);
  }

  private synchronized int nextMatchNo() {
    return matchNo++;
  }
}
