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
package net.rptools.maptool.client.utilities;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * JUnit test suite for {@link net.rptools.maptool.client.utilities.RandomSuffixFactory}.
 *
 * @author Alexander "d4rkAlf" Johansson Werne
 */
class RandomSuffixFactoryTest {
  RandomSuffixFactory rsf;

  @BeforeEach
  void setUp() throws Exception {
    rsf = new RandomSuffixFactory();
  }

  @AfterEach
  void tearDown() throws Exception {
    rsf = null;
  }

  @Test
  @DisplayName("Test getting a Random Suffix.")
  void testGetASuffix() {
    int s = rsf.nextSuffixForToken("d4rkAlf");
    assertTrue(1 <= s && s < 257);
  }

  @Test
  @DisplayName("Test Getting all Suffixes.")
  void testGetAllSuffixes() {
    RandomSuffixFactory testRSF = new RandomSuffixFactory();
    boolean[] bytes = new boolean[256];
    Arrays.fill(bytes, false);
    for (int i = 0; i < bytes.length; i++) {
      int suffix = testRSF.nextSuffixForToken("Test");
      bytes[suffix - 1] = true;
    }
    for (int i = 0; i < 256; i++) {
      assertTrue(bytes[i], "Suffix number " + i + " is missing");
    }
  }

  @Test
  @DisplayName("Test More then 256 suffixes")
  void testGetMoreThan256Suffixes() {
    int max = 0;
    for (int i = 0; i < 512; i++) {
      int suffix = rsf.nextSuffixForToken("Too many monsters!");
      max = Math.max(suffix, max);
    }

    assertTrue(max > 256);
  }

  @Test
  @DisplayName("Test that the suffixes are unique")
  void testThatSuffixesAreUnique() {
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
