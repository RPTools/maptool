/**
 * 
 */
package net.rptools.tokentool;

import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.IOException;

import javax.swing.ImageIcon;

import net.rptools.tokentool.util.ImageSelection;

/**
 * @author bdornauf
 */
public class AppCopyPaste {

	public AppCopyPaste() {
	}

	/**
	 * If an image is on the system clipboard, this method returns it; otherwise it returns null.
	 * 
	 * @return image or null
	 */
	public static Image getClipboardImage() {
		Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);

		try {
			if (t != null && t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				File f = (File) t.getTransferData(DataFlavor.javaFileListFlavor);
				System.out.println("Got a file in the clipboard! Filename=" + f.getAbsolutePath());
			}
		} catch (UnsupportedFlavorException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			if (t != null && t.isDataFlavorSupported(DataFlavor.imageFlavor)) {
				Image text = (Image) t.getTransferData(DataFlavor.imageFlavor);
				return text;
			}
		} catch (UnsupportedFlavorException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			if (t != null && t.isDataFlavorSupported(DataFlavor.imageFlavor)) {
				Image text = (Image) t.getTransferData(DataFlavor.imageFlavor);
				return text;
			}
		} catch (UnsupportedFlavorException e) {
		} catch (IOException e) {
		}
		return null;
	}

	public static BufferedImage getClipboardBufferedImage() {
		Image tmp = getClipboardImage();
		if (tmp != null)
			return toBufferedImage(tmp);
		else
			return null;
	}

	/**
	 * This method returns a buffered image with the contents of an image http://www.exampledepot.com/egs/java.awt.image/Image2Buf.html
	 * 
	 * @param image
	 * @return
	 */
	public static BufferedImage toBufferedImage(Image image) {
		if (image instanceof BufferedImage) {
			return (BufferedImage) image;
		}

		// This code ensures that all the pixels in the image are loaded
		image = new ImageIcon(image).getImage();

		// Determine if the image has transparent pixels; for this method's
		// implementation, see Determining If an Image Has Transparent Pixels
		boolean hasAlpha = hasAlpha(image);

		// Create a buffered image with a format that's compatible with the screen
		BufferedImage bimage = null;
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		try {
			// Determine the type of transparency of the new buffered image
			int transparency = Transparency.OPAQUE;
			if (hasAlpha) {
				transparency = Transparency.BITMASK;
			}

			// Create the buffered image
			GraphicsDevice gs = ge.getDefaultScreenDevice();
			GraphicsConfiguration gc = gs.getDefaultConfiguration();
			bimage = gc.createCompatibleImage(image.getWidth(null), image.getHeight(null), transparency);
		} catch (HeadlessException e) {
			// The system does not have a screen
		}

		if (bimage == null) {
			// Create a buffered image using the default color model
			int type = BufferedImage.TYPE_INT_RGB;
			if (hasAlpha) {
				type = BufferedImage.TYPE_INT_ARGB;
			}
			bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
		}

		// Copy image to buffered image
		Graphics g = bimage.createGraphics();

		// Paint the image onto the buffered image
		g.drawImage(image, 0, 0, null);
		g.dispose();

		return bimage;
	}

	/**
	 * This method returns true if the specified image has transparent pixels
	 * 
	 * http://www.exampledepot.com/egs/java.awt.image/HasAlpha.html
	 * 
	 * @param image
	 * @return
	 */
	private static boolean hasAlpha(Image image) {
		// If buffered image, the color model is readily available
		if (image instanceof BufferedImage) {
			BufferedImage bimage = (BufferedImage) image;
			return bimage.getColorModel().hasAlpha();
		}

		// Use a pixel grabber to retrieve the image's color model;
		// grabbing a single pixel is usually sufficient
		PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
		}

		// Get the image's color model
		ColorModel cm = pg.getColorModel();
		return cm.hasAlpha();
	}

	// This method writes a image to the system clipboard.
	// otherwise it returns null.
	public static void setClipboard(Image image) {
		if (image != null) {
			ImageSelection.copyImageToClipboard(image);
		}
	}

	/**
	 * @author cif
	 * @param composedToken
	 */
	public static void setClipboardBufferedImage(BufferedImage composedToken) {
		// System.out.println("setClipboardBufferedImage called.");
		if (composedToken != null) {
			ImageSelection.copyImageToClipboard(toImage(composedToken));
		}
	}

	// This method returns an Image object from a buffered image
	private static Image toImage(BufferedImage bufferedImage) {
		// System.out.println("toImage called.");
		return Toolkit.getDefaultToolkit().createImage(bufferedImage.getSource());
	}

}
