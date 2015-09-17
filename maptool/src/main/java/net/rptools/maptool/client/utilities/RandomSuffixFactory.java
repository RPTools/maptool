/*
 * This software copyright by various authors including the RPTools.net
 * development team, and licensed under the LGPL Version 3 or, at your option,
 * any later version.
 *
 * Portions of this software were originally covered under the Apache Software
 * License, Version 1.1 or Version 2.0.
 *
 * See the file LICENSE elsewhere in this distribution for license details.
 */

package net.rptools.maptool.client.utilities;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.management.RuntimeErrorException;

import net.rptools.maptool.client.MapTool;

/**
 * This class produces a numeric suffix in the range of 1 to 256 intended to
 * uniquely identify a token on the map. The class remembers previous token
 * names and will never return the same value for any given name. If all
 * possible 256 suffixes have been used then the class simply starts over
 * but using an offset of 256.
 * <p>
 * Internally the class uses bytes (almost) exclusively in order to reduce the
 * memory footprint. In addition, the class has been designed in a manner to
 * reduce running time.
 *
 * @author Alexander "d4rkAlf" Johansson Werne
 */
public class RandomSuffixFactory {

	private static final Random RAND = new Random();
	private static final byte[] ALL_BYTE_VALUES;
	private final Map<String, SuffixGenerator> tokenSuffixMap;

	static {
		ALL_BYTE_VALUES = new byte[256];
		int suffixNbr = Byte.MIN_VALUE;
		do {
			ALL_BYTE_VALUES[suffixNbr & 0xFF] = (byte) suffixNbr;
			suffixNbr++;
		} while (suffixNbr <= Byte.MAX_VALUE);
	}

	/**
	 */
	public RandomSuffixFactory() {
		this.tokenSuffixMap = new HashMap<String, SuffixGenerator>();
	}

	/**
	 * Returns a unique suffix for any given token name. The first batch will
	 * generate values in the range of 1 to 256. When this first batch has been
	 * used up it will return values in the range of 257 to 512 and so on and so
	 * forth.
	 *
	 * @param tokenName is used to check if a suffix has already been generated for
	 *            that name
	 * @return a unique suffix for the given token name
	 */
	public int nextSuffixForToken(String tokenName) {
		SuffixGenerator rs;
		if (tokenSuffixMap.containsKey(tokenName)) {
			rs = tokenSuffixMap.get(tokenName);
		} else {
			rs = new SuffixGenerator(tokenName);
		}
		return rs.nextSuffix();
	}

	/**
	 * This inner class is used to encapsulate an array containing all possible
	 * suffix values in random order, an index and an offset.
	 */
	private class SuffixGenerator {
		private byte[] suffixes;
		private short index;
		private int offset;

		private SuffixGenerator(String tokenName) {
			index = 0;
			offset = 0;
			initializeAndShuffle();
			tokenSuffixMap.put(tokenName, this);
		}

		/**
		 * Simultaneously initialize and shuffle the suffixes by using the
		 * inside-out Fisher-Yates shuffle.
		 */
		private void initializeAndShuffle() {
			suffixes = new byte[256];
			for (int i = 0; i < ALL_BYTE_VALUES.length; i++) {
				int j = RAND.nextInt(i + 1);
				if (j != i) {
					suffixes[i] = suffixes[j];
				}
				suffixes[j] = ALL_BYTE_VALUES[i];
			}
		}

		/**
		 * Gives out the next random suffix in order and increments the index.
		 *
		 * @return a value between 1 and 256
		 */
		private int nextSuffix() {
			int unsignedByte = suffixes[index] & 0xFF;

			int suffix = unsignedByte + 1;
			suffix += (256 * (offset));

			if (index < 255) {
				index++;
			} else {
				index = 0;
				offset++;
			}
			return suffix;
		}
	}
}
