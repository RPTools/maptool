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
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class ImageTransferable implements Transferable {

  public static final DataFlavor FLAVOR =
      new DataFlavor("image/x-java-image; class=java.awt.Image", "Image");

  private Image image;

  public ImageTransferable(Image image) {
    this.image = image;
  }

  public DataFlavor[] getTransferDataFlavors() {
    return new DataFlavor[] {FLAVOR};
  }

  public boolean isDataFlavorSupported(DataFlavor flavor) {
    return flavor.equals(FLAVOR);
  }

  public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {

    if (!flavor.equals(FLAVOR)) {
      throw new UnsupportedFlavorException(flavor);
    }

    return image;
  }
}
