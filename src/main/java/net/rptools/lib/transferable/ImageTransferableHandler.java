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
package net.rptools.lib.transferable;

import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import net.rptools.lib.image.ImageUtil;

public class ImageTransferableHandler extends TransferableHandler {
  private enum Flavor {
    image(new DataFlavor("image/x-java-image; class=java.awt.Image", "Image")),
    url(new DataFlavor("text/plain; class=java.lang.String", "Image"));

    DataFlavor flavor;

    private Flavor(DataFlavor flavor) {
      this.flavor = flavor;
    }

    public DataFlavor getFlavor() {
      return flavor;
    }
  }

  @Override
  public Image getTransferObject(Transferable transferable)
      throws IOException, UnsupportedFlavorException {
    if (transferable.isDataFlavorSupported(Flavor.image.getFlavor())) {
      Image image = (Image) transferable.getTransferData(Flavor.image.getFlavor());

      if (!(image instanceof BufferedImage)) {
        image = ImageUtil.createCompatibleImage(image);
      }
      return image;
    }

    if (transferable.isDataFlavorSupported(Flavor.url.getFlavor())) {
      String urlStr = (String) transferable.getTransferData(Flavor.url.getFlavor());

      try {
        URL url = new URL(urlStr);
        Image image = null;
        try {
          image = ImageIO.read(url);
        } catch (Exception e) {
          // try the old fasioned way
          image = Toolkit.getDefaultToolkit().getImage(url);
          MediaTracker mt = new MediaTracker(new JPanel());
          mt.addImage(image, 0);
          try {
            mt.waitForID(0);
          } catch (InterruptedException ie) {
            ie.printStackTrace();
          }
        }
        if (!(image instanceof BufferedImage)) {
          image = ImageUtil.createCompatibleImage(image);
        }
        return image;
      } catch (MalformedURLException mue) {
        // TODO: this can probably be ignored
        mue.printStackTrace();
      }
    }
    if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
      @SuppressWarnings("unchecked")
      List<File> fileList =
          (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
      return ImageUtil.getImage(fileList.get(0));
    }
    throw new UnsupportedFlavorException(null);
  }
}
