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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * JUnit test suite for
 * {@link net.rptools.maptool.client.utilities.RandomSuffixFactory}.
 *
 * @author Alexander "d4rkAlf" Johansson Werne
 */
public class RandomSuffixFactoryTest extends TestCase {
	RandomSuffixFactory rsf;

	@Override
	@Before
	public void setUp() throws Exception {
		rsf = new RandomSuffixFactory();
	}

	@Override
	@After
	public void tearDown() throws Exception {
		rsf = null;
	}

	@Test
	public void testGetASuffix() {
		int s = rsf.nextSuffixForToken("d4rkAlf");
		assertTrue(1 <= s && s < 257);
	}

	@Test
	public void testGetAllSuffixes() {
		RandomSuffixFactory testRSF = new RandomSuffixFactory();
		boolean[] bytes = new boolean[256];
		Arrays.fill(bytes, false);
		for (int i = 0; i < bytes.length; i++) {
			int suffix = testRSF.nextSuffixForToken("Test");
			bytes[suffix - 1] = true;
		}
		for (int i = 0; i < 256; i++) {
			assertTrue("Suffix number " + i + " is missing", bytes[i]);
		}
	}

	@Test
	public void testGetMoreThan256Suffixes() {
		int max = 0;
		for (int i = 0; i < 512; i++) {
			int suffix = rsf.nextSuffixForToken("Too many monsters!");
			max = Math.max(suffix, max);
		}

		assertTrue(max > 256);
	}

	@Test
	public void testThatSuffixesAreUnique() {
		boolean[] uniqueSuffixes = new boolean[10_000_000];
		for (int i = 1; i < uniqueSuffixes.length + 1; i++) {
			int suffix = rsf.nextSuffixForToken("Special Snowflake");
			if (uniqueSuffixes[i - 1] != true) {
				uniqueSuffixes[i - 1] = true;
			} else {
				fail("The suffix '" + suffix + "' was generated twice");
			}
		}
	}
}
