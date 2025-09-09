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
package net.rptools.maptool.util;

import javax.swing.JOptionPane;
import net.rptools.dicelib.expression.ExpressionParser;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolExpressionParser;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Token;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;

public class ExpressionParserFactory {
  public ExpressionParser create() {
    return new ExpressionParser(
        ExpressionParserFactory::getAdvancedRollVariable,
        ExpressionParserFactory::getAdvancedRollProperty,
        ExpressionParserFactory::getAdvancedRollPromptedValue);
  }

  public MapToolExpressionParser createMT() {
    return new MapToolExpressionParser(
        ExpressionParserFactory::getAdvancedRollVariable,
        ExpressionParserFactory::getAdvancedRollProperty,
        ExpressionParserFactory::getAdvancedRollPromptedValue);
  }

  /**
   * Get the variable value.
   *
   * @param resolver the variable resolver.
   * @param name the name of the variable.
   * @return the value of the variable.
   */
  private static Object getAdvancedRollVariable(VariableResolver resolver, String name) {
    if (!resolver.getVariables().contains(name.toLowerCase())) {
      throw new IllegalArgumentException(I18N.getText("advanced.roll.unknownVariable", name));
    }
    try {
      return resolver.getVariable(name);
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
  private static Object getAdvancedRollProperty(VariableResolver resolver, String name) {
    Token token =
        resolver instanceof MapToolVariableResolver mtResolver
            ? mtResolver.getTokenInContext()
            : null;
    if (token == null) {
      throw new IllegalArgumentException(I18N.getText("advanced.roll.noTokenInContext"));
    }

    var value = token.getProperty(name);
    if (value == null) {
      throw new IllegalArgumentException(I18N.getText("advanced.roll.unknownProperty", name));
    }
    return value;
  }

  /**
   * Prompt the user for a value.
   *
   * @param name the name of the value.
   * @return the value.
   */
  private static String getAdvancedRollPromptedValue(String name) {
    var option =
        JOptionPane.showInputDialog(
            MapTool.getFrame(),
            I18N.getText("lineParser.dialogValueFor", name),
            I18N.getText("lineParser.dialogTitleNoToken"),
            JOptionPane.QUESTION_MESSAGE,
            null,
            null,
            1);
    return option.toString();
  }
}
