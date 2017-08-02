/*
 * This software Copyright by the RPTools.net development team, and licensed under the Affero GPL Version 3 or, at your option, any later version.
 *
 * MapTool Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License * along with this source Code. If not, please visit <http://www.gnu.org/licenses/> and specifically the Affero license text
 * at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.tokentool.util;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;

/**
 * A Transferable able to transfer an AWT Image. Similar to the JDK StringSelection class.
 * 
 * according to this tutorial http://elliotth.blogspot.com/2005/09/copying-images-to-clipboard-with-java.html
 */

public class ImageSelection implements Transferable {
	private Image image;

	public static void copyImageToClipboard(Image image) {
		// Work around a Sun bug that causes a hang in "sun.awt.image.ImageRepresentation.reconstruct".
		new javax.swing.ImageIcon(image); // Force load.
		BufferedImage newImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		newImage.createGraphics().drawImage(image, 0, 0, null);
		image = newImage;

		ImageSelection imageSelection = new ImageSelection(image);
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		toolkit.getSystemClipboard().setContents(imageSelection, null);
	}

	public ImageSelection(Image image) {
		this.image = image;
	}

	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
		if (flavor.equals(DataFlavor.imageFlavor) == false) {
			throw new UnsupportedFlavorException(flavor);
		}
		return image;
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor.equals(DataFlavor.imageFlavor);
	}

	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] {
				DataFlavor.imageFlavor
		};
	}
}
