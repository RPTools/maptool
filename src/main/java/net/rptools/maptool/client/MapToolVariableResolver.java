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
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.math.BigDecimal;
import java.util.*;
import javax.swing.JOptionPane;
import net.rptools.CaseInsensitiveHashMap;
import net.rptools.maptool.client.functions.*;
import net.rptools.maptool.client.functions.json.JSONMacroFunctions;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.*;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableModifiers;
import net.rptools.parser.VariableResolver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MapToolVariableResolver implements VariableResolver {

  // Logger for this class.
  private static final Logger LOGGER = LogManager.getLogger(MapToolVariableResolver.class);

  /** The prefix for querying and setting state values . */
  public static final String BAR_PREFIX = "bar.";

  /** The variable name for querying and setting the current round in initiative. */
  public static final String INITIATIVE_ROUND = "init.round";

  /** The variable name for querying and setting the current initiative. */
  public static final String INITIATIVE_CURRENT = "init.current";

  public static final String CAMPAIGN_PANEL = "campaign";
  public static final String GM_PANEL = "gm";

  /** The prefix for querying and setting state values . */
  public static final String STATE_PREFIX = "state.";

  /** The variable name for querying and setting token halos. */
  public static final String TOKEN_HALO = "token.halo";

  /** The variable name for querying and setting token name */
  private static final String TOKEN_NAME = "token.name";

  /** The variable name for querying and setting token the gm name */
  private static final String TOKEN_GMNAME = "token.gm_name";

  /** The variable name for querying and setting token name */
  private static final String TOKEN_LABEL = "token.label";

  /** The variable name for querying and setting the initiative of the current token. */
  public static final String TOKEN_INITIATIVE = "token.init";

  /** The variable name for querying and setting the initiative of the current token. */
  public static final String TOKEN_INITIATIVE_HOLD = "token.initHold";

  /** The variable name for querying and setting token visible state */
  private static final String TOKEN_VISIBLE = "token.visible";

  private static final Map<String, Object> CONSTANTS =
      Map.of(
          "true",
          BigDecimal.ONE,
          "false",
          BigDecimal.ZERO,
          "json.null",
          JsonNull.INSTANCE,
          "json.true",
          new JsonPrimitive(true),
          "json.false",
          new JsonPrimitive(false),
          "panel.campaign",
          CAMPAIGN_PANEL,
          "panel.gm",
          GM_PANEL);
  private final Map<String, Object> variables = new CaseInsensitiveHashMap<>();

  private List<Runnable> delayedActionList;

  private Token tokenInContext;

  private boolean autoPrompt;

  public MapToolVariableResolver(Token tokenInContext) {
    this.tokenInContext = tokenInContext;
    autoPrompt = true;
    // Set the default macro.args to "" so that it is always present.
    try {
      this.setVariable("macro.args", "");
      this.setVariable("macro.catchAbort", BigDecimal.ZERO);
      this.setVariable("macro.catchAssert", BigDecimal.ZERO);
      this.setVariable("macro.args.num", BigDecimal.ZERO);
      this.setVariable(TokenMoveFunctions.ON_TOKEN_MOVE_DENY_VARIABLE, 0);
      this.setVariable(TokenMoveFunctions.ON_TOKEN_MOVE_COUNT_VARIABLE, 1);
      this.setVariable(InitiativeList.ON_INITIATIVE_CHANGE_DENY_VARIABLE, 0);
    } catch (ParserException e) {
      LOGGER.error("Error: Unable to set macro.args to default value <br>" + e.getMessage());
    }

    for (Map.Entry<String, Object> entry : CONSTANTS.entrySet()) {
      try {
        setVariable(entry.getKey(), entry.getValue());
      } catch (ParserException e) {
        LOGGER.error("Error: Unable to set constant " + entry.getKey() + " to " + entry.getValue());
      }
    }
  }

  /**
   * Initialize this resolver
   *
   * @return true if initialization actually happened, false if it's already been initialized
   */
  public boolean initialize() {
    if (delayedActionList == null) {
      delayedActionList = new ArrayList<>();
      return true;
    }
    return false;
  }

  /**
   * Add an action to be performed after the full expression has been evaluated.
   *
   * @param runnable the action to be performed.
   */
  public void addDelayedAction(Runnable runnable) {
    if (!delayedActionList.contains(runnable)) {
      delayedActionList.add(runnable);
    }
  }

  /** Perform any delayed actions. This should called by the command framework only. */
  public void flush() {
    for (Runnable r : delayedActionList) {
      r.run();
    }
  }

  public void setAutoPrompt(boolean value) {
    autoPrompt = value;
  }

  public boolean getAutoPrompt() {
    return autoPrompt;
  }

  @Override
  public void setVariable(String name, Object value) throws ParserException {
    setVariable(name, VariableModifiers.None, value);
  }

  @Override
  public boolean containsVariable(String name) {
    return containsVariable(name, VariableModifiers.None);
  }

  @Override
  public boolean containsVariable(String name, VariableModifiers mods) {
    // If we don't have the value then we'll prompt for it
    return true;
  }

  /**
   * Gets the token in context.
   *
   * @return the token in context
   */
  public Token getTokenInContext() {
    return tokenInContext;
  }

  @Override
  public Object getVariable(String name) throws ParserException {
    return getVariable(name, VariableModifiers.None);
  }

  @Override
  public Object getVariable(String name, VariableModifiers mods) throws ParserException {

    boolean evaluate = false; // Should we try to evaluate the value.

    // MT Script doesnt have much in the way of types.
    if (name.startsWith(MarkDownFunctions.MARKDOWN_PREFIX)) {
      return new MarkDownFunctions().getMTSTypeLabel(name);
    }

    Object result = null;
    if (tokenInContext != null) {
      if (name.startsWith(STATE_PREFIX)) {
        String stateName = name.substring(STATE_PREFIX.length());
        return TokenStateFunction.getState(tokenInContext, stateName);
      } else if (name.startsWith(BAR_PREFIX)) {
        String barName = name.substring(BAR_PREFIX.length());
        return TokenBarFunction.getValue(getTokenInContext(), barName);
      } else if (name.equals(TOKEN_HALO)) {
        // We don't want this evaluated as the # format is more useful to us then the
        // evaluated
        // format.
        return TokenHaloFunction.getHalo(tokenInContext).toString();
      } else if (name.equals(TOKEN_NAME)) {
        // Don't evaluate return value.
        return TokenNameFunction.getName(tokenInContext);
      } else if (name.equals(TOKEN_GMNAME)) {
        // Don't evaluate return value.
        return TokenGMNameFunction.getGMName(tokenInContext);
      } else if (name.equals(TOKEN_LABEL)) {
        // Don't evaluate return value.
        return TokenLabelFunction.getLabel(tokenInContext);
      } else if (name.equals(TOKEN_VISIBLE)) {
        // Don't evaluate return value.
        return TokenVisibleFunction.getVisible(tokenInContext);
      } else if (name.equals(TOKEN_INITIATIVE)) {
        return TokenInitFunction.getInitiative(tokenInContext);
      } else if (name.equals(TOKEN_INITIATIVE_HOLD)) {
        return TokenInitHoldFunction.getInitiativeHold(tokenInContext);
      } // endif

      if (this.validTokenProperty(name, tokenInContext)) {
        result = tokenInContext.getEvaluatedProperty(name);
      } else {
        // If the token has no property of that name check to see if there s a defaulted
        // value for the property for the token type.
        List<TokenProperty> propertyList =
            MapTool.getCampaign()
                .getCampaignProperties()
                .getTokenPropertyList(tokenInContext.getPropertyType());
        if (propertyList != null) {
          for (TokenProperty property : propertyList) {
            if (name.equalsIgnoreCase(property.getName())) {
              result = property.getDefaultValue();
              evaluate = true;
              break;
            }
          }
        }
      }
    } else {
      if (name.equals(INITIATIVE_CURRENT)) {
        if (!MapTool.getFrame().getInitiativePanel().hasGMPermission())
          throw new ParserException(I18N.getText("lineParser.onlyGMCanGet", INITIATIVE_CURRENT));
        return CurrentInitiativeFunction.getCurrentInitiative();
      } else if (name.equals(INITIATIVE_ROUND)) {
        return InitiativeRoundFunction.getInitiativeRound();
      } // endif
    }

    // Default
    if (result == null) {
      result = variables.get(name);
    }

    // Prompt
    if ((result == null && autoPrompt) || mods == VariableModifiers.Prompt) {
      String DialogTitle = I18N.getText("lineParser.dialogTitleNoToken");
      if (tokenInContext != null
          && tokenInContext.getGMName() != null
          && MapTool.getPlayer().isGM()) {
        DialogTitle = I18N.getText("lineParser.dialogTitle", tokenInContext.getGMName());
      }
      if (tokenInContext != null
          && (tokenInContext.getGMName() == null || !MapTool.getPlayer().isGM())) {
        DialogTitle = I18N.getText("lineParser.dialogTitle", tokenInContext.getName());
      }
      result =
          JOptionPane.showInputDialog(
              MapTool.getFrame(),
              I18N.getText("lineParser.dialogValueFor", name),
              DialogTitle,
              JOptionPane.QUESTION_MESSAGE,
              null,
              null,
              result != null ? result.toString() : "0");
      evaluate = true;
    }
    if (result == null) {
      throw new ParserException(I18N.getText("lineParser.unresolvedValue", name));
    }

    Object value;

    if (result instanceof JsonArray
        || result instanceof JsonObject
        || result instanceof JsonNull
        || (result instanceof JsonPrimitive primitive && primitive.isBoolean())) {
      value = result;
    } else if (result instanceof BigDecimal) {
      value = result;
    } else {

      // First we try convert it to a JSON object.
      if (result.toString().trim().startsWith("[") || result.toString().trim().startsWith("{")) {
        JsonElement json = JSONMacroFunctions.getInstance().asJsonElement(result.toString());
        if (json.isJsonArray() || json.isJsonObject()) {
          return json;
        }
      }

      if (evaluate) {
        // Try parse the value if we can not parse it then just return it as a string.
        try {
          value = MapTool.getParser().parseLine(tokenInContext, result.toString());
        } catch (Exception e) {
          value = result.toString();
        }
      } else {
        value = result.toString();
      }

      // Attempt to convert to a number ...
      try {
        value = new BigDecimal((String) value);
      } catch (Exception e) {
        // Ignore, use previous value of "value"
      }
    }
    return value;
  }

  @Override
  public Set<String> getVariables() {
    return Collections.unmodifiableSet(variables.keySet());
  }

  protected void updateTokenProperty(Token token, String varname, String value) {
    // this logic allows unit tests to execute MT script that changes token properties
    // there should be no other context where we have no server of any kind
    MapTool.serverCommand()
        .updateTokenProperty(tokenInContext, Token.Update.setProperty, varname, value);
  }

  @Override
  public void setVariable(String varname, VariableModifiers modifiers, Object value)
      throws ParserException {

    if (CONSTANTS.containsKey(varname.toLowerCase())
        && variables.containsKey(varname)) { // allow to be set first time
      throw new ParserException(I18N.getText("lineParser.cantAssignToConstant", varname));
    }

    if (tokenInContext != null && validTokenProperty(varname, tokenInContext)) {
      updateTokenProperty(tokenInContext, varname, value.toString());
    }

    // Check to see if it is a token state.
    if (varname.startsWith(STATE_PREFIX)) {
      String stateName = varname.substring(STATE_PREFIX.length());
      TokenStateFunction.setState(tokenInContext, stateName, value);
      return;
    } else if (varname.startsWith(BAR_PREFIX)) {
      String barName = varname.substring(BAR_PREFIX.length());
      TokenBarFunction.setValue(tokenInContext, barName, value);
      return;
    } else if (varname.equals(TOKEN_HALO)) {
      TokenHaloFunction.setHalo(tokenInContext, value);
      return;
    } else if (varname.equals(TOKEN_NAME)) {
      if (value.toString().equals("")) {
        throw new ParserException(I18N.getText("lineParser.emptyTokenName"));
      }
      TokenNameFunction.setName(tokenInContext, value.toString());
      return;
    } else if (varname.equals(TOKEN_GMNAME)) {
      TokenGMNameFunction.setGMName(tokenInContext, value.toString());
      return;
    } else if (varname.equals(TOKEN_LABEL)) {
      TokenLabelFunction.setLabel(tokenInContext, value.toString());
      return;
    } else if (varname.endsWith(TOKEN_VISIBLE)) {
      TokenVisibleFunction.setVisible(tokenInContext, value.toString());
      return;
    } else if (varname.equals(TOKEN_INITIATIVE)) {
      TokenInitFunction.setInitiative(tokenInContext, value.toString());
      return;
    } else if (varname.equals(TOKEN_INITIATIVE_HOLD)) {
      boolean set = FunctionUtil.getBooleanValue(value);
      TokenInitHoldFunction.setInitiativeHold(tokenInContext, set);
      return;
    } else if (varname.equals(INITIATIVE_CURRENT)) {
      if (!MapTool.getFrame().getInitiativePanel().hasGMPermission())
        throw new ParserException(I18N.getText("lineParser.onlyGMCanSet", INITIATIVE_CURRENT));
      CurrentInitiativeFunction.setCurrentInitiative(value);
      return;
    } else if (varname.equals(INITIATIVE_ROUND)) {
      if (!MapTool.getFrame().getInitiativePanel().hasGMPermission())
        throw new ParserException(I18N.getText("lineParser.onlyGMCanSet", INITIATIVE_ROUND));
      InitiativeRoundFunction.setInitiativeRound(value);
      return;
    }
    variables.put(varname, value);
  }

  /**
   * Checks to see if the specified property is valid for the token.
   *
   * @param prop The name of the property to check.
   * @param token The token to check.
   * @return <code>true</code> if the property is valid for the token.
   */
  private boolean validTokenProperty(String prop, Token token) {
    for (TokenProperty tp : MapTool.getCampaign().getTokenPropertyList(token.getPropertyType())) {
      if (tp.getName().equalsIgnoreCase(prop)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Sets the token that is in context for this variable resolver. You will only ever need to call
   * this method if you want to change the in context token mid macro.
   *
   * @param token The new token in context.
   */
  public void setTokenIncontext(Token token) {
    tokenInContext = token;
  }
}
