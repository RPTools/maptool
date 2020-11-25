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
package net.rptools.maptool.client.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import javax.swing.*;
import net.rptools.maptool.client.WebDownloader;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

public class AddResourceDialogTest {

  private final JList<LibraryRow> testList = new JList<>();

  @Test
  public void testDownloadListWorker_doInBackground_oneNewEntry() throws Exception {
    WebDownloader downloader = createDownloaderReturning("name|path|100");
    DownloadListWorker worker = new DownloadListWorker(testList, downloader, new HashSet<>());

    worker.doInBackground();
    worker.done();

    assertEquals(1, testList.getModel().getSize());
    assertLibraryRowEquals("name", "path", 100, testList.getModel().getElementAt(0));
  }

  @Test
  public void testDownloadListWorker_doInBackground_entryAlreadyPresent() throws Exception {
    WebDownloader downloader = createDownloaderReturning("name|path|100");
    Set<File> assetRoots = new HashSet<>();
    assetRoots.add(new File("name"));
    DownloadListWorker worker = new DownloadListWorker(testList, downloader, assetRoots);

    worker.doInBackground();
    worker.done();

    assertEquals(0, testList.getModel().getSize());
  }

  @Test
  public void testDownloadListWorker_doInBackground_multipleEntriesAreOrdered() throws Exception {
    WebDownloader downloader = createDownloaderReturning("z_name|path|100\n" + "a_name|path|200");
    Set<File> assetRoots = new HashSet<>();
    DownloadListWorker worker = new DownloadListWorker(testList, downloader, assetRoots);

    worker.doInBackground();
    worker.done();

    assertEquals(2, testList.getModel().getSize());
    assertLibraryRowEquals("a_name", "path", 200, testList.getModel().getElementAt(0));
    assertLibraryRowEquals("z_name", "path", 100, testList.getModel().getElementAt(1));
  }

  @Test
  public void testLibraryRow_constructor_separateArgs() {
    LibraryRow row = createSampleRow("name", "path", 100);

    assertLibraryRowEquals("name", "path", 100, row);
  }

  @Test
  public void testLibraryRow_constructor_separateArgs_spaces() {
    LibraryRow row = createSampleRow(" name ", " path ", 100);

    assertLibraryRowEquals("name", "path", 100, row);
  }

  @Test
  public void testLibraryRow_constructor_string() {
    LibraryRow row = createSampleRow("name|path|100");

    assertLibraryRowEquals("name", "path", 100, row);
  }

  @Test
  public void testLibraryRow_toString() {
    LibraryRow row = createSampleRow("name|path|100");

    assertEquals("<html><b>name</b> <i>(100 bytes)</i>", row.toString());
  }

  @Test
  public void testLibraryRow_getSizeString_bytes() {
    LibraryRow row = createSampleRow("name|path|999");

    assertEquals("<html><b>name</b> <i>(999 bytes)</i>", row.toString());
  }

  @Test
  public void testLibraryRow_getSizeString_kb() {
    LibraryRow row = createSampleRow("name|path|999999");

    assertEquals("<html><b>name</b> <i>(999 k)</i>", row.toString());
  }

  @Test
  public void testLibraryRow_getSizeString_mb() {
    LibraryRow row = createSampleRow("name|path|1000000");

    assertEquals("<html><b>name</b> <i>(1 mb)</i>", row.toString());
  }

  @Test
  public void testMessageListModel_getSize_alwaysOne() {
    MessageListModel model = new MessageListModel("a_message");

    assertEquals(1, model.getSize());
  }

  @Test
  public void testMessageListModel_getElementAt_alwaysMessage() {
    MessageListModel model = new MessageListModel("a_message");

    assertEquals("a_message", model.getElementAt(0));
    assertEquals("a_message", model.getElementAt(1));
    assertEquals("a_message", model.getElementAt(100));
  }

  private void assertLibraryRowEquals(String name, String path, int size, LibraryRow actual) {
    assertEquals(name, actual.name);
    assertEquals(path, actual.path);
    assertEquals(size, actual.size);
  }

  @NotNull
  private LibraryRow createSampleRow(String name, String path, int size) {
    return new LibraryRow(name, path, size);
  }

  @NotNull
  private LibraryRow createSampleRow(String line) {
    return new LibraryRow(line);
  }

  private WebDownloader createDownloaderReturning(String lines) throws MalformedURLException {
    return new WebDownloader(new URL("http://localhost/something")) {
      @Override
      public String read() throws IOException {
        return lines;
      }
    };
  }
}
