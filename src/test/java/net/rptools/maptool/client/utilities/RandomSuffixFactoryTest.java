/*
 * This software Copyright by the RPTools.net development team, and licensed under the Affero GPL Version 3 or, at your option, any later version.
 *
 * MapTool Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License * along with this source Code. If not, please visit <http://www.gnu.org/licenses/> and specifically the Affero license text
 * at <http://www.gnu.org/licenses/agpl.html>.
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
 * JUnit test suite for {@link net.rptools.maptool.client.utilities.RandomSuffixFactory}.
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
