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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import net.rptools.maptool.util.library.LibraryUtils;
import org.junit.jupiter.api.Test;

public class LibraryUtilsTest {
  @Test
  public void testParseLibraryListSimple() {
    var result = LibraryUtils.parseLibraryList("name|path|100");

    assertEquals(1, result.size());

    var library = result.getFirst();
    assertEquals("name", library.name());
    assertEquals("https://library.rptools.net/1.3/path", library.location().toString());
    assertEquals(100L, library.size());
  }

  @Test
  public void testParseLibraryListHandlesLargeFiles() {
    // The asset size in this case is above Integer.MAX_VALUE.
    var result = LibraryUtils.parseLibraryList("name|path|3000000000");

    assertEquals(1, result.size());

    var library = result.getFirst();
    assertEquals("name", library.name());
    assertEquals("https://library.rptools.net/1.3/path", library.location().toString());
    assertEquals(3000000000L, library.size());
  }

  @Test
  public void testParseLibraryListHandlesEmptyName() {
    var result = LibraryUtils.parseLibraryList("|path|100");

    assertEquals(0, result.size());
  }

  @Test
  public void testParseLibraryListHandlesEmptyPath() {
    var result = LibraryUtils.parseLibraryList("name||100");

    assertEquals(0, result.size());
  }

  @Test
  public void testParseLibraryListHandlesInvalidURL() {
    // '^' is not a valid character for a URL path.
    var result = LibraryUtils.parseLibraryList("name|p^th|100");

    assertEquals(0, result.size());
  }

  @Test
  public void testParseLibraryListHandlesEmptySize() {
    var result = LibraryUtils.parseLibraryList("name|path|");

    assertEquals(0, result.size());
  }

  @Test
  public void testParseLibraryListHandlesMissingAuthor() {
    var result = LibraryUtils.parseLibraryList("name|path|100");

    assertEquals(1, result.size());

    var library = result.getFirst();
    assertEquals("name", library.name());
    assertEquals("https://library.rptools.net/1.3/path", library.location().toString());
    assertEquals(100L, library.size());
    assertNull(library.author());
  }

  @Test
  public void testParseLibraryListHandlesEmptyAuthor() {
    var result = LibraryUtils.parseLibraryList("name|path|100|");

    assertEquals(1, result.size());

    var library = result.getFirst();
    assertEquals("name", library.name());
    assertEquals("https://library.rptools.net/1.3/path", library.location().toString());
    assertEquals(100L, library.size());
    assertNull(library.author());
  }

  @Test
  public void testParseLibraryListHandlesAuthor() {
    var result = LibraryUtils.parseLibraryList("name|path|100|an author");

    assertEquals(1, result.size());

    var library = result.getFirst();
    assertEquals("name", library.name());
    assertEquals("https://library.rptools.net/1.3/path", library.location().toString());
    assertEquals(100L, library.size());
    assertEquals("an author", library.author());
  }

  @Test
  public void testParseLibraryListHandlesRealisticData() {
    // Tests various things: empty line elision; support for arbitrary characters.
    var result =
        LibraryUtils.parseLibraryList(
            """
        Somebody's Art|artpacks/art_ifact.zip|81405643
        Pack by Random Guy|artpacks/thisisapack.zip|12345678

        Charåcter … test 🔒|artpacks/chår.zip|547578329
        """);

    assertEquals(3, result.size());

    {
      var library = result.get(0);
      assertEquals("Somebody's Art", library.name());
      assertEquals(
          "https://library.rptools.net/1.3/artpacks/art_ifact.zip", library.location().toString());
      assertEquals(81405643L, library.size());
    }
    {
      var library = result.get(1);
      assertEquals("Pack by Random Guy", library.name());
      assertEquals(
          "https://library.rptools.net/1.3/artpacks/thisisapack.zip",
          library.location().toString());
      assertEquals(12345678L, library.size());
    }
    {
      var library = result.get(2);
      assertEquals("Charåcter … test 🔒", library.name());
      assertEquals(
          "https://library.rptools.net/1.3/artpacks/chår.zip", library.location().toString());
      assertEquals(547578329L, library.size());
    }
  }
}
