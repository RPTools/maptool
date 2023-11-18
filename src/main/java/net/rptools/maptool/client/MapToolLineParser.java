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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.rptools.dicelib.expression.Result;
import net.rptools.maptool.client.functions.*;
import net.rptools.maptool.client.functions.exceptions.*;
import net.rptools.maptool.client.functions.json.JSONMacroFunctions;
import net.rptools.maptool.client.ui.htmlframe.HTMLFrameFactory;
import net.rptools.maptool.client.ui.htmlframe.HTMLFrameFactory.FrameType;
import net.rptools.maptool.client.ui.macrobuttons.buttons.MacroButtonPrefs;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.MacroButtonProperties;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.library.LibraryManager;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.Function;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MapToolLineParser {

  // Logger for this class.
  private static final Logger log = LogManager.getLogger(MapToolLineParser.class);

  /** Name and Source or macros that come from chat. */
  public static final String CHAT_INPUT = "chat";

  /** Name of macro to divert calls to unknown macros on a lib macro to. */
  public static final String UNKNOWN_LIB_MACRO = "!!unknown-macro!!";

  /** Stack that holds our contexts. */
  private final Stack<MapToolMacroContext> contextStack = new Stack<MapToolMacroContext>();

  /** Was every context we entered during the macro trusted. */
  private volatile boolean macroPathTrusted = false;

  /** The macro button index of the button on the impersonated token that called us. */
  private volatile int macroButtonIndex = -1;

  /** The default value for maximum macro recursion. */
  private static final int DEFAULT_MAX_RECURSIVE_DEPTH = 150;

  /** The current parser recursion depth. */
  private int parserRecurseDepth = 0;

  /** The current macro recursive depth. */
  private int macroRecurseDepth = 0;

  /** The maximum parser and macro recursive depth. */
  private int maxRecursionDepth = DEFAULT_MAX_RECURSIVE_DEPTH;

  /** The default maximum loop iterations. */
  private static final int DEFAULT_MAX_LOOP_ITERATIONS = 10000;

  /** The maximum amount of loop iterations. */
  private int maxLoopIterations = DEFAULT_MAX_LOOP_ITERATIONS;

  /** The dice rolls that occurred. */
  private List<Integer> lastRolled = new LinkedList<>();

  /** The dice rolls that occurred in the previous parse this one. */
  private List<Integer> rolled = new LinkedList<>();

  /**
   * The dice rolls that occurred since either start of the macro or the previous time {@link
   * #getNewRolls()} was called.
   */
  private List<Integer> newRolls = new LinkedList<>();

  private enum Output { // Mutually exclusive output formats
    NONE,
    RESULT,
    TOOLTIP,
    EXPANDED,
    UNFORMATTED,
  }

  private enum LoopType { // Mutually exclusive looping options
    NO_LOOP,
    COUNT,
    FOR,
    WHILE,
    FOREACH,
  }

  private enum BranchType { // Mutually exclusive branching options
    NO_BRANCH,
    IF,
    SWITCH,
  }

  private enum CodeType { // Mutually exclusive code-execution options
    NO_CODE,
    MACRO,
    CODEBLOCK,
  }

  private enum OutputLoc { // Mutually exclusive output location
    CHAT,
    DIALOG,
    OVERLAY,
    DIALOG5,
    FRAME,
    FRAME5
  }

  private enum ScanState {
    SEARCHING_FOR_ROLL,
    SEARCHING_FOR_QUOTE,
    SEARCHING_FOR_CLOSE_BRACKET,
    SKIP_NEXT_CHAR
  }

  public Map<String, String> listAllMacroFunctions() {
    Map<String, String> functionList = new HashMap<String, String>();

    for (Function function : MapToolExpressionParser.getMacroFunctions()) {
      if (function instanceof AdditionalFunctionDescription) {
        for (String alias : function.getAliases()) {
          functionList.put(alias, function.getClass().getName());
          //          log.info(alias + " : " + function.getClass().getName());
        }
      } else {
        for (String alias : function.getAliases())
          functionList.put(alias, function.getClass().getName());
      }
    }

    return functionList;
  }

  // Class to hold the inline rolls and where they start and end.
  private static class InlineRollMatch {
    final int start;
    final int end;
    final String match;
    final int optEnd;

    @SuppressWarnings("unused")
    InlineRollMatch(int start, int end, String match) {
      this.start = start;
      this.end = end;
      this.match = match;
      this.optEnd = -1;
    }

    InlineRollMatch(int start, int end, String match, int optEnd) {
      this.start = start;
      this.end = end;
      this.match = match;
      this.optEnd = optEnd;
    }

    public int getStart() {
      return start;
    }

    public int getEnd() {
      return end;
    }

    public String getMatch() {
      return match;
    }

    @SuppressWarnings("unused")
    public int getOptEnd() {
      return optEnd;
    }

    public String getOpt() {
      if (optEnd > 0) {
        return match.substring(1, optEnd - start);
      } else {
        return "";
      }
    }

    public String getRoll() {
      if (optEnd > 0) {
        return match.substring(optEnd + 1 - start, end - start);
      } else {
        return match.substring(1, end - start);
      }
    }
  }

  public String parseLine(String line) throws ParserException {
    return parseLine(null, line);
  }

  public String parseLine(Token tokenInContext, String line) throws ParserException {
    return parseLine(tokenInContext, line, null);
  }

  public String parseLine(Token tokenInContext, String line, MapToolMacroContext context)
      throws ParserException {
    return parseLine(null, tokenInContext, line, context);
  }

  public String parseLine(MapToolVariableResolver res, Token tokenInContext, String line)
      throws ParserException {
    return parseLine(res, tokenInContext, line, null);
  }

  public String parseLine(
      MapToolVariableResolver res, Token tokenInContext, String line, MapToolMacroContext context)
      throws ParserException {
    // copy previous rolls and clear out for new rolls.
    if (parserRecurseDepth == 0 && macroRecurseDepth == 0) {
      lastRolled.clear();
      lastRolled.addAll(rolled);
      rolled.clear();
      newRolls.clear();
    }

    if (line == null) {
      return "";
    }
    line = line.trim();
    if (line.length() == 0) {
      return "";
    }
    Stack<Token> contextTokenStack = new Stack<Token>();
    context = enterContext(context);
    MapToolVariableResolver resolver = null;
    boolean resolverInitialized = false;
    String opts = null;
    String roll = null;
    try {
      // Keep the same variable context for this line
      resolver = (res == null) ? new MapToolVariableResolver(tokenInContext) : res;
      resolverInitialized = resolver.initialize();
      StringBuilder builder = new StringBuilder();
      int start = 0;
      List<InlineRollMatch> matches = this.locateInlineRolls(line);

      for (InlineRollMatch match : matches) {
        builder.append(line, start, match.getStart()); // add everything before the roll

        start = match.getEnd() + 1;
        // These variables will hold data extracted from the roll options.
        Output output =
            context.isUseToolTipsForUnformatedRolls()
                ? MapToolLineParser.Output.TOOLTIP
                : MapToolLineParser.Output.EXPANDED;

        String text = null; // used by the T option
        HashSet<String> outputOpts = new HashSet<String>();
        OutputLoc outputTo = OutputLoc.CHAT;

        LoopType loopType = LoopType.NO_LOOP;
        int loopStart = 0, loopEnd = 0, loopStep = 1;
        int loopCount = 0;
        String loopSep = null;
        String loopVar = null, loopCondition = null;
        List<String> foreachList = new ArrayList<String>();

        BranchType branchType = BranchType.NO_BRANCH;
        Object branchCondition = null;

        CodeType codeType = CodeType.NO_CODE;
        String macroName = null;

        String frameName = null;
        String frameOpts = null;

        if (match.getMatch().startsWith("[")) {
          opts = match.getOpt();
          roll = match.getRoll();
          if (opts != null) {
            // Turn the opts string into a list of OptionInfo objects.
            List<OptionInfo> optionList = null;
            try {
              optionList = OptionInfo.getRollOptionList(opts);
            } catch (OptionInfo.RollOptionException roe) {
              throw doError(roe.msg, opts, roll);
            }

            // Scan the roll options and prepare variables for later use
            for (OptionInfo option : optionList) {
              String error;
              /*
               * TODO: If you're adding a new option, add a new case here to collect info from the parameters. If your option uses parameters, use the option.getXxxParam() methods to get
               * the text or parsed values of the parameters.
               */
              switch (option.getOptionType()) {

                  ///////////////////////////////////////////////////
                  // OUTPUT FORMAT OPTIONS
                  ///////////////////////////////////////////////////
                case HIDDEN:
                  output = Output.NONE;
                  break;
                case RESULT:
                  output = Output.RESULT;
                  break;
                case EXPANDED:
                  output = Output.EXPANDED;
                  break;
                case UNFORMATTED:
                  output = Output.UNFORMATTED;
                  outputOpts.add("u");
                  break;
                case TOOLTIP:
                  // T(display_text)
                  output = Output.TOOLTIP;
                  text = option.getStringParam(0);
                  break;

                  ///////////////////////////////////////////////////
                  // VISIBILITY OPTIONS
                  ///////////////////////////////////////////////////
                case GM:
                  outputOpts.add("g");
                  break;
                case SELF:
                  outputOpts.add("s");
                  break;
                case WHISPER:
                  outputOpts.add("w");
                  for (int i = 0; i < option.getParamCount(); i++) {
                    String arg =
                        parseExpression(resolver, tokenInContext, option.getStringParam(i), false)
                            .getValue()
                            .toString();
                    if (arg.trim().startsWith("[")) {
                      JsonElement json = JSONMacroFunctions.getInstance().asJsonElement(arg);
                      if (json.isJsonArray()) {
                        for (JsonElement name : json.getAsJsonArray()) {
                          outputOpts.add("w:" + name.getAsString().toLowerCase());
                        }
                      }
                    } else {
                      outputOpts.add("w:" + arg.toLowerCase());
                    }
                  }
                  break;

                  ///////////////////////////////////////////////////
                  // TOOLTIP VISIBILITY OPTIONS
                  ///////////////////////////////////////////////////
                case GMTT:
                  outputOpts.add("gt");
                  break;
                case SELFTT:
                  outputOpts.add("st");
                  break;

                  ///////////////////////////////////////////////////
                  // LOOP OPTIONS
                  ///////////////////////////////////////////////////
                case COUNT:
                  // COUNT(num [, sep])
                  loopType = LoopType.COUNT;
                  error = null;
                  try {
                    loopCount = option.getParsedIntParam(0, resolver, tokenInContext, this);
                    if (loopCount < 0) error = I18N.getText("lineParser.countNonNeg", loopCount);

                  } catch (ParserException pe) {
                    error = I18N.getText("lineParser.errorProcessingOpt", "COUNT", pe.getMessage());
                  }
                  loopSep = option.getStringParam(1);

                  if (error != null) throw doError(error, opts, roll);

                  break;

                case FOR:
                  // FOR(var, start, end [, step [, sep]])
                  loopType = LoopType.FOR;
                  error = null;
                  try {
                    loopVar = option.getIdentifierParam(0);
                    loopStart = option.getParsedIntParam(1, resolver, tokenInContext, this);
                    loopEnd = option.getParsedIntParam(2, resolver, tokenInContext, this);
                    try {
                      loopStep = option.getParsedIntParam(3, resolver, tokenInContext, this);
                    } catch (ParserException pe) {
                      // Build a more informative error message for this common mistake
                      String msg = pe.getMessage();
                      msg = msg + " " + I18N.getText("lineParser.nonDefLoopSep");
                      throw new ParserException(msg);
                    }
                    loopSep = option.getStringParam(4);
                    if (loopStep != 0)
                      loopCount =
                          Math.max(
                              1,
                              (int)
                                  Math.ceil(
                                      Math.abs(
                                          (double) (loopEnd - loopStart) / (double) loopStep)));

                    if (loopVar.equalsIgnoreCase(""))
                      error = I18N.getText("lineParser.forVarMissing");
                    if (loopStep == 0) error = I18N.getText("lineParser.forNoZeroStep");
                    if ((loopEnd <= loopStart && loopStep > 0)
                        || (loopEnd >= loopStart && loopStep < 0)) loopCount = 0;

                  } catch (ParserException pe) {
                    error = I18N.getText("lineParser.errorProcessingOpt", "FOR", pe.getMessage());
                  }

                  if (error != null) throw doError(error, opts, roll);

                  break;

                case FOREACH:
                  // FOREACH(var, list [, outputDelim [, inputDelim]])
                  loopType = LoopType.FOREACH;
                  error = null;
                  try {
                    loopVar = option.getIdentifierParam(0);
                    String listString =
                        option.getParsedParam(1, resolver, tokenInContext, this).toString();
                    loopSep = option.getStringParam(2);
                    String listDelim = option.getStringParam(3);
                    if (listDelim.trim().startsWith("\"")) {
                      listDelim =
                          parseExpression(resolver, tokenInContext, listDelim, false)
                              .getValue()
                              .toString();
                    }

                    foreachList = null;
                    if (listString.trim().startsWith("{") || listString.trim().startsWith("[")) {
                      // if String starts with [ or { it is JSON -- try to treat it as a JSON String
                      JsonElement json = JSONMacroFunctions.getInstance().asJsonElement(listString);
                      if (json.isJsonArray()) {
                        foreachList = new ArrayList<>(json.getAsJsonArray().size());
                        for (JsonElement ele : json.getAsJsonArray()) {
                          foreachList.add(JSONMacroFunctions.getInstance().jsonToScriptString(ele));
                        }
                      } else if (json.isJsonObject()) {
                        foreachList = new ArrayList<>(json.getAsJsonObject().keySet());
                      }
                    }

                    // If we still dont have a list treat it list a string list
                    if (foreachList == null) {
                      foreachList = StrListFunctions.toList(listString, listDelim);
                    }
                    loopCount = foreachList.size();

                    if (loopVar.equalsIgnoreCase(""))
                      error = I18N.getText("lineParser.foreachVarMissing");
                  } catch (ParserException pe) {
                    error =
                        I18N.getText("lineParser.errorProcessingOpt", "FOREACH", pe.getMessage());
                  }

                  if (error != null) throw doError(error, opts, roll);

                  break;

                case WHILE:
                  // WHILE(cond [, sep])
                  loopType = LoopType.WHILE;
                  loopCondition = option.getStringParam(0);
                  loopSep = option.getStringParam(1);
                  break;

                  ///////////////////////////////////////////////////
                  // BRANCH OPTIONS
                  ///////////////////////////////////////////////////
                case IF:
                  // IF(condition)
                  branchType = BranchType.IF;
                  branchCondition = option.getStringParam(0);
                  break;
                case SWITCH:
                  // SWITCH(condition)
                  branchType = BranchType.SWITCH;
                  branchCondition = option.getObjectParam(0);
                  break;

                  ///////////////////////////////////////////////////
                  // DIALOG AND FRAME OPTIONS
                  ///////////////////////////////////////////////////
                case FRAME:
                  codeType = CodeType.CODEBLOCK;
                  frameName = option.getParsedParam(0, resolver, tokenInContext, this).toString();
                  frameOpts = option.getParsedParam(1, resolver, tokenInContext, this).toString();
                  outputTo = OutputLoc.FRAME;
                  break;
                case DIALOG:
                  codeType = CodeType.CODEBLOCK;
                  frameName = option.getParsedParam(0, resolver, tokenInContext, this).toString();
                  frameOpts = option.getParsedParam(1, resolver, tokenInContext, this).toString();
                  outputTo = OutputLoc.DIALOG;
                  break;
                case DIALOG5:
                  codeType = CodeType.CODEBLOCK;
                  frameName = option.getParsedParam(0, resolver, tokenInContext, this).toString();
                  frameOpts = option.getParsedParam(1, resolver, tokenInContext, this).toString();
                  outputTo = OutputLoc.DIALOG5;
                  break;
                case FRAME5:
                  codeType = CodeType.CODEBLOCK;
                  frameName = option.getParsedParam(0, resolver, tokenInContext, this).toString();
                  frameOpts = option.getParsedParam(1, resolver, tokenInContext, this).toString();
                  outputTo = OutputLoc.FRAME5;
                  break;
                case OVERLAY:
                  codeType = CodeType.CODEBLOCK;
                  frameName = option.getParsedParam(0, resolver, tokenInContext, this).toString();
                  frameOpts = option.getParsedParam(1, resolver, tokenInContext, this).toString();
                  outputTo = OutputLoc.OVERLAY;
                  break;
                  ///////////////////////////////////////////////////
                  // CODE OPTIONS
                  ///////////////////////////////////////////////////
                case MACRO:
                  // MACRO("macroName@location")
                  codeType = CodeType.MACRO;
                  macroName = option.getStringParam(0);
                  break;
                case CODE:
                  codeType = CodeType.CODEBLOCK;
                  break;
                  ///////////////////////////////////////////////////
                  // MISC OPTIONS
                  ///////////////////////////////////////////////////
                case TOKEN:
                  if (!isMacroTrusted()) {
                    throw new ParserException(I18N.getText("macro.function.roll.noPerm"));
                  }
                  Token newToken =
                      MapTool.getFrame()
                          .getCurrentZoneRenderer()
                          .getZone()
                          .resolveToken(
                              option.getParsedParam(0, resolver, tokenInContext, this).toString());
                  if (newToken != null) {
                    contextTokenStack.push(resolver.getTokenInContext());
                    resolver.setTokenIncontext(newToken);
                  }
                  break;
                default:
                  // should never happen
                  log.error(I18N.getText("lineParser.badOptionFound", opts, roll));
                  throw doError("lineParser.badOptionFound", opts, roll);
              }
            }
          }

          // Now that the options have been dealt with, process the body of the roll.
          // We deal with looping first, then branching, then deliver the output.
          StringBuilder expressionBuilder = new StringBuilder();
          int iteration = 0;
          boolean doLoop = true;
          while (doLoop) {
            int loopConditionValue;
            Integer branchConditionValue = null;
            Object branchConditionParsed = null;

            // Process loop settings
            if (iteration > maxLoopIterations) {
              throw doError("lineParser.tooManyLoops", opts, roll);
            }

            if (loopType != LoopType.NO_LOOP) {
              // We only update roll.count in a loop statement. This allows simple nested
              // statements to inherit roll.count from the outer statement.
              resolver.setVariable("roll.count", iteration);
            }

            switch (loopType) {
                /*
                 * TODO: If you're adding a new looping option, add a new case to handle the iteration
                 */
              case NO_LOOP:
                if (iteration > 0) { // stop after first iteration
                  doLoop = false;
                }
                break;
              case COUNT:
                if (iteration == loopCount) {
                  doLoop = false;
                }
                break;
              case FOR:
                if (iteration != loopCount) {
                  resolver.setVariable(loopVar, new BigDecimal(loopStart + loopStep * iteration));
                } else {
                  doLoop = false;
                  resolver.setVariable(loopVar, null);
                }
                break;
              case FOREACH:
                if (iteration != loopCount) {
                  String item = foreachList.get(iteration);
                  resolver.setVariable(loopVar, item);
                } else {
                  doLoop = false;
                  resolver.setVariable(loopVar, null);
                }
                break;
              case WHILE:
                // This is a hack to get around a bug with the parser's comparison operators.
                // The InlineTreeFormatter class in the parser chokes on comparison operators,
                // because they're
                // not listed in the operator precedence table.
                //
                // The workaround is that "non-deterministic" functions fully evaluate their
                // arguments,
                // so the comparison operators are reduced to a number by the time the buggy code is
                // reached.
                // The if() function defined in dicelib is such a function, so we use it here to eat
                // any comparison operators.
                String hackCondition =
                    (loopCondition == null) ? null : String.format("if(%s, 1, 0)", loopCondition);
                // Stop loop if the while condition is false
                try {
                  Result result = parseExpression(resolver, tokenInContext, hackCondition, false);
                  loopConditionValue = ((Number) result.getValue()).intValue();
                  if (loopConditionValue == 0) {
                    doLoop = false;
                  }
                } catch (Exception e) {
                  throw doError(I18N.getText("lineParser.invalidWhile", loopCondition), opts, roll);
                }
                break;
            }

            // Output the loop separator
            if (doLoop && iteration != 0 && output != Output.NONE) {
              expressionBuilder.append(
                  parseExpression(resolver, tokenInContext, loopSep, false).getValue());
            }

            if (!doLoop) {
              break;
            }

            iteration++;

            // Extract the appropriate branch to evaluate.

            // Evaluate the branch condition/expression
            if (branchCondition != null) {
              // This is a similar hack to the one used for the loopCondition above.
              String hackCondition = (branchCondition == null) ? null : branchCondition.toString();
              if (branchType == BranchType.IF) {
                hackCondition =
                    (hackCondition == null) ? null : String.format("if(%s, 1, 0)", hackCondition);
              }
              Result result;
              try {
                result = parseExpression(resolver, tokenInContext, hackCondition, false);
              } catch (Exception e) {
                throw doError(
                    I18N.getText(
                        "lineParser.invalidCondition",
                        branchType.toString(),
                        branchCondition.toString()),
                    opts,
                    roll);
              }
              branchConditionParsed = result.getValue();
              if (branchConditionParsed instanceof Number) {
                branchConditionValue = ((Number) branchConditionParsed).intValue();
              }
            }

            // Set up regexes for scanning through the branches.
            // branchRegex then defines one matcher group for the parseable content of the branch.
            String rollBranch = roll;
            String branchRegex, branchSepRegex, branchLastSepRegex;
            if (codeType != CodeType.CODEBLOCK) {
              // matches any text not containing a ";" (skipping over strings)
              String noCodeRegex = "((?:[^\";]|\"[^\"]*\"|'[^']*')*)";
              branchRegex = noCodeRegex;
              branchSepRegex = ";";
              branchLastSepRegex = ";?"; // The last clause doesn't have to end with a separator
            } else {
              // matches text inside braces "{...}", skipping over strings (one level of {} nesting
              // allowed)
              String codeRegex =
                  "\\{((?:[^{}\"]|\"[^\"]*\"|'[^']*'|\\{(?:[^}\"]|\"[^\"]*\"|'[^']*')*})*)}";
              branchRegex = codeRegex;
              branchSepRegex = ";";
              branchLastSepRegex = ";?"; // The last clause doesn't have to end with a separator
            }

            // Extract the branch to use
            switch (branchType) {
                /*
                 * TODO: If you're adding a new branching option, add a new case to extract the branch text
                 */
              case NO_BRANCH:
                {
                  // There's only one branch, so our regex is very simple
                  String testRegex = String.format("^\\s*%s\\s*$", branchRegex);
                  Matcher testMatcher = Pattern.compile(testRegex).matcher(roll);
                  if (testMatcher.find()) {
                    rollBranch = testMatcher.group(1);
                  } else {
                    throw doError("lineParser.errorBodyRoll", opts, roll);
                  }
                  break;
                }
              case IF:
                {
                  // IF can have one or two branches.
                  // When there's only one branch and the condition is false, there's no output.
                  if (branchConditionValue == null) {
                    throw doError(
                        I18N.getText(
                            "lineParser.invalidIfCond",
                            branchCondition,
                            branchConditionParsed.toString()),
                        opts,
                        roll);
                  }
                  int whichBranch = (branchConditionValue != 0) ? 0 : 1;
                  String testRegex =
                      String.format(
                          "^\\s*%s\\s*(?:%s\\s*%s\\s*%s)?\\s*$",
                          branchRegex, branchSepRegex, branchRegex, branchLastSepRegex);
                  Matcher testMatcher = Pattern.compile(testRegex).matcher(roll);
                  if (testMatcher.find()) { // verifies that roll body is well-formed
                    rollBranch = testMatcher.group(1 + whichBranch);
                    if (rollBranch == null) {
                      // Produces no output. If codeblock, empty string to fix #1876.
                      rollBranch = codeType == CodeType.CODEBLOCK ? "" : "''";
                    }
                    rollBranch = rollBranch.trim();
                  } else {
                    throw doError("lineParser.ifError", opts, roll);
                  }
                  break;
                }
              case SWITCH:
                {
                  // We augment the branch regex to detect the "case xxx:" or "default:" prefixes,
                  // and search for a match. An error is thrown if no case match is found.

                  // Regex matches 'default', 'case 123:', 'case "123":', 'case "abc":', but not
                  // 'case abc:'
                  branchRegex =
                      "(?:case\\s*\"?((?<!\")(?:\\+|-)?[\\d]+(?!\")|(?<=\")[^\"]*(?=\"))\"?|(default))\\s*:\\s*"
                          + branchRegex;
                  String caseTarget = branchConditionParsed.toString();
                  String testRegex =
                      String.format(
                          "^(?:\\s*%s\\s*%s\\s*)*\\s*%s\\s*%s\\s*$",
                          branchRegex, branchSepRegex, branchRegex, branchLastSepRegex);
                  Matcher testMatcher = Pattern.compile(testRegex).matcher(roll);
                  if (testMatcher.find()) { // verifies that roll body is well-formed
                    String scanRegex =
                        String.format("\\s*%s\\s*(?:%s)?", branchRegex, branchSepRegex);
                    Matcher scanMatcher = Pattern.compile(scanRegex).matcher(roll);
                    boolean foundMatch = false;
                    while (!foundMatch && scanMatcher.find()) {
                      String caseLabel = scanMatcher.group(1); // "case (xxx):"
                      String def = scanMatcher.group(2); // "(default):"
                      String branch = scanMatcher.group(3);
                      if (def != null) {
                        rollBranch = branch.trim();
                        foundMatch = true;
                        ;
                      }
                      if (caseLabel != null && caseLabel.matches(caseTarget)) {
                        rollBranch = branch.trim();
                        foundMatch = true;
                      }
                    }
                    if (!foundMatch) {
                      throw doError(
                          I18N.getText("lineParser.switchNoMatch", caseTarget), opts, roll);
                    }
                  } else {
                    throw doError("lineParser.switchError", opts, roll);
                  }

                  break;
                }
            } // end of switch(branchType) statement

            // Construct the output.
            // If a MACRO or CODE block is being used, we default to bare output as in the RESULT
            // style.
            // The output style NONE is also allowed in these cases.
            Result result;
            String output_text;
            switch (codeType) {
              case NO_CODE:
                // If none of the code options are active, any of the formatting options can be
                // used.
                switch (output) {
                    /*
                     * TODO: If you're adding a new formatting option, add a new case to build the output
                     */
                  case NONE:
                    parseExpression(resolver, tokenInContext, rollBranch, false);
                    break;
                  case RESULT:
                    result = parseExpression(resolver, tokenInContext, rollBranch, false);
                    output_text = result != null ? result.getValue().toString() : "";
                    if (!this.isMacroTrusted()) {
                      output_text =
                          output_text.replaceAll(
                              "\u00AB|\u00BB|&#171;|&#187;|&laquo;|&raquo;|\036|\037", "");
                    }
                    if (outputOpts.isEmpty()) {
                      expressionBuilder.append(output_text);
                    } else {
                      outputOpts.add("r");
                      expressionBuilder.append(rollString(outputOpts, output_text));
                    }

                    break;
                  case TOOLTIP:
                    String tooltip = rollBranch + " = ";
                    output_text = null;
                    result = parseExpression(resolver, tokenInContext, rollBranch, true);
                    tooltip += result.getDetailExpression();
                    if (text == null) {
                      output_text = result.getValue().toString();
                    } else {
                      if (!result.getDetailExpression().equals(result.getValue().toString())) {
                        tooltip += " = " + result.getValue();
                      }
                      resolver.setVariable("roll.result", result.getValue());
                      output_text =
                          parseExpression(resolver, tokenInContext, text, false)
                              .getValue()
                              .toString();
                    }
                    tooltip = tooltip.replaceAll("'", "&#39;");
                    expressionBuilder.append(
                        output_text != null ? rollString(outputOpts, tooltip, output_text) : "");
                    break;
                  case EXPANDED:
                    expressionBuilder.append(
                        rollString(
                            outputOpts,
                            rollBranch + " = " + expandRoll(resolver, tokenInContext, rollBranch)));
                    break;
                  case UNFORMATTED:
                    output_text =
                        rollBranch + " = " + expandRoll(resolver, tokenInContext, rollBranch);

                    // Escape quotes so that the result can be used in a title attribute
                    output_text = output_text.replaceAll("'", "&#39;");
                    output_text = output_text.replaceAll("\"", "&#34;");

                    expressionBuilder.append(rollString(outputOpts, output_text));
                } // end of switch(output) statement
                break; // end of case NO_CODE in switch(codeType) statement
                /*
                 * TODO: If you're adding a new code option, add a new case to execute the code
                 */
              case MACRO:
                // [MACRO("macroName@location"): args]
                result = parseExpression(resolver, tokenInContext, macroName, false);
                String callName = result.getValue().toString();
                result = parseExpression(resolver, tokenInContext, rollBranch, false);
                String macroArgs = result.getValue().toString();

                try {
                  output_text = runMacro(resolver, tokenInContext, callName, macroArgs);
                } catch (AbortFunctionException e) {
                  // required to catch abort that are not
                  // in a (UDF)function call
                  // but in a real "macro(...)" call
                  log.debug(e);
                  boolean catchAbort =
                      BigDecimal.ONE.equals(resolver.getVariable("macro.catchAbort"));
                  if (!catchAbort) throw e;
                  output_text = "";
                } catch (AssertFunctionException assertEx) {
                  // required to catch assert that are not
                  // in a (UDF)function call
                  // but in a real "macro(...)" call
                  log.debug(assertEx);
                  boolean catchAssert =
                      BigDecimal.ONE.equals(resolver.getVariable("macro.catchAssert"));
                  if (!catchAssert) throw assertEx;
                  MapTool.addLocalMessage(assertEx.getMessage());
                  output_text = "";
                } catch (ParserException e) {
                  e.addMacro(callName);
                  throw e;
                }

                if (output != Output.NONE) {
                  expressionBuilder.append(output_text);
                }
                resolver.setVariable(
                    "roll.count", iteration); // reset this because called code might change it
                break;

              case CODEBLOCK:
                output_text = runMacroBlock(resolver, tokenInContext, rollBranch);
                resolver.setVariable(
                    "roll.count", iteration); // reset this because called code might change it
                if (output != Output.NONE) {
                  expressionBuilder.append(output_text);
                }
                break;
            }
          }
          switch (outputTo) {
            case FRAME:
              // Macros can not interact with internal frames/dialogs/overlays
              if (HTMLFrameFactory.isInternalOnly(frameName)) {
                throw new ParserException(I18N.getText("msg.error.frame.reservedName", frameName));
              }
              HTMLFrameFactory.show(
                  frameName, FrameType.FRAME, false, frameOpts, expressionBuilder.toString());
              break;
            case DIALOG:
              // Macros can not interact with internal frames/dialogs/overlays
              if (HTMLFrameFactory.isInternalOnly(frameName)) {
                throw new ParserException(I18N.getText("msg.error.frame.reservedName", frameName));
              }
              HTMLFrameFactory.show(
                  frameName, FrameType.DIALOG, false, frameOpts, expressionBuilder.toString());
              break;
            case OVERLAY:
              // Macros can not interact with internal frames/dialogs/overlays
              if (HTMLFrameFactory.isInternalOnly(frameName)) {
                throw new ParserException(I18N.getText("msg.error.frame.reservedName", frameName));
              }
              HTMLFrameFactory.show(
                  frameName, FrameType.OVERLAY, true, frameOpts, expressionBuilder.toString());
              break;
            case CHAT:
              builder.append(expressionBuilder);
              break;
            case FRAME5:
              // Macros can not interact with internal frames/dialogs/overlays
              if (HTMLFrameFactory.isInternalOnly(frameName)) {
                throw new ParserException(I18N.getText("msg.error.frame.reservedName", frameName));
              }
              HTMLFrameFactory.show(
                  frameName, FrameType.FRAME, true, frameOpts, expressionBuilder.toString());
              break;
            case DIALOG5:
              // Macros can not interact with internal frames/dialogs/overlays
              if (HTMLFrameFactory.isInternalOnly(frameName)) {
                throw new ParserException(I18N.getText("msg.error.frame.reservedName", frameName));
              }
              HTMLFrameFactory.show(
                  frameName,
                  HTMLFrameFactory.FrameType.DIALOG,
                  true,
                  frameOpts,
                  expressionBuilder.toString());
              break;
          }

          // Revert to our previous token if [token(): ] was used
          if (contextTokenStack.size() > 0) {
            resolver.setTokenIncontext(contextTokenStack.pop());
          }
        } else if (match.getMatch().startsWith("{")) {
          roll = match.getRoll();
          Result result = parseExpression(resolver, tokenInContext, roll, false);
          if (isMacroTrusted()) {
            builder.append(result != null ? result.getValue().toString() : "");
          } else {
            builder.append(
                result != null
                    ? result
                        .getValue()
                        .toString()
                        .replaceAll("\u00AB|\u00BB|&#171;|&#187;|&laquo;|&raquo;|\036|\037", "")
                    : "");
          }
        }
      }
      builder.append(line.substring(start));
      return builder.toString();
    } catch (ParserException e) {
      // do nothing; this exception will never generate any output
      // throw doError("macroExecutionAbort", opts == null ? "" : opts, roll == null ? line : roll);
      throw e;
    } catch (Exception e) {
      log.info(line, e);
      throw doError(
          "lineParser.errorBodyRoll", opts == null ? "" : opts, roll == null ? line : roll);
    } finally {
      exitContext();
      if (resolverInitialized) {
        // This is the top level call, time to clean up
        resolver.flush();
      }
      if (MapTool.getFrame() != null) {
        // Repaint in case macros changed anything.
        MapTool.getFrame().refresh();
      }
    }
  }

  public Result parseExpression(String expression, boolean makeDeterministic)
      throws ParserException {
    return parseExpression(null, expression, makeDeterministic);
  }

  public Result parseExpression(Token tokenInContext, String expression, boolean makeDeterministic)
      throws ParserException {
    return parseExpression(
        new MapToolVariableResolver(tokenInContext), tokenInContext, expression, makeDeterministic);
  }

  public Result parseExpression(
      MapToolVariableResolver resolver,
      Token tokenInContext,
      String expression,
      boolean makeDeterministic)
      throws ParserException {
    if (parserRecurseDepth > maxRecursionDepth) {
      parserRecurseDepth = 0;
      macroRecurseDepth = 0;
      throw new ParserException(I18N.getText("lineParser.maxRecursion"));
    }
    try {
      parserRecurseDepth++;
      if (log.isDebugEnabled()) {
        String b = " ".repeat(Math.max(0, parserRecurseDepth - 1)) + expression;
        log.debug(b);
      }
      List<Integer> origRolled = List.copyOf(rolled);
      Result res = expressionParser.evaluate(expression, resolver, makeDeterministic);
      // if rolled has changed, we've been in a context that has updated it already
      if (origRolled.equals(rolled)) {
        rolled.addAll(res.getRolled());
        newRolls.addAll(res.getRolled());
      }

      return res;
    } catch (AbortFunctionException e) {
      log.debug(e);
      boolean catchAbort = BigDecimal.ONE.equals(resolver.getVariable("macro.catchAbort"));
      if (!catchAbort) throw e;

      // return an empty result to not collide with tooltips
      // when catching an abort
      Result result = new Result("");
      result.setValue("");
      return result;
    } catch (AssertFunctionException e) {
      log.debug(e);
      boolean catchAssert = BigDecimal.ONE.equals(resolver.getVariable("macro.catchAssert"));
      if (!catchAssert) throw e;
      MapTool.addLocalMessage(e.getMessage());

      // return an empty result to not collide with tooltips
      // when catching an assert`
      Result result = new Result("");
      result.setValue("");
      return result;
    } catch (ParserException pe) {
      log.debug(pe);
      throw pe;
    } catch (Exception e) {
      if (e.getCause() instanceof ParserException) {
        log.debug(e.getCause());
        throw (ParserException) e.getCause();
      }
      log.debug(e);
      throw new ParserException(
          I18N.getText("lineParser.errorExecutingExpression", e.toString(), expression));
    } finally {
      parserRecurseDepth--;
    }
  }

  public String expandRoll(String roll) throws ParserException {
    return expandRoll(null, roll);
  }

  public String expandRoll(Token tokenInContext, String roll) throws ParserException {
    return expandRoll(new MapToolVariableResolver(tokenInContext), tokenInContext, roll);
  }

  public String expandRoll(MapToolVariableResolver resolver, Token tokenInContext, String roll)
      throws ParserException {
    try {
      Result result = parseExpression(resolver, tokenInContext, roll, true);
      StringBuilder sb = new StringBuilder();

      if (result.getDetailExpression().equals(result.getValue().toString())) {
        sb.append(result.getDetailExpression());
      } else {
        sb.append(result.getDetailExpression()).append(" = ").append(result.getValue());
      }
      return sb.toString();
    } catch (ParserException pe) {
      throw pe;
    } catch (Exception e) {
      return I18N.getText("lineParser.invalidExpr", roll);
    }
  }

  public String runMacro(
      MapToolVariableResolver resolver, Token tokenInContext, String qMacroName, String args)
      throws ParserException {
    return runMacro(resolver, tokenInContext, qMacroName, args, true);
  }

  /**
   * Runs a macro from a specified location.
   *
   * @param resolver the {@link MapToolVariableResolver} used for resolving variables in the macro
   *     being run.
   * @param tokenInContext the {@code Token} if any that is the "current" token for the macro.
   * @param qMacroName the qualified macro name. (i.e. macro name and location of macro).
   * @param args the arguments to pass to the macro when executing it.
   * @param createNewVariableContext if {@code true} a new varaible scope is created for the macro,
   *     if {@code false} then the macro uses the calling scope and can read/modify/create variables
   *     visible to the caller.
   * @return the result of the macro execution.
   * @throws ParserException when an error occurs parsing or executing the macro.
   */
  public String runMacro(
      MapToolVariableResolver resolver,
      Token tokenInContext,
      String qMacroName,
      String args,
      boolean createNewVariableContext)
      throws ParserException {
    MapToolMacroContext macroContext;
    String macroBody = null;
    String[] macroParts = qMacroName.split("@", 2);
    String macroLocation;

    String macroName = macroParts[0];
    if (macroParts.length == 1) {
      macroLocation = null;
    } else {
      macroLocation = macroParts[1];
    }
    // For convenience to macro authors, no error on a blank macro name
    if (macroName.equalsIgnoreCase("")) return "";

    // IF the macro is a @this, then we get the location of the current macro and use that.
    if (macroLocation != null && macroLocation.equalsIgnoreCase("this")) {
      macroLocation = getMacroSource();
      if (macroLocation.equals(CHAT_INPUT) || macroLocation.toLowerCase().startsWith("token:")) {
        macroLocation = "TOKEN";
      }
    }
    if (macroLocation == null || macroLocation.length() == 0 || macroLocation.equals(CHAT_INPUT)) {
      // Unqualified names are not allowed.
      throw new ParserException(I18N.getText("lineParser.invalidMacroLoc", macroName));
    } else if (macroLocation.equalsIgnoreCase("TOKEN")) {
      boolean trusted = false;
      if (tokenInContext != null) {
        // Search token for the macro
        MacroButtonProperties mbp = tokenInContext.getMacro(macroName, false);
        if (mbp == null) {
          throw new ParserException(I18N.getText("lineParser.atTokenNotFound", macroName));
        }
        macroBody = mbp.getCommand();
        trusted = !mbp.getAllowPlayerEdits();
      }
      macroContext = new MapToolMacroContext(macroName, "token", trusted);
    } else if (macroLocation.equalsIgnoreCase("CAMPAIGN")) {
      MacroButtonProperties mbp = null;
      for (MacroButtonProperties m : MapTool.getCampaign().getMacroButtonPropertiesArray()) {
        if (m.getLabel().equals(macroName)) {
          mbp = m;
          break;
        }
      }
      if (mbp == null) {
        throw new ParserException(I18N.getText("lineParser.unknownCampaignMacro", macroName));
      }
      macroBody = mbp.getCommand();
      macroContext = new MapToolMacroContext(macroName, "campaign", !mbp.getAllowPlayerEdits());
    } else if (macroLocation.equalsIgnoreCase("Gm")) {
      MacroButtonProperties mbp = null;
      for (MacroButtonProperties m : MapTool.getCampaign().getGmMacroButtonPropertiesArray()) {
        if (m.getLabel().equals(macroName)) {
          mbp = m;
          break;
        }
      }
      if (mbp == null) {
        throw new ParserException(I18N.getText("lineParser.unknownCampaignMacro", macroName));
      }
      macroBody = mbp.getCommand();
      // GM macros can't be edited by players, so they are always trusted.
      macroContext = new MapToolMacroContext(macroName, "Gm", true);
    } else if (macroLocation.equalsIgnoreCase("GLOBAL")) {
      macroContext = new MapToolMacroContext(macroName, "global", MapTool.getPlayer().isGM());
      MacroButtonProperties mbp = null;
      for (MacroButtonProperties m : MacroButtonPrefs.getButtonProperties()) {
        if (m.getLabel().equals(macroName)) {
          mbp = m;
          break;
        }
      }
      if (mbp == null) {
        throw new ParserException(I18N.getText("lineParser.unknownGlobalMacro", macroName));
      }
      macroBody = mbp.getCommand();
    } else { // Search for a token called macroLocation (must start with "Lib:")
      try {
        var lib = new LibraryManager().getLibrary(macroLocation.substring(4));
        if (lib.isEmpty()) {
          throw new ParserException(I18N.getText("lineParser.unknownLibToken", macroLocation));
        }
        var library = lib.get();
        var macroInfo = library.getMTScriptMacroInfo(macroName).get();
        if (macroInfo.isEmpty()) {
          // if the macro source is the same as the location then check private macros.
          if (contextStackEmpty() || macroLocation.equalsIgnoreCase(getMacroSource())) {
            macroInfo = library.getPrivateMacroInfo(macroName).get();
          }
          if (macroInfo.isEmpty()) {
            throw new ParserException(I18N.getText("lineParser.unknownMacro", macroName));
          }
        }
        macroBody = macroInfo.get().macro();

        macroContext = new MapToolMacroContext(macroName, macroLocation, macroInfo.get().trusted());

      } catch (ExecutionException | InterruptedException e) {
        throw new ParserException(e);
      }
    }

    // Error if macro not found
    if (macroBody == null) {
      throw new ParserException(I18N.getText("lineParser.unknownMacro", macroName));
    }
    MapToolVariableResolver macroResolver;
    if (createNewVariableContext) {
      macroResolver = new MapToolVariableResolver(tokenInContext);
    } else {
      macroResolver = resolver;
    }
    macroResolver.setVariable("macro.args", args);
    JsonElement json = JSONMacroFunctions.getInstance().asJsonElement(args);
    if (json.isJsonArray()) {
      JsonArray jarr = json.getAsJsonArray();
      macroResolver.setVariable("macro.args.num", BigDecimal.valueOf(jarr.size()));
      for (int i = 0; i < jarr.size(); i++) {
        macroResolver.setVariable("macro.args." + i, asMacroArg(jarr.get(i)));
      }
    } else {
      macroResolver.setVariable("macro.args.num", BigDecimal.ZERO);
    }
    macroResolver.setVariable("macro.return", "");

    // Call the macro
    macroRecurseDepth++;
    if (macroRecurseDepth > maxRecursionDepth) {
      parserRecurseDepth = 0;
      macroRecurseDepth = 0;
      throw new ParserException(I18N.getText("lineParser.maxRecursion"));
    }
    try {
      String macroOutput = null;

      try {
        macroOutput = runMacroBlock(macroResolver, tokenInContext, macroBody, macroContext);
        // Copy the return value of the macro into our current variable scope.
        resolver.setVariable("macro.return", macroResolver.getVariable("macro.return"));
      } catch (ReturnFunctionException returnEx) {
        Object result = returnEx.getResult();
        if (result != null) {
          resolver.setVariable("macro.return", result);
          macroOutput = result.toString();
        }
      }
      if (macroOutput != null) {
        // Note! Its important that trim is not used to replace the following two lines.
        // If you use String.trim() you may inadvertnatly remove the special characters
        // used to mark rolls.
        macroOutput = macroOutput.replaceAll("^\\s+", "");
        macroOutput = macroOutput.replaceAll("\\s+$", "");
      }
      return macroOutput;
    } finally {
      // exitContext();
      macroRecurseDepth--;
    }
  }

  /**
   * Returns the JsonElement as a valid macro argument.
   *
   * @param jsonElement The JsonElement to convert.
   * @return The converted JsonElement.
   */
  private Object asMacroArg(JsonElement jsonElement) {
    if (jsonElement == null) {
      return "";
    } else if (jsonElement.isJsonNull()) {
      return "";
    } else {
      return JSONMacroFunctions.getInstance().asScriptType(jsonElement);
    }
  }

  /**
   * Run a block of text as a macro.
   *
   * @param tokenInContext the token in context.
   * @param macroBody the macro text to run.
   * @param contextName the name of the macro context to use.
   * @param contextSource the source of the macro block.
   * @param trusted is the context trusted or not.
   * @return the macro output.
   * @throws ParserException when an error occurs parsing or executing the macro.
   */
  public String runMacroBlock(
      Token tokenInContext,
      String macroBody,
      String contextName,
      String contextSource,
      boolean trusted)
      throws ParserException {
    MapToolVariableResolver resolver = new MapToolVariableResolver(tokenInContext);
    MapToolMacroContext context = new MapToolMacroContext(contextName, contextSource, trusted);
    return runMacroBlock(resolver, tokenInContext, macroBody, context);
  }

  /** Executes a string as a block of macro code. */
  String runMacroBlock(MapToolVariableResolver resolver, Token tokenInContext, String macroBody)
      throws ParserException {
    return runMacroBlock(resolver, tokenInContext, macroBody, null);
  }

  /** Executes a string as a block of macro code. */
  String runMacroBlock(
      MapToolVariableResolver resolver,
      Token tokenInContext,
      String macroBody,
      MapToolMacroContext context)
      throws ParserException {
    String macroOutput = parseLine(resolver, tokenInContext, macroBody, context);
    return macroOutput;
  }

  /**
   * Searches all maps for a token and returns the the requested lib: macro.
   *
   * @param location the location of the library macro.
   * @return The token which holds the library.
   * @throws ParserException if the token name is illegal, the token appears multiple times, or if
   *     the caller doesn't have access to the token.
   */
  public Token getTokenMacroLib(String location) throws ParserException {
    if (location == null) {
      return null;
    }
    if (!location.matches("(?i)^lib:.*")) {
      throw new ParserException(I18N.getText("lineParser.notALibToken"));
    }
    final String libTokenName = location;
    Token libToken = null;
    if (libTokenName.length() > 0) {
      List<ZoneRenderer> zrenderers = MapTool.getFrame().getZoneRenderers();
      for (ZoneRenderer zr : zrenderers) {
        List<Token> tokenList =
            zr.getZone().getTokensFiltered(t -> t.getName().equalsIgnoreCase(libTokenName));

        for (Token token : tokenList) {
          // If we are not the GM and the token is not visible to players then we don't
          // let them get functions from it.
          if (!MapTool.getPlayer().isGM() && !token.isVisible()) {
            throw new ParserException(I18N.getText("lineParser.libUnableToExec", libTokenName));
          }
          if (libToken != null) {
            throw new ParserException(I18N.getText("lineParser.duplicateLibTokens", libTokenName));
          }

          libToken = token;
        }
      }
      return libToken;
    }
    return null;
  }

  /**
   * Searches all maps for a token and returns the zone that the lib: macro is in.
   *
   * @param location the location of the library macro.
   * @return The zone which holds the library.
   * @throws ParserException if the token name is illegal, the token appears multiple times, or if
   *     the caller doesn't have access to the token.
   */
  public Zone getTokenMacroLibZone(String location) throws ParserException {
    if (location == null) {
      return null;
    }
    if (!location.matches("(?i)^lib:.*")) {
      throw new ParserException(I18N.getText("lineParser.notALibToken"));
    }
    final String libTokenName = location;
    Zone libTokenZone = null;
    if (libTokenName.length() > 0) {
      List<ZoneRenderer> zrenderers = MapTool.getFrame().getZoneRenderers();
      for (ZoneRenderer zr : zrenderers) {
        List<Token> tokenList =
            zr.getZone().getTokensFiltered(t -> t.getName().equalsIgnoreCase(libTokenName));

        for (Token token : tokenList) {
          // If we are not the GM and the token is not visible to players then we don't
          // let them get functions from it.
          if (!MapTool.getPlayer().isGM() && !token.isVisible()) {
            throw new ParserException(I18N.getText("lineParser.libUnableToExec", libTokenName));
          }

          if (libTokenZone != null) {
            throw new ParserException(I18N.getText("lineParser.duplicateLibTokens", libTokenName));
          }

          libTokenZone = zr.getZone();
        }
      }
      return libTokenZone;
    }
    return null;
  }

  /**
   * Throws a helpful ParserException that shows the roll options and body.
   *
   * @param msg The message
   * @param opts The roll options
   * @param roll The roll body
   */
  private ParserException doError(String msg, String opts, String roll) {
    return new ParserException(errorString(msg, opts, roll));
  }

  /** Builds a formatted string showing the roll options and roll body. */
  String errorString(String msg, String opts, String roll) {
    String retval = "<br>&nbsp;&nbsp;&nbsp;" + I18N.getText(msg);
    retval +=
        "<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<u>"
            + I18N.getText("lineParser.errorStmtOpts")
            + "</u>: "
            + opts;
    if (roll.length() <= 200) {
      retval +=
          "<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<u>"
              + I18N.getText("lineParser.errorStmtBody")
              + "</u>: "
              + roll;
    } else {
      retval +=
          "<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<u>"
              + I18N.getText("lineParser.errorStmtBodyFirst200")
              + "</u>: "
              + roll.substring(0, 199);
    }
    return retval;
  }

  public static final MapToolExpressionParser expressionParser = new MapToolExpressionParser();

  private String rollString(Collection<String> options, String text) {
    return rollString(options, null, text);
  }

  private String rollString(Collection<String> options, String tooltip, String text) {
    StringBuilder s = new StringBuilder("\036");
    if (options != null) {
      s.append("\001").append(StringUtils.join(options, ",")).append("\002");
    }

    if (tooltip != null) {
      tooltip = tooltip.replaceAll("'", "&#39;");
      s.append(tooltip).append("\037");
    }
    s.append(text).append("\036");
    return s.toString();
  }

  /**
   * Enters a new context for the macro.
   *
   * @param context The context for the macro. If context is null and there is a current context it
   *     is reentered. If context is null and there is no current context then a new top level
   *     context is created.
   */
  public MapToolMacroContext enterContext(MapToolMacroContext context) {
    // First time through set our trusted path to same as first context.
    // Any subsequent trips through we only change trusted path if context
    // is not trusted (if context == null on subsequent calls we dont change
    // anything as trusted context will remain the same as it was before the call).
    if (contextStack.size() == 0) {
      // The path is untrusted if any typing is involved, including GM's
      macroPathTrusted = context != null && context.isTrusted();
      macroButtonIndex = context == null ? -1 : context.getMacroButtonIndex();
    } else if (context != null && !context.isTrusted()) {
      macroPathTrusted = false;
    }
    if (context == null) {
      if (contextStack.size() == 0) {
        context =
            new MapToolMacroContext(
                MapToolLineParser.CHAT_INPUT,
                MapToolLineParser.CHAT_INPUT,
                MapTool.getPlayer().isGM());
      } else {
        context = contextStack.peek();
      }
    }
    contextStack.push(context);
    return context;
  }

  /**
   * Leaves the current context reverting to the previous context.
   *
   * @return The context that you leave.
   */
  public MapToolMacroContext exitContext() {
    return contextStack.pop();
  }

  /**
   * Convenience method to enter a new trusted context.
   *
   * @param name The name of the macro.
   * @param source Where the macro comes from.
   */
  public void enterTrustedContext(String name, String source) {
    enterContext(new MapToolMacroContext(name, source, true));
  }

  /**
   * Gets the current context for the execution. It is save to enter a context a second time.
   *
   * @return the current context.
   */
  public MapToolMacroContext getContext() {
    return contextStack.size() > 0 ? contextStack.peek() : null;
  }

  /**
   * Convenience method to enter a new insecure context.
   *
   * @param name The name of the macro.
   * @param source Where the macro comes from.
   */
  public void enterUntrustedContext(String name, String source) {
    enterContext(new MapToolMacroContext(name, source, false));
  }

  /**
   * Convenience method to enter a new context.
   *
   * @param name The name of the macro.
   * @param source Where the macro comes from.
   * @param secure Is the context secure or not.
   */
  public void enterContext(String name, String source, boolean secure) {
    enterContext(new MapToolMacroContext(name, source, secure));
  }

  /**
   * Returns if the context stack is empty.
   *
   * @return true if the context stack is empty.
   */
  private boolean contextStackEmpty() {
    return contextStack.size() == 0;
  }

  /**
   * Gets the macro name for the current context.
   *
   * @return The macro name for the current context.
   */
  public String getMacroName() {
    return contextStack.peek().getName();
  }

  /**
   * Gets the name of the source for where the macro resides.
   *
   * @return The name of the source for where the macro resides.
   */
  public String getMacroSource() {
    return contextStack.peek().getSource();
  }

  /**
   * Gets if the macro context is trusted or not. An empty context stack returns false.
   *
   * @return if the macro context is trusted or not.
   */
  public boolean isMacroTrusted() {
    return !contextStack.isEmpty() && contextStack.peek().isTrusted();
  }

  /**
   * Locate the inline rolls within the input line.
   *
   * @param line The line to search for the rolls in.
   * @return A list of the rolls.
   */
  private List<InlineRollMatch> locateInlineRolls(String line) {
    List<InlineRollMatch> matches = new ArrayList<InlineRollMatch>();
    ScanState scanState = ScanState.SEARCHING_FOR_ROLL;
    int startMatch = 0;
    int bracketLevel = 0;
    char quoteChar = ' ';
    char bracketChar = ' ';
    ScanState savedState = null;
    int optEnd = -1;

    for (int i = 0, strMax = line.length(); i < strMax; i++) {
      char c = line.charAt(i);
      switch (scanState) {
        case SEARCHING_FOR_ROLL:
          if (c == '{' || c == '[') {
            startMatch = i;
            scanState = ScanState.SEARCHING_FOR_CLOSE_BRACKET;
            bracketChar = c;
            bracketLevel++;
            optEnd = -1;
          }
          break;

        case SEARCHING_FOR_CLOSE_BRACKET:
          if (c == bracketChar) {
            bracketLevel++;
          } else if (bracketChar == '[' && c == ']') {
            bracketLevel--;
            if (bracketLevel == 0) {
              matches.add(
                  new InlineRollMatch(startMatch, i, line.substring(startMatch, i + 1), optEnd));
              scanState = ScanState.SEARCHING_FOR_ROLL;
            }
          } else if (bracketChar == '{' && c == '}') {
            bracketLevel--;
            if (bracketLevel == 0) {
              matches.add(
                  new InlineRollMatch(startMatch, i, line.substring(startMatch, i + 1), optEnd));
              scanState = ScanState.SEARCHING_FOR_ROLL;
            }
          } else if (c == '"' || c == '\'') {
            quoteChar = c;
            scanState = ScanState.SEARCHING_FOR_QUOTE;
          } else if (c == '\\') {
            savedState = scanState;
            scanState = ScanState.SKIP_NEXT_CHAR;
          } else if (bracketChar == '[' && optEnd == -1 && c == ':') {
            optEnd = i;
          }
          break;

        case SEARCHING_FOR_QUOTE:
          if (c == quoteChar) {
            scanState = ScanState.SEARCHING_FOR_CLOSE_BRACKET;
          } else if (c == '\\') {
            savedState = scanState;
            scanState = ScanState.SKIP_NEXT_CHAR;
          }
          break;

        case SKIP_NEXT_CHAR:
          scanState = savedState;
          break;
      }
    }
    return matches;
  }

  /**
   * Gets if the whole of the macro path up to this point has been running in a trusted context.
   *
   * @return true if the whole of the macro path is running in trusted context.
   */
  public boolean isMacroPathTrusted() {
    return macroPathTrusted;
  }

  /**
   * Gets the index of the macro button on the index token.
   *
   * @return the index of the macro button.
   */
  public int getMacroButtonIndex() {
    return macroButtonIndex;
  }

  /**
   * Gets the maximum number of iterations for a loop in a loop.
   *
   * @return the maximum number of macro loop iterations.
   */
  public int getMaxLoopIterations() {
    return maxLoopIterations;
  }

  /**
   * Sets the maximum number of iterations allowed in a macro. Note: this will not set the value
   * smaller than the initial starting value.
   *
   * @param loopIterations The maximum number of iterations allowed.
   */
  public void setMaxLoopIterations(int loopIterations) {
    maxLoopIterations = Math.max(loopIterations, DEFAULT_MAX_LOOP_ITERATIONS);
  }

  /**
   * Gets the maximum recursive depth allowed for macros.
   *
   * @return The maxiumum recursive depth allowed for macros.
   */
  public int getMaxRecursionDepth() {
    return maxRecursionDepth;
  }

  /**
   * Sets the maximum recursive depth for macros. Note: this will not set the value smaller than the
   * initial starting value.
   *
   * @param recursionDepth The maximum recursive depth allowed.
   */
  public void setMaxRecursionDepth(int recursionDepth) {
    maxRecursionDepth = Math.max(recursionDepth, DEFAULT_MAX_RECURSIVE_DEPTH);
  }

  /**
   * Gets the current recursive depth. This will be the macro or parser recursive depth which ever
   * is greater.
   *
   * @return the current recursive depth.
   */
  public int getRecursionDepth() {
    return Math.max(parserRecurseDepth, macroRecurseDepth);
  }

  public int getContextStackSize() {
    return contextStack.size();
  }

  /**
   * Returns the raw dice rolls that have occurred during this pars / execution.
   *
   * @return the raw dice rolls that have occurred during this parse / execution.
   */
  public List<Integer> getRolled() {
    return List.copyOf(rolled);
  }

  /**
   * Returns the raw dice rolls that occurred during the last parse / execution.
   *
   * @return the raw dice rolls that occurred during the last parse / execution.
   */
  public List<Integer> getLastRolled() {
    return List.copyOf(lastRolled);
  }

  /**
   * Returns the raw dice rolls that have occurred during this parse since the last time <code>
   * getNewRolls()</code> was called. If <code>getNewRolls()</code> has not yet been called during
   * this parse / execution then all rolls since the start of the parse / execution will be
   * returned.
   *
   * @return the raw dice rolls that occurred since last call to this funnction.
   */
  public List<Integer> getNewRolls() {
    List<Integer> rolls = List.copyOf(newRolls);
    newRolls.clear();
    return rolls;
  }

  /** Resets all the lists of rolls that have occurred. */
  public void clearRolls() {
    newRolls.clear();
    lastRolled.clear();
    rolled.clear();
  }
}
