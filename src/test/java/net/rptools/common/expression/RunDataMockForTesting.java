package net.rptools.common.expression;

import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

/**
 * A version of RunData that replaces the random integer generation with a queue of preconfigured "rolls".  Useful for testing evaluation of dice expressions against a deliberately crafted sequence of rolled values.
 */
public class RunDataMockForTesting extends RunData {
    private static final Logger log = Logger.getLogger(RunDataMockForTesting.class.getName());
    private final Queue<Integer> toRoll = new LinkedList<>();

    /**
     * Construct the RunData with desired sequence of values to return as "roll" results.
     *
     * @param result the Result object, required by {@link RunData}
     * @param rolls  the roll values to return, in order
     */
    public RunDataMockForTesting(Result result, int[] rolls) {
        super(result);
        for (int i : rolls)
            toRoll.add(i);
    }

    /**
     * Gets the next value from the pre-configured queue, or throws an exception if the queue is empty.
     *
     * @return the next value, if any
     * @throws ArrayIndexOutOfBoundsException if the queue is empty
     */
    private int getNextInt() {
        Integer next = toRoll.poll();
        if (next == null)
            throw new ArrayIndexOutOfBoundsException("Requested more rolls than were provided to the RunDataMock");
        log.fine("Providing next pre-configured roll: " + next);
        rolled.add(next);
        return next;
    }

    /**
     * Gets the next value from the pre-configured queue.  If that value would be greater than maxValue, an exception is thrown instead.
     *
     * @param maxValue the upper bound
     * @return an integer less than or equal to maxValue
     * @throws IllegalArgumentException if maxValue is too low for the next pre-configured roll
     */
    @Override
    public int randomInt(int maxValue) {
        int next = getNextInt();
        if (next > maxValue)
            throw new IllegalArgumentException("The given maxValue is too low for the next configured roll: " + next);
        return next;
    }

    /**
     * Gets the next N values from the pre-configured queue.  If any of those values would be greater than maxValue, an exception is thrown instead.
     *
     * @param num      the desired number of rolls (N)
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
     * Gets the next value from the pre-configured queue.  If that value would be less than minValue or greater than maxValue, an exception is thrown instead.
     *
     * @param minValue the lower bound
     * @param maxValue the upper bound
     * @return an integer less than or equal to maxValue
     * @throws IllegalArgumentException if minValue is too high or maxValue is too low for the next pre-configured roll
     */
    @Override
    public int randomInt(int minValue, int maxValue) {
        int next = getNextInt();
        if (next < minValue)
            throw new IllegalArgumentException("The given minValue is too high for the next configured roll: " + next);
        if (next > maxValue)
            throw new IllegalArgumentException("The given maxValue is too low for the next configured roll: " + next);
        return next;
    }

    /**
     * Gets the next N values from the pre-configured queue.  If any of those values would be less than minValue or greater than maxValue, an exception is thrown instead.
     *
     * @param num      the desired number of rolls (N)
     * @param minValue the lower bound
     * @param maxValue the upper bound
     * @return integers less than or equal to maxValue
     * @throws IllegalArgumentException if minValue is too high or maxValue is too low for the next N pre-configured rolls
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
