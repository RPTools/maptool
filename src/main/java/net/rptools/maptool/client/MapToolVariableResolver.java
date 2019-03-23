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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import net.rptools.maptool.client.functions.CurrentInitiativeFunction;
import net.rptools.maptool.client.functions.InitiativeRoundFunction;
import net.rptools.maptool.client.functions.JSONMacroFunctions;
import net.rptools.maptool.client.functions.TokenBarFunction;
import net.rptools.maptool.client.functions.TokenGMNameFunction;
import net.rptools.maptool.client.functions.TokenHaloFunction;
import net.rptools.maptool.client.functions.TokenInitFunction;
import net.rptools.maptool.client.functions.TokenInitHoldFunction;
import net.rptools.maptool.client.functions.TokenLabelFunction;
import net.rptools.maptool.client.functions.TokenNameFunction;
import net.rptools.maptool.client.functions.TokenStateFunction;
import net.rptools.maptool.client.functions.TokenVisibleFunction;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.TokenProperty;
import net.rptools.parser.MapVariableResolver;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableModifiers;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MapToolVariableResolver extends MapVariableResolver {

  // Logger for this class.
  private static final Logger LOGGER = LogManager.getLogger(MapToolVariableResolver.class);

  /** The prefix for querying and setting state values . */
  public static final String STATE_PREFIX = "state.";

  /** The prefix for querying and setting state values . */
  public static final String BAR_PREFIX = "bar.";

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

  /** The variable name for querying and setting the current round in initiative. */
  public static final String INITIATIVE_ROUND = "init.round";

  /** The variable name for querying and setting the current initiative. */
  public static final String INITIATIVE_CURRENT = "init.current";

  /** The variable name for querying and setting token visible state */
  private static final String TOKEN_VISIBLE = "token.visible";

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
      this.setVariable("tokens.denyMove", 0);
      this.setVariable("tokens.moveCount", 1);
    } catch (ParserException e) {
      LOGGER.error("Error: Unable to set macro.args to default value <br>" + e.getMessage());
    }
  }

  /**
   * Initialize this resolver
   *
   * @return true if initialization actually happened, false if it's already been initialized
   */
  public boolean initialize() {
    if (delayedActionList == null) {
      delayedActionList = new ArrayList<Runnable>();
      return true;
    }
    return false;
  }

  /** Add an action to be performed after the full expression has been evaluated. */
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
  public Object getVariable(String name, VariableModifiers mods) throws ParserException {

    boolean evaluate = false; // Should we try to evaluate the value.

    Object result = null;
    if (tokenInContext != null) {

      if (name.startsWith(STATE_PREFIX)) {
        String stateName = name.substring(STATE_PREFIX.length());
        return TokenStateFunction.getInstance().getState(tokenInContext, stateName);
      } else if (name.startsWith(BAR_PREFIX)) {
        String barName = name.substring(BAR_PREFIX.length());
        return TokenBarFunction.getInstance().getValue(getTokenInContext(), barName);
      } else if (name.equals(TOKEN_HALO)) {
        // We don't want this evaluated as the # format is more useful to us then the
        // evaluated
        // format.
        return TokenHaloFunction.getInstance().getHalo(tokenInContext).toString();
      } else if (name.equals(TOKEN_NAME)) {
        // Don't evaluate return value.
        return TokenNameFunction.getInstance().getName(tokenInContext);
      } else if (name.equals(TOKEN_GMNAME)) {
        // Don't evaluate return value.
        return TokenGMNameFunction.getInstance().getGMName(tokenInContext);
      } else if (name.equals(TOKEN_LABEL)) {
        // Don't evaluate return value.
        return TokenLabelFunction.getInstance().getLabel(tokenInContext);
      } else if (name.equals(TOKEN_VISIBLE)) {
        // Don't evaluate return value.
        return TokenVisibleFunction.getInstance().getVisible(tokenInContext);
      } else if (name.equals(TOKEN_INITIATIVE)) {
        return TokenInitFunction.getInstance().getTokenValue(tokenInContext);
      } else if (name.equals(TOKEN_INITIATIVE_HOLD)) {
        return TokenInitHoldFunction.getInstance().getTokenValue(tokenInContext);
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
        return CurrentInitiativeFunction.getInstance().getCurrentInitiative();
      } else if (name.equals(INITIATIVE_ROUND)) {
        return InitiativeRoundFunction.getInstance().getInitiativeRound();
      } // endif
    }

    // Default
    if (result == null) {
      result = super.getVariable(name, mods);
    }

    // Prompt
    if ((result == null && autoPrompt == true) || mods == VariableModifiers.Prompt) {
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
              I18N.getText("lineParser.dialogValueFor") + " " + name,
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

    if (result instanceof JSONArray) {
      value = result;
    } else if (result instanceof JSONObject) {
      value = result;
    } else if (result instanceof BigDecimal) {
      value = result;
    } else {

      // First we try convert it to a JSON object.
      if (result.toString().trim().startsWith("[") || result.toString().trim().startsWith("{")) {
        Object obj = JSONMacroFunctions.convertToJSON(result.toString());
        if (obj != null) {
          return obj;
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
  public void setVariable(String varname, VariableModifiers modifiers, Object value)
      throws ParserException {
    if (tokenInContext != null) {
      if (validTokenProperty(varname, tokenInContext)) {
        tokenInContext.setProperty(varname, value.toString());
        addDelayedAction(
            new PutTokenAction(
                MapTool.getFrame().getCurrentZoneRenderer().getZone().getId(), tokenInContext));
        return;
      }
    }

    // Check to see if it is a token state.
    if (varname.startsWith(STATE_PREFIX)) {
      String stateName = varname.substring(STATE_PREFIX.length());
      TokenStateFunction.getInstance().setState(tokenInContext, stateName, value);
      addDelayedAction(
          new PutTokenAction(
              MapTool.getFrame().getCurrentZoneRenderer().getZone().getId(), tokenInContext));
      return;
    } else if (varname.startsWith(BAR_PREFIX)) {
      String barName = varname.substring(BAR_PREFIX.length());
      TokenBarFunction.getInstance().setValue(tokenInContext, barName, value);
      return;
    } else if (varname.equals(TOKEN_HALO)) {
      TokenHaloFunction.getInstance().setHalo(tokenInContext, value);
      return;
    } else if (varname.equals(TOKEN_NAME)) {
      if (value.toString().equals("")) {
        throw new ParserException(I18N.getText("lineParser.emptyTokenName"));
      }
      TokenNameFunction.getInstance().setName(tokenInContext, value.toString());
      return;
    } else if (varname.equals(TOKEN_GMNAME)) {
      TokenGMNameFunction.getInstance().setGMName(tokenInContext, value.toString());
      return;
    } else if (varname.equals(TOKEN_LABEL)) {
      TokenLabelFunction.getInstance().setLabel(tokenInContext, value.toString());
      return;
    } else if (varname.endsWith(TOKEN_VISIBLE)) {
      TokenVisibleFunction.getInstance().setVisible(tokenInContext, value.toString());
      return;
    } else if (varname.equals(TOKEN_INITIATIVE)) {
      TokenInitFunction.getInstance().setTokenValue(tokenInContext, value);
      return;
    } else if (varname.equals(TOKEN_INITIATIVE_HOLD)) {
      TokenInitHoldFunction.getInstance().setTokenValue(tokenInContext, value);
      return;
    } else if (varname.equals(INITIATIVE_CURRENT)) {
      if (!MapTool.getFrame().getInitiativePanel().hasGMPermission())
        throw new ParserException(I18N.getText("lineParser.onlyGMCanSet", INITIATIVE_CURRENT));
      CurrentInitiativeFunction.getInstance().setCurrentInitiative(value);
      return;
    } else if (varname.equals(INITIATIVE_ROUND)) {
      if (!MapTool.getFrame().getInitiativePanel().hasGMPermission())
        throw new ParserException(I18N.getText("lineParser.onlyGMCanSet", INITIATIVE_ROUND));
      InitiativeRoundFunction.getInstance().setInitiativeRound(value);
      return;
    }
    super.setVariable(varname, modifiers, value);
  }

  /**
   * Gets the boolean value of the tokens state.
   *
   * @param token The token to get the state of.
   * @param stateName The name of the state to get.
   * @return the value of the state.
   */
  private boolean getBooleanTokenState(Token token, String stateName) {
    Object val = token.getState(stateName);
    if (val instanceof Integer) {
      return ((Integer) val).intValue() != 0;
    } else if (val instanceof Boolean) {
      return ((Boolean) val).booleanValue();
    } else {
      try {
        return Integer.parseInt(val.toString()) != 0;
      } catch (NumberFormatException e) {
        return Boolean.parseBoolean(val.toString());
      }
    }
  }

  /**
   * Sets the boolean state of a token.
   *
   * @param token The token to set the state of.
   * @param stateName The state to set.
   * @param val set or unset the state.
   */
  private void setBooleanTokenState(Token token, String stateName, Object val) {
    boolean set;
    if (val instanceof Integer) {
      set = ((Integer) val).intValue() != 0;
    } else if (val instanceof Boolean) {
      set = ((Boolean) val).booleanValue();
    } else {
      try {
        set = Integer.parseInt(val.toString()) != 0;
      } catch (NumberFormatException e) {
        set = Boolean.parseBoolean(val.toString());
      }
      token.setState(stateName, set);
    }
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
   * Sets the value of all token states.
   *
   * @param token The token to set the state of.
   * @param val set or unset the state.
   */
  private void setAllBooleanTokenStates(Token token, Object value) {
    for (Object stateName : MapTool.getCampaign().getTokenStatesMap().keySet()) {
      setBooleanTokenState(token, stateName.toString(), value);
    }
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

  public static class PutTokenAction implements Runnable {
    private final GUID zoneId;
    private final Token token;

    public PutTokenAction(GUID zoneId, Token token) {
      this.zoneId = zoneId;
      this.token = token;
    }

    public void run() {
      MapTool.serverCommand().putToken(zoneId, token);
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof PutTokenAction)) {
        return false;
      }

      PutTokenAction other = (PutTokenAction) obj;

      return zoneId.equals(other.zoneId) && token.equals(other.token);
    }
  }
}
