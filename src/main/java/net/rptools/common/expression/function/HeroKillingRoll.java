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
import net.rptools.common.expression.RunData;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractNumberFunction;

/*
 * Hero System Dice
 *
 * Used to get both the stun & body of an attack roll.
 *
 */
public class HeroKillingRoll extends AbstractNumberFunction {
  public HeroKillingRoll() {
    super(2, 3, false, "herokilling", "herokilling2", "killing", "heromultiplier", "multiplier");
  }

  // Use variable names with illegal character to minimize chances of variable overlap
  private static String lastKillingBodyVar = "#Hero-LastKillingBodyVar";

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver vr, String functionName, List<Object> parameters)
      throws ParserException {
    int n = 0;

    double times = ((BigDecimal) parameters.get(n++)).doubleValue();
    int sides = ((BigDecimal) parameters.get(n++)).intValue();
    double half = times - Math.floor(times);
    int extra = 0;
    if (parameters.size() > 2) extra = ((BigDecimal) parameters.get(n++)).intValue();

    RunData runData = RunData.getCurrent();

    if (functionName.equalsIgnoreCase("herokilling")) {

      int body = DiceHelper.rollDice((int) times, sides);
      body = body + extra;
      /*
       * If value half or more roll a half die
       */
      if (half >= 0.5) {
        /*
         * Roll a half dice.
         */
        int die = runData.randomInt(sides);
        body += (die + 1) / 2;
      } else if (half >= 0.2) {
        /*
         * Add a single pip
         */
        body++;
      }

      vr.setVariable(lastKillingBodyVar, new BigDecimal(body));
      return new BigDecimal(body);
    } else if (functionName.equalsIgnoreCase("herokilling2")) {

      int body = DiceHelper.rollDice((int) times, sides);
      body = body + extra;
      /*
       * If value half or more roll a die -1.  minimum value of 1.
       */
      if (half >= 0.5) {
        /*
         * Roll a half dice.
         */
        int die = runData.randomInt(sides);
        if (die > 1) die = die - 1;
        body += die;
      } else if (half >= 0.2) {
        /*
         * Add a single pip
         */
        body++;
      }

      vr.setVariable(lastKillingBodyVar, new BigDecimal(body));
      return new BigDecimal(body);
    } else {
      int multi = DiceHelper.rollDice((int) times, sides);
      multi = multi + extra;
      if (multi < 1) multi = 1;

      int lastBody = 0;
      if (vr.containsVariable(lastKillingBodyVar))
        lastBody = ((BigDecimal) vr.getVariable(lastKillingBodyVar)).intValue();

      return new BigDecimal(lastBody * multi);
    }
  }
}
