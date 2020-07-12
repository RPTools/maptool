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
package net.rptools.common.expression.function;

import java.math.BigDecimal;
import java.util.List;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractNumberFunction;

public class ArsMagicaStress extends AbstractNumberFunction {

  public ArsMagicaStress() {
    super("arsMagicaStressNum", "arsMagicaStress");
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {

    int botchDice = ((BigDecimal) parameters.get(0)).intValue();
    int bonus = ((BigDecimal) parameters.get(1)).intValue();

    if ("arsMagicaStressNum".equals(functionName)) {
      return arsMagicaStress(botchDice, bonus);
    } else if ("arsMagicaStress".equalsIgnoreCase(functionName)) {
      return arsMagicaStressAsString(botchDice, bonus);
    }
    throw new ParserException("Unknown function name: " + functionName);
  }

  private String arsMagicaStressAsString(int botchDice, int bonus) {
    int val = arsMagicaStress(botchDice, bonus).intValue();
    if (val == -1) {
      return "0 (1 botch)";
    } else if (val < -1) {
      return "0 (" + (-val) + " botches)";
    } else {
      return Integer.toString(val);
    }
  }

  /**
   * Returns the results of an Ars Magicia Stress roll. If there are botches rolled then the result
   * will be a negative value indicating the number of botches.
   *
   * @param botchDice the number of dice to roll if the first roll is a 0.
   * @param bonus
   * @return
   */
  private BigDecimal arsMagicaStress(int botchDice, int bonus) {
    int multiplier = 1;
    boolean done = false;
    int botches = 0;
    int rollTotal = 0;
    while (!done) {
      int roll = DiceHelper.rollDice(1, 10);
      if (roll == 10 && multiplier == 1) { // only a botch if we haven't rolled a 1 before
        for (int i = 0; i < botchDice; i++) {
          if (DiceHelper.rollDice(1, 10) == 10) {
            botches++;
          }
        }
        if (botches > 0) {
          bonus = 0; // Once you have botches you no longer get any bonus.
        }
        done = true;
      } else if (roll == 1) {
        multiplier *= 2;
      } else {
        rollTotal = roll * multiplier;
        done = true;
      }
    }

    /*
     * If 10 (0) was rolled as first roll and no other 10s are rolled then
     *    rollTotal = 0
     *    bonus = bonus
     *    botches = 0
     *
     * If 10 (0) was rolled as first roll then more 10s where rolled
     *    rollTotal = 0
     *    bonus = 0
     *    botches = number of 10s rolled after first
     *
     * if the first roll was not 10 (0) then
     *    rollTotal = total from the roll
     *    bonus = bonus
     *    botches = 0
     */
    int val = Math.max(rollTotal + bonus, 0) - botches;
    return BigDecimal.valueOf(val);
  }
}
