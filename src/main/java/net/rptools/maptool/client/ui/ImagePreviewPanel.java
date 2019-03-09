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
package net.rptools.maptool.client.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.JComponent;
import net.rptools.lib.swing.SwingUtil;

@SuppressWarnings("serial")
public class ImagePreviewPanel extends JComponent {

  private Image img;

  public ImagePreviewPanel() {
    setPreferredSize(new Dimension(150, 100));
    setMinimumSize(new Dimension(150, 100));
  }

  public void setImage(Image image) {

    this.img = image;
    repaint();
  }

  public Image getImage() {
    return img;
  }

  @Override
  protected void paintComponent(Graphics g) {

    // Image
    Dimension size = getSize();
    if (img != null) {
      Dimension imgSize = new Dimension(img.getWidth(null), img.getHeight(null));
      SwingUtil.constrainTo(imgSize, size.width, size.height);

      // Border
      int x = (size.width - imgSize.width) / 2;
      int y = (size.height - imgSize.height) / 2;

      g.drawImage(img, x, y, imgSize.width, imgSize.height, null);
      g.setColor(Color.black);
      g.drawRect(x, y, imgSize.width - 1, imgSize.height - 1);
    }
  }
}
