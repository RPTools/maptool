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

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import net.rptools.lib.image.ImageUtil;
import net.rptools.lib.image.RenderQuality;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.Grid;
import net.rptools.maptool.model.GridFactory;
import net.rptools.maptool.model.LookupTable;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.TokenFootprint;
import net.rptools.parser.ParserException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ImageSupport {
  private static final Logger log = LogManager.getLogger(ImageSupport.class);

  /** Direction for use in processing image flipping */
  public enum FlipDirection {
    NONE,
    HORIZONTAL,
    VERTICAL,
    HORIZONTAL_VERTICAL,
    ISOMETRIC,
    ISOMETRIC_HORIZONTAL,
    ISOMETRIC_VERTICAL,
    ISOMETRIC_HORIZONTAL_VERTICAL;

    public static FlipDirection getFlipDirection(boolean x, boolean y, boolean iso) {
      if (!x && !y && !iso) {
        return NONE;
      } else if (x && y && iso) {
        return ISOMETRIC_HORIZONTAL_VERTICAL;
      } else if (!x && y && iso) {
        return ISOMETRIC_VERTICAL;
      } else if (x && !y && iso) {
        return ISOMETRIC_HORIZONTAL;
      } else if (!x && !y) {
        return ISOMETRIC;
      } else if (x && y) {
        return HORIZONTAL_VERTICAL;
      } else if (!x) {
        return VERTICAL;
      } else {
        return HORIZONTAL;
      }
    }

    public boolean isFlippedH() {
      return switch (this) {
        case HORIZONTAL, HORIZONTAL_VERTICAL, ISOMETRIC_HORIZONTAL, ISOMETRIC_HORIZONTAL_VERTICAL ->
            true;
        default -> false;
      };
    }

    public boolean isFlippedIso() {
      return switch (this) {
        case ISOMETRIC, ISOMETRIC_HORIZONTAL, ISOMETRIC_VERTICAL, ISOMETRIC_HORIZONTAL_VERTICAL ->
            true;
        default -> false;
      };
    }

    public boolean isFlippedV() {
      return switch (this) {
        case VERTICAL, HORIZONTAL_VERTICAL, ISOMETRIC_VERTICAL, ISOMETRIC_HORIZONTAL_VERTICAL ->
            true;
        default -> false;
      };
    }
  }

  public static BufferedImage getScaledTokenImage(
      BufferedImage img, Token token, Grid grid, double zoom, RenderQuality renderQuality) {

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
          return ImageUtil.scaleBufferedImage(img, outputWidth, outputHeight, renderQuality);
        } catch (Exception e) {
          log.warn(e.getLocalizedMessage(), e);
          return img;
        }
      }
    } else {
      Rectangle b = token.getImageBounds(grid.getZone());
      try {
        return ImageUtil.scaleBufferedImage(
            img, (int) Math.ceil(b.width * zoom), (int) Math.ceil(b.height * zoom), renderQuality);
      } catch (Exception e) {
        log.warn(e.getLocalizedMessage(), e);
        return img;
      }
    }
    return img; // fallback, return original
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

  /**
   * Flip the image and return a new image
   *
   * @param image the image to flip
   * @param direction 0-nothing, 1-horizontal, 2-vertical, 3-both
   * @return flipped BufferedImage
   */
  public static BufferedImage flipCartesian(BufferedImage image, FlipDirection direction) {
    boolean flipH = direction.isFlippedH();
    boolean flipV = direction.isFlippedV();
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
}
