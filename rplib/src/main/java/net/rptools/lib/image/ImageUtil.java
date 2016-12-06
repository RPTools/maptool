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
package net.rptools.lib.image;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import org.apache.commons.io.FileUtils;

/**
 * 
 * @author trevor
 */
public class ImageUtil {

	public static final String HINT_TRANSPARENCY = "hintTransparency";

	// TODO: perhaps look at reintroducing this later
	//private static GraphicsConfiguration graphicsConfig = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

	public static final FilenameFilter SUPPORTED_IMAGE_FILE_FILTER = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			name = name.toLowerCase();
			//			TODO: FJE: When we move to Java 6, use <code>ImageIO.getReaderFileSuffixes()</code> instead
			return name.endsWith(".png") ||
					name.endsWith(".gif") ||
					name.endsWith(".jpg") ||
					name.endsWith(".jpeg") ||
					name.endsWith(".bmp");
		}
	};

	//	public static void setGraphicsConfiguration(GraphicsConfiguration config) {
	//		graphicsConfig = config;
	//	}
	//
	/**
	 * Load the image. Does not create a graphics configuration compatible version.
	 */
	public static Image getImage(File file) throws IOException {
		return bytesToImage(FileUtils.readFileToByteArray(file));
	}

	/**
	 * Load the image in the classpath. Does not create a graphics configuration compatible version.
	 */
	public static Image getImage(String image) throws IOException {
		ByteArrayOutputStream dataStream = new ByteArrayOutputStream(8192);

		int bite;
		InputStream inStream = ImageUtil.class.getClassLoader().getResourceAsStream(image);
		if (inStream == null) {
			throw new IOException("Image not found: " + image);
		}
		inStream = new BufferedInputStream(inStream);
		while ((bite = inStream.read()) >= 0) {
			dataStream.write(bite);
		}
		return bytesToImage(dataStream.toByteArray());
	}

	public static Image getImage(String image, int w, int h) throws IOException {
		return resizeImage(getImage(image), w, h);
	}

	public static BufferedImage getCompatibleImage(String image) throws IOException {
		return getCompatibleImage(image, null);
	}

	public static BufferedImage getCompatibleImage(String image, Map<String, Object> hints) throws IOException {
		return createCompatibleImage(getImage(image), hints);
	}

	/**
	 * Create a copy of the image that is compatible with the current graphics context
	 * 
	 * @param img
	 * @return
	 */
	public static BufferedImage createCompatibleImage(Image img) {
		return createCompatibleImage(img, null);
	}

	public static BufferedImage createCompatibleImage(Image img, Map<String, Object> hints) {
		if (img == null) {
			return null;
		}
		return createCompatibleImage(img, img.getWidth(null), img.getHeight(null), hints);
	}

	public static BufferedImage createCompatibleImage(int width, int height, int transparency) {
		return new BufferedImage(width, height, transparency);
	}

	/**
	 * Create a copy of the image that is compatible with the current graphics context and scaled to the supplied size
	 */
	public static BufferedImage createCompatibleImage(Image img, int width, int height, Map<String, Object> hints) {
		width = Math.max(width, 1);
		height = Math.max(height, 1);

		int transparency;
		if (hints != null && hints.containsKey(HINT_TRANSPARENCY)) {
			transparency = (Integer) hints.get(HINT_TRANSPARENCY);
		} else {
			transparency = pickBestTransparency(img);
		}
		BufferedImage compImg = new BufferedImage(width, height, transparency);

		Graphics2D g = null;
		try {
			g = compImg.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

			g.drawImage(img, 0, 0, width, height, null);
		} finally {
			if (g != null) {
				g.dispose();
			}
		}
		return compImg;
	}

	/**
	 * Look at the image and determine which Transparency is most appropriate. If it finds any translucent pixels it
	 * returns Transparency.TRANSLUCENT, if it finds at least one purely transparent pixel and no translucent pixels it
	 * will return Transparency.BITMASK, in all other cases it returns Transparency.OPAQUE, including errors
	 * 
	 * @param image
	 * @return one of Transparency constants
	 */
	public static int pickBestTransparency(Image image) {
		// Take a shortcut if possible
		if (image instanceof BufferedImage) {
			return pickBestTransparency((BufferedImage) image);
		}

		// Legacy method
		// NOTE: This is a horrible memory hog
		int width = image.getWidth(null);
		int height = image.getHeight(null);
		int[] pixelArray = new int[width * height];
		PixelGrabber pg = new PixelGrabber(image, 0, 0, width, height, pixelArray, 0, width);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
			System.err.println("interrupted waiting for pixels!");
			return Transparency.OPAQUE;
		}
		if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
			System.err.println("image fetch aborted or errored");
			return Transparency.OPAQUE;
		}
		// Look for specific pixels
		boolean foundTransparent = false;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				// Get the next pixel
				int pixel = pixelArray[y * width + x];
				int alpha = (pixel >> 24) & 0xff;

				// Is there translucency or just pure transparency ?
				if (alpha > 0 && alpha < 255) {
					return Transparency.TRANSLUCENT;
				}
				if (alpha == 0 && !foundTransparent) {
					foundTransparent = true;
				}
			}
		}
		return foundTransparent ? Transparency.BITMASK : Transparency.OPAQUE;
	}

	public static int pickBestTransparency(BufferedImage image) {
		// See if we can short circuit
		ColorModel colorModel = image.getColorModel();
		if (colorModel.getTransparency() == Transparency.OPAQUE) {
			return Transparency.OPAQUE;
		}
		// Get the pixels
		int width = image.getWidth();
		int height = image.getHeight();

		// Look for specific pixels
		boolean foundTransparent = false;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				// Get the next pixel
				int pixel = image.getRGB(x, y);
				int alpha = (pixel >> 24) & 0xff;

				// Is there translucency or just pure transparency ?
				if (alpha > 0 && alpha < 255) {
					return Transparency.TRANSLUCENT;
				}
				if (alpha == 0 && !foundTransparent) {
					foundTransparent = true;
				}
			}
		}
		return foundTransparent ? Transparency.BITMASK : Transparency.OPAQUE;
	}

	public static byte[] imageToBytes(BufferedImage image) throws IOException {
		return imageToBytes(image, "jpg");
	}

	public static byte[] imageToBytes(BufferedImage image, String format) throws IOException {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream(10000);
		ImageIO.write(image, format, outStream);

		return outStream.toByteArray();
	}

	private static final JPanel observer = new JPanel();

	/**
	 * Converts a byte array into an {@link Image} instance.
	 * 
	 * @param imageBytes
	 *            bytes to convert
	 * @return
	 * @throws IOException
	 */
	public static Image bytesToImage(byte[] imageBytes) throws IOException {
		if (imageBytes == null) {
			throw new IOException("Could not load image - no data provided");
		}
		boolean interrupted = false;
		Throwable exception = null;
		Image image = null;
		image = Toolkit.getDefaultToolkit().createImage(imageBytes);
		MediaTracker tracker = new MediaTracker(observer);
		tracker.addImage(image, 0);
		do {
			try {
				interrupted = false;
				tracker.waitForID(0); // This is the only method that throws an exception
			} catch (InterruptedException t) {
				interrupted = true;
				continue;
			} catch (Throwable t) {
				exception = t;
			}
		} while (interrupted);
		if (image == null || exception != null || image.getWidth(null) <= 0 || image.getHeight(null) <= 0) {
			// Try the newer way (although it pretty much sucks rocks)
			image = ImageIO.read(new ByteArrayInputStream(imageBytes));
		}
		if (image == null) {
			throw new IOException("Could not load image", exception);
		}
		return image;
	}

	public static void clearImage(BufferedImage image) {
		if (image == null)
			return;

		Graphics2D g = null;
		try {
			g = (Graphics2D) image.getGraphics();
			Composite oldComposite = g.getComposite();
			g.setComposite(AlphaComposite.Clear);
			g.fillRect(0, 0, image.getWidth(), image.getHeight());
			g.setComposite(oldComposite);
		} finally {
			if (g != null) {
				g.dispose();
			}
		}
	}

	public static BufferedImage rgbToGrayscale(BufferedImage image) {
		if (image == null) {
			return null;
		}
		BufferedImage returnImage = new BufferedImage(image.getWidth(), image.getHeight(), pickBestTransparency(image));
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int encodedPixel = image.getRGB(x, y);

				int alpha = (encodedPixel >> 24) & 0xff;
				int red = (encodedPixel >> 16) & 0xff;
				int green = (encodedPixel >> 8) & 0xff;
				int blue = (encodedPixel) & 0xff;

				int average = (int) ((red + blue + green) / 3.0);

				// y = 0.3R + 0.59G + 0.11B luminance formula
				int value = (alpha << 24) + (average << 16) + (average << 8) + average;
				returnImage.setRGB(x, y, value);
			}
		}
		return returnImage;
	}

	private static final int[][] outlineNeighborMap = {
			{ 0, -1, 100 }, // N
			{ 1, 0, 100 }, // E
			{ 0, 1, 100 }, // S
			{ -1, 0, 100 } // W
			,
			{ -1, -1 }, // NW
			{ 1, -1 }, // NE
			{ -1, 1 }, // SW
			{ 1, 1 }, // SE
	};

	public static BufferedImage createOutline(BufferedImage sourceImage, Color color) {
		if (sourceImage == null) {
			return null;
		}
		BufferedImage image = new BufferedImage(sourceImage.getWidth() + 2, sourceImage.getHeight() + 2, Transparency.BITMASK);

		for (int row = 0; row < image.getHeight(); row++) {
			for (int col = 0; col < image.getWidth(); col++) {
				int sourceX = col - 1;
				int sourceY = row - 1;

				// Pixel under current location
				if (sourceX >= 0 && sourceY >= 0 && sourceX <= sourceImage.getWidth() - 1 && sourceY <= sourceImage.getHeight() - 1) {
					int sourcePixel = sourceImage.getRGB(sourceX, sourceY);
					if (sourcePixel >> 24 != 0) {
						// Not an empty pixel, don't overwrite it
						continue;
					}
				}
				for (int i = 0; i < outlineNeighborMap.length; i++) {
					int[] neighbor = outlineNeighborMap[i];
					int x = sourceX + neighbor[0];
					int y = sourceY + neighbor[1];

					if (x >= 0 && y >= 0 && x <= sourceImage.getWidth() - 1 && y <= sourceImage.getHeight() - 1) {
						if ((sourceImage.getRGB(x, y) >> 24) != 0) {
							image.setRGB(col, row, color.getRGB());
							break;
						}
					}
				}
			}
		}
		return image;
	}

	/**
	 * Flip the image and return a new image
	 * 
	 * @param direction
	 *            0-nothing, 1-horizontal, 2-vertical, 3-both
	 * @return
	 */
	public static BufferedImage flip(BufferedImage image, int direction) {
		BufferedImage workImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getTransparency());

		boolean flipHorizontal = (direction & 1) == 1;
		boolean flipVertical = (direction & 2) == 2;

		int workW = image.getWidth() * (flipHorizontal ? -1 : 1);
		int workH = image.getHeight() * (flipVertical ? -1 : 1);
		int workX = flipHorizontal ? image.getWidth() : 0;
		int workY = flipVertical ? image.getHeight() : 0;

		Graphics2D wig = workImage.createGraphics();
		wig.drawImage(image, workX, workY, workW, workH, null);
		wig.dispose();

		return workImage;
	}

	/*
	 * Jamz: Some common image utility methods
	 */
	public static ImageIcon resizeImage(ImageIcon imageIcon) {
		// Default to 30x30 w/h not passed
		return resizeImage(imageIcon, 30, 30);
	}

	public static ImageIcon resizeImage(ImageIcon imageIcon, int w, int h) {
		return new ImageIcon(imageIcon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
	}

	public static Image resizeImage(Image image) {
		// Default to 30x30 w/h not passed
		return resizeImage(image, 30, 30);
	}

	public static Image resizeImage(Image image, int w, int h) {
		// Default to 30x30 w/h not passed
		return image.getScaledInstance(w, h, Image.SCALE_SMOOTH);
	}
}
