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
package net.rptools.lib.swing.preference.net.rptools.lib.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import net.rptools.lib.io.PackedFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class PackedFileTest {

  public static final String A_PATH_TXT = "a_path.txt";
  public static final String PACKED_TEST_FILE = "packedTestFile";
  private static int counter = 1;

  @Test
  public void emptySave(@TempDir File tempDir) throws IOException {
    File f = new File(tempDir, PACKED_TEST_FILE);
    PackedFile pf = new PackedFile(f);
    pf.save();
    assertTrue(f.exists());
  }

  @Test
  public void saveWithOneResource(@TempDir File tempDir) throws IOException {
    File f = new File(tempDir, PACKED_TEST_FILE);
    String test_content = "some content";
    try (PackedFile pf = new PackedFile(f)) {
      pf.putFile(A_PATH_TXT, test_content.getBytes());
      pf.save();
    }

    try (PackedFile loaded = new PackedFile(f)) {
      InputStream is = loaded.getFileAsInputStream(A_PATH_TXT);
      String s = new String(is.readAllBytes());
      assertEquals(test_content, s);
    }
  }
}
