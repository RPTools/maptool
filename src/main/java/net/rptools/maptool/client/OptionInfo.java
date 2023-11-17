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
package net.rptools.maptool.client;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.rptools.dicelib.expression.Result;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Token;
import net.rptools.parser.ParserException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * ******************************************************************************** OptionInfo class
 * - holds extracted name and parameters for a roll option.
 * ********************************************************************************
 */
class OptionInfo {

  // Logger for this class.
  private static final Logger log = LogManager.getLogger(OptionInfo.class);

  // These items are only used in the enum below, but have to be declared out here
  // because they must appear before being used in the enum definitions.
  private static final String defaultLoopSep = "\", \"";
  private static final Object nullParam = null;
  private static final Pattern PATTERN_COMMA = Pattern.compile("^\\s*,\\s*(?!$)");

  /*
   * In order to add a new roll option, follow the instructions in the "todo" comments in this file.
   */
  enum OptionType {
    /*
     * TODO: If you're adding a new option, make an entry in this table
     */

    // The format is:
    // NAME (nameRegex, minParams, maxParams, defaultValues...)
    //
    // You must provide (maxParams - minParams) default values (BigDecimal or String types).
    //
    // If maxParams is -1, unlimited params may be passed in.
    NO_OPTION("", 0, 0),
    // output formats
    EXPANDED("e|expanded", 0, 0),
    HIDDEN("h|hidden|hide", 0, 0),
    RESULT("r|result", 0, 0),
    UNFORMATTED("u|unformatted", 0, 0),
    TOOLTIP("t|tooltip", 0, 1, nullParam),
    // visibility
    GM("g|gm", 0, 0),
    SELF("s|self", 0, 0),
    WHISPER("w|whisper", 1, -1),
    // tooltip visibility
    GMTT("gt|gmtt", 0, 0),
    SELFTT("st|selftt", 0, 0),
    // WHISPER ("wt|whispertt", 1, -1),
    // loops
    COUNT("c|count", 1, 2, defaultLoopSep),
    FOR("for", 3, 5, BigDecimal.ONE, defaultLoopSep),
    FOREACH("foreach", 2, 4, defaultLoopSep, ","),
    WHILE("while", 1, 2, defaultLoopSep),
    // branches
    IF("if", 1, 1),
    SWITCH("switch", 1, 1),
    // code
    CODE("code", 0, 0),
    MACRO("macro", 1, 1),
    // HTML Dockable Frame
    FRAME("frame", 1, 2, "\"\""),
    // HTML Dialog
    DIALOG("dialog", 1, 2, "\"\""),
    DIALOG5("dialog5", 1, 2, "\"\""),
    // HTML webView
    FRAME5("frame5", 1, 2, "\"\""),
    // HTML overlay
    OVERLAY("overlay", 1, 2, "\"\""),
    // Run for another token
    TOKEN("token", 1, 1);

    protected final Pattern namePattern;
    protected final int minParams, maxParams;
    protected final Object[] defaultParams;

    OptionType(String nameRegex, int minParams, int maxParams, Object... defaultParams) {
      this.namePattern = Pattern.compile("^\\s*" + nameRegex + "\\s*$", Pattern.CASE_INSENSITIVE);
      this.minParams = minParams;
      this.maxParams = maxParams;
      if (defaultParams == null) {
        // The Java 5 varargs facility has a small hack which we must work around.
        // If you pass a single null argument, Java doesn't know whether you wanted a single
        // variable arg of null,
        // or if you meant to say that the variable argument array itself should be null.
        // Java chooses the latter, but we want the former.
        this.defaultParams = new Object[1];
        this.defaultParams[0] = null;
      } else {
        this.defaultParams = defaultParams;
      }
      if (maxParams != -1 && this.defaultParams.length != (maxParams - minParams)) {
        log.error(
            String.format(
                "Internal error: roll option %s specifies wrong number of default parameters",
                name()));
      }
    }

    /** Obtain one of the enum values, or null if <code>strName</code> doesn't match any of them. */
    protected static OptionType optionTypeFromName(String strName) {
      for (OptionType rot : OptionType.values()) {
        if (rot.getNamePattern().matcher(strName).matches()) {
          return rot;
        }
      }
      return null;
    }

    /** Returns the regex that matches all valid names for this option. */
    public Pattern getNamePattern() {
      return namePattern;
    }

    public int getMinParams() {
      return minParams;
    }

    public int getMaxParams() {
      return maxParams;
    }

    /** Returns a copy of the default params array for this option type. */
    public Object[] getDefaultParams() {
      if (maxParams == -1) return null;

      Object[] retval = new Object[maxParams];
      if (maxParams - minParams >= 0)
        System.arraycopy(defaultParams, 0, retval, minParams, maxParams - minParams);

      return retval;
    }

    @Override
    public String toString() {
      StringBuilder retval = new StringBuilder(name() + ", default params: [");
      boolean first = true;
      for (Object p : defaultParams) {
        if (first) {
          first = false;
        } else {
          retval.append(", ");
        }
        if (p == null) retval.append("null");
        else if (p instanceof String) retval.append("\"").append(p).append("\"");
        else retval.append(p.toString());
      }
      retval.append("]");
      return retval.toString();
    }
  }

  /** Thrown when a roll option can't be parsed. */
  @SuppressWarnings("serial")
  public static class RollOptionException extends Exception {
    public String msg;

    public RollOptionException(String msg) {
      this.msg = msg;
    }
  }

  private OptionType optionType;
  private String optionName;
  private final int optionStart;
  private int optionEnd;
  private final String srcString;
  private Object[] params;

  /**
   * Attempts to create an OptionInfo object by parsing the text in <code>optionString</code>
   * beginning at position <code>start</code>.
   */
  public OptionInfo(String optionString, int start) throws RollOptionException {
    srcString = optionString;
    optionStart = start;
    parseOptionString(optionString, start);
  }

  private static final Cache<String, List<OptionInfo>> OPTION_INFO_CACHE =
      CacheBuilder.newBuilder().softValues().build();

  /**
   * Scans a string of options and builds OptionInfo objects for each option found.
   *
   * @param optionString A string containing a comma-delimited list of roll options.
   * @throws RollOptionException if any of the options are unknown or don't match the template for
   *     that option type.
   */
  static List<OptionInfo> getRollOptionList(String optionString) throws RollOptionException {

    // check for null
    if (optionString == null) return null;

    // check if already parsed
    List<OptionInfo> list = OPTION_INFO_CACHE.getIfPresent(optionString);
    if (list != null) return list;

    list = new ArrayList<>();
    optionString = optionString.trim();
    int start = 0;
    int endOfString = optionString.length();
    boolean atEnd = false;

    while (start < endOfString) {
      OptionInfo roi;
      if (atEnd) {
        // If last param didn't end with ",", there shouldn't have been another option
        throw new RollOptionException(I18N.getText("lineParser.rollOptionComma"));
      }
      // Eat the next option from string, and add parsed option to list
      roi = new OptionInfo(optionString, start);
      list.add(roi);
      start = roi.getEnd();
      // Eat any "," sitting between options
      Matcher matcher = PATTERN_COMMA.matcher(optionString);
      matcher.region(start, endOfString);
      if (matcher.find()) {
        start = matcher.end();
        atEnd = false;
      } else {
        atEnd = true;
      }
    }

    OPTION_INFO_CACHE.put(optionString, list);

    return list;
  }

  private static final Pattern PATTERN_OPTION_STRING_NAME =
      Pattern.compile("^\\s*(?:(\\w+)\\s*\\(|(\\w+))"); // matches "abcd(" or "abcd"

  private static final Pattern PATTERN_OPTION_STRING_PARAMETER =
      Pattern.compile(
          "^(?:((?:[^()\"',]|\"[^\"]*\"|'[^']*'|\\((?:[^()\"']|\"[^\"]*\"|'[^']*')*\\))+)(,|\\))){1}?");

  /**
   * Parses a roll option and sets the RollOptionType and parameters. <br>
   * Missing optional parameters are set to the default for the type.
   *
   * @param optionString The string containing the option
   * @param start Where in the string to begin parsing from
   * @throws RollOptionException if the option string can't be parsed.
   */
  private void parseOptionString(String optionString, int start) throws RollOptionException {
    boolean paramsFound; // does the option string have a "(" after the name?
    int endOfString = optionString.length();

    // Find the name
    Matcher matcher = PATTERN_OPTION_STRING_NAME.matcher(optionString);
    matcher.region(start, endOfString);
    if (!matcher.find()) {
      throw new RollOptionException(I18N.getText("lineParser.badRollOpt", optionString));
    }
    paramsFound = (matcher.group(1) != null);
    String name = paramsFound ? matcher.group(1).trim() : matcher.group(2).trim();
    start = matcher.end();
    matcher.region(start, endOfString);

    // Get the option type and default params from the name
    optionType = OptionType.optionTypeFromName(name);
    if (optionType == null) {
      throw new RollOptionException(I18N.getText("lineParser.unknownOptionName", name));
    }
    optionName = name;
    params = optionType.getDefaultParams(); // begin with default values for optional params

    // If no params found (i.e. no "(" after option name), we're done
    if (!paramsFound) {
      if (optionType.getMinParams() == 0) {
        optionEnd = start;
        return;
      } else {
        throw new RollOptionException(
            I18N.getText("lineParser.optRequiresParam", optionName, optionType.getMaxParams()));
      }
    }

    // Otherwise, match the individual parameters one at a time
    matcher = PATTERN_OPTION_STRING_PARAMETER.matcher(optionString);
    matcher.region(start, endOfString);
    List<String> paramList = new ArrayList<>();
    boolean lastItem = false; // true if last match ended in ")"
    if (")".equals(optionString.substring(start))) {
      lastItem = true;
      start += 1;
    }

    while (!lastItem) {
      if (matcher.find()) {
        String param = matcher.group(1).trim();
        paramList.add(param);
        lastItem = matcher.group(2).equalsIgnoreCase(")");
        start = matcher.end();
        matcher.region(start, endOfString);
      } else {
        throw new RollOptionException(
            I18N.getText("lineParser.optBadParam", optionName, optionType.getMaxParams()));
      }
    }

    // Error checking
    int min = optionType.getMinParams(), max = optionType.getMaxParams();
    int numParamsFound = paramList.size();
    if (numParamsFound < min || (max != -1 && numParamsFound > max)) {
      throw new RollOptionException(
          I18N.getText(
              "lineParser.optWrongParam", optionName, min, max, numParamsFound, srcString));
    }

    // Fill in the found parameters, converting to BigDecimal if possible.
    if (params == null) params = new Object[numParamsFound];

    for (int i = 0; i < numParamsFound; i++) {
      params[i] = toNumIfPossible(paramList.get(i));
    }

    optionEnd = start;
  }

  /** Converts a String to a BigDecimal if possible, otherwise returns original String. */
  private Object toNumIfPossible(String s) {
    Object retval = s;
    try {
      retval = new BigDecimal(Integer.decode(s));
    } catch (NumberFormatException nfe) {
      // Do nothing
    }
    return retval;
  }

  @SuppressWarnings("unused")
  public String getName() {
    return optionName;
  }

  @SuppressWarnings("unused")
  public int getStart() {
    return optionStart;
  }

  public int getEnd() {
    return optionEnd;
  }

  /** Returns the number of options passed in */
  public int getParamCount() {
    return params.length;
  }

  public OptionType getOptionType() {
    return optionType;
  }

  /** Gets a parameter (Object type). */
  public Object getObjectParam(int index) {
    return params[index];
  }

  /** Gets the text of a parameter. */
  public String getStringParam(int index) {
    Object o = params[index];
    return (o == null) ? null : o.toString();
  }

  /**
   * Gets the text of a parameter if it is a valid identifier.
   *
   * @throws ParserException if the parameter text is not a valid identifier.
   */
  public String getIdentifierParam(int index) throws ParserException {
    String s = params[index].toString();
    if (!s.matches("[a-zA-Z]\\w*")) { // MapTool doesn't allow variable names to start with '_'
      throw new ParserException(I18N.getText("lineParser.notValidVariableName", s));
    }
    return s;
  }

  /** Gets a parameter, casting it to BigDecimal. */
  public BigDecimal getNumericParam(int index) {
    return (BigDecimal) params[index];
  }

  /** Gets the integer value of a parameter. */
  @SuppressWarnings("unused")
  public int getIntParam(int index) {
    return getNumericParam(index).intValue();
  }

  /** Returns a param, parsing it as an expression if it is a string. */
  public Object getParsedParam(
      int index, MapToolVariableResolver res, Token tokenInContext, MapToolLineParser parser)
      throws ParserException {
    Object retval = params[index];
    // No parsing is done if the param isn't a String (e.g. it's already a BigDecimal)
    if (params[index] instanceof String) {
      Result result = parser.parseExpression(res, tokenInContext, (String) params[index], false);
      retval = result.getValue();
    }
    return retval;
  }

  /** Returns a param as int, parsing it as an expression if it is a string. */
  public int getParsedIntParam(
      int index, MapToolVariableResolver res, Token tokenInContext, MapToolLineParser parser)
      throws ParserException {
    Object retval = getParsedParam(index, res, tokenInContext, parser);
    if (!(retval instanceof BigDecimal))
      throw new ParserException(I18N.getText("lineParser.notValidNumber", retval.toString()));
    return ((BigDecimal) retval).intValue();
  }

  @Override
  public String toString() {
    StringBuilder retval = new StringBuilder(optionName + ": params: (");
    boolean first = true;
    for (Object p : params) {
      if (first) {
        first = false;
      } else {
        retval.append(", ");
      }
      if (p == null) retval.append("null");
      else if (p instanceof String) retval.append("\"").append(p).append("\"");
      else retval.append(p.toString());
    }
    retval.append(")");
    return retval.toString();
  }
} ///////////////////// end of OptionInfo class
