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
package net.rptools.maptool.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import org.apache.commons.io.IOUtils;

public class WebDownloader {
  private final URL url;

  public WebDownloader(URL url) {
    if (url == null) {
      throw new IllegalArgumentException("URL cannot be null");
    }
    this.url = url;
  }

  /**
   * Read the data at the given URL. This method should not be called on the EDT.
   *
   * @return File pointer to the location of the data, file will be deleted at program end
   * @throws IOException if error while reading
   */
  public String read() throws IOException {
    URLConnection conn = url.openConnection();

    conn.setConnectTimeout(5000);
    conn.setReadTimeout(5000);

    // Send the request.
    conn.connect();

    InputStream in = null;
    ByteArrayOutputStream out = null;
    try {
      in = conn.getInputStream();
      out = new ByteArrayOutputStream();

      int buflen = 1024 * 30;
      int bytesRead = 0;
      byte[] buf = new byte[buflen];

      for (int nRead = in.read(buf); nRead != -1; nRead = in.read(buf)) {
        bytesRead += nRead;
        out.write(buf, 0, nRead);
      }
    } finally {
      IOUtils.closeQuietly(in);
      IOUtils.closeQuietly(out);
    }
    return out != null ? new String(out.toByteArray()) : null;
  }

  public static void main(String[] args) throws Exception {
    WebDownloader downloader =
        new WebDownloader(new URL("http://library.rptools.net/1.3/listArtPacks"));
    String result = downloader.read();
    System.out.println(result);
  }
}
