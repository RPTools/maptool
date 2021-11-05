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
package net.rptools.maptool.client.swing;

import java.awt.Image;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import net.rptools.lib.image.ImageUtil;
import net.rptools.maptool.util.ImageManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ImageLoaderCache {

  private static final Logger log = LogManager.getLogger(ImageLoaderCache.class);

  private final Map<String, Image> imageMap = new HashMap<String, Image>();

  public void flush() {
    imageMap.clear();
  }

  public Image get(URL url, ImageObserver... observers) {
    // URLs take a huge amount of time in equals(), so simplify by
    // converting to a string
    if (url == null) {
      log.debug("ImageLoaderCache.get(null), using BROKEN_IMAGE");
      return ImageManager.BROKEN_IMAGE;
    }
    Image image = imageMap.get(url.toString());
    if (image == null) {
      String protocol = url.getProtocol();
      String path = url.getHost() + url.getPath();

      if ("cp".equals(protocol)) {
        try {
          image = ImageUtil.getImage(path);
        } catch (IOException ioe) {
          log.debug("ImageLoaderCache.get(" + url.toString() + "), using BROKEN_IMAGE", ioe);
          return ImageManager.BROKEN_IMAGE;
        }
      } else if ("asset".equals(protocol)) {
        image = ImageManager.getImageFromUrl(url);
      } else {
        try {
          image = ImageIO.read(url);
        } catch (IOException e) {
          log.error("Unable to load image " + url, e);
        }
      }
      imageMap.put(url.toString(), image);
    }
    return image;
  }
}
