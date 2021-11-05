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
import java.io.IOException;
import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import net.rptools.lib.image.ImageUtil;
import net.rptools.maptool.util.ImageManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HTMLPanelImageCache extends Dictionary<URL, Image> {

  private static final Logger log = LogManager.getLogger();

  private final Map<String, Image> imageMap = new HashMap<String, Image>();

  public void flush() {
    imageMap.clear();
  }

  @Override
  public Enumeration elements() {
    // Not used
    return null;
  }

  @Override
  public Image get(Object key) {
    URL url = (URL) key;

    // URLs take a huge amount of time in equals(), so simplify by
    // converting to a string
    Image image = imageMap.get(url.toString());
    if (image == null) {

      String protocol = url.getProtocol();
      String path = url.getHost() + url.getPath();

      if ("cp".equals(protocol)) {
        try {
          image = ImageUtil.getImage(path);
        } catch (IOException ioe) {
          log.debug("HTMLPanelImageCache.get(" + url.toString() + "), using BROKEN_IMAGE", ioe);
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

  @Override
  public boolean isEmpty() {
    // Not used
    return false;
  }

  @Override
  public Enumeration keys() {
    // Not used
    return null;
  }

  @Override
  public Image put(URL key, Image value) {
    // Not used
    return null;
  }

  @Override
  public Image remove(Object key) {
    // Not used
    return null;
  }

  @Override
  public int size() {
    // Not used
    return 0;
  }
}
