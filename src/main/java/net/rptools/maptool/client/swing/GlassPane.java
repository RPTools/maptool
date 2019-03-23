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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class GlassPane extends JPanel {
  private BufferedImage backImage;

  private boolean modal;

  public GlassPane() {
    super(null); // No layout manager
  }

  public void setModel(boolean modal) {
    this.modal = modal;
  }

  @Override
  public void setVisible(boolean aFlag) {

    if (modal) {
      JComponent root = getRootPane();
      Dimension size = root.getSize();
      if (backImage == null
          || backImage.getWidth() != size.width
          || backImage.getHeight() != size.height) {
        backImage = new BufferedImage(size.width, size.height, Transparency.OPAQUE);
      }

      // Get a copy of the current application state
      Graphics2D g = backImage.createGraphics();
      g.setClip(0, 0, size.width, size.height);
      root.paint(g);

      // Shade it
      g.setColor(new Color(1, 1, 1, .5f));
      g.fillRect(0, 0, size.width, size.height);

      g.dispose();

      // Consume all actions
      addMouseMotionListener(new MouseMotionAdapter() {});
      addMouseListener(new MouseAdapter() {});
    } else {
      for (MouseMotionListener listener : getMouseMotionListeners()) {
        removeMouseMotionListener(listener);
      }
      for (MouseListener listener : getMouseListeners()) {
        removeMouseListener(listener);
      }
    }

    setOpaque(modal);

    super.setVisible(aFlag);

    if (getComponents().length > 0) {
      getComponents()[0].requestFocus();
    }
  }

  @Override
  protected void paintComponent(Graphics g) {

    if (!modal) {
      return;
    }

    // Show the application contents behind the pane
    g.drawImage(backImage, 0, 0, this);
  }
}
