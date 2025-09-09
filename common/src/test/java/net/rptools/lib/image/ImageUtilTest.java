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
package net.rptools.lib.image;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import org.junit.jupiter.api.Test;

public class ImageUtilTest {

  @Test
  void testPickTransparencyNoShortcut() throws IOException {
    BufferedImage bufferedImage =
        ImageUtil.getCompatibleImage("net/rptools/lib/image/squareToken.gif");
    Image img = bufferedImage.getScaledInstance(10, 10, Image.SCALE_FAST);
    assertEquals(Transparency.OPAQUE, ImageUtil.pickBestTransparency(img));

    bufferedImage = ImageUtil.getCompatibleImage("net/rptools/lib/image/circleToken.png");
    img = bufferedImage.getScaledInstance(10, 10, Image.SCALE_FAST);
    assertEquals(Transparency.BITMASK, ImageUtil.pickBestTransparency(img));

    bufferedImage = ImageUtil.getCompatibleImage("net/rptools/lib/image/cross.png");
    img = bufferedImage.getScaledInstance(10, 10, Image.SCALE_FAST);
    assertEquals(Transparency.TRANSLUCENT, ImageUtil.pickBestTransparency(img));
  }

  @Test
  void testPickTransparencyShortcut() throws IOException {
    Image img = ImageUtil.getCompatibleImage("net/rptools/lib/image/squareToken.gif");
    assertEquals(Transparency.OPAQUE, ImageUtil.pickBestTransparency(img));

    img = ImageUtil.getCompatibleImage("net/rptools/lib/image/circleToken.png");
    assertEquals(Transparency.BITMASK, ImageUtil.pickBestTransparency(img));

    img = ImageUtil.getCompatibleImage("net/rptools/lib/image/cross.png");
    assertEquals(Transparency.TRANSLUCENT, ImageUtil.pickBestTransparency(img));
  }

  @Test
  void testReadSvgAsBufferedImage() throws IOException {
    Image img = ImageUtil.getCompatibleImage("net/rptools/lib/image/star.svg");
    assertEquals(255, img.getWidth(null));
    assertEquals(240, img.getHeight(null));
  }

  @Test
  void testReadTiffAsBufferedImage() throws IOException {
    Image img = ImageUtil.getCompatibleImage("net/rptools/lib/image/temple.tif");
    assertEquals(583, img.getWidth(null));
    assertEquals(738, img.getHeight(null));
  }
}
