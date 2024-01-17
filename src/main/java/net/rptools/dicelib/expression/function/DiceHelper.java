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
package net.rptools.dicelib.expression.function;

import java.util.Arrays;
import java.util.Comparator;
import net.rptools.dicelib.expression.RunData;
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

  public static String explodingSuccessDice(int times, int sides, int target, int limit)
      throws EvaluationException {
    String rolls = "Dice: ";
    int successes = 0;

    for (int i = 0; i < times; i++) {
      int currentRoll = explodeDice(1, sides, limit);
      rolls += currentRoll + ", ";
      if (currentRoll >= target) successes++;
    }
    return rolls + "Successes: " + successes;
  }

  public static String explodingSuccessDice(int times, int sides, int target)
      throws EvaluationException {
    return explodingSuccessDice(times, sides, target, -1);
  }

  public static String openTestDice(int times, int sides, int limit) throws EvaluationException {
    String rolls = "Dice: ";
    int max = 0;

    for (int i = 0; i < times; i++) {
      int currentRoll = explodeDice(1, sides, limit);
      rolls += currentRoll + ", ";
      if (currentRoll > max) max = currentRoll;
    }
    return rolls + "Maximum: " + max;
  }

  public static String openTestDice(int times, int sides) throws EvaluationException {
    return openTestDice(times, sides, -1);
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
      while ((roll = runData.randomInt(sides)) < lowerBound)
        ;

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

  public static int explodeDice(int times, int sides, int limit) throws EvaluationException {
    int result = 0;
    boolean infiniteExplode = limit <= 0;

    if (sides == 0 || sides == 1) throw new EvaluationException("Number of sides must be > 1");

    RunData runData = RunData.getCurrent();

    for (int i = 0; i < times; i++) {
      int thisDieRolls = 0;
      boolean endRolling = false;
      while (!endRolling && (thisDieRolls < limit || infiniteExplode)) {
        int roll = runData.randomInt(sides);
        thisDieRolls++;
        if (roll != sides) {
          endRolling = true;
        }
        result += roll;
      }
    }

    return result;
  }

  public static int explodeDice(int times, int sides) throws EvaluationException {
    return explodeDice(times, sides, -1);
  }

  public static int countSuccessDice(int times, int sides, int success) {
    RunData runData = RunData.getCurrent();

    int result = 0;
    for (int value : runData.randomInts(times, sides)) {
      if (value >= success) result++;
    }

    return result;
  }

  public enum ShadowrunEdition {
    EDITION_4,
    EDITION_5
  }

  public static String countShadowRun(
      int poolSize, int gremlins, boolean explode, ShadowrunEdition edition) {
    RunData runData = RunData.getCurrent();

    int hitCount = 0;
    int oneCount = 0;
    int sides = 6;
    int success = 5;
    StringBuilder actual = new StringBuilder();

    int times = poolSize;
    for (int i = 0; i < times; i++) {
      int value = runData.randomInt(sides);

      if (value >= success) hitCount++;

      if (value == 1) oneCount++;

      if (value == 6 && explode) times++;

      actual.append(value).append(" ");
    }

    // Check for Glitchs
    // TODO check, if there already was a bug here concerning glitches on exploding dice in SR4
    // in SR5, Exploding dice are re-rolled and do not increase the pool size tested here
    boolean normalGlitch =
        edition == ShadowrunEdition.EDITION_4
            // SR4: half of pool or more
            ? ((double) oneCount >= ((double) times / 2))
            // SR5: strictly more than half of pool
            : ((double) oneCount > ((double) poolSize / 2));

    boolean gremlinGlitch =
        edition == ShadowrunEdition.EDITION_4
            ? ((double) oneCount >= ((double) times / 2 - gremlins))
            : ((double) oneCount > ((double) poolSize / 2 - gremlins));

    boolean noSuccess = hitCount == 0;
    // Both Editions: Critical, if no success
    String criticalPart = noSuccess ? "Critical " : "";
    // Signalize glitches only caused due to gremlins for storytelling
    String gremlinPart = (gremlinGlitch ^ normalGlitch) ? "Gremlin " : "";
    // but only if this was a glitch.
    // if anyone feeds invalid negative gremlin values into this, non-glitches will become gremlin
    // glitches.
    String glitchFormatted =
        (normalGlitch || gremlinGlitch) ? " *" + criticalPart + gremlinPart + "Glitch*" : "";

    String result =
        "Hits: " + hitCount + " Ones: " + oneCount + glitchFormatted + "  Results: " + actual;

    return result;
  }

  public static int rollModWithBounds(int times, int sides, int mod, int lower, int upper) {
    int result = 0;

    for (int i = 0; i < times; i++) {
      int roll = rollDice(1, sides);
      int val = Math.min(Math.max(roll + mod, lower), upper);
      result += val;
    }

    return result;
  }
}
