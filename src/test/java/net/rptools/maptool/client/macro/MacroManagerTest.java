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
package net.rptools.maptool.client.macro;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MacroManagerTest {

  @Test
  @DisplayName("Test Split function in StringUtil")
  void testSplit() throws Exception {

    assertEquals(0, MacroManager.split("").size());

    compare(MacroManager.split("one"), "one");
    compare(MacroManager.split(" one"), "one");
    compare(MacroManager.split("one "), "one");
    compare(MacroManager.split(" one "), "one");

    compare(MacroManager.split("one two"), "one", "two");
    compare(MacroManager.split("one two three"), "one", "two", "three");
    compare(MacroManager.split("  one   two   three  "), "one", "two", "three");

    compare(MacroManager.split("\"one\""), "one");
    compare(MacroManager.split("\"one two\""), "one two");
    compare(MacroManager.split("\"one two\" three"), "one two", "three");

    compare(MacroManager.split("\"one \\\"two\\\"\" three"), "one \"two\"", "three");
  }

  @Test
  @DisplayName("Test Perform Substitution in StringUtil.")
  void testPerformSubstitution() throws Exception {

    compare("", "", "");
    compare("one", "one", "one");

    compare("one $1", "one", "one one");
    compare("one $2 $1", "one two", "one two one");

    compare("one ${1}", "one", "one one");
    compare("one ${2} ${1}", "one two", "one two one");
  }

  private void compare(String text, String details, String result) {

    String subResult = MacroManager.performSubstitution(text, details);
    assertEquals(result, subResult, "\"" + subResult + "\" != \"" + result + "\"");
  }

  private void compare(List<String> parsed, String... expected) {
    if (parsed.size() != expected.length) {
      fail("Sizes do not match:" + parsed);
    }

    for (int i = 0; i < parsed.size(); i++) {
      if (!parsed.get(i).equals(expected[i])) {
        fail("Does not match: " + parsed.get(i) + " != " + expected[i]);
      }
    }
  }
}
