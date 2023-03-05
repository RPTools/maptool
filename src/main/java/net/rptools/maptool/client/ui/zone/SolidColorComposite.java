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
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.Arrays;

/** A simple composite to rendering a single output colour regardless of the inputs. */
public class SolidColorComposite implements Composite {
  private final int colour;

  public SolidColorComposite(int argb) {
    this.colour = argb;
  }

  @Override
  public CompositeContext createContext(
      ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
    return new BlackCompositeContext();
  }

  public final class BlackCompositeContext implements CompositeContext {
    @Override
    public void dispose() {}

    @Override
    public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
      final int w = Math.min(src.getWidth(), dstIn.getWidth());
      final int h = Math.min(src.getHeight(), dstIn.getHeight());

      final int[] dstPixels = new int[w];
      Arrays.fill(dstPixels, SolidColorComposite.this.colour);

      for (int y = 0; y < h; y++) {
        dstOut.setDataElements(0, y, w, 1, dstPixels);
      }
    }
  }
}
