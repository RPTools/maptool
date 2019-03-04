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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import net.rptools.lib.image.ImageUtil;

/** @author drice */
public class CreateGridFile {

  private static BufferedImage createImage(
      int width, int height, int gridSize, Color color, Color backgroundColor) {
    BufferedImage image =
        ImageUtil.createCompatibleImage(width, height, BufferedImage.TYPE_INT_RGB);

    Graphics g = image.getGraphics();

    g.setColor(backgroundColor);
    g.fillRect(0, 0, width, height);

    g.setColor(color);
    drawGrid(g, width, height, gridSize);

    return image;
  }

  private static void drawGrid(Graphics g, int width, int height, int gridSize) {
    for (int x = 0; x < width; x += gridSize) {
      g.drawLine(x, 0, x, height - 1);
    }

    for (int y = 0; y < height; y += gridSize) {
      g.drawLine(0, y, width - 1, y);
    }
  }

  public static void main(String[] args) throws Exception {
    BufferedImage image = createImage(501, 501, 10, Color.RED, Color.WHITE);

    ImageIO.write(image, "png", new File("grid_10.png"));
  }
}
