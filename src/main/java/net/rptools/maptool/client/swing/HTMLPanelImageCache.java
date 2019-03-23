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

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import net.rptools.lib.MD5Key;
import net.rptools.lib.image.ImageUtil;
import net.rptools.lib.swing.SwingUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.util.ImageManager;

public class HTMLPanelImageCache extends Dictionary<URL, Image> {

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
          MapTool.showWarning("Can't find 'cp://" + key.toString() + "' in image cache?!", ioe);
        }
      } else if ("asset".equals(protocol)) {
        // Look for size request
        int index = path.indexOf("-");
        int size = -1;
        if (index >= 0) {
          String szStr = path.substring(index + 1);
          path = path.substring(0, index);
          size = Integer.parseInt(szStr);
        }
        image = ImageManager.getImageAndWait(new MD5Key(path));

        if (size > 0) {
          Dimension sz = new Dimension(image.getWidth(null), image.getHeight(null));
          SwingUtil.constrainTo(sz, size);

          BufferedImage img =
              new BufferedImage(sz.width, sz.height, ImageUtil.pickBestTransparency(image));
          Graphics2D g = img.createGraphics();
          g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
          g.drawImage(image, 0, 0, sz.width, sz.height, null);
          g.dispose();

          image = img;
        }
      } else {
        // Normal method
        image = Toolkit.getDefaultToolkit().createImage(url);
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
