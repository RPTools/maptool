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
package net.rptools.maptool.client.swing;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import net.rptools.lib.swing.SwingUtil;

public class DragImageGlassPane extends JPanel {
  private static final int DEFAULT_MAX_SIZE = 75;

  private BufferedImage image;
  private Point location;

  public DragImageGlassPane() {
    setOpaque(false);
  }

  public void setImage(BufferedImage image) {
    this.image = image;

    if (image == null) {
      location = null;
    }
    repaint();
  }

  public void setImagePosition(Point p) {
    if (p.equals(location)) {
      return;
    }
    if (p != null && image != null) {
      Dimension size = getImageSize();

      if (location == null) {
        location = p;
      }
      Rectangle newRect = new Rectangle(p.x, p.y, size.width, size.height);
      Rectangle oldRect = new Rectangle(location.x, location.y, size.width, size.height);

      newRect.add(oldRect);
      newRect.translate(-size.width / 2, -size.height / 2); // account for drawing centered on point

      repaint(newRect);

      location = new Point(p);
    }
  }

  @Override
  protected void paintComponent(Graphics g) {
    if (image == null || location == null) {
      return;
    }
    Dimension size = getImageSize();
    g.drawImage(
        image,
        location.x - (size.width / 2),
        location.y - (size.height / 2),
        size.width,
        size.height,
        this);
  }

  private Dimension getImageSize() {
    Dimension size = new Dimension(image.getWidth(), image.getHeight());
    if (size.width > DEFAULT_MAX_SIZE || size.height > DEFAULT_MAX_SIZE) {
      SwingUtil.constrainTo(size, DEFAULT_MAX_SIZE);
    }
    return size;
  }
}
