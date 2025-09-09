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
   * <p>To use to good effect, the initial image should be black (when used together with {@link
   * #OverlaidLights}) or clear (when used with {@link java.awt.AlphaComposite}) and then lights
   * should be added to it one-by-one.
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
    // Technically contexts can be multithreaded, but not in practice. If need be, the buffers could
    // be made thread-local. A length of 4K was chosen as the size since it is larger than most
    // screen widths (a little more than 4K resolution) while not be too high for this sort of use.
    // So this should support typical cases without needing to blend rows in chunks.
    private static final int BUFFER_LENGTH = 4 * 1024;
    private static final int[] SRC_BUFFER = new int[BUFFER_LENGTH];
    private static final int[] DST_BUFFER = new int[BUFFER_LENGTH];

    private final Blender blender;

    public BlenderContext(Blender blender) {
      this.blender = blender;
    }

    @Override
    public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
      final int w = Math.min(src.getWidth(), dstIn.getWidth());
      final int h = Math.min(src.getHeight(), dstIn.getHeight());

      final int[] srcPixels = SRC_BUFFER;
      final int[] dstPixels = DST_BUFFER;

      for (int y = 0; y < h; y++) {
        // region "Fast path". If w < BUFFER_LENGTH, this just blends in one go.
        final var firstChunkLength = (w - 1) % BUFFER_LENGTH + 1;
        src.getDataElements(src.getMinX(), y + src.getMinY(), firstChunkLength, 1, srcPixels);
        dstIn.getDataElements(dstIn.getMinX(), y + dstIn.getMinY(), firstChunkLength, 1, dstPixels);
        blender.blendRow(dstPixels, srcPixels, firstChunkLength);
        dstOut.setDataElements(
            dstOut.getMinX(), y + dstOut.getMinY(), firstChunkLength, 1, dstPixels);
        // endregion

        // region Repeat as necessary. This is meant to not be done very often.
        for (var x = firstChunkLength; x < w; x += BUFFER_LENGTH) {
          src.getDataElements(x + src.getMinX(), y + src.getMinY(), BUFFER_LENGTH, 1, srcPixels);
          dstIn.getDataElements(
              x + dstIn.getMinX(), y + dstIn.getMinY(), BUFFER_LENGTH, 1, dstPixels);
          blender.blendRow(dstPixels, srcPixels, BUFFER_LENGTH);
          dstOut.setDataElements(
              x + dstOut.getMinX(), y + dstOut.getMinY(), BUFFER_LENGTH, 1, dstPixels);
        }
        // endregion
      }
    }

    @Override
    public void dispose() {}
  }

  /**
   * Renormalizes a product of bytes by magically dividing by 255.
   *
   * <p>This is very non-obvious, but is described well in this course sheet: <a
   * href="https://docs.google.com/document/d/1tNrMWShq55rfltcZxAx1N-6f82Dt7MWLDHm-5GQVEnE/edit">Dividing
   * by 255</a>.
   *
   * <p>The SWAR technique of multiplying in parallel is inapplicable to our operations, so that is
   * not represented anywhere.
   *
   * @param x The result of a byte product, in the range 0 .. 0xFFFF.
   * @return The normalized value of x, in the range 0 .. 0xFF.
   */
  private static int renormalize(int x) {
    return (x + (x >>> 8)) >>> 8;
  }

  public interface Blender {
    /**
     * Blend source and destination pixels for a row of pixels.
     *
     * <p>The pixels must be encoded as 32-bit ARGB, and the result will be likewise. Results are
     * written back to `dstPixels`.
     *
     * @param dstPixels The bottom layer pixels.
     * @param srcPixels The top layer pixels.
     * @param samples The number of pixels in the row.
     */
    void blendRow(int[] dstPixels, int[] srcPixels, int samples);
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
    public void blendRow(int[] dstPixels, int[] srcPixels, int samples) {
      for (int x = 0; x < samples; ++x) {
        final int srcPixel = srcPixels[x];
        final int dstPixel = dstPixels[x];

        final int resultR, resultG, resultB;

        {
          final var dstC = (dstPixel >>> 16) & 0xFF;
          final var srcC = (srcPixel >>> 16) & 0xFF;
          resultR = renormalize((255 - srcC) * dstC);
        }
        {
          final var dstC = (dstPixel >>> 8) & 0xFF;
          final var srcC = (srcPixel >>> 8) & 0xFF;
          resultG = renormalize((255 - srcC) * dstC);
        }
        {
          final var dstC = dstPixel & 0xFF;
          final var srcC = srcPixel & 0xFF;
          resultB = renormalize((255 - srcC) * dstC);
        }

        // This keeps the light alpha around instead of the base.
        dstPixels[x] = srcPixel + ((resultR << 16) | (resultG << 8) | resultB);
      }
    }
  }

  /**
   * Inspired by overlay blending, this is an alternative that never darkens and which boosts dark
   * components by no more than some multiple of the component.
   *
   * <p>When the bottom component ({@code dstC}) is low, the result is between the bottom component
   * and twice the bottom component. The exact result is determined by using the top component
   * ({@code srcC}) to interpolate between the two bounds.
   *
   * <p>When the bottom component is high, the result is between the bottom component and 255, again
   * using the top component to interpolate between the two.
   *
   * <p>The transition point from low to high is at 0.5 (or 128 as an int).
   *
   * <p>When viewed as a function of the bottom component, this blend mode is built from two linear
   * pieces. The first piece has a slope no less than 1, and the second piece has a slope no greater
   * than one. So in addition to brightening, this function increases the contrast in dark regions,
   * while tapering off in bright regions.
   *
   * <p>The behaviour is actually is very similar to overlay, but where the value at the transition
   * point is always greater than the bottom component (in overlay it can be greater than or less
   * than the bottom component). It also has a much looser relation to the soft light blend mode,
   * which inspired the idea of constraining the increase of dark components by some multiple.
   *
   * <p>Special cases:
   *
   * <ul>
   *   <li>When the bottom component is 0, the result is 0.
   *   <li>When the bottom component is maxed, the result is maxed.
   *   <li>When the top component is 0, the result is the bottom component.
   * </ul>
   */
  private static final class ConstrainedBrightenBlender implements Blender {
    public void blendRow(int[] dstPixels, int[] srcPixels, int samples) {
      for (int x = 0; x < samples; ++x) {
        final int srcPixel = srcPixels[x];
        final int dstPixel = dstPixels[x];

        final int resultR, resultG, resultB;

        {
          final var dstC = (dstPixel >>> 16) & 0xFF;
          final var srcC = (srcPixel >>> 16) & 0xFF;
          resultR = renormalize(srcC * (dstC < 128 ? dstC : 255 - dstC));
        }
        {
          final var dstC = (dstPixel >>> 8) & 0xFF;
          final var srcC = (srcPixel >>> 8) & 0xFF;
          resultG = renormalize(srcC * (dstC < 128 ? dstC : 255 - dstC));
        }
        {
          final var dstC = dstPixel & 0xFF;
          final var srcC = srcPixel & 0xFF;
          resultB = renormalize(srcC * (dstC < 128 ? dstC : 255 - dstC));
        }

        // This deliberately keeps the bottom alpha around.
        dstPixels[x] = dstPixel + ((resultR << 16) | (resultG << 8) | resultB);
      }
    }
  }
}
