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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import net.rptools.lib.image.ImageUtil;
import net.rptools.maptool.util.ImageManager;

/**
 * Support "asset://" in Swing components
 *
 * @author Azhrei
 */
public class AssetURLStreamHandler extends URLStreamHandler {

  @Override
  protected URLConnection openConnection(URL u) {
    return new AssetURLConnection(u);
  }

  private static class AssetURLConnection extends URLConnection {

    public AssetURLConnection(URL url) {
      super(url);
    }

    @Override
    public void connect() {
      // Nothing to do
    }

    @Override
    public InputStream getInputStream() throws IOException {
      BufferedImage img = ImageManager.getImageFromUrl(url);
      return new ByteArrayInputStream(ImageUtil.imageToBytes(img, "png"));
    }
  }
}
