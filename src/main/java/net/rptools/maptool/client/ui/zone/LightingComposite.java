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

/**
 * A custom Composite class to replace AlphaComposite for the purposes of mixing lights, auras, and
 * other colored effects. <a
 * href="http://www.java2s.com/Code/Java/2D-Graphics-GUI/BlendCompositeDemo.htm">...</a>
 */
public class LightingComposite implements Composite {
  /**
   * Used to blend lights together to give an additive effect.
   *
   * <p>To use to good effect, the initial image should be black and then lights should be added to
   * it one-by-one.
   */
  public static final Composite BlendedLights = new LightingComposite(new ScreenBlender());

  /** Used to blend lighting results with an underlying image. */
  public static final Composite OverlaidLights =
      new LightingComposite(new ConstrainedBrightenBlender());

  // Blenders are stateless, so no point making new ones all the time.
  private final Blender blender;

  public LightingComposite(Blender blender) {
    this.blender = blender;
  }

  private static void checkComponentsOrder(ColorModel cm) throws RasterFormatException {
    if (cm.getTransferType() != DataBuffer.TYPE_INT) {
      throw new RasterFormatException("Color model must be represented as an int array.");
    }
    if (cm instanceof DirectColorModel directCM) {
      if (directCM.getRedMask() != 0x00FF0000
          || directCM.getGreenMask() != 0x0000FF00
          || directCM.getBlueMask() != 0x000000FF
          || (directCM.getNumComponents() == 4 && directCM.getAlphaMask() != 0xFF000000)) {
        throw new RasterFormatException("Color model must be RGB or ARGB");
      }
    } else {
      throw new RasterFormatException(
          "Color model must be a DirectColorModel so that each pixel is one int");
    }
  }

  @Override
  public CompositeContext createContext(
      ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
    checkComponentsOrder(srcColorModel);
    checkComponentsOrder(dstColorModel);

    return new BlenderContext(blender);
  }

  private static final class BlenderContext implements CompositeContext {
    private final Blender blender;

    public BlenderContext(Blender blender) {
      this.blender = blender;
    }

    @Override
    public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
      final int w = Math.min(src.getWidth(), dstIn.getWidth());
      final int h = Math.min(src.getHeight(), dstIn.getHeight());

      final int[] srcPixels = new int[w];
      final int[] dstPixels = new int[w];
      final int[] dstOutPixels = new int[w];

      for (int y = 0; y < h; y++) {
        src.getDataElements(src.getMinX(), y + src.getMinY(), w, 1, srcPixels);
        dstIn.getDataElements(dstIn.getMinX(), y + dstIn.getMinY(), w, 1, dstPixels);

        blender.blendRow(dstPixels, srcPixels, dstOutPixels, w);

        dstOut.setDataElements(dstOut.getMinX(), y + dstOut.getMinY(), w, 1, dstOutPixels);
      }
    }

    @Override
    public void dispose() {}
  }

  public interface Blender {
    /**
     * Blend source and destination pixels for a row of pixels.
     *
     * <p>The pixels must be encoded as 32-bit ARGB, and the result will be likewise.
     *
     * @param dstPixels The bottom layer pixels.
     * @param srcPixels The top layer pixels.
     * @param outPixels A buffer that this method will write results into.
     * @param samples The number of pixels in the row.
     */
    void blendRow(int[] dstPixels, int[] srcPixels, int[] outPixels, int samples);
  }

  /**
   * Additive lights based on the screen blend mode.
   *
   * <p>The result of screen blending is always greater than the top and bottom inputs.
   *
   * <p>Special cases:
   *
   * <ul>
   *   <li>When the bottom component is 0, the result is the top component.
   *   <li>When the top component is 0, the result is the bottom component.
   *   <li>When either the top component or the bottom component is maxed, the result is maxed.
   * </ul>
   */
  private static final class ScreenBlender implements Blender {
    public void blendRow(int[] dstPixels, int[] srcPixels, int[] outPixels, int samples) {
      for (int x = 0; x < samples; ++x) {
        final int srcPixel = srcPixels[x];
        final int dstPixel = dstPixels[x];

        // This keeps the bottom alpha around.
        int resultPixel = dstPixel & (0xFF << 24);

        for (int shift = 0; shift < 24; shift += 8) {
          final var dstC = (dstPixel >>> shift) & 0xFF;
          final var srcC = (srcPixel >>> shift) & 0xFF;

          final var resultC = dstC + srcC - (dstC * srcC) / 255;

          resultPixel |= (resultC << shift);
        }

        outPixels[x] = resultPixel;
      }
    }
  }

  /**
   * Inspired by overlay blending, this is an alternative that never darkens and which boosts dark
   * components by no more than some multiple of the component.
   *
   * <p>When the bottom component ({@code dstC}) is low, the result is between the bottom component
   * and a multiple of the bottom component controlled by {@link #MAX_DARKNESS_BOOST_PER_128}. The
   * exact result is determined by using the top component ({@code srcC}) to interpolate between the
   * two bounds.
   *
   * <p>When the bottom component is high, the result is between the bottom component and 255, again
   * using the top component to interpolate between the two.
   *
   * <p>The transition point from low to high depends on the value of {@link
   * #MAX_DARKNESS_BOOST_PER_128} - the higher that value, the lower the transition point.
   *
   * <p>When viewed as a function of the bottom component, this blend mode is built from two linear
   * pieces. The first piece has a slope no less than 1, and the second piece has a slope no greater
   * than one. So in addition to brightening, this function increases the contrast in dark regions,
   * while tapering off in bright regions.
   *
   * <p>The behaviour is actually is very similar to overlay, but where the value at the transition
   * point is always greater than the bottom component (in overlay it can be greater than or less
   * than the bottom component). The relation can be best seen when {@link
   * #MAX_DARKNESS_BOOST_PER_128} is set to 128. It also has a much looser relation to the soft
   * light blend mode, which inspired the idea of constraining the increase of dark components by
   * some multiple.
   *
   * <p>Special cases:
   *
   * <ul>
   *   <li>When the bottom component is 0, the result is 0.
   *   <li>When the bottom component is maxed, the result is maxed.
   *   <li>When the top component is 0, the result is the bottom component.
   *   <li>
   * </ul>
   */
  private static final class ConstrainedBrightenBlender implements Blender {
    private static final int MAX_DARKNESS_BOOST_PER_128 = 128;

    public void blendRow(int[] dstPixels, int[] srcPixels, int[] outPixels, int samples) {
      for (int x = 0; x < samples; ++x) {
        final int srcPixel = srcPixels[x];
        final int dstPixel = dstPixels[x];

        // This keeps the bottom alpha around.
        int resultPixel = dstPixel & (0xFF << 24);

        for (int shift = 0; shift < 24; shift += 8) {
          final var dstC = (dstPixel >>> shift) & 0xFF;
          final var srcC = (srcPixel >>> shift) & 0xFF;

          final var resultC =
              dstC + srcC * Math.min(MAX_DARKNESS_BOOST_PER_128 * dstC / 128, 255 - dstC) / 255;

          resultPixel |= (resultC << shift);
        }

        outPixels[x] = resultPixel;
      }
    }
  }
}
