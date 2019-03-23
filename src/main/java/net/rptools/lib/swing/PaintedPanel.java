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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class PaintedPanel extends JPanel {

  private Paint paint;

  public PaintedPanel() {
    this(null);
  }

  public PaintedPanel(Paint paint) {
    this.paint = paint;
    setMinimumSize(new Dimension(10, 10));
    setPreferredSize(getMinimumSize());
  }

  public Paint getPaint() {
    return paint;
  }

  public void setPaint(Paint paint) {
    this.paint = paint;
    repaint();
  }

  @Override
  protected void paintComponent(Graphics g) {

    Dimension size = getSize();
    g.setColor(getBackground());
    g.fillRect(0, 0, size.width, size.height);

    if (paint != null) {
      ((Graphics2D) g).setPaint(paint);
      g.fillRect(0, 0, size.width, size.height);
    } else {
      try {
        BufferedImage texture;
        texture =
            ImageIO.read(getClass().getResource("/net/rptools/lib/image/icons/transparent.png"));
        TexturePaint tp = new TexturePaint(texture, new Rectangle(0, 0, 28, 28));
        ((Graphics2D) g).setPaint(tp);
        g.fillRect(0, 0, size.width, size.height);
      } catch (IOException e) {
        System.out.println(e.getMessage());
        g.setColor(Color.white);
        g.fillRect(0, 0, size.width, size.height);
        g.setColor(Color.red);
        g.drawLine(size.width - 1, 0, 0, size.height - 1);
      }
    }
  }
}
