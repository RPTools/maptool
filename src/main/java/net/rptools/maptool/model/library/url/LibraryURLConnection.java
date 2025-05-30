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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import net.rptools.maptool.client.ui.htmlframe.content.HTMLContent;

/**
 * This class is a URL connection that fetches HTML content from a specified URL for library
 * resources.
 */
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
    var content = HTMLContent.fromURL(url).injectJSAPI().InjectContentSecurityPolicy();
    var query = url.getQuery();
    if (query != null && query.toLowerCase().contains("mtvuefix=true")) {
      content = content.injectVueHack();
    }

    content = content.preprocess();
    if (content.isBinaryAsset()) {
      return content.getAsset().getDataAsInputStream();
    } else {
      return new ByteArrayInputStream(content.getHtmlString().getBytes());
    }
  }
}
