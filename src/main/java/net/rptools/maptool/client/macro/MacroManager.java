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
package net.rptools.maptool.client.macro;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolMacroContext;
import net.rptools.maptool.client.functions.AbortFunction;
import net.rptools.maptool.client.functions.AssertFunction;
import net.rptools.maptool.client.macro.impl.*;
import net.rptools.maptool.client.ui.MapToolFrame;
import net.rptools.maptool.client.ui.commandpanel.CommandPanel;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.util.StringUtil;
import net.rptools.parser.ParserException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** @author drice */
public class MacroManager {

  private static final Logger log = LogManager.getLogger(MacroManager.class);

  private static final int MAX_RECURSE_COUNT = 10;

  private static Macro UNDEFINED_MACRO = new UndefinedMacro();

  private static Map<String, Macro> MACROS = new HashMap<String, Macro>();

  private static Map<String, String> aliasMap = new HashMap<String, String>();

  static {
    registerMacro(new SayMacro());
    registerMacro(new HelpMacro());
    registerMacro(new GotoMacro());
    registerMacro(new ClearMacro());
    registerMacro(new RollMeMacro());
    registerMacro(new RollAllMacro());
    registerMacro(new RollGMMacro());
    registerMacro(new WhisperMacro());
    registerMacro(new EmoteMacro());
    registerMacro(new AliasMacro());
    registerMacro(new LoadAliasesMacro());
    registerMacro(new SaveAliasesMacro());
    registerMacro(new ClearAliasesMacro());
    registerMacro(new AddTokenStateMacro());
    registerMacro(new LoadTokenStatesMacro());
    registerMacro(new SaveTokenStatesMacro());
    registerMacro(new SetTokenStateMacro());
    registerMacro(new SetTokenPropertyMacro());
    registerMacro(new RollSecretMacro());
    registerMacro(new EmitMacro());
    registerMacro(new SelfMacro());
    registerMacro(new ImpersonateMacro());
    registerMacro(new RunTokenMacroMacro());
    registerMacro(new RunTokenSpeechMacro());
    registerMacro(new LookupTableMacro());
    registerMacro(new ToGMMacro());
    registerMacro(new OOCMacro());
    registerMacro(new ChangeColorMacro());
    registerMacro(new WhisperReplyMacro());
    registerMacro(new EmotePluralMacro());
    registerMacro(new ExperimentsMacro());

    registerMacro(UNDEFINED_MACRO);
  }

  public static void setAlias(String key, String value) {
    aliasMap.put(key, value);
  }

  public static void removeAlias(String key) {
    aliasMap.remove(key);
  }

  public static void removeAllAliases() {
    aliasMap.clear();
  }

  public static Map<String, String> getAliasMap() {
    return Collections.unmodifiableMap(aliasMap);
  }

  public static Set<Macro> getRegisteredMacros() {
    Set<Macro> ret = new HashSet<Macro>();
    ret.addAll(MACROS.values());
    return ret;
  }

  public static Macro getRegisteredMacro(String name) {
    Macro ret = MACROS.get(name);
    if (ret == null) return UNDEFINED_MACRO;
    return ret;
  }

  public static void registerMacro(Macro macro) {
    MacroDefinition def = macro.getClass().getAnnotation(MacroDefinition.class);

    if (def == null) return;

    MACROS.put(def.name(), macro);
    for (String alias : def.aliases()) {
      MACROS.put(alias, macro);
    }
  }

  public static void executeMacro(String command) {
    executeMacro(command, null);
  }

  public static void executeMacro(String command, MapToolMacroContext macroExecutionContext) {
    MacroContext context = new MacroContext();
    context.addTransform(command);

    try {
      command = preprocess(command);
      context.addTransform(command);

      int recurseCount = 0;
      while (recurseCount < MAX_RECURSE_COUNT) {
        recurseCount++;

        command = command.trim();
        if (command == null || command.length() == 0) {
          return;
        }
        if (command.charAt(0) == '/') {
          command = command.substring(1);
        } else {
          // Default to a say
          command = "s " + command;
        }

        // Macro name is the first word
        List<String> cmd = StringUtil.splitNextWord(command);
        String key = cmd.get(0);
        String details = cmd.size() > 1 ? cmd.get(1) : "";

        Macro macro = getRegisteredMacro(key);
        MacroDefinition def = macro.getClass().getAnnotation(MacroDefinition.class);

        boolean trustedPath =
            macroExecutionContext == null ? false : macroExecutionContext.isTrusted();
        String macroButtonName =
            macroExecutionContext == null
                ? "<chat>"
                : macroExecutionContext.getName() + "@" + macroExecutionContext.getSource();

        // Preprocess line if required.
        if (def == null || def.expandRolls()) {
          // TODO: fix this, wow I really hate this, it's very, very ugly.
          Token tokenInContext = null;
          ZoneRenderer zr = MapTool.getFrame().getCurrentZoneRenderer();
          if (zr != null) {
            final MapToolFrame frame = MapTool.getFrame();
            final CommandPanel cpanel = frame.getCommandPanel();
            if (cpanel.getIdentityGUID() != null)
              tokenInContext = zr.getZone().getToken(cpanel.getIdentityGUID());
            else tokenInContext = zr.getZone().resolveToken(cpanel.getIdentity());
          }
          details = MapTool.getParser().parseLine(tokenInContext, details, macroExecutionContext);
          trustedPath = MapTool.getParser().isMacroPathTrusted();
        }
        context.addTransform(key + " " + details);
        postprocess(details);

        context.addTransform(key + " " + details);
        if (macro != UNDEFINED_MACRO) {
          executeMacro(context, macro, details, macroExecutionContext);
          return;
        }

        // Is it an alias ?
        String alias = aliasMap.get(key);
        if (alias == null) {
          executeMacro(context, UNDEFINED_MACRO, command, macroExecutionContext);
          return;
        }
        command = resolveAlias(alias, details);
        context.addTransform(command);
        continue;
      }
    } catch (AbortFunction.AbortFunctionException afe) {
      // Do nothing, just silently exit
      return;
    } catch (AssertFunction.AssertFunctionException afe) {
      MapTool.addLocalMessage(afe.getMessage());
      return;
    } catch (ParserException e) {
      MapTool.addLocalMessage(e.getMessage());
      // These are not errors to worry about as they are usually user input errors so no need to log
      // them.
      return;
    } catch (Exception e) {
      MapTool.addLocalMessage(
          I18N.getText("macromanager.couldNotExecute", command, e.getMessage()));
      log.warn("Exception executing command: " + command);
      log.warn(e.getStackTrace());
      return;
    }

    // We'll only get here if the recurseCount is exceeded
    MapTool.addLocalMessage(I18N.getText("macromanager.tooManyResolves", command));
  }

  static String postprocess(String command) {
    command = command.replace("\n", "<br>");

    return command;
  }

  static String preprocess(String command) {
    return command;
  }

  // Package level for testing
  static String resolveAlias(String aliasText, String details) {

    return performSubstitution(aliasText, details);
  }

  private static final Pattern SUBSTITUTION_PATTERN =
      Pattern.compile("\\$\\{([^\\}]+)\\}|\\$(\\w+)");

  // Package level for testing
  static String performSubstitution(String text, String details) {

    List<String> detailList = split(details);

    StringBuffer buffer = new StringBuffer();
    Matcher matcher = SUBSTITUTION_PATTERN.matcher(text);
    while (matcher.find()) {

      String replacement = details;
      String replIndexStr = matcher.group(1);
      if (replIndexStr == null) {
        replIndexStr = matcher.group(2);
      }
      if (!"*".equals(replIndexStr)) {
        try {
          int replaceIndex = Integer.parseInt(replIndexStr);
          if (replaceIndex > detailList.size() || replaceIndex < 1) {
            replacement = "";
          } else {
            // 1-based
            replacement = detailList.get(replaceIndex - 1);
          }
        } catch (NumberFormatException nfe) {

          // Try an alias lookup
          replacement = aliasMap.get(replIndexStr);
          if (replacement == null) {
            replacement = I18N.getText("macromanager.alias.indexNotFound", replIndexStr);
          }
        }
      }
      matcher.appendReplacement(buffer, replacement);
    }
    matcher.appendTail(buffer);

    return buffer.toString();
  }

  // Package level for testing
  // TODO: This should probably go in a util class in rplib
  static List<String> split(String line) {

    List<String> list = new ArrayList<String>();
    StringBuilder currentWord = new StringBuilder();
    boolean isInQuote = false;
    char previousChar = 0;
    for (int i = 0; i < line.length(); i++) {

      char ch = line.charAt(i);

      try {
        // Word boundaries
        if (Character.isWhitespace(ch) && !isInQuote) {
          if (currentWord.length() > 0) {
            list.add(currentWord.toString());
          }
          currentWord.setLength(0);
          continue;
        }

        // Quoted boundary
        if (ch == '"' && previousChar != '\\') {

          if (isInQuote) {
            isInQuote = false;
            if (currentWord.length() > 0) {
              list.add(currentWord.toString());
              currentWord.setLength(0);
            }
          } else {
            isInQuote = true;
          }

          continue;
        }

        if (ch == '\\') {
          continue;
        }

        currentWord.append(ch);

      } finally {
        previousChar = ch;
      }
    }

    if (currentWord.length() > 0) {
      list.add(currentWord.toString());
    }

    return list;
  }

  private static void executeMacro(
      MacroContext context, Macro macro, String parameter, MapToolMacroContext executionContext) {
    if (log.isDebugEnabled()) {
      log.debug(
          "Starting macro: "
              + macro.getClass().getSimpleName()
              + "----------------------------------------------------------------------------------");
    }
    macro.execute(context, parameter, executionContext);
  }
}
