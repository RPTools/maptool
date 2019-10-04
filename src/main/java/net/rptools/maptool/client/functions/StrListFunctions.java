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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.rptools.maptool.language.I18N;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;
import net.rptools.parser.function.ParameterException;
import org.apache.commons.lang.StringUtils;

/**
 * Implements various string utility functions. <br>
 * <br>
 * The <code>list*</code> functions operate on a list string of the form "item1, item2, ...". <br>
 * An optional final argument <code>delim</code> sets the item delimiter.
 *
 * @author knizia.fan
 */
public class StrListFunctions extends AbstractFunction {
  public StrListFunctions() {
    super(
        1,
        UNLIMITED_PARAMETERS,
        "listGet",
        "listDelete",
        "listCount",
        "listFind",
        "listContains",
        "listAppend",
        "listInsert",
        "listReplace",
        "listSort",
        "listFormat");
  }

  /** The singleton instance. */
  private static final StrListFunctions instance = new StrListFunctions();

  /**
   * Gets the Input instance.
   *
   * @return the instance.
   */
  public static StrListFunctions getInstance() {
    return instance;
  }

  /**
   * Parses a list.
   *
   * @param listStr has the form "item1, item2, ..."
   * @param list is populated with the list items.
   * @param delim is the list delimiter to use.
   */
  public static void parse(String listStr, List<String> list, String delim) {
    if (StringUtils.isEmpty(listStr.trim())) return; // null strings have zero entries
    String patt;
    if (delim.equalsIgnoreCase("")) {
      patt = "(.)()";
    } else {
      String escDelim = fullyQuoteString(delim);
      patt = "(.*?)" + escDelim + "|(.*)";
    }
    // This pattern needs to be compiled with the DOTALL flag or line terminators might
    // cause premature termination of the matcher.find() operations...
    Pattern pattern = Pattern.compile(patt, Pattern.DOTALL);
    Matcher matcher = pattern.matcher(listStr);
    boolean lastItem = false;
    while (matcher.find()) {
      if (!lastItem) {
        String grp = matcher.group(1);
        if (grp == null) {
          grp = matcher.group(2);
          // We're here because there was no trailing delimiter in this match.
          // In this case, the next match will be empty, but we don't want to grab it.
          // (We do grab the final empty match if the string ended with the delimiter.)
          // This flag will prevent that.
          lastItem = true;
        }
        list.add(grp.trim());
      }
    }
  }

  /**
   * Prepares a string for use in regex operations.
   *
   * @param s the String that could have non-alphanumeric characters
   * @return a new String, with the non-alphanumeric characters escaped
   */
  public static String fullyQuoteString(String s) {
    // We escape each non-alphanumeric character in the delimiter string
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      if (!Character.isLetterOrDigit(s.charAt(i))) {
        sb.append("\\");
      }
      sb.append(s.charAt(i));
    }
    return sb.toString();
  }

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> parameters)
      throws ParserException {
    Object retval = "";
    String listStr = parameters.get(0).toString().trim();
    String lastParam = parameters.get(parameters.size() - 1).toString();

    ArrayList<String> list = new ArrayList<String>();

    if ("listGet".equalsIgnoreCase(functionName))
      retval = listGet(parameters, listStr, lastParam, list);
    else if ("listDelete".equalsIgnoreCase(functionName))
      retval = listDelete(parameters, listStr, lastParam, list);
    else if ("listCount".equalsIgnoreCase(functionName))
      retval = listCount(parameters, listStr, lastParam, list);
    else if ("listFind".equalsIgnoreCase(functionName))
      retval = listFind(parameters, listStr, lastParam, list);
    else if ("listContains".equalsIgnoreCase(functionName))
      retval = listContains(parameters, listStr, lastParam, list);
    else if ("listAppend".equalsIgnoreCase(functionName))
      retval = listAppend(parameters, listStr, lastParam, list);
    else if ("listInsert".equalsIgnoreCase(functionName))
      retval = listInsert(parameters, listStr, lastParam, list);
    else if ("listReplace".equalsIgnoreCase(functionName))
      retval = listReplace(parameters, listStr, lastParam, list);
    else if ("listSort".equalsIgnoreCase(functionName))
      retval = listSort(parameters, listStr, lastParam, list);
    else if ("listFormat".equalsIgnoreCase(functionName))
      retval = listFormat(parameters, listStr, lastParam, list);

    return retval;
  }

  /**
   * MapTool call: <code>listGet(list, index [,delim])</code>
   *
   * @param parameters the parameters of the function call
   * @param listStr the String of the list
   * @param lastParam the last parameter
   * @param list the list that will contain the elements
   * @return The item at position <code>index</code>, or <code>""</code> if out of bounds.
   */
  public Object listGet(
      List<Object> parameters, String listStr, String lastParam, List<String> list)
      throws ParameterException {
    Object retval = "";
    String delim = ",";

    int minParams = 2;
    int maxParams = minParams + 1;
    checkVaryingParameters(
        "listGet()",
        minParams,
        maxParams,
        parameters,
        new Class[] {null, BigDecimal.class, String.class});
    if (parameters.size() == maxParams) delim = lastParam;
    parse(listStr, list, delim);

    int index = ((BigDecimal) parameters.get(1)).intValue();
    if (index >= 0 && index < list.size()) {
      String value = list.get(index);
      if (value != null) {
        // convert to numeric value if possible
        Integer intval = strToInt(value);
        retval = (intval == null) ? value : new BigDecimal(intval);
      } else {
        retval = "";
      }
    }
    return retval;
  }

  /**
   * <code>listDelete(list, index [,delim])</code>
   *
   * @param parameters the parameters of the function call
   * @param listStr the String of the list
   * @param lastParam the last parameter
   * @param list the list that will contain the parsed element
   * @return A new list with the item at position <code>index</code> deleted.
   */
  public Object listDelete(
      List<Object> parameters, String listStr, String lastParam, List<String> list)
      throws ParameterException {
    Object retval = "";
    String delim = ",";

    int minParams = 2;
    int maxParams = minParams + 1;
    checkVaryingParameters(
        "listDelete()",
        minParams,
        maxParams,
        parameters,
        new Class[] {null, BigDecimal.class, String.class});
    if (parameters.size() == maxParams) {
      delim = lastParam;
    }
    parse(listStr, list, delim);

    int index = ((BigDecimal) parameters.get(1)).intValue();
    StringBuilder sb = new StringBuilder();
    boolean inRange = (index >= 0 && index < list.size());
    for (int i = 0; i < list.size(); i++) {
      if (i != index) {
        sb.append(list.get(i));
        sb.append(delim);
        sb.append(" ");
      }
    }
    if (list.size() > (inRange ? 1 : 0)) {
      // Delete the last delimiter and space
      sb.delete(sb.length() - 1 - delim.length(), sb.length());
    }
    retval = sb.toString();
    return retval;
  }

  /**
   * MapTool call: <code>listCount(list [,delim])</code>
   *
   * @param parameters the parameters of the function call
   * @param listStr the String of the list
   * @param lastParam the last parameter
   * @param list the list that will contain the parsed element
   * @return The number of entries in the list.
   */
  public Object listCount(
      List<Object> parameters, String listStr, String lastParam, List<String> list)
      throws ParameterException {
    Object retval = "";
    String delim = ",";

    int minParams = 1;
    int maxParams = minParams + 1;
    checkVaryingParameters(
        "listCount()", minParams, maxParams, parameters, new Class[] {null, String.class});
    if (parameters.size() == maxParams) {
      delim = lastParam;
    }
    parse(listStr, list, delim);

    retval = new BigDecimal(list.size());
    return retval;
  }

  /**
   * MapTool call: <code>listFind(list, target [,delim])</code>
   *
   * @param parameters the parameters of the function call
   * @param listStr the String of the list
   * @param lastParam the last parameter
   * @param list the list that will contain the parsed element
   * @return The index of the first occurence of <code>target</code>, or -1 if not found.
   */
  public Object listFind(
      List<Object> parameters, String listStr, String lastParam, List<String> list)
      throws ParameterException {
    Object retval = "";
    String delim = ",";

    int minParams = 2;
    int maxParams = minParams + 1;
    checkVaryingParameters(
        "listFind()", minParams, maxParams, parameters, new Class[] {null, null, String.class});
    if (parameters.size() == maxParams) {
      delim = lastParam;
    }
    parse(listStr, list, delim);

    String target = parameters.get(1).toString().trim();
    int index;
    for (index = 0; index < list.size(); index++) {
      if (target.equalsIgnoreCase(list.get(index))) {
        break;
      }
    }
    if (index == list.size()) index = -1;
    retval = new BigDecimal(index);
    return retval;
  }

  /**
   * MapTool call: <code>listContains(list, target [,delim])</code>
   *
   * @param parameters the parameters of the function call
   * @param listStr the String of the list
   * @param lastParam the last parameter
   * @param list the list that will contain the parsed element
   * @return Number of occurrences of <code>target</code> in <code>list</code>.
   */
  public Object listContains(
      List<Object> parameters, String listStr, String lastParam, List<String> list)
      throws ParameterException {
    Object retval = "";
    String delim = ",";

    int minParams = 2;
    int maxParams = minParams + 1;
    checkVaryingParameters(
        "listContains()", minParams, maxParams, parameters, new Class[] {null, null, String.class});
    if (parameters.size() == maxParams) {
      delim = lastParam;
    }
    parse(listStr, list, delim);

    String target = parameters.get(1).toString().trim();
    int numMatches = 0;
    for (int index = 0; index < list.size(); index++) {
      if (target.equalsIgnoreCase(list.get(index))) {
        numMatches++;
      }
    }
    retval = new BigDecimal(numMatches);
    return retval;
  }

  /**
   * MapTool call: <code>listAppend(list, target [,delim])</code>
   *
   * @param parameters the parameters of the function call
   * @param listStr the String of the list
   * @param lastParam the last parameter
   * @param list the list that will contain the parsed element
   * @return A new list with <code>target</code> appended.
   */
  public Object listAppend(
      List<Object> parameters, String listStr, String lastParam, List<String> list)
      throws ParameterException {
    Object retval = "";
    String delim = ",";

    int minParams = 2;
    int maxParams = minParams + 1;
    checkVaryingParameters(
        "listAppend()", minParams, maxParams, parameters, new Class[] {null, null, String.class});
    if (parameters.size() == maxParams) {
      delim = lastParam;
    }
    parse(listStr, list, delim);

    String target = parameters.get(1).toString().trim();
    StringBuilder sb = new StringBuilder();
    for (String item : list) {
      sb.append(item);
      sb.append(delim);
      sb.append(" ");
    }
    sb.append(target);
    retval = sb.toString();
    return retval;
  }

  /**
   * MapTool call: <code>listInsert(list, index, target [,delim])</code>
   *
   * @param parameters the parameters of the function call
   * @param listStr the String of the list
   * @param lastParam the last parameter
   * @param list the list that will contain the parsed element
   * @return A new list with <code>target</code> inserted before the item at position <code>index
   *     </code>
   */
  public Object listInsert(
      List<Object> parameters, String listStr, String lastParam, List<String> list)
      throws ParameterException {
    Object retval = "";
    String delim = ",";

    int minParams = 3;
    int maxParams = minParams + 1;
    checkVaryingParameters(
        "listInsert()",
        minParams,
        maxParams,
        parameters,
        new Class[] {null, BigDecimal.class, null, String.class});
    if (parameters.size() == maxParams) {
      delim = lastParam;
    }
    parse(listStr, list, delim);

    int index = ((BigDecimal) parameters.get(1)).intValue();
    String target = parameters.get(2).toString().trim();
    StringBuilder sb = new StringBuilder();
    if (list.size() == 0) {
      if (index == 0) {
        retval = target;
      }
    } else {
      for (int i = 0; i < list.size() + 1; i++) {
        if (i == index) {
          sb.append(target);
          sb.append(delim);
          sb.append(" ");
        }
        if (i < list.size()) {
          sb.append(list.get(i));
          sb.append(delim);
          sb.append(" ");
        }
      }
      sb.delete(sb.length() - 1 - delim.length(), sb.length()); // remove the trailing ", "
      retval = sb.toString();
    }
    return retval;
  }

  /**
   * MapTool call: <code>listReplace(list, index, target [,delim])</code>
   *
   * @param parameters the parameters of the function call
   * @param listStr the String of the list
   * @param lastParam the last parameter
   * @param list the list that will contain the parsed element
   * @return A new list with the entry at <code>index</code> repaced by <code>target</code>
   */
  public Object listReplace(
      List<Object> parameters, String listStr, String lastParam, List<String> list)
      throws ParameterException {
    Object retval = "";
    String delim = ",";

    int minParams = 3;
    int maxParams = minParams + 1;
    checkVaryingParameters(
        "listReplace()",
        minParams,
        maxParams,
        parameters,
        new Class[] {null, BigDecimal.class, null, String.class});
    if (parameters.size() == maxParams) {
      delim = lastParam;
    }
    parse(listStr, list, delim);

    if (list.size() == 0) // can't replace if there are no entries
    return retval;

    int index = ((BigDecimal) parameters.get(1)).intValue();
    String target = parameters.get(2).toString().trim();
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < list.size(); i++) {
      if (i == index) {
        sb.append(target);
      } else {
        sb.append(list.get(i));
      }
      sb.append(delim);
      sb.append(" ");
    }
    sb.delete(sb.length() - 1 - delim.length(), sb.length()); // remove the trailing ", "
    retval = sb.toString();
    return retval;
  }

  /**
   * MapTool call: <code>listSort(list, sortType [,delim])</code>
   *
   * @param parameters the parameters of the function call
   * @param listStr the String of the list
   * @param lastParam the last parameter
   * @param list the list that will contain the parsed element
   * @return A new sorted list
   * @throws ParameterException if the number of parameters is incorrect
   */
  public Object listSort(
      List<Object> parameters, String listStr, String lastParam, List<String> list)
      throws ParameterException {
    Object retval = "";
    String delim = ",";

    int minParams = 2;
    int maxParams = minParams + 1;
    checkVaryingParameters(
        "listSort()",
        minParams,
        maxParams,
        parameters,
        new Class[] {null, String.class, String.class});

    // Check params and parse the list
    if (parameters.size() == maxParams) {
      delim = lastParam;
    }
    String sortStr = (String) parameters.get(1);
    parse(listStr, list, delim);

    // Sort the list appropriately and construct the new list string
    Collections.sort(list, new strComp(sortStr));

    StringBuilder sb = new StringBuilder();
    int size = list.size();
    for (int i = 0; i < size; i++) {
      sb.append(list.get(i));
      if (i < size - 1) {
        sb.append(delim);
        sb.append(" ");
      }
    }
    retval = sb.toString();

    return retval;
  }

  /**
   * MapTool call: <code>listFormat(list, listFormat, itemFormat, separator [,delim])</code>
   *
   * @param parameters the parameters of the function call
   * @param listStr the String of the list
   * @param lastParam the last parameter
   * @param list the list that will contain the parsed element
   * @return A string containing the formatted list.
   * @throws ParameterException
   */
  public Object listFormat(
      List<Object> parameters, String listStr, String lastParam, List<String> list)
      throws ParameterException {
    Object retval = "";
    String delim = ",";

    int minParams = 4;
    int maxParams = minParams + 1;
    checkVaryingParameters(
        "listFormat()",
        minParams,
        maxParams,
        parameters,
        new Class[] {null, String.class, String.class, String.class, String.class});
    if (parameters.size() == maxParams) {
      delim = lastParam;
    }
    parse(listStr, list, delim);

    String listFormat = parameters.get(1).toString();
    String entryFormat = parameters.get(2).toString();
    String separator = parameters.get(3).toString();

    StringBuilder sb = new StringBuilder();
    boolean firstEntry = true;
    for (String item : list) {
      item = fullyQuoteString(item);
      if (firstEntry) {
        firstEntry = false;
      } else {
        sb.append(separator);
      }
      String entry = entryFormat;
      entry = entry.replaceAll("\\%item", item);
      sb.append(entry);
    }

    retval = listFormat.replaceFirst("\\%list", sb.toString());
    return retval;
  }

  /** Custom comparator for string sorting */
  public static class strComp implements Comparator<String> {
    public sortType st; // The type of sort to use
    public int so; // The order of the sort (1 ascending, -1 descending)
    public static final Pattern pattern = Pattern.compile("^([^0-9]*)([0-9]+)(.*)");

    enum sortType {
      ALPHA,
      NUMERIC;
    }

    public strComp(String sortTypeStr) {
      st = sortType.ALPHA; // default
      so = 1; // default
      if (sortTypeStr.length() > 0) {
        String ch0 = sortTypeStr.substring(0, 1);
        if (ch0.equalsIgnoreCase("A")) st = sortType.ALPHA;
        else if (ch0.equalsIgnoreCase("N")) st = sortType.NUMERIC;
      }
      if (sortTypeStr.length() > 1) {
        String ch1 = sortTypeStr.substring(1, 2);
        if (ch1.equalsIgnoreCase("+")) so = +1;
        else if (ch1.equalsIgnoreCase("-")) so = -1;
      }
    }

    public int compare(String s0, String s1) {
      int order;
      Matcher matcher0 = null, matcher1 = null;

      switch (st) {
        case NUMERIC:
          matcher0 = pattern.matcher(s0);
          matcher1 = pattern.matcher(s1);
          if (matcher0.find() && matcher1.find()) {
            String num0 = matcher0.group(2);
            String num1 = matcher1.group(2);
            String s0new = matcher0.replaceFirst("$1" + pad(num0) + "$3");
            String s1new = matcher1.replaceFirst("$1" + pad(num1) + "$3");
            order = s0new.compareToIgnoreCase(s1new);
          } else {
            order = s0.compareToIgnoreCase(s1);
          }
          break;

        case ALPHA:
        default:
          order = s0.compareToIgnoreCase(s1);
          break;
      }

      if (so < 1) order = -order;
      return order;
    }

    /** Pads a string with leading zeros out to 4 digits */
    public String pad(String s) {
      int l = 4 - s.length();
      switch (l) {
        case 0:
          return s;
        case 1:
          return "0" + s;
        case 2:
          return "00" + s;
        case 3:
          return "000" + s;
        default:
          return s;
      }
    }
  }

  /** Tries to convert a string to a number, returning <code>null</code> on failure. */
  public Integer strToInt(String s) {
    Integer intval = null;
    try { // convert to numeric value if possible
      intval = Integer.decode(s);
    } catch (Exception e) {
      intval = null;
    }
    return intval;
  }

  @Override
  public void checkParameters(String functionName, List<Object> parameters)
      throws ParameterException {
    super.checkParameters(functionName, parameters);
    // The work is done in checkVaryingParameters() instead.
  }

  /**
   * Checks number and types of parameters (pass null type to suppress typechecking for that slot).
   */
  public void checkVaryingParameters(
      String funcName, int minParams, int maxParams, List<Object> parameters, Class<?>[] expected)
      throws ParameterException {
    if (parameters.size() < minParams || parameters.size() > maxParams) {
      if (minParams == maxParams) {
        throw new ParameterException(
            I18N.getText("macro.function.strLst.incorrectParamExact", funcName, minParams));
      } else {
        throw new ParameterException(
            I18N.getText("macro.function.strLst.incorrectParam", funcName, minParams, maxParams));
      }
    }
    int numToCheck = expected.length;
    if (numToCheck > parameters.size()) numToCheck = parameters.size();

    for (int i = 0; i < numToCheck; i++) {
      if (expected[i] != null && !(expected[i].isInstance(parameters.get(i))))
        throw new ParameterException(
            I18N.getText(
                "macro.function.strLst.incorrectParamType",
                funcName,
                i + 1,
                expected[i].getSimpleName(),
                parameters.get(i),
                parameters.get(i).getClass().getSimpleName()));
    }
  }
}

// @formatter:off

/*
 * Here is a test macro
 *
 * <b>Tests</b> [h: OK = "OK"] [h: Fail = "<font color=red><b>Fail</b></font>"] <br>listGet(): {if( listGet("",0)=="" &&
 * listGet("x",0)=="x" && listGet("a,b",1)=="b" && listGet("1",0)==1 && listGet("0,1",1)==1 && (listGet("0,1",1)+5)==6 ,
 * OK, Fail)}
 *
 * <br>listDelete(): {if( listDelete("",0)=="" && listDelete("x",0)=="" && listDelete("a,b",0)=="b" &&
 * listDelete("a,b",1)=="a" && listDelete("a,b",2)=="a, b" , OK, Fail)}
 *
 * <br>listCount(): {if( listCount("")==0 && listCount("x")==1 && listCount(",")==2 , OK, Fail)}
 *
 * <br>listFind(): {if( listFind("","x")==-1 && listFind(",","")==0 && listFind("x","x")==0 && listFind("a,b","b")==1 &&
 * listFind("a,b","c")==-1 && listFind("a,0,b",0)==1 && listFind("a,0,b",1)==-1 , OK, Fail)}
 *
 * <br>listAppend(): {if( listAppend("","a")=="a" && listAppend("",0)=="0" && listAppend("x","y")=="x, y" &&
 * listAppend("x",1)=="x, 1" && listAppend("a,b","c")=="a, b, c" && listAppend("1,b",2)=="1, b, 2" &&
 * listAppend(",","z")==", , z" , OK, Fail)}
 *
 * <br>listInsert(): {if( listInsert("",0,"x")=="x" && listInsert("",1,"x")=="" && listInsert("",0,5)=="5" &&
 * listInsert("x",0,"y")=="y, x" && listInsert("x",1,5)=="x, 5" && listInsert("x",2,"y")=="x" &&
 * listInsert("a,b",0,3)=="3, a, b" && listInsert("a,b",2,"c")=="a, b, c" && listInsert("a,b",3,"c")=="a, b" , OK,
 * Fail)}
 *
 * <br>listReplace(): {if( listReplace("",0,"a")=="" && listReplace("",0,55)=="" && listReplace("x",0,3)=="3" &&
 * listReplace("x",1,"y")=="x" && listReplace(",",0,"a")=="a, " && listReplace(",",1,"b")==", b" &&
 * listReplace(",",2,"c")==", " && listReplace("a,b",1,"d")=="a, d" , OK, Fail)}
 *
 * <br>listSort(): {if( listSort("M3,M10,M1","A")=="M1, M10, M3" && listSort("M3,M10,M1","N")=="M1, M3, M10" , OK,
 * Fail)}
 *
 * <br>listFormat(): {if( listFormat("a,b,c","[[%list]]", "(%item)", "...")=="[[(a)...(b)...(c)]]" , OK, Fail)}
 *
 *
 *
 *
 * <br><br><b>Tests with non-default separator:</b> [h: OK = "OK"] [h: Fail = "<font color=red><b>Fail</b></font>"]
 * <br>listGet(): {if( listGet("",0,";")=="" && listGet("x",0,";")=="x" && listGet("a;b",1,";")=="b" &&
 * listGet("0;1",1,";")==1 && (listGet("0;1",1,";")+5)==6 , OK, Fail)}
 *
 * <br>listDelete(): {if( listDelete("",0,";")=="" && listDelete("x",0,";")=="" && listDelete("a;b",0,";")=="b" &&
 * listDelete("a;b",1,";")=="a" && listDelete("a;b",2,";")=="a; b" , OK, Fail)}
 *
 * <br>listCount(): {if( listCount("",";")==0 && listCount("x",";")==1 && listCount(";",";")==2 , OK, Fail)}
 *
 * <br>listFind(): {if( listFind("","x",";")==-1 && listFind(";","",";")==0 && listFind("x","x",";")==0 &&
 * listFind("a;b","b",";")==1 && listFind("a;b","c",";")==-1 && listFind("a;0;b",0,";")==1 &&
 * listFind("a;0;b",1,";")==-1 , OK, Fail)}
 *
 * <br>listAppend(): {if( listAppend("","a",";")=="a" && listAppend("",0,";")=="0" && listAppend("x","y",";")=="x; y" &&
 * listAppend("x",1,";")=="x; 1" && listAppend("a;b","c",";")=="a; b; c" && listAppend("1;b",2,";")=="1; b; 2" &&
 * listAppend(";","z",";")=="; ; z" , OK, Fail)}
 *
 * <br>listInsert(): {if( listInsert("",0,"x",";")=="x" && listInsert("",1,"x",";")=="" && listInsert("",0,5,";")=="5"
 * && listInsert("x",0,"y",";")=="y; x" && listInsert("x",1,5,";")=="x; 5" && listInsert("x",2,"y",";")=="x" &&
 * listInsert("a;b",0,3,";")=="3; a; b" && listInsert("a;b",2,"c",";")=="a; b; c" && listInsert("a;b",3,"c",";")=="a; b"
 * , OK, Fail)}
 *
 * <br>listReplace(): {if( listReplace("",0,"a",";")=="" && listReplace("",0,55,";")=="" &&
 * listReplace("x",0,3,";")=="3" && listReplace("x",1,"y",";")=="x" && listReplace(";",0,"a",";")=="a; " &&
 * listReplace(";",1,"b",";")=="; b" && listReplace(";",2,"c",";")=="; " && listReplace("a;b",1,"d",";")=="a; d" , OK,
 * Fail)}
 *
 * <br>listSort(): {if( listSort("M3;M10;M1","A",";")=="M1; M10; M3" && listSort("M3;M10;M1","N",";")=="M1; M3; M10" ,
 * OK, Fail)}
 *
 * <br>listFormat(): {if( listFormat("a;b;c","[[%list]]", "(%item)", "...", ";")=="[[(a)...(b)...(c)]]" , OK, Fail)}
 *
 *
 *
 *
 *
 * <br><br><b>Examples</b> <br>list = [list = "a,b,c,,e"] <br>count = [listCount(list)] <br>item 1 = '[listGet(list,
 * 1)]' <br>item 3 = '[listGet(list, 3)]' <br>delete 2 --> [listDelete(list, 2)] <br>find "c" --> [listFind(list, "c")]
 * <br>find "" --> [listFind(list, "")] <br>append "f" --> [listAppend(list, "f")] <br>insert "aa" at 1 -->
 * [listInsert(list, 1, "aa")] <br>insert "f" at 5 --> [listInsert(list, 5, "f")] <br>replace 0 with "A" -->
 * [listReplace(list, 0, "A")] <br>replace 3 with "D" --> [listReplace(list, 3, "D")] <br>replace 5 with "F" -->
 * [listReplace(list, 5, "F")] <br>replace 1 with "" --> [listReplace(list, 1, "")] <br>
 *
 * <br>list = [list = "a;b,c;;,e"] <br>count = [listCount(list, ";")] <br>item 1 = '[listGet(list, 1, ";")]' <br>item 3
 * = '[listGet(list, 3, ";")]' <br>delete 2 --> [listDelete(list, 2, ";")] <br>find "c" --> [listFind(list, "c", ";")]
 * <br>find "" --> [listFind(list, "", ";")] <br>append "f" --> [listAppend(list, "f", ";")] <br>insert "aa" at 1 -->
 * [listInsert(list, 1, "aa", ";")] <br>insert "f" at 5 --> [listInsert(list, 5, "f", ";")] <br>replace 0 with "A" -->
 * [listReplace(list, 0, "A", ";")] <br>replace 3 with "D" --> [listReplace(list, 3, "D", ";")] <br>replace 5 with "F"
 * --> [listReplace(list, 5, "F", ";")] <br>replace 1 with "" --> [listReplace(list, 1, "", ";")]
 */

// @formatter:on
