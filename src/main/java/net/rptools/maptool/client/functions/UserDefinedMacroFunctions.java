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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.functions.json.JSONMacroFunctions;
import net.rptools.maptool.client.ui.macrobuttons.buttons.MacroButtonPrefs;
import net.rptools.maptool.client.ui.syntax.MapToolScriptSyntax;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.MacroButtonProperties;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.util.EventMacroUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.Function;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UserDefinedMacroFunctions implements Function, AdditionalFunctionDescription {
  private static final Logger log = LogManager.getLogger(UserDefinedMacroFunctions.class);

  private final Map<String, FunctionDefinition> userDefinedFunctions =
      new HashMap<String, FunctionDefinition>();
  private final Map<String, FunctionRedefinition> redefinedFunctions =
      new HashMap<String, FunctionRedefinition>();
  private final Stack<String> currentFunction = new Stack<String>();

  private static UserDefinedMacroFunctions instance = new UserDefinedMacroFunctions();

  private static String ON_LOAD_CAMPAIGN_CALLBACK = "onCampaignLoad";

  private static int nameCounter = 0;

  private static int getNameCounter() {
    return nameCounter++;
  }

  private static class FunctionDefinition {
    public FunctionDefinition(String macroName, boolean ignoreOutput, boolean newVariableContext) {
      this.macroName = macroName;
      this.ignoreOutput = ignoreOutput;
      this.newVariableContext = newVariableContext;
    }

    String macroName;
    boolean ignoreOutput;
    boolean newVariableContext;
  }

  private static class FunctionRedefinition {
    String functionName;
    Function function;
  }

  public static UserDefinedMacroFunctions getInstance() {
    return instance;
  }

  private UserDefinedMacroFunctions() {}

  @Override
  public void checkParameters(String functionName, List<Object> parameters) {
    // Do nothing as we do not know what we will need.
  }

  public Object evaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {
    MapToolVariableResolver newResolver;
    JsonArray jarr = new JsonArray();

    for (Object obj : parameters) {
      if (obj
          instanceof
          String) { // Want to make sure we dont translate string arguments where not wanted
        String s = obj.toString();
        if (!s.startsWith("[") && !s.startsWith("{")) {
          jarr.add(new JsonPrimitive(s));
        } else {
          jarr.add(JSONMacroFunctions.getInstance().asJsonElement(obj));
        }
      } else {
        jarr.add(JSONMacroFunctions.getInstance().asJsonElement(obj));
      }
    }
    String macroArgs = jarr.size() > 0 ? jarr.toString() : "";
    String output;
    FunctionDefinition funcDef = userDefinedFunctions.get(functionName);

    if (funcDef.newVariableContext) {
      newResolver =
          new MapToolVariableResolver(((MapToolVariableResolver) resolver).getTokenInContext());
    } else {
      newResolver = (MapToolVariableResolver) resolver;
    }

    try {
      currentFunction.push(functionName);
      output =
          MapTool.getParser()
              .runMacro(
                  (MapToolVariableResolver) resolver,
                  newResolver.getTokenInContext(),
                  funcDef.macroName,
                  macroArgs,
                  funcDef.newVariableContext);
    } catch (ParserException e) {
      e.addMacro(funcDef.macroName);
      throw e;
    } finally {
      currentFunction.pop();
    }
    // resolver.setVariable("macro.return", newResolver
    // .getVariable("macro.return"));

    if (funcDef.ignoreOutput) {
      return resolver.getVariable("macro.return");
    }

    if (output == null) {
      return "";
    }

    String stripOutput = output.replaceAll("(?s)<!--.*?-->", ""); // Strip comments
    if (stripOutput.trim().length() == 0) {
      output = resolver.getVariable("macro.return").toString();
      stripOutput = output;
    }

    String trim = stripOutput.trim();
    if (trim.startsWith("[") || trim.startsWith("{")) {
      JsonElement json = JSONMacroFunctions.getInstance().asJsonElement(trim);
      if (json != null) {
        return json;
      }
    }

    try {
      return new BigDecimal(stripOutput.trim());
    } catch (Exception e) {
      return output;
    }
  }

  public String[] getAliases() {
    String[] aliases = new String[userDefinedFunctions.keySet().size()];
    aliases = userDefinedFunctions.keySet().toArray(aliases);
    if (aliases == null) {
      return new String[0];
    } else {
      return aliases;
    }
  }

  public int getMaximumParameterCount() {
    // User defined functions could accept any number of parameters.
    return Function.UNLIMITED_PARAMETERS;
  }

  public int getMinimumParameterCount() {
    // User defined functions could accept any number of parameters.
    return 0;
  }

  public boolean isDeterministic() {
    return true;
  }

  public void defineFunction(
      Parser parser, String name, String macro, boolean ignoreOutput, boolean newVariableContext) {
    if (parser.getFunction(name) != null) {
      FunctionRedefinition fr = new FunctionRedefinition();
      fr.function = parser.getFunction(name);
      fr.functionName = name;
      // fr.description = "<html>This is a test doc!</html>";

      if (isFunctionDefined(name)) {
        // If it is already defined as what this then do nothing...
        if (userDefinedFunctions.get(name).macroName.equals(macro)) {
          return;
        }
        // We have to rename the old function
        fr.functionName = "redefined_" + getNameCounter() + "_" + name;
        redefinedFunctions.put(fr.functionName, redefinedFunctions.get(name));
        redefinedFunctions.put(name, fr);
        userDefinedFunctions.put(fr.functionName, userDefinedFunctions.get(name));
      }
      redefinedFunctions.put(name, fr);
    }
    userDefinedFunctions.put(name, new FunctionDefinition(macro, ignoreOutput, newVariableContext));

    MapToolScriptSyntax.resetScriptSyntax();
  }

  public Object executeOldFunction(
      Parser parser, VariableResolver resolver, List<Object> parameters) throws ParserException {
    String functionName = currentFunction.peek();
    FunctionRedefinition functionRedef = redefinedFunctions.get(functionName);
    if (functionRedef == null) {
      throw new ParserException("Old definition for function " + functionName + " does not exist");
    }
    Function function = functionRedef.function;
    function.checkParameters(functionName, parameters);
    return function.evaluate(parser, resolver, functionRedef.functionName, parameters);
  }

  public boolean isFunctionDefined(String name) {
    return userDefinedFunctions.containsKey(name);
  }

  /**
   * Clears any previously mapped UDFs and handles the {@value #ON_LOAD_CAMPAIGN_CALLBACK} macro
   * event. Suppresses chat output on the called macros.
   */
  public void handleCampaignLoadMacroEvent() {
    userDefinedFunctions.clear();
    List<Token> libTokens = EventMacroUtil.getEventMacroTokens(ON_LOAD_CAMPAIGN_CALLBACK);
    String prefix = ON_LOAD_CAMPAIGN_CALLBACK + "@";
    for (Token handler : libTokens) {
      EventMacroUtil.callEventHandler(
          prefix + handler.getName(), "", handler, Collections.emptyMap(), true);
    }
  }

  /**
   * Macro function to retrieve information about all defined functions. Uses the corresponding
   * macroButton's tooltip, if any, as a function description.
   *
   * <p>If the provided delim is empty, this function produces plain text output: listAngryNPCs -
   * Lists all NPCs that are angry at PCs.
   *
   * <p>If the provided delim is "json", this function produces a JsonArray: [{"name":
   * "listAngryNPCs", "description": "Lists all NPCs that are angry at PCs."}]
   *
   * <p>Otherwise, the output is a string list separated by the given delimiter:
   * "listAngryNPCs","Lists all NPCs that are angry at PCs."
   *
   * @param delim delimiter used to separate values in string list. "" and "json" produce special
   *     formatting
   * @param showFullLocations whether fully-qualified macro locations should be included in the
   *     output
   * @return a list of user defined functions
   */
  public Object getDefinedFunctions(String delim, boolean showFullLocations) {
    List<String> aliases = Arrays.asList(getAliases());
    log.info("Found {} defined functions", aliases.size());
    Collections.sort(aliases);
    if ("".equals(delim)) {
      // plain-text output
      List<String> lines = new ArrayList<>();
      for (String name : aliases) {
        StringBuilder line = new StringBuilder(name);
        if (showFullLocations) line.append(" - ").append(getFunctionLocation(name));
        String tooltip = getFunctionTooltip(name);
        if (tooltip != null && !tooltip.isEmpty()) {
          tooltip = StringFunctions.getInstance().replace(tooltip, "<", "&lt;");
          line.append(" - ");
          line.append(tooltip);
        }
        lines.add(line.toString());
      }
      return StringUtils.join(lines, "<br />");
    } else if ("json".equals(delim)) {
      // json output
      JsonArray jsonArray = new JsonArray();
      for (String name : aliases) {
        JsonObject fDef = new JsonObject();
        fDef.addProperty("name", name);
        if (showFullLocations) fDef.addProperty("source", getFunctionLocation(name));
        String tooltip = getFunctionTooltip(name);
        if (tooltip != null && !tooltip.isEmpty()) {
          tooltip = StringFunctions.getInstance().replace(tooltip, "<", "&lt;");
          fDef.addProperty("description", tooltip);
        }
        jsonArray.add(fDef);
      }
      return jsonArray;
    } else {
      // string list output, using delim
      List<String> strings = new ArrayList<>();
      for (String name : aliases) {
        strings.add(name);
        if (showFullLocations) strings.add(getFunctionLocation(name));
        strings.add(getFunctionTooltip(name));
      }
      return StringUtils.join(strings, delim);
    }
  }

  @Override
  public String getFunctionSummary(String functionName) {
    return getFunctionTooltip(functionName);
  }

  @Override
  public String getFunctionDescription(String functionName) {
    return getFunctionLocation(functionName);
  }

  /**
   * Get the macro location for the given defined function
   *
   * @param functionName the UDF name
   * @return the macroName, or null if no such function exists
   */
  public String getFunctionLocation(String functionName) {
    if (functionName == null) {
      return null;
    }

    FunctionDefinition theDef = userDefinedFunctions.get(functionName);
    return (theDef == null) ? null : theDef.macroName;
  }

  /**
   * Get the tooltip from the macro button mapped to the given defined function
   *
   * @param functionName the UDF name
   * @return the evaluated tooltip, an appropriate "summary not available" message if no
   *     corresponding macro button can be found, or null if the given functionName is not a valid
   *     UDF
   */
  public String getFunctionTooltip(String functionName) {
    if (functionName == null) {
      return null;
    }
    FunctionDefinition theDef = userDefinedFunctions.get(functionName);
    if (theDef != null) {
      String[] macroParts = theDef.macroName.split("@", 2);
      if (macroParts.length != 2) return null;
      String macroName = macroParts[0];
      String macroLocation = macroParts[1];
      MacroButtonProperties buttonProps;
      if ("CAMPAIGN".equalsIgnoreCase(macroLocation)) {
        // campaign macro
        List<MacroButtonProperties> mbps = MapTool.getCampaign().getMacroButtonPropertiesArray();
        buttonProps =
            mbps.stream().filter(m -> m.getLabel().equals(macroName)).findFirst().orElse(null);
      } else if ("GM".equalsIgnoreCase(macroLocation)) {
        // GM macro
        List<MacroButtonProperties> mbps = MapTool.getCampaign().getGmMacroButtonPropertiesArray();
        buttonProps =
            mbps.stream().filter(m -> m.getLabel().equals(macroName)).findFirst().orElse(null);
      } else if ("GLOBAL".equalsIgnoreCase(macroLocation)) {
        // Global macro
        List<MacroButtonProperties> mbps = MacroButtonPrefs.getButtonProperties();
        buttonProps =
            mbps.stream().filter(m -> m.getLabel().equals(macroName)).findFirst().orElse(null);
      } else {
        // token macro
        try {
          Token libToken = MapTool.getParser().getTokenMacroLib(macroLocation);
          buttonProps = (libToken == null) ? null : libToken.getMacro(macroName, false);
        } catch (ParserException e) {
          // duplicate lib:token, not a Lib:token, not visible to player, etc.
          return I18N.getText(
              "msg.error.udf.tooltip.loading", theDef.macroName, e.getLocalizedMessage());
        }
      }
      return (buttonProps == null)
          ? I18N.getText("msg.error.udf.tooltip.missingTarget", theDef.macroName)
          : buttonProps.getEvaluatedToolTip();
    } else {
      log.warn("Looking up tooltip for {}, but that UDF that doesn't exist?", functionName);
      return null;
    }
  }
}
