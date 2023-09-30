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
package net.rptools.dicelib.expression.function.advanced;

import java.util.Map;
import javax.swing.JOptionPane;
import net.rptools.maptool.advanceddice.genesys.GenesysDiceResult;
import net.rptools.maptool.advanceddice.genesys.GenesysDiceRoller;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.ui.theme.ThemeSupport;
import net.rptools.maptool.client.ui.theme.ThemeSupport.ThemeColor;
import net.rptools.maptool.language.I18N;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;

/** Function to roll dice using the advanced dice roller. */
public class GenesysDiceRolls {

  /** Enum to represent the different dice types. */
  public enum DiceType {
    StarWars("sw"),
    Genesys("gs");

    /** The variable prefix for the dice type. */
    public final String variablePrefix;

    /**
     * Constructor.
     *
     * @param variablePrefix the prefix to use for variables.
     */
    DiceType(String variablePrefix) {
      this.variablePrefix = variablePrefix;
    }

    /**
     * Get the variable prefix for the dice type.
     *
     * @return the variable prefix.
     */
    public String getVariablePrefix() {
      return variablePrefix;
    }
  }

  /** Map of font names for the different systems. */
  private static final Map<DiceType, String> GS_FONT_NAME_MAP =
      Map.of(DiceType.StarWars, "EotE Symbol", DiceType.Genesys, "Genesys Glyphs and Dice");

  /**
   * Roll the given dice string using genesys/starwars dice roll parser.
   *
   * @param diceType the type of dice to roll.
   * @param diceExpression the expression to roll.
   * @param resolver the variable resolver.
   * @return the result of the roll.
   * @throws ParserException if there is an error parsing the expression.
   */
  Object roll(DiceType diceType, String diceExpression, VariableResolver resolver)
      throws ParserException {
    var roller = new GenesysDiceRoller();
    try {
      var result =
          roller.roll(
              diceExpression,
              n -> getVariable(resolver, n),
              n -> getProperty(resolver, n),
              n -> getPromptedValue(resolver, n));

      if (result.hasErrors()) {
        var errorSb = new StringBuilder();
        for (var error : result.getErrors()) {
          String msg;
          int ind = error.charPositionInLine();
          if (result.getRollString().length() > ind + 3) {
            msg = result.getRollString().substring(ind, ind + 3) + "...";
          } else {
            msg = result.getRollString().substring(ind);
          }
          var errorText = I18N.getText("advanced.roll.parserError", error.line(), ind + 1, msg);
          errorSb.append(errorText).append("<br/>");
        }
        throw new ParserException(errorSb.toString());
      }
      var varPrefix = diceType.getVariablePrefix() + ".lastRoll";
      setVars(resolver, varPrefix, result);

      return formatResults(diceType, result);

    } catch (IllegalArgumentException e) {
      throw new ParserException(e.getMessage());
    }
  }

  /**
   * Format the results of the roll.
   *
   * @param diceType the type of dice.
   * @param result the result of the roll.
   * @return the formatted results.
   */
  private String formatResults(DiceType diceType, GenesysDiceResult result) {
    var gray = ThemeSupport.getThemeColorHexString(ThemeColor.GREY);

    var sb = new StringBuilder();
    sb.append("<span>");
    sb.append("<font face='")
        .append(GS_FONT_NAME_MAP.get(diceType))
        .append("' size ='+1'")
        .append("'>");
    for (var dice : result.getRolls()) {
      sb.append(dice.resultType().getFontCharacters());
    }
    sb.append("</font>");
    sb.append("</span>");
    return sb.toString();
  }

  /**
   * Set the variables for the result.
   *
   * @param resolver the variable resolver.
   * @param varPrefix the variable prefix.
   * @param result the result.
   * @throws ParserException if there is an error setting the variables.
   */
  private void setVars(VariableResolver resolver, String varPrefix, GenesysDiceResult result)
      throws ParserException {
    resolver.setVariable(varPrefix + ".expression", result.getRollString());
    resolver.setVariable(varPrefix + ".success", result.getSuccessCount());
    resolver.setVariable(varPrefix + ".failure", result.getFailureCount());
    resolver.setVariable(varPrefix + ".advantage", result.getAdvantageCount());
    resolver.setVariable(varPrefix + ".threat", result.getThreatCount());
    resolver.setVariable(varPrefix + ".triumph", result.getTriumphCount());
    resolver.setVariable(varPrefix + ".despair", result.getDespairCount());
    resolver.setVariable(varPrefix + ".light", result.getLightCount());
    resolver.setVariable(varPrefix + ".dark", result.getDarkCount());

    for (var group : result.getGroupNames()) {
      var groupResult = result.getGroup(group);
      setVars(resolver, varPrefix + ".group." + group, groupResult);
    }
  }

  /**
   * Get the variable value.
   *
   * @param resolver the variable resolver.
   * @param name the name of the variable.
   * @return the value of the variable.
   */
  private int getVariable(VariableResolver resolver, String name) {
    if (!resolver.getVariables().contains(name.toLowerCase())) {
      throw new IllegalArgumentException(I18N.getText("advanced.roll.unknownVariable", name));
    }

    try {
      var value = resolver.getVariable(name);
      var result = integerResult(value, false);

      if (result == null) {
        throw new IllegalArgumentException(I18N.getText("advanced.roll.variableNotNumber", name));
      }

      return result;
    } catch (ParserException e) {
      throw new IllegalArgumentException(e);
    }
  }

  /**
   * Get the property value for the token in context.
   *
   * @param resolver the variable resolver.
   * @param name the name of the property.
   * @return the value of the property.
   */
  private int getProperty(VariableResolver resolver, String name) {
    var mtResolver = (MapToolVariableResolver) resolver;
    var token = mtResolver.getTokenInContext();
    if (token == null) {
      throw new IllegalArgumentException(I18N.getText("advanced.roll.noTokenInContext"));
    }
    var value = token.getProperty(name);

    if (value == null) {
      throw new IllegalArgumentException(I18N.getText("advanced.roll.unknownProperty", name));
    }

    var result = integerResult(value, true);
    if (result == null) {
      throw new IllegalArgumentException(I18N.getText("advanced.roll.propertyNotNumber", name));
    }

    return result;
  }

  /**
   * Prompt the user for a value.
   *
   * @param resolver the variable resolver.
   * @param name the name of the value.
   * @return the value.
   */
  private int getPromptedValue(VariableResolver resolver, String name) {
    var option =
        JOptionPane.showInputDialog(
            MapTool.getFrame(),
            I18N.getText("lineParser.dialogValueFor", name),
            I18N.getText("lineParser.dialogTitleNoToken"),
            JOptionPane.QUESTION_MESSAGE,
            null,
            null,
            1);
    try {
      return Integer.parseInt(option.toString());
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(I18N.getText("advanced.roll.inputNotNumber", name));
    }
  }

  /**
   * Get the integer result.
   *
   * @param value the value.
   * @param parseString attempt to parse string value of object if it is not a number.
   * @return the integer result.
   */
  private Integer integerResult(Object value, boolean parseString) {
    if (value instanceof Integer i) {
      return i;
    }

    if (value instanceof Number n) {
      return n.intValue();
    }

    if (parseString) {
      try {
        return Integer.parseInt(value.toString());
      } catch (NumberFormatException e) {
        return null;
      }
    }

    return null;
  }
}
