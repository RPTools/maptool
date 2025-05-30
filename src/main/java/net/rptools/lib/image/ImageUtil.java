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
package net.rptools.lib.image;

import com.twelvemonkeys.image.ResampleOp;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.io.*;
import java.util.Arrays;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.*;
import net.rptools.lib.MathUtil;
import net.rptools.maptool.client.AppConstants;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.*;
import net.rptools.maptool.util.ImageManager;
import net.rptools.parser.ParserException;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author trevor
 */
public class ImageUtil {
  public static final String HINT_TRANSPARENCY = "hintTransparency";

  public static final FilenameFilter SUPPORTED_IMAGE_FILE_FILTER =
      (dir, name) -> {
        name = name.toLowerCase();
        return Arrays.asList(ImageIO.getReaderFileSuffixes()).contains(name);
      };

  private static final Logger log = LogManager.getLogger();

  private static final JPanel observer = new JPanel();
  private static final int[][] outlineNeighborMap = {
    {0, -1, 100}, // N
    {1, 0, 100}, // E
    {0, 1, 100}, // S
    {-1, 0, 100} // W
    ,
    {-1, -1}, // NW
    {1, -1}, // NE
    {-1, 1}, // SW
    {1, 1}, // SE
  };
  private static RenderingHints renderingHintsQuality;

  public static BufferedImage replaceColor(BufferedImage src, int sourceRGB, int replaceRGB) {
    for (int y = 0; y < src.getHeight(); y++) {
      for (int x = 0; x < src.getWidth(); x++) {
        int rawRGB = src.getRGB(x, y);
        int rgb = rawRGB & 0xffffff;
        int alpha = rawRGB & 0xff000000;

        if (rgb == sourceRGB) {
          src.setRGB(x, y, alpha | replaceRGB);
        }
      }
    }
    return src;
  }

  public static BufferedImage negativeImage(BufferedImage originalImage) {
    // Get the dimensions of the image
    int width = originalImage.getWidth();
    int height = originalImage.getHeight();
    // Create a new BufferedImage for the negative image
    BufferedImage negativeImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    // Loop through each pixel of the original image and convert it to its negative
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int negativeRGB = negativeColourInt(originalImage.getRGB(x, y));
        negativeImage.setRGB(x, y, negativeRGB);
      }
    }
    return negativeImage;
  }

  public static int negativeColourInt(int rgb) {
    int r = 255 - ((rgb >> 16) & 0xFF);
    int g = 255 - ((rgb >> 8) & 0xFF);
    int b = 255 - (rgb & 0xFF);
    return (r << 16) | (g << 8) | b;
  }

  public static BufferedImage getScaledTokenImage(
      BufferedImage img, Token token, Grid grid, double zoom) {

    double imgW = img.getWidth();
    double imgH = img.getHeight();
    if (token.isSnapToScale()) {
      TokenFootprint footprint = token.getFootprint(grid);
      Rectangle2D footprintBounds = footprint.getBounds(grid);
      // except gridless, this should be 1 for footprints larger than the grid
      double fpS = footprint.getScale();
      double fpW, fpH;
      // size:  multiply by zoom level to prevent multiple scaling ops which lose definition, i.e.
      // scale once
      if (GridFactory.getGridType(grid).equals(GridFactory.NONE)) {
        fpW = fpH = grid.getSize() * fpS * zoom; // all gridless are relative to the grid size
      } else {
        fpW = footprintBounds.getWidth() * fpS * zoom;
        fpH = footprintBounds.getHeight() * fpS * zoom;
      }
      double sXY = token.getSizeScale();
      double sX = token.getScaleX();
      double sY = token.getScaleY();
      // scale to fit image inside footprint bounds using the dimension that needs the most scaling,
      // i.e. lowest ratio
      double imageFootprintRatio;
      if (token.getShape() == Token.TokenShape.FIGURE && grid.isIsometric()) {
        imageFootprintRatio = fpW / imgW;
      } else {
        imageFootprintRatio = Math.min(fpW / imgW, fpH / imgH);
      }
      // combine with token scale properties
      if (sX != 1 || sY != 1 || sXY != 1 || imageFootprintRatio != 1) {
        int outputWidth = (int) Math.ceil(imgW * sXY * sX * imageFootprintRatio);
        int outputHeight = (int) Math.ceil(imgH * sXY * sY * imageFootprintRatio);
        token.setWidth(outputWidth);
        token.setHeight(outputHeight);
        try {
          return ImageUtil.scaleBufferedImage(img, outputWidth, outputHeight);
        } catch (Exception e) {
          log.warn(e.getLocalizedMessage(), e);
          return img;
        }
      }
    } else {
      Rectangle b = token.getBounds(grid.getZone());
      try {
        return ImageUtil.scaleBufferedImage(
            img, (int) Math.ceil(b.width * zoom), (int) Math.ceil(b.height * zoom));
      } catch (Exception e) {
        log.warn(e.getLocalizedMessage(), e);
        return img;
      }
    }
    return img; // fallback, return original
  }

  /**
   * Create a copy of the image that is compatible with the current graphics context
   *
   * @param img to use
   * @return compatible BufferedImage
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

  /**
   * Create a copy of the image that is compatible with the current graphics context and scaled to
   * the supplied size
   *
   * @param img the image to copy
   * @param width width of the created image
   * @param height height of the created image
   * @param hints a {@link Map} that may contain the key HINT_TRANSPARENCY to define a the
   *     transparency color
   * @return a {@link BufferedImage} with a copy of img
   */
  public static BufferedImage createCompatibleImage(
      Image img, int width, int height, Map<String, Object> hints) {
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
      AppPreferences.renderQuality.get().setRenderingHints(g);
      g.drawImage(img, 0, 0, width, height, null);
    } finally {
      if (g != null) {
        g.dispose();
      }
    }
    return compImg;
  }

  /**
   * Look at the image and determine which Transparency is most appropriate. If it finds any
   * translucent pixels it returns Transparency.TRANSLUCENT, if it finds at least one purely
   * transparent pixel and no translucent pixels it will return Transparency.BITMASK, in all other
   * cases it returns Transparency.OPAQUE, including errors
   *
   * @param image to pick transparency from
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
      log.error("Interrupted waiting for pixels", e);
      return Transparency.OPAQUE;
    }
    if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
      log.error("image fetch aborted or errored");
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

  public static BufferedImage createCompatibleImage(int width, int height, int transparency) {
    return new BufferedImage(width, height, transparency);
  }

  /**
   * Convert a BufferedImage to byte[] in the jpg format.
   *
   * @param image the buffered image.
   * @return the byte[] of the image
   * @throws IOException if the image cannot be written to the output stream.
   */
  public static byte[] imageToBytes(BufferedImage image) throws IOException {

    // First try jpg, if it cant be converted to jpg try png
    byte[] imageBytes = imageToBytes(image, "jpg");
    if (imageBytes.length > 0) {
      return imageBytes;
    }

    return imageToBytes(image, "png");
  }

  /**
   * Convert a BufferedImage to byte[] in an given format.
   *
   * @param image the buffered image.
   * @param format a String containing the informal name of the format.
   * @return the byte[] of the image.
   * @throws IOException if the image cannot be written to the output stream.
   */
  public static byte[] imageToBytes(BufferedImage image, String format) throws IOException {
    ByteArrayOutputStream outStream = new ByteArrayOutputStream(10000);
    ImageIO.write(image, format, outStream);

    return outStream.toByteArray();
  }

  public static void clearImage(BufferedImage image) {
    if (image == null) {
      return;
    }

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

  public static BufferedImage createOutline(BufferedImage sourceImage, Color color) {
    if (sourceImage == null) {
      return null;
    }
    BufferedImage image =
        new BufferedImage(
            sourceImage.getWidth() + 2, sourceImage.getHeight() + 2, Transparency.BITMASK);

    for (int row = 0; row < image.getHeight(); row++) {
      for (int col = 0; col < image.getWidth(); col++) {
        int sourceX = col - 1;
        int sourceY = row - 1;

        // Pixel under current location
        if (sourceX >= 0
            && sourceY >= 0
            && sourceX <= sourceImage.getWidth() - 1
            && sourceY <= sourceImage.getHeight() - 1) {
          int sourcePixel = sourceImage.getRGB(sourceX, sourceY);
          if (sourcePixel >> 24 != 0) {
            // Not an empty pixel, don't overwrite it
            continue;
          }
        }
        for (int[] neighbor : outlineNeighborMap) {
          int x = sourceX + neighbor[0];
          int y = sourceY + neighbor[1];

          if (x >= 0
              && y >= 0
              && x <= sourceImage.getWidth() - 1
              && y <= sourceImage.getHeight() - 1
              && (sourceImage.getRGB(x, y) >> 24) != 0) {
            image.setRGB(col, row, color.getRGB());
            break;
          }
        }
      }
    }
    return image;
  }

  /**
   * Flip the image and return a new image
   *
   * @param image the image to flip
   * @param direction 0-nothing, 1-horizontal, 2-vertical, 3-both
   * @return flipped BufferedImage
   */
  public static BufferedImage flipCartesian(
      BufferedImage image, AppConstants.FLIP_DIRECTION direction) {
    boolean flipH = AppConstants.FLIP_DIRECTION.isFlippedH(direction);
    boolean flipV = AppConstants.FLIP_DIRECTION.isFlippedV(direction);
    if (!flipH && !flipV) {
      return image;
    }

    BufferedImage workImage =
        new BufferedImage(image.getWidth(), image.getHeight(), image.getTransparency());

    int workW = image.getWidth() * (flipH ? -1 : 1);
    int workH = image.getHeight() * (flipV ? -1 : 1);
    int workX = flipH ? image.getWidth() : 0;
    int workY = flipV ? image.getHeight() : 0;

    Graphics2D wig = workImage.createGraphics();
    wig.drawImage(image, workX, workY, workW, workH, null);
    wig.dispose();

    return workImage;
  }

  /**
   * Load the image. Does not create a graphics configuration compatible version.
   *
   * @param file the file with the image in it
   * @return an {@link Image} from the content of the file
   * @throws IOException when the image can't be read in the file
   */
  public static Image getImage(File file) throws IOException {
    return bytesToImage(FileUtils.readFileToByteArray(file), file.getCanonicalPath());
  }

  /**
   * Converts a byte array into an {@link Image} instance.
   *
   * @param imageBytes bytes to convert
   * @param imageName name of image
   * @return the image
   * @throws IOException if image could not be loaded
   */
  public static Image bytesToImage(byte[] imageBytes, String imageName) throws IOException {
    if (imageBytes == null) {
      throw new IOException("Could not load image - no data provided");
    }
    boolean interrupted = false;
    Throwable exception = null;
    Image image;
    image = ImageIO.read(new ByteArrayInputStream(imageBytes));
    MediaTracker tracker = new MediaTracker(observer);
    tracker.addImage(image, 0);
    do {
      try {
        interrupted = false;
        tracker.waitForID(0); // This is the only method that throws an exception
      } catch (InterruptedException t) {
        interrupted = true;
      } catch (Throwable t) {
        exception = t;
      }
    } while (interrupted);
    if (image == null) {
      throw new IOException("Could not load image " + imageName, exception);
    }
    return image;
  }

  public static BufferedImage getCompatibleImage(String image) throws IOException {
    return getCompatibleImage(image, null);
  }

  public static BufferedImage getCompatibleImage(String image, Map<String, Object> hints)
      throws IOException {
    return createCompatibleImage(getImage(image), hints);
  }

  /**
   * Load the image in the classpath. Does not create a graphics configuration compatible version.
   *
   * @param image the resource name of the image file
   * @return an {@link Image} from the content of the file
   * @throws IOException when the image can't be read in the file
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
    return bytesToImage(dataStream.toByteArray(), image);
  }

  public static double getIsoFigureHeightOffset(Token token, Rectangle2D footprintBounds) {
    if (token.getShape().equals(Token.TokenShape.FIGURE) && !token.getIsFlippedIso()) {
      double imageFitRatio = getIsoFigureScaleFactor(token, footprintBounds);
      double th = token.getHeight() * imageFitRatio;
      return footprintBounds.getHeight() - th;
    }
    return 0;
  }

  /**
   * Use width ratio unless height exceeds double footprint height
   *
   * @param token Token
   * @param footprintBounds Rectangle
   * @return double
   */
  public static double getIsoFigureScaleFactor(Token token, Rectangle2D footprintBounds) {
    return Math.min(
        footprintBounds.getWidth() / token.getWidth(),
        footprintBounds.getHeight() * 2 / token.getHeight());
  }

  /**
   * Checks to see if token has an image table and references that if the token has a facing
   * otherwise uses basic image
   *
   * @param token the token to get the image from.
   * @return BufferedImage
   */
  public static BufferedImage getTokenImage(Token token, ImageObserver... observers) {
    BufferedImage image = null;
    // Get the basic image
    if (token.getHasImageTable() && token.hasFacing() && token.getImageTableName() != null) {
      LookupTable lookupTable =
          MapTool.getCampaign().getLookupTableMap().get(token.getImageTableName());
      if (lookupTable != null) {
        try {
          LookupTable.LookupEntry result =
              lookupTable.getLookup(Integer.toString(token.getFacing()));
          if (result != null) {
            image = ImageManager.getImage(result.getImageId(), observers);
          }
        } catch (ParserException p) {
          // do nothing
        }
      }
    }

    if (image == null) {
      // Adds zr as observer so we can repaint once the image is ready. Fixes #1700.
      image = ImageManager.getImage(token.getImageAssetId(), observers);
    }
    return image;
  }

  public static BufferedImage flipIsometric(BufferedImage image, boolean toRhombus) {
    BufferedImage workImage;
    boolean isSquished =
        MathUtil.inTolerance(image.getHeight(), image.getWidth() / 2d, image.getHeight() * 0.05);
    if (image.getWidth() != image.getHeight()) {
      int maxDim = Math.max(image.getWidth(), image.getHeight());
      int w, h = 1;
      if (toRhombus) {
        // make it square and centred
        w = h = maxDim;
      } else {
        if (!isSquished) {
          w = maxDim;
          h = (int) Math.ceil(maxDim / 2d);
        } else {
          w = image.getWidth();
          w = (int) Math.ceil(image.getWidth() / 2d);
        }
      }
      workImage = new BufferedImage(w, h, image.getTransparency());
      Graphics2D wig = workImage.createGraphics();
      wig.drawImage(
          image,
          (workImage.getWidth() - image.getWidth()) / 2,
          (workImage.getHeight() - image.getHeight()) / 2,
          image.getWidth(),
          image.getHeight(),
          null);
      wig.dispose();
      image = workImage;
    }
    if (toRhombus) {
      image = rotateImage(image, 45);
      image = scaleBufferedImage(image, image.getWidth(), image.getHeight() / 2);
    } else {
      image = scaleBufferedImage(image, image.getWidth(), image.getWidth());
      image = rotateImage(image, -45);
    }
    return image;
  }

  /**
   * Scales a BufferedImage to a desired width and height and returns the result.
   *
   * @param image The BufferedImage to be scaled
   * @param width Desired width in px
   * @param height Desired height in px
   * @return The scaled BufferedImage
   */
  public static BufferedImage scaleBufferedImage(BufferedImage image, int width, int height) {
    ResampleOp resampleOp =
        new ResampleOp(width, height, AppPreferences.renderQuality.get().getResampleOpFilter());
    return resampleOp.filter(image, null);
  }

  public static ImageIcon scaleImageIcon(ImageIcon icon, int w, int h) {
    int nw = icon.getIconWidth();
    int nh = icon.getIconHeight();

    if (icon.getIconWidth() > w) {
      nw = w;
      nh = (nw * icon.getIconHeight()) / icon.getIconWidth();
    }

    if (nh > h) {
      nh = h;
      nw = (icon.getIconWidth() * nh) / icon.getIconHeight();
    }

    return new ImageIcon(icon.getImage().getScaledInstance(nw, nh, Image.SCALE_DEFAULT));
  }

  public static RenderingHints getRenderingHintsQuality() {
    if (renderingHintsQuality == null) {
      renderingHintsQuality =
          new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      renderingHintsQuality.put(
          RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
      renderingHintsQuality.put(
          RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
      renderingHintsQuality.put(
          RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
      renderingHintsQuality.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
    }
    return renderingHintsQuality;
  }

  public static BufferedImage rotateImage(BufferedImage img, double degrees) {
    double rads = Math.toRadians(degrees);
    double sin = Math.abs(Math.sin(rads)), cos = Math.abs(Math.cos(rads));
    int w = img.getWidth();
    int h = img.getHeight();
    int newWidth = (int) Math.floor(w * cos + h * sin);
    int newHeight = (int) Math.floor(h * cos + w * sin);

    BufferedImage rotated = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = rotated.createGraphics();
    g2d.setRenderingHints(getRenderingHintsQuality());
    AffineTransform at = new AffineTransform();
    at.translate((newWidth - w) / 2.0, (newHeight - h) / 2.0);

    double x = w / 2.0;
    double y = h / 2.0;

    at.rotate(rads, x, y);
    g2d.setTransform(at);
    g2d.drawImage(img, 0, 0, null);
    g2d.dispose();
    return rotated;
  }
}
