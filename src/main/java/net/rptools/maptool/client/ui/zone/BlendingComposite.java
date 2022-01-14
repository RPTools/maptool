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
 * A custom Composite class to replace AlphaComposite for the purposes of mixing lights, auras,
 * and other colored effects.
 * http://www.java2s.com/Code/Java/2D-Graphics-GUI/BlendCompositeDemo.htm
 */
class BlendingComposite implements Composite {

  float srcAlphaMultiplier;

  private BlendingComposite() {
    this(1.0f);
  }

  public BlendingComposite(float srcAlphaMultiplier) {
    this.srcAlphaMultiplier = srcAlphaMultiplier;
  }

  public static BlendingComposite getInstance() {
    return new BlendingComposite();
  }

  public static BlendingComposite getInstance(float srcAlphaMultiplier) {
    return new BlendingComposite(srcAlphaMultiplier);
  }

  public float getSrcAlphaMultiplier() {
    return srcAlphaMultiplier;
  }

  private static boolean checkComponentsOrder(ColorModel cm) {
    if (cm instanceof DirectColorModel directCM &&
        cm.getTransferType() == DataBuffer.TYPE_INT) {

      return directCM.getRedMask() == 0x00FF0000 &&
          directCM.getGreenMask() == 0x0000FF00 &&
          directCM.getBlueMask() == 0x000000FF &&
          (directCM.getNumComponents() != 4 ||
              directCM.getAlphaMask() == 0xFF000000);
    }

    return false;
  }

  @Override
  public CompositeContext createContext(
      ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
    if (!checkComponentsOrder(srcColorModel) ||
        !checkComponentsOrder(dstColorModel)) {
      throw new RasterFormatException("Incompatible color models");
    }

    return new BlendingContext(srcAlphaMultiplier);
  }


  private static final class BlendingContext implements CompositeContext {

    float srcAlphaMultiplier;

    public BlendingContext(float srcAlphaMultiplier) {
      this.srcAlphaMultiplier = srcAlphaMultiplier;
    }

    @Override
    public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
      int w = Math.min(src.getWidth(), dstIn.getWidth());
      int h = Math.min(src.getHeight(), dstIn.getHeight());

      int[] srcRgba = new int[4];
      int[] dstRgba = new int[4];
      int[] newRgba = new int[4];
      int[] srcPixels = new int[w];
      int[] dstInPixels = new int[w];
      int[] dstOutPixels = new int[w];

      for (int y = 0; y < h; y++) {
        src.getDataElements(0, y, w, 1, srcPixels);
        dstIn.getDataElements(0, y, w, 1, dstInPixels);

        for (int x = 0; x < w; x++) {
          // pixels are stored as INT_ARGB
          // our arrays are [R, G, B, A]
          int pixel = srcPixels[x];
          srcRgba[0] = (pixel >> 16) & 0xFF;
          srcRgba[1] = (pixel >> 8) & 0xFF;
          srcRgba[2] = (pixel) & 0xFF;
          srcRgba[3] = (pixel >> 24) & 0xFF;

          pixel = dstInPixels[x];
          dstRgba[0] = (pixel >> 16) & 0xFF;
          dstRgba[1] = (pixel >> 8) & 0xFF;
          dstRgba[2] = (pixel) & 0xFF;
          dstRgba[3] = (pixel >> 24) & 0xFF;

          // Combine colors appropriately
          newRgba[0] = Math.min(srcRgba[0] + dstRgba[0], 255);
          newRgba[1] = Math.min(srcRgba[1] + dstRgba[1], 255);
          newRgba[2] = Math.min(srcRgba[2] + dstRgba[2], 255);

//          if (dstRgba[3] == 0) {
//            newRgba[3] = srcRgba[3];
//          } else {
//            newRgba[3] = Math.min(srcRgba[3], dstRgba[3]);
//          }

          // this could be why there's a line between two light sources
          newRgba[3] = Math.max(srcRgba[3], dstRgba[3]);

          // Recombine [R, G, B, A] array to INT_ARGB
          dstOutPixels[x] =
              (newRgba[3] & 0xFF) << 24
                  | (newRgba[0] & 0xFF) << 16
                  | (newRgba[1] & 0xFF) << 8
                  | newRgba[2] & 0xFF;
        }

        dstOut.setDataElements(0, y, w, 1, dstOutPixels);
      }
    }

    @Override
    public void dispose() {}
  }
}