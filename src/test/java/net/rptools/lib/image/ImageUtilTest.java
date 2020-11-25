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
        ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/squareToken.gif");
    Image img = bufferedImage.getScaledInstance(10, 10, Image.SCALE_FAST);
    assertEquals(ImageUtil.pickBestTransparency(img), Transparency.OPAQUE);

    bufferedImage =
        ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/circleToken.png");
    img = bufferedImage.getScaledInstance(10, 10, Image.SCALE_FAST);
    assertEquals(ImageUtil.pickBestTransparency(img), Transparency.BITMASK);

    bufferedImage = ImageUtil.getCompatibleImage("net/rptools/lib/image/icons/cross.png");
    img = bufferedImage.getScaledInstance(10, 10, Image.SCALE_FAST);
    assertEquals(ImageUtil.pickBestTransparency(img), Transparency.TRANSLUCENT);
  }

  @Test
  void testPickTransparencyShortcut() throws IOException {
    Image img = ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/squareToken.gif");
    assertEquals(ImageUtil.pickBestTransparency(img), Transparency.OPAQUE);

    img = ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/circleToken.png");
    assertEquals(ImageUtil.pickBestTransparency(img), Transparency.BITMASK);

    img = ImageUtil.getCompatibleImage("net/rptools/lib/image/icons/cross.png");
    assertEquals(ImageUtil.pickBestTransparency(img), Transparency.TRANSLUCENT);
  }

  @Test
  void testReadSvgAsBufferedImage() throws IOException {
    Image img = ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/star.svg");
    assertEquals(img.getWidth(null), 51);
    assertEquals(img.getHeight(null), 48);
  }

  @Test
  void testReadTiffAsBufferedImage() throws IOException {
    Image img = ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/temple.tif");
    assertEquals(img.getWidth(null), 583);
    assertEquals(img.getHeight(null), 738);
  }
}
