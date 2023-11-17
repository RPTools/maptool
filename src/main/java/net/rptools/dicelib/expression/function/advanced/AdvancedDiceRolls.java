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

import java.util.List;
import net.rptools.dicelib.expression.function.advanced.GenesysDiceRolls.DiceType;
import net.rptools.maptool.language.I18N;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;

/** Function to roll dice using the advanced dice roller. */
public class AdvancedDiceRolls extends AbstractFunction {

  /** Constructor. */
  public AdvancedDiceRolls() {
    super(2, 2, false, "advancedRoll");
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {
    String diceName = parameters.get(0).toString().toLowerCase();
    String diceExpression = parameters.get(1).toString();

    try {
      return switch (diceName) {
        case "sw" -> new GenesysDiceRolls().roll(DiceType.StarWars, diceExpression, resolver);
        case "gs" -> new GenesysDiceRolls().roll(DiceType.Genesys, diceExpression, resolver);
        default -> throw new ParserException(
            I18N.getText("advanced.roll.unknownDiceType", diceName));
      };
    } catch (IllegalArgumentException e) {
      throw new ParserException(e.getMessage());
    }
  }
}
