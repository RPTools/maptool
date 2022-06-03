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
package net.rptools.maptool.client.ui.zone;

import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.RasterFormatException;
import java.awt.image.WritableRaster;
import org.checkerframework.common.value.qual.IntRange;

/**
 * A custom Composite class to replace AlphaComposite for the purposes of mixing lights, auras, and
 * other colored effects. http://www.java2s.com/Code/Java/2D-Graphics-GUI/BlendCompositeDemo.htm
 */
class BlendingComposite implements Composite {

  public BlendingComposite() {}

  public static BlendingComposite getInstance() {
    return new BlendingComposite();
  }

  private static boolean checkComponentsOrder(ColorModel cm) {
    if (cm instanceof DirectColorModel directCM && cm.getTransferType() == DataBuffer.TYPE_INT) {

      return directCM.getRedMask() == 0x00FF0000
          && directCM.getGreenMask() == 0x0000FF00
          && directCM.getBlueMask() == 0x000000FF
          && (directCM.getNumComponents() != 4 || directCM.getAlphaMask() == 0xFF000000);
    }

    return false;
  }

  @Override
  public CompositeContext createContext(
      ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
    if (!checkComponentsOrder(srcColorModel) || !checkComponentsOrder(dstColorModel)) {
      throw new RasterFormatException("Incompatible color models");
    }

    return new BlendingContext();
  }

  private static final class BlendingContext implements CompositeContext {

    public BlendingContext() {}

    /**
     * Applies a <a href="https://en.wikipedia.org/wiki/Blend_modes#Screen">"screen" blend mode</a>
     * to two color components.
     *
     * <p>This operation is basically fancy multiplication, so it is commutative and associative
     * (i.e., it is order independent). A zero input (black) acts as an identity element, causing
     * the result to simply be the other input. A maximum input (white) acts an absorbing element,
     * causing the result to also be white. For all other inputs, the result is brighter than both
     * of the inputs, though not as bright as would be with addition.
     *
     * @param src The first color component to blend.
     * @param dst The second color component to blend.
     * @return The screen blending of the inputs.
     */
    private @IntRange(from = 0, to = 255) int blendScreen(
        @IntRange(from = 0, to = 255) int src, @IntRange(from = 0, to = 255) int dst) {
      return 255 - (255 - src) * (255 - dst) / 255;
    }

    @Override
    public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
      final int w = Math.min(src.getWidth(), dstIn.getWidth());
      final int h = Math.min(src.getHeight(), dstIn.getHeight());

      final int[] srcPixels = new int[w];
      final int[] dstPixels = new int[w];

      for (int y = 0; y < h; y++) {
        src.getDataElements(0, y, w, 1, srcPixels);
        dstIn.getDataElements(0, y, w, 1, dstPixels);

        for (int x = 0; x < w; x++) {
          final int srcPixel = srcPixels[x];
          final int dstPixel = dstPixels[x];

          /*
           * checkComponentsOrder() guarantees that we are handling simple integer ARGB formats.
           * If this changes in the future, we could work with the color model here to handle more
           * cases, though with more overhead.
           */

          if (dstPixel == 0) {
            // Since lights are drawn onto a transparent black image, a fairly common case is that
            // dstPixel is black and will not affect srcPixel at all.
            dstPixels[x] = srcPixel;
          } else {
            dstPixels[x] =
                (blendScreen((dstPixel >>> 24) & 0xFF, (srcPixel >>> 24) & 0xFF) << 24)
                    | (blendScreen((dstPixel >>> 16) & 0xFF, (srcPixel >>> 16) & 0xFF) << 16)
                    | (blendScreen((dstPixel >>> 8) & 0xFF, (srcPixel >>> 8) & 0xFF) << 8)
                    | (blendScreen(dstPixel & 0xFF, srcPixel & 0xFF));
          }
        }

        dstOut.setDataElements(0, y, w, 1, dstPixels);
      }
    }

    @Override
    public void dispose() {}
  }
}
