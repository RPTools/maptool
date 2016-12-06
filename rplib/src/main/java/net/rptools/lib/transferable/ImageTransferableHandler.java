/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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
		image(new DataFlavor("image/x-java-image; class=java.awt.Image", "Image")), url(new DataFlavor("text/plain; class=java.lang.String", "Image"));

		DataFlavor flavor;

		private Flavor(DataFlavor flavor) {
			this.flavor = flavor;
		}

		public DataFlavor getFlavor() {
			return flavor;
		}
	}

	@Override
	public Image getTransferObject(Transferable transferable) throws IOException, UnsupportedFlavorException {
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
			List<File> fileList = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
			return ImageUtil.getImage(fileList.get(0));
		}
		throw new UnsupportedFlavorException(null);
	}
}
