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
package net.rptools.maptool.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StringUtilTest {

  @Test
  @DisplayName("Test of countOccurances in StringUtil")
  void testCountOccurances() throws Exception {

    String str = "<div>";

    assertEquals(0, StringUtil.countOccurances("", str));
    assertEquals(1, StringUtil.countOccurances("<div>", str));
    assertEquals(1, StringUtil.countOccurances("one<div>two", str));
    assertEquals(2, StringUtil.countOccurances("one<div>two<div>three", str));
    assertEquals(3, StringUtil.countOccurances("one<div>two<div>three<div>", str));
  }
}
