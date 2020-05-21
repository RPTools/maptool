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
package net.rptools.common.expression;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class RunData {
  private static ThreadLocal<RunData> current = new ThreadLocal<RunData>();
  public static Random RANDOM = new SecureRandom();

  private final Result result;

  private long randomValue;
  private long randomMax;
  private long randomMin;

  protected List<Integer> rolled = new LinkedList<>();

  public RunData(Result result) {
    this.result = result;
  }

  /** Returns a random integer between 1 and <code>maxValue</code> */
  public int randomInt(int maxValue) {
    return randomInt(1, maxValue);
  }

  /** Returns a list of random integers between 1 and <code>maxValue</code> */
  public int[] randomInts(int num, int maxValue) {
    int[] ret = new int[num];
    for (int i = 0; i < num; i++) {
      ret[i] = randomInt(maxValue);
    }
    return ret;
  }

  /** Returns a random integer between <code>minValue</code> and <code>maxValue</code> */
  public int randomInt(int minValue, int maxValue) {
    randomMin += minValue;
    randomMax += maxValue;

    int result = RANDOM.nextInt(maxValue - minValue + 1) + minValue;

    rolled.add(result);

    randomValue += result;

    return result;
  }

  /**
   * Returns a list of random integers between <code>minValue</code> and <code>maxValue</code>
   *
   * @return
   */
  public int[] randomInts(int num, int minValue, int maxValue) {
    int[] ret = new int[num];
    for (int i = 0; i < num; i++) ret[i] = randomInt(minValue, maxValue);
    return ret;
  }

  public Result getResult() {
    return result;
  }

  public static boolean hasCurrent() {
    return current.get() != null;
  }

  public static RunData getCurrent() {
    RunData data = current.get();
    if (data == null) {
      throw new NullPointerException("data cannot be null");
    }
    return data;
  }

  public static void setCurrent(RunData data) {
    current.set(data);
  }

  // If a seed is set we need to switch from SecureRandom to
  // random.
  public static void setSeed(long seed) {
    RANDOM = new Random(seed);
  }

  public List<Integer> getRolled() {
    return Collections.unmodifiableList(rolled);
  }

  /**
   * Create a new RunData instance for a child execution.
   *
   * @param childResult the Result object for the new RunData
   * @return a new RunData
   */
  public RunData createChildRunData(Result childResult) {
    return new RunData(childResult);
  }
}
