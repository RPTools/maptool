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
public class HeroRoll extends AbstractNumberFunction {
  public HeroRoll() {
    super(2, 2, false, "hero", "herostun", "herobody");
  }

  // Use variable names with illegal character to minimize chances of variable overlap
  private static String lastTimesVar = "#Hero-LastTimesVar";
  private static String lastSidesVar = "#Hero-LastSidesVar";
  private static String lastBodyVar = "#Hero-LastBodyVar";

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver vr, String functionName, List<Object> parameters)
      throws ParserException {
    int n = 0;

    double times = ((BigDecimal) parameters.get(n++)).doubleValue();
    int sides = ((BigDecimal) parameters.get(n++)).intValue();

    if (functionName.equalsIgnoreCase("herobody")) {
      double lastTimes = 0;
      if (vr.containsVariable(lastTimesVar))
        lastTimes = ((BigDecimal) vr.getVariable(lastTimesVar)).doubleValue();

      int lastSides = 0;
      if (vr.containsVariable(lastSidesVar))
        lastSides = ((BigDecimal) vr.getVariable(lastSidesVar)).intValue();

      int lastBody = 0;
      if (vr.containsVariable(lastBodyVar))
        lastBody = ((BigDecimal) vr.getVariable(lastBodyVar)).intValue();

      if (times == lastTimes && sides == lastSides) return new BigDecimal(lastBody);

      return new BigDecimal(-1); // Should this be -1?  Perhaps it should return null.
    } else if ("hero".equalsIgnoreCase(functionName) || "herostun".equalsIgnoreCase(functionName)) {
      // assume stun

      double lastTimes = times;
      int lastSides = sides;
      int lastBody = 0;

      RunData runData = RunData.getCurrent();

      int stun = 0;
      double half = times - Math.floor(times);
      for (int i = 0; i < Math.floor(times); i++) {
        int die = runData.randomInt(sides);
        /*
         * Keep track of the body generated.  In theory
         * Hero System only uses 6-sided where a 1 is
         * 0 body, 2-5 is 1 body and 6 is 2 body but I
         * left the sides unbounded just in case.
         */
        if (die > 1) lastBody++;
        if (die == sides) lastBody++;

        stun += die;
      }

      if (half >= 0.5) {
        /*
         * Roll a half dice.  In theory Hero System
         * only uses 6-sided and for half dice
         * 1 & 2 = 1 Stun 0 body
         * 3     = 2 stun 0 body
         * 4     = 2 stun 1 body
         * 5 & 6 = 3 stun 1 body
         */
        int die = runData.randomInt(sides);
        if (die * 2 > sides) lastBody++;

        stun += (die + 1) / 2;
      }

      vr.setVariable(lastTimesVar, new BigDecimal(lastTimes));
      vr.setVariable(lastSidesVar, new BigDecimal(lastSides));
      vr.setVariable(lastBodyVar, new BigDecimal(lastBody));

      return new BigDecimal(stun);
    }
    throw new ParserException("Unknown function name: " + functionName);
  }
}
