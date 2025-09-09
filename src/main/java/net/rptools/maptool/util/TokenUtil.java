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
package net.rptools.maptool.util;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TokenUtil {
  private static final Logger log = LogManager.getLogger(TokenUtil.class);

  /**
   * Builds the transformation required to render a token image at the correct size and location.
   *
   * @param zone The zone in which the token is being rendered.
   * @param token The token being rendered.
   * @param imageSize The size of the token image to render.
   * @param footprintBounds The token's footprint.
   * @return The {@link AffineTransform} that can be applied to a {@link Graphics2D} object to
   *     render the token image.
   */
  public static AffineTransform getRenderTransform(
      Zone zone, Token token, Dimension imageSize, Rectangle2D footprintBounds) {
    var isoFigure =
        zone.getGrid().isIsometric()
            && !token.getIsFlippedIso()
            && token.getShape() == Token.TokenShape.FIGURE;

    // centre image
    double imageCx = -imageSize.getWidth() / 2d;
    double imageCy =
        isoFigure
            ? -imageSize.getHeight() + imageSize.getWidth() / 4d
            : -imageSize.getHeight() / 2d;

    // Compose a transformation to draw the token image at the correct place.
    // Because the last transform applies first, read these steps backwards for a better
    // understanding.
    AffineTransform imageTransform = new AffineTransform();
    {
      // Now move the image back to its actual location.
      imageTransform.translate(footprintBounds.getCenterX(), footprintBounds.getCenterY());

      // Rotate around the anchor.
      if (token.hasFacing() && token.getShape() == Token.TokenShape.TOP_DOWN) {
        imageTransform.rotate(Math.toRadians(token.getFacingInDegrees()));
      }

      // Rotation applies around the anchor, so nudge the token first.
      imageTransform.translate(token.getAnchorX(), token.getAnchorY());

      // Size image to footprint.
      // For snap-to-scale, fit to footprint, then scale according to layout.
      // For others, the X/Y scale is already incorporated into the footprint, so just fill the
      // footprint.
      if (token.isSnapToScale()) {
        var scale =
            isoFigure
                // Fit width
                ? footprintBounds.getWidth() / imageSize.getWidth()
                // Scale to fit
                : Math.min(
                    footprintBounds.getWidth() / imageSize.getWidth(),
                    footprintBounds.getHeight() / imageSize.getHeight());
        scale *= token.getSizeScale();
        imageTransform.scale(scale, scale);
      } else {
        var scaleX = footprintBounds.getWidth() / imageSize.getWidth();
        var scaleY = footprintBounds.getHeight() / imageSize.getHeight();
        imageTransform.scale(scaleX, scaleY);
      }

      // Iso flip
      if (token.getIsFlippedIso() && zone.getGrid().isIsometric()) {
        imageTransform.scale(Math.sqrt(2), 1 / Math.sqrt(2));
        imageTransform.rotate(Math.toRadians(45));
      }

      // Cartesian flip.
      imageTransform.scale(token.isFlippedX() ? -1 : 1, token.isFlippedY() ? -1 : 1);

      // Move the image center to (0, 0) so rotations and scales can be easily applied.
      imageTransform.translate(imageCx, imageCy);
    }

    return imageTransform;
  }

  public static Token.TokenShape guessTokenType(Image image) {
    if (image instanceof BufferedImage) {
      return guessTokenType((BufferedImage) image);
    }
    int pixelCount = 0;
    int width = image.getWidth(null);
    int height = image.getHeight(null);
    int[] pixelArray = new int[width * height];
    PixelGrabber pg = new PixelGrabber(image, 0, 0, width, height, pixelArray, 0, width);
    try {
      pg.grabPixels();
    } catch (InterruptedException e) {
      String msg = "interrupted waiting for pixels!";
      log.warn(msg);
      return Token.TokenShape.TOP_DOWN;
    }
    if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
      String msg = "image fetch aborted or errored";
      log.warn(msg);
      return Token.TokenShape.TOP_DOWN;
    }
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        // Get the next pixel
        int pixel = pixelArray[y * width + x];
        if ((pixel & 0xff000000) != 0) {
          pixelCount++;
        }
      }
    }
    return guessTokenType(new Dimension(image.getWidth(null), image.getHeight(null)), pixelCount);
  }

  public static Token.TokenShape guessTokenType(BufferedImage image) {
    int pixelCount = 0;
    for (int row = 0; row < image.getHeight(); row++) {
      for (int col = 0; col < image.getWidth(); col++) {
        int pixel = image.getRGB(col, row);
        if ((pixel & 0xff000000) != 0) {
          pixelCount++;
        }
      }
    }
    return guessTokenType(new Dimension(image.getWidth(), image.getHeight()), pixelCount);
  }

  private static Token.TokenShape guessTokenType(Dimension size, int pixelCount) {
    double circlePixelCount = (int) (Math.PI * (size.width / 2) * (size.height / 2));
    double squarePixelCount = size.width * size.height;
    double topDownPixelCount = circlePixelCount * 3 / 4; // arbitrary
    double circleResult = Math.abs(1 - (pixelCount / circlePixelCount));
    double squareResult = Math.abs(1 - (pixelCount / squarePixelCount));
    double topDownResult = Math.abs(1 - (pixelCount / topDownPixelCount));

    if (circleResult < squareResult && circleResult < topDownResult) {
      return Token.TokenShape.CIRCLE;
    }
    if (squareResult < circleResult && squareResult < topDownResult) {
      return Token.TokenShape.SQUARE;
    }
    return Token.TokenShape.TOP_DOWN;
  }
}
