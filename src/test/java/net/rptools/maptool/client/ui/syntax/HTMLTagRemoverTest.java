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
package net.rptools.maptool.client.ui.syntax;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HTMLTagRemoverTest {

  private final HTMLTagRemover remover = new HTMLTagRemover();

  @Test
  void nullRemainsNull() {
    assertEquals(null, remover.remove(null));
  }

  @Test
  void emptyStringRemainsEmpty() {
    assertEquals("", remover.remove(""));
  }

  @Test
  void openTagsAreRemoved() {
    assertEquals("text some text", remover.remove("text <tag>some text"));
  }

  @Test
  void closeTagsAreRemoved() {
    assertEquals("text some text", remover.remove("text </tag>some text"));
  }

  @Test
  void unfinishedTagsAreNotRemoved() {
    assertEquals("text <tag some text", remover.remove("text <tag some text"));
  }

  @Test
  void multipleTagsAreRemoved() {
    assertEquals(
        "text more text even more text",
        remover.remove("text <tag>more text</tag> even more text"));
  }
}
