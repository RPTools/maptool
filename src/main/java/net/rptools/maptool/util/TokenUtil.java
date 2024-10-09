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
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import net.rptools.maptool.model.Token;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TokenUtil {
  private static final Logger log = LogManager.getLogger(TokenUtil.class);

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
