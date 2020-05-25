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

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

/**
 * A version of RunData that replaces the random integer generation with a queue of preconfigured
 * "rolls". Useful for testing evaluation of dice expressions against a deliberately crafted
 * sequence of rolled values.
 */
public class RunDataMockForTesting extends RunData {
  private static final Logger log = Logger.getLogger(RunDataMockForTesting.class.getName());
  private Queue<Integer> toRoll = new ConcurrentLinkedQueue<>();

  /**
   * Construct the RunData with desired sequence of values to return as "roll" results.
   *
   * @param result the Result object, required by {@link RunData}
   * @param rolls the roll values to return, in order
   */
  public RunDataMockForTesting(Result result, int[] rolls) {
    super(result);
    for (int i : rolls) toRoll.add(i);
  }

  /**
   * Private constructor to create a new instance with an existing pre-configured queue.
   *
   * @param result the Result object, required by {@link RunData}
   * @param rolls the pre-configured queue of rolls
   */
  private RunDataMockForTesting(Result result, Queue<Integer> rolls) {
    super(result);
    toRoll = rolls;
  }

  /**
   * Create a child RunData instance, sharing the queue of pre-configured rolls. This allows child
   * execution contexts (such as UDFs) to continue operating from the same pre-configured queue of
   * rolls.
   *
   * @param childResult the Result object for the new RunData
   * @return the child RunData
   */
  @Override
  public RunData createChildRunData(Result childResult) {
    return new RunDataMockForTesting(childResult, toRoll);
  }

  /**
   * Gets the next value from the pre-configured queue, or throws an exception if the queue is
   * empty.
   *
   * @return the next value, if any
   * @throws ArrayIndexOutOfBoundsException if the queue is empty
   */
  private int getNextInt() {
    Integer next = toRoll.poll();
    if (next == null)
      throw new ArrayIndexOutOfBoundsException(
          "Requested more rolls than were provided to the RunDataMock");
    log.fine("Providing next pre-configured roll: " + next);
    rolled.add(next);
    return next;
  }

  /**
   * Gets the next value from the pre-configured queue. If that value would be greater than
   * maxValue, an exception is thrown instead.
   *
   * @param maxValue the upper bound
   * @return an integer less than or equal to maxValue
   * @throws IllegalArgumentException if maxValue is too low for the next pre-configured roll
   */
  @Override
  public int randomInt(int maxValue) {
    int next = getNextInt();
    if (next > maxValue)
      throw new IllegalArgumentException(
          "The given maxValue is too low for the next configured roll: " + next);
    return next;
  }

  /**
   * Gets the next N values from the pre-configured queue. If any of those values would be greater
   * than maxValue, an exception is thrown instead.
   *
   * @param num the desired number of rolls (N)
   * @param maxValue the upper bound
   * @return integers less than or equal to maxValue
   * @throws IllegalArgumentException if maxValue is too low for the next N pre-configured rolls
   */
  @Override
  public int[] randomInts(int num, int maxValue) {
    int[] ret = new int[num];
    for (int i = 0; i < num; i++) {
      ret[i] = randomInt(maxValue);
    }
    return ret;
  }

  /**
   * Gets the next value from the pre-configured queue. If that value would be less than minValue or
   * greater than maxValue, an exception is thrown instead.
   *
   * @param minValue the lower bound
   * @param maxValue the upper bound
   * @return an integer less than or equal to maxValue
   * @throws IllegalArgumentException if minValue is too high or maxValue is too low for the next
   *     pre-configured roll
   */
  @Override
  public int randomInt(int minValue, int maxValue) {
    int next = getNextInt();
    if (next < minValue)
      throw new IllegalArgumentException(
          "The given minValue is too high for the next configured roll: " + next);
    if (next > maxValue)
      throw new IllegalArgumentException(
          "The given maxValue is too low for the next configured roll: " + next);
    return next;
  }

  /**
   * Gets the next N values from the pre-configured queue. If any of those values would be less than
   * minValue or greater than maxValue, an exception is thrown instead.
   *
   * @param num the desired number of rolls (N)
   * @param minValue the lower bound
   * @param maxValue the upper bound
   * @return integers greater than or equal to minValue and less than or equal to maxValue
   * @throws IllegalArgumentException if minValue is too high or maxValue is too low for the next N
   *     pre-configured rolls
   */
  @Override
  public int[] randomInts(int num, int minValue, int maxValue) {
    int[] ret = new int[num];
    for (int i = 0; i < num; i++) {
      ret[i] = randomInt(minValue, maxValue);
    }
    return ret;
  }
}
