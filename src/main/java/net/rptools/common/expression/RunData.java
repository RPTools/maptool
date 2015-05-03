/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package net.rptools.common.expression;

import java.security.SecureRandom;
import java.util.Random;

public class RunData {
	private static ThreadLocal<RunData> current = new ThreadLocal<RunData>(); 
	public static Random RANDOM = new SecureRandom();
	
	private final Result result;
	
	private long randomValue;
	private long randomMax;
	private long randomMin;
	
	public RunData(Result result) {
		this.result = result;
	}
	
	/**
	 * Returns a random integer between 1 and <code>maxValue</code>
	 */
	public int randomInt(int maxValue) {
		return randomInt(1, maxValue);
	}
	
	/**
	 * Returns a list of random integers between 1 and <code>maxValue</code>
	 */
	public int[] randomInts(int num, int maxValue) {
		int[] ret = new int[num];
		for (int i = 0; i < num; i++)
			ret[i] = randomInt(maxValue);
		return ret;
	}

	/**
	 * Returns a random integer between <code>minValue</code> and <code>maxValue</code>
	 */
	public int randomInt(int minValue, int maxValue) {
		randomMin += minValue;
		randomMax += maxValue;
		
		int result = RANDOM.nextInt(maxValue - minValue + 1) + minValue;
		
		randomValue += result;
		
		return result;
	}

	/**
	 * Returns a list of random integers between <code>minValue</code> and <code>maxValue</code>
	 * @return
	 */
	public int[] randomInts(int num, int minValue, int maxValue) {
		int[] ret = new int[num];
		for (int i = 0; i < num; i++)
			ret[i] = randomInt(minValue, maxValue);
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
}
