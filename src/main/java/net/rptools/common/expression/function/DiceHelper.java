/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package net.rptools.common.expression.function;

import java.util.Arrays;

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

	public static String explodingSuccessDice(int times, int sides, int target) throws EvaluationException {
		String rolls = "Dice: ";
		int successes = 0;

		for (int i = 0; i < times; i++) {
			int currentRoll = explodeDice(1, sides);
			rolls += currentRoll + ", ";
			if (currentRoll >= target)
				successes++;
		}
		return rolls + "Successes: " + successes;
	}

	public static String openTestDice(int times, int sides) throws EvaluationException {
		String rolls = "Dice: ";
		int max = 0;

		for (int i = 0; i < times; i++) {
			int currentRoll = explodeDice(1, sides);
			rolls += currentRoll + ", ";
			if (currentRoll > max)
				max = currentRoll;
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
		return dropDice(times, sides, times - keep);
	}

	public static int dropDice(int times, int sides, int drop) throws EvaluationException {
		if (times - drop <= 0)
			throw new EvaluationException("You cannot drop more dice than you roll");

		RunData runData = RunData.getCurrent();

		int[] values = runData.randomInts(times, sides);

		Arrays.sort(values);

		int result = 0;
		for (int i = drop; i < times; i++) {
			result += values[i];
		}

		return result;
	}

	public static int dropDiceLowest(int times, int sides, int drop) throws EvaluationException {
		if (times - drop <= 0)
			throw new EvaluationException("You cannot drop more dice than you roll");

		RunData runData = RunData.getCurrent();

		int[] values = runData.randomInts(times, sides);

		Arrays.sort(values);

		int result = 0;
		for (int i = drop; i < times; i++) {
			result += values[i];
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

	public static int explodeDice(int times, int sides) throws EvaluationException {
		int result = 0;

		if (sides == 0 || sides == 1)
			throw new EvaluationException("Number of sides must be > 1");

		RunData runData = RunData.getCurrent();

		for (int i = 0; i < times; i++) {
			int roll = runData.randomInt(sides);
			if (roll == sides)
				times++;
			result += roll;
		}

		return result;
	}

	public static int countSuccessDice(int times, int sides, int success) {
		RunData runData = RunData.getCurrent();

		int result = 0;
		for (int value : runData.randomInts(times, sides)) {
			if (value >= success)
				result++;
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

			if (value >= success)
				hitCount++;

			if (value == 1)
				oneCount++;

			if (value == 6 && explode)
				times++;

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
			result += Math.min(Math.max(rollDice(1, sides) - sub, lower), upper);
		}

		return result;
	}

}
