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

import java.util.Arrays;
import java.util.Comparator;
import net.rptools.common.expression.RunData;
import net.rptools.parser.function.*;

public class DiceHelper {
  public static int rollDice(int times, int sides) {
    int result = 0;

    RunData runData = RunData.getCurrent();

    for (int i = 0; i < times; i++) {
      result += runData.randomInt(sides);
    }

    return result;
  }

  public static String explodingSuccessDice(int times, int sides, int target)
      throws EvaluationException {
    String rolls = "Dice: ";
    int successes = 0;

    for (int i = 0; i < times; i++) {
      int currentRoll = explodeDice(1, sides);
      rolls += currentRoll + ", ";
      if (currentRoll >= target) successes++;
    }
    return rolls + "Successes: " + successes;
  }

  public static String openTestDice(int times, int sides) throws EvaluationException {
    String rolls = "Dice: ";
    int max = 0;

    for (int i = 0; i < times; i++) {
      int currentRoll = explodeDice(1, sides);
      rolls += currentRoll + ", ";
      if (currentRoll > max) max = currentRoll;
    }
    return rolls + "Maximum: " + max;
  }

  public static int fudgeDice(int times) {
    return rollDice(times, 3) - (2 * times);
  }

  public static int ubiquityDice(int times) {
    return rollDice(times, 2) - times;
  }

  public static int keepDice(int times, int sides, int keep) throws EvaluationException {
    if (keep > times) throw new EvaluationException("You cannot keep more dice than you roll");
    return dropDice(times, sides, times - keep);
  }

  public static int keepLowestDice(int times, int sides, int keep) throws EvaluationException {
    if (keep > times) throw new EvaluationException("You cannot keep more dice than you roll");
    return dropDiceHighest(times, sides, times - keep);
  }

  public static int dropDice(int times, int sides, int drop) throws EvaluationException {
    if (times - drop <= 0) throw new EvaluationException("You cannot drop more dice than you roll");

    RunData runData = RunData.getCurrent();

    int[] values = runData.randomInts(times, sides);

    Arrays.sort(values);

    int result = 0;
    for (int i = drop; i < times; i++) {
      result += values[i];
    }

    return result;
  }

  public static int dropDiceHighest(int times, int sides, int drop) throws EvaluationException {
    if (times - drop <= 0) throw new EvaluationException("You cannot drop more dice than you roll");

    RunData runData = RunData.getCurrent();

    int[] values = runData.randomInts(times, sides);

    int[] descValues =
        Arrays.stream(values)
            .boxed()
            .sorted(Comparator.reverseOrder())
            .mapToInt(Integer::intValue)
            .toArray();

    int result = 0;
    for (int i = drop; i < times; i++) {
      result += descValues[i];
    }

    return result;
  }

  public static int rerollDice(int times, int sides, int lowerBound) throws EvaluationException {
    RunData runData = RunData.getCurrent();

    if (lowerBound > sides)
      throw new EvaluationException(
          "When rerolling, the lowerbound must be smaller than the number of sides on the rolling dice.");

    int[] values = new int[times];

    for (int i = 0; i < values.length; i++) {
      int roll;
      while ((roll = runData.randomInt(sides)) < lowerBound) ;

      values[i] = roll;
    }

    int result = 0;
    for (int i = 0; i < values.length; i++) {
      result += values[i];
    }

    return result;
  }

  /**
   * Rolls X dice with Y sides each, with any result lower than L being re-rolled once. If
   * chooseHigher is true, the higher of the two rolled values is kept. Otherwise, the new roll is
   * kept regardless.
   *
   * <p>Differs from {@link #rerollDice(int, int, int)} in that the new results are allowed to fall
   * beneath the given lowerBound, instead of being re-rolled again.
   *
   * @param times the number of dice
   * @param sides the number of sides
   * @param lowerBound the number below which dice will be re-rolled. Must be strictly lower than
   *     the number of sides.
   * @param chooseHigher whether the original result may be preserved if it was the higher value
   * @return the total of the rolled and re-rolled dice
   * @throws EvaluationException if an invalid lowerBound is provided
   */
  public static int rerollDiceOnce(int times, int sides, int lowerBound, boolean chooseHigher)
      throws EvaluationException {
    RunData runData = RunData.getCurrent();

    if (lowerBound > sides)
      throw new EvaluationException(
          "When rerolling, the lowerbound must be smaller than the number of sides on the rolling dice.");

    int[] values = new int[times];

    for (int i = 0; i < values.length; i++) {
      int roll = runData.randomInt(sides);
      if (roll < lowerBound) {
        int roll2 = runData.randomInt(sides);
        if (chooseHigher) {
          roll = Math.max(roll, roll2);
        } else {
          roll = roll2;
        }
      }
      values[i] = roll;
    }

    int result = 0;
    for (int i = 0; i < values.length; i++) {
      result += values[i];
    }

    return result;
  }

  public static int explodeDice(int times, int sides) throws EvaluationException {
    int result = 0;

    if (sides == 0 || sides == 1) throw new EvaluationException("Number of sides must be > 1");

    RunData runData = RunData.getCurrent();

    for (int i = 0; i < times; i++) {
      int roll = runData.randomInt(sides);
      if (roll == sides) times++;
      result += roll;
    }

    return result;
  }

  public static int countSuccessDice(int times, int sides, int success) {
    RunData runData = RunData.getCurrent();

    int result = 0;
    for (int value : runData.randomInts(times, sides)) {
      if (value >= success) result++;
    }

    return result;
  }

  public static String countShadowRun4(int times, int gremlins, boolean explode) {
    RunData runData = RunData.getCurrent();

    int hitCount = 0;
    int oneCount = 0;
    int sides = 6;
    int success = 5;
    String actual = "";
    String glitch = "";

    for (int i = 0; i < times; i++) {
      int value = runData.randomInt(sides);

      if (value >= success) hitCount++;

      if (value == 1) oneCount++;

      if (value == 6 && explode) times++;

      actual = actual + value + " ";
    }

    // Check for Glitchs
    if (oneCount != 0) {
      if ((hitCount == 0) && ((double) times / 2 - gremlins) <= (double) oneCount) {
        glitch = " *Critical Glitch*";
      } else if ((double) (times / 2 - gremlins) <= (double) oneCount) {
        glitch = " *Glitch*";
      }
    }

    String result = "Hits: " + hitCount + " Ones: " + oneCount + glitch + "  Results: " + actual;

    return result;
  }

  public static int rollModWithBounds(int times, int sides, int sub, int lower, int upper) {
    int result = 0;

    for (int i = 0; i < times; i++) {
      int roll = rollDice(1, sides);
      int val = Math.min(Math.max(roll + sub, lower), upper);
      result += val;
    }

    return result;
  }
}
