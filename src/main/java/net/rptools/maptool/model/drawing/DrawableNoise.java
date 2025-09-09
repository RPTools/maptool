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
package net.rptools.maptool.model.drawing;

import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;
import net.rptools.noiselib.PerlinNoise;

/**
 * This class is used to generate a image from a noise function that can be used to break up
 * repeating patterns in other images.
 */
public class DrawableNoise {

  /**
   * Small offset in the X direction in case width period of noise is a multiple of texture its used
   * to break up.
   */
  private static final int OFFFSET_X_TWEAK = 5;

  /**
   * Small offset in the Y direction in case height period of noise is a multiple of texture its
   * used to break up.
   */
  private static final int OFFFSET_Y_TWEAK = 5;

  /** The number of times the noise pattern fits into the noise image width. */
  private static final double WIDTH_DIVISOR = 12.0;

  /** The number of times the noise pattern fits into the noise image height. */
  private static final double HEIGHT_DIVISOR = 9.0;

  /** The width of the noise image. */
  private static final int WIDTH = 256 * (int) WIDTH_DIVISOR;

  /** The height of the noise image. */
  private static final int HEIGHT = 256 * (int) HEIGHT_DIVISOR;

  /** The default seed to used for the generation of noise. */
  private static final long DEFAULT_SEED = 42;

  /** The default alpha to use when applying the noise to another image. */
  private static final float DEFAULT_ALPHA = 0.20f;

  /** Noise generator. */
  private PerlinNoise perlinNoise;

  /** The alpha used to apply this noise to other images. */
  private float noiseAlpha;

  /** Flags if the noise "image" needs recalculation or not. */
  private boolean needsRecalc = true;

  /** The seed used to generate the noise. */
  private long noiseSeed;

  /** The buffered image of the rendered noise. */
  private BufferedImage noiseImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);

  /** Recalculate the noise image. */
  private void recalc() {
    needsRecalc = false;
    int[] array = new int[WIDTH * HEIGHT];
    int alpha = (int) (noiseAlpha * 255) << 24;
    for (int x = 0; x < WIDTH; x++) {
      for (int y = 0; y < HEIGHT; y++) {
        double noiseVal = perlinNoise.noise(x / WIDTH_DIVISOR, y / HEIGHT_DIVISOR);
        int colVal = (int) (255 * noiseVal);
        array[y * WIDTH + x] = colVal | (colVal << 8) | (colVal << 16) | alpha;
      }
    }
    noiseImage.setRGB(0, 0, WIDTH, HEIGHT, array, 0, WIDTH);
  }

  /**
   * Creates a new <code>DrawableNoisePant</code> object with the specified seed and alpha.
   *
   * @param seed The seed used to generate the noise.
   * @param alpha The alpha value the noise will be applied with.
   */
  public DrawableNoise(long seed, float alpha) {
    noiseSeed = seed;
    perlinNoise = new PerlinNoise(seed);
    noiseAlpha = alpha;
    recalc();
  }

  /** Creates a new <code>DrawableNoisePant</code> object with default seed and alpha values. */
  public DrawableNoise() {
    this(DEFAULT_SEED, DEFAULT_ALPHA);
  }

  /**
   * Returns the alpha level that is used to apply the noise.
   *
   * @return The alpha level that is used to apply the noise.
   */
  public float getNoiseAlpha() {
    return noiseAlpha;
  }

  /**
   * Sets the alpha level that is used to apply the noise.
   *
   * @param alpha the alpha level that is used to apply the noise.
   */
  public void setNoiseAlpha(float alpha) {
    noiseAlpha = alpha;
    needsRecalc = true;
    recalc();
  }

  /**
   * Returns the seed used to generate the noise..
   *
   * @return The seed used to generate the noise.
   */
  public long getNoiseSeed() {
    return noiseSeed;
  }

  /**
   * Sets the seed that is used to generate the noise.
   *
   * @param seed the seed that is used to generate the noise.
   */
  public void setNoiseSeed(long seed) {
    noiseSeed = seed;
    perlinNoise = new PerlinNoise(seed);
    recalc();
  }

  /**
   * Returns a {@link Paint} object of the noise.
   *
   * @param offsetX The x offset of the view.
   * @param offsetY The y offset of the view.
   * @param scale The scale of the view.
   * @return a {@link Paint} object that can be used to paint the noise.
   */
  public Paint getPaint(int offsetX, int offsetY, double scale) {
    return new TexturePaint(
        noiseImage,
        new Rectangle(
            offsetX + OFFFSET_X_TWEAK,
            offsetY + OFFFSET_Y_TWEAK,
            (int) (WIDTH * scale),
            (int) (HEIGHT * scale)));
  }

  /**
   * Sets the seed and alpha value for the noise.
   *
   * @param seed The seed value used to generate the noise.
   * @param alpha The alpha used to apply the noise.
   */
  public void setNoiseValues(long seed, float alpha) {
    if (seed != noiseSeed || alpha != noiseAlpha) {
      needsRecalc = true;
    }

    noiseSeed = seed;
    noiseAlpha = alpha;

    if (needsRecalc) {
      recalc();
    }
  }
}
