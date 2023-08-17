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
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolMacroContext;
import net.rptools.maptool.client.functions.AboutMacro;
import net.rptools.maptool.client.functions.exceptions.*;
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

/**
 * This class manages the slash commands for the client.
 *
 * @note The reference to macro in this class is actually a slash command and not a macro function
 *     for the parser.
 */
public class MacroManager {

  /** The logger instance for this class. */
  private static final Logger log = LogManager.getLogger(MacroManager.class);

  /** The maximum number of times a macro can recurse before it is considered an error. */
  private static final int MAX_RECURSE_COUNT = 10;

  /** An empty macro that is returned when a macro is not found. */
  private static final Macro UNDEFINED_MACRO = new UndefinedMacro();

  /** Map of all slash commands that have been registered. */
  private static final Map<String, Macro> MACROS = new HashMap<>();

  /** Map of all slash command aliases that have been registered. */
  private static final Map<String, MacroDetails> aliasMap = new HashMap<>();

  /** Enum for the scope of the validity of the slash command. */
  public enum Scope {
    /** Not tied to the campaign and persists between campaigns. */
    CLIENT,
    /** Tied to the campaign and does not persist between campaign loads. */
    CAMPAIGN,
    /** Tied to an addon. */
    ADDON
  }

  /**
   * This record is used to store the details of a slash command.
   *
   * @param name The name of the slash command.
   * @param command The command to execute.
   * @param description The description of the slash command.
   * @param scope The scope of the slash command.
   * @param addOnNamespace The namespace of the add on the slash command is tied to.
   * @param addOnName The name of the add on the slash command is tied to.
   */
  public record MacroDetails(
      String name,
      String command,
      String description,
      Scope scope,
      String addOnNamespace,
      String addOnName) {}

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
    registerMacro(new TextureNoise());
    registerMacro(new VersionMacro());
    registerMacro(new AboutMacro());
    registerMacro(UNDEFINED_MACRO);
  }

  /**
   * This method is used to set the alias for a slash command.
   *
   * @param alias The key to use for the alias.
   * @param value The value to use for the alias.
   * @param scope The scope of the alias.
   * @param description The description of the alias.
   */
  public static void setAlias(String alias, String value, Scope scope, String description) {
    description = Objects.requireNonNullElse(description, "");
    aliasMap.put(alias, new MacroDetails(alias, value, description, scope, "", ""));
  }

  /**
   * This method is used to set the alias for a slash command.
   *
   * @param details The details of the alias.
   */
  public static void setAlias(MacroDetails details) {
    aliasMap.put(details.name(), details);
  }

  /**
   * This method is used to remove an alias.
   *
   * @param alias The alias to remove.
   */
  public static void removeAlias(String alias) {
    aliasMap.remove(alias);
  }

  /** This method is used to remove all aliases. */
  public static void removeAllAliases() {
    aliasMap.clear();
  }

  /**
   * Returns the scope of the alias.
   *
   * @param alias The alias to get the scope for.
   * @return The scope of the alias.
   */
  public static Scope getAliasScope(String alias) {
    MacroDetails def = aliasMap.get(alias);
    if (def == null) {
      return null;
    }
    return def.scope();
  }

  /**
   * This method is used to get the alias map.
   *
   * @return The alias map.
   */
  public static Map<String, String> getAliasCommandMap() {
    return aliasMap.entrySet().stream()
        .map(e -> Map.entry(e.getKey(), e.getValue().command()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  /**
   * This method is used to get the details of the defined aliases.
   *
   * @return The details of the defined aliases.
   */
  public static Map<String, MacroDetails> getAliasDetails() {
    return Map.copyOf(aliasMap);
  }

  /**
   * This method is used to get the details of the defined aliases.
   *
   * @param alias The alias to get the details for.
   * @return The details of the defined aliases.
   */
  public static MacroDetails getAliasDetails(String alias) {
    return aliasMap.get(alias);
  }

  /**
   * This method is used to get the registered macros.
   *
   * @return The alias map.
   */
  public static Set<Macro> getRegisteredMacros() {
    Set<Macro> ret = new HashSet<Macro>(MACROS.values());
    return ret;
  }

  /**
   * Returns the registered macro with the given name.
   *
   * @param name The name of the macro to get.
   * @return The macro with the given name.
   */
  private static Macro getRegisteredMacro(String name) {
    Macro ret = MACROS.get(name);
    if (ret == null) {
      return UNDEFINED_MACRO;
    }
    return ret;
  }

  /**
   * Registers a macro with the given name.
   *
   * @param macro The macro to register.
   */
  private static void registerMacro(Macro macro) {
    MacroDefinition def = macro.getClass().getAnnotation(MacroDefinition.class);

    if (def == null) return;

    MACROS.put(def.name(), macro);
    for (String alias : def.aliases()) {
      MACROS.put(alias, macro);
    }
  }

  /**
   * This method is used to execute a macro.
   *
   * @param command The command to execute.
   */
  public static void executeMacro(String command) {
    executeMacro(command, null);
  }

  /**
   * This method is used to execute a macro.
   *
   * @param command The command to execute.
   * @param macroExecutionContext The context in which the macro is being executed.
   */
  public static void executeMacro(String command, MapToolMacroContext macroExecutionContext) {
    MacroContext context = new MacroContext();
    context.addTransform(command);
    String macroButtonName =
        macroExecutionContext == null
            ? "chat"
            : macroExecutionContext.getName() + "@" + macroExecutionContext.getSource();

    try {
      command = preprocess(command);
      context.addTransform(command);

      int recurseCount = 0;
      while (recurseCount < MAX_RECURSE_COUNT) {
        recurseCount++;

        command = command.trim();
        if (command.length() == 0) {
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

        boolean trustedPath = macroExecutionContext != null && macroExecutionContext.isTrusted();

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
        MacroDetails mdet = aliasMap.get(key);
        if (mdet == null) {
          executeMacro(context, UNDEFINED_MACRO, command, macroExecutionContext);
          return;
        }
        String alias = mdet.command();
        command = resolveAlias(alias, details);
        context.addTransform(command);
        continue;
      }
    } catch (AbortFunctionException | ReturnFunctionException fe) {
      // Do nothing, just silently exit
      return;
    } catch (JavascriptFunctionException | AssertFunctionException afe) {
      MapTool.addLocalMessage(afe.getMessage());
      return;
    } catch (ParserException e) {
      e.addMacro(macroButtonName);
      MapTool.addErrorMessage(e);
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

  /**
   * Perform post processing on the command.
   *
   * @param command The command to perform the post processing on.
   * @return The post processed output.
   */
  private static String postprocess(String command) {
    command = command.replace("\n", "<br>");

    return command;
  }

  /**
   * Perform pre-processing on the command.
   *
   * @param command The command to perform the pre-processing on.
   * @return The pre-processed output.
   */
  static String preprocess(String command) {
    return command;
  }

  // Package level for testing
  static String resolveAlias(String aliasText, String details) {

    return performSubstitution(aliasText, details);
  }

  /** This pattern is used to find the substitution variables in the alias text. */
  private static final Pattern SUBSTITUTION_PATTERN =
      Pattern.compile("\\$\\{([^\\}]+)\\}|\\$(\\w+)");

  // Package level for testing

  /**
   * This method performs the substitution of the alias text.
   *
   * @param text The text to perform the substitution on.
   * @param details The details to use for the substitution.
   * @return The substituted text.
   */
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
          MacroDetails mdet = aliasMap.get(replIndexStr);
          if (mdet == null) {
            replacement = I18N.getText("macromanager.alias.indexNotFound", replIndexStr);
          } else {
            replacement = mdet.command();
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

  /** Clear all campaign scoped aliases. */
  public static void removeCampaignAliases() {
    List<String> campaignAliases =
        aliasMap.entrySet().stream()
            .filter(e -> e.getValue().scope() == Scope.CAMPAIGN)
            .map(Entry::getKey)
            .toList();
    campaignAliases.forEach(aliasMap::remove);
  }

  /**
   * Clear all aliases for the given add on namespace.
   *
   * @param namespace The namespace to clear the aliases for.
   */
  public static void removeAddOnAliases(String namespace) {
    List<String> campaignAliases =
        aliasMap.entrySet().stream()
            .filter(e -> namespace.equalsIgnoreCase(e.getValue().addOnNamespace()))
            .map(Entry::getKey)
            .toList();
    campaignAliases.forEach(aliasMap::remove);
  }

  /**
   * Execute the macro.
   *
   * @param context The macro context.
   * @param macro The macro to execute.
   * @param parameter The parameter to pass to the macro.
   * @param executionContext The execution context.
   */
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
