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
package net.rptools.lib.swing;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import net.rptools.lib.image.ImageUtil;

public class ImageLabel {

  private BufferedImage labelBoxLeftImage;
  private BufferedImage labelBoxRightImage;
  private BufferedImage labelBoxMiddleImage;
  private int leftMargin = 4;
  private int rightMargin = 4;

  public ImageLabel(String labelImage, int leftMargin, int rightMargin) {
    this.leftMargin = leftMargin;
    this.rightMargin = rightMargin;

    try {
      parseImage(ImageUtil.getCompatibleImage(labelImage));
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  public void renderLabel(Graphics2D g, int x, int y, int width, int height) {
    g.drawImage(labelBoxLeftImage, x, y, labelBoxLeftImage.getWidth(), height, null);
    g.drawImage(labelBoxRightImage, x + width - rightMargin, y, rightMargin, height, null);
    g.drawImage(
        labelBoxMiddleImage, x + leftMargin, y, width - rightMargin - leftMargin, height, null);
  }

  private void parseImage(BufferedImage image) {

    labelBoxLeftImage = image.getSubimage(0, 0, leftMargin, image.getHeight());
    labelBoxRightImage =
        image.getSubimage(image.getWidth() - rightMargin, 0, rightMargin, image.getHeight());
    labelBoxMiddleImage =
        image.getSubimage(
            leftMargin, 0, image.getWidth() - leftMargin - rightMargin, image.getHeight());
  }
}
