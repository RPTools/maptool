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
package net.rptools.maptool.model.library.url;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import net.rptools.maptool.model.library.Library;
import net.rptools.maptool.model.library.LibraryManager;

public class LibraryURLConnection extends URLConnection {

  /**
   * Constructs a URL connection to the specified URL.
   *
   * @param url the specified URL.
   */
  LibraryURLConnection(URL url) {
    super(url);
  }

  @Override
  public void connect() throws IOException {
    // Nothing to do
  }

  @Override
  public InputStream getInputStream() throws IOException {
    try {
      Optional<Library> libraryOpt = new LibraryManager().getLibrary(url).get();
      if (libraryOpt.isEmpty()) {
        throw new IOException("Unable to read location " + url.toExternalForm());
      }

      var library = libraryOpt.get();
      return library.read(url).get();
    } catch (InterruptedException | ExecutionException e) {
      throw new IOException(e);
    }
  }
}
