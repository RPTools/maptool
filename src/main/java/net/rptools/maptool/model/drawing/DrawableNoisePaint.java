package net.rptools.maptool.model.drawing;

import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;
import net.rptools.noiselib.PerlinNoise;

public class DrawableNoisePaint {

  private static final int WIDTH = 3840;
  private static final int HEIGHT = 2304;

  private static final double WIDTH_DIVISOR = 15.0;
  private static final double HEIGHT_DIVISOR = 9.0;

  private static final long DEFAULT_SEED = 42;
  private static final float DEFAULT_ALPHA = 0.15f;


  private PerlinNoise perlinNoise;

  private float noiseAlpha;

  private boolean needsRecalc = true;

  private long noiseSeed;



  private BufferedImage noiseImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);


  private void recalc() {
    needsRecalc = false;
    int[] array = new int[WIDTH * HEIGHT];
    int alpha = (int)(noiseAlpha * 255) << 24;
    for (int x = 0; x < WIDTH; x++) {
      for (int y = 0; y < HEIGHT; y++) {
        double noiseVal = perlinNoise.noise(x / WIDTH_DIVISOR,   y / HEIGHT_DIVISOR) ;
        int colVal = (int)(255 * noiseVal);
        array[y * WIDTH + x] = colVal | (colVal << 8) | (colVal << 16) | alpha;
      }
    }
    noiseImage.setRGB(0,0, WIDTH, HEIGHT, array, 0, WIDTH);
  }

  public DrawableNoisePaint(long seed, float alpha) {
      noiseSeed = seed;
      perlinNoise = new PerlinNoise(seed);
      noiseAlpha = alpha;
      recalc();
  }

  public DrawableNoisePaint() {
    this(DEFAULT_SEED, DEFAULT_ALPHA);
  }


  public float getNoiseAlpha() {
    return noiseAlpha;
  }

  public void setNoiseAlpha(float alpha) {
    noiseAlpha = alpha;
    needsRecalc = true;
    recalc();
  }

  public long getNoiseSeed() {
    return noiseSeed;
  }

  public void setNoiseSeed(long seed) {
    noiseSeed = seed;
    perlinNoise = new PerlinNoise(seed);
    recalc();
  }

  public Paint getPaint(int offsetX, int offsetY, double scale) {
    return new TexturePaint(
        noiseImage,
        new Rectangle(offsetX, offsetY, (int)(WIDTH * scale), (int)(HEIGHT * scale))
    );
  }

}
