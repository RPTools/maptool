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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.border.BevelBorder;
import net.rptools.maptool.client.MapTool;

/** @author trevor */
public class ColorPickerButton extends JComponent {

  private Color color;
  private String title;

  public ColorPickerButton(String title, Color defaultColor) {
    color = defaultColor;
    this.title = title;

    addMouseListener(
        new MouseAdapter() {

          public void mouseClicked(MouseEvent e) {

            Color oldColor = color;
            Color newColor =
                JColorChooser.showDialog(
                    MapTool.getFrame(), ColorPickerButton.this.title, oldColor);

            if (newColor != null) {
              ColorPickerButton.this.color = newColor;
              repaint();
            }
          }
        });

    setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
  }

  /*
   * (non-Javadoc)
   *
   * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
   */
  protected void paintComponent(Graphics g) {

    Dimension mySize = getSize();

    g.setColor(color);
    g.fillRect(0, 0, mySize.width, mySize.height);
  }

  public Color getSelectedColor() {
    return color;
  }

  /*
   * (non-Javadoc)
   *
   * @see javax.swing.JComponent#getMinimumSize()
   */
  public Dimension getMinimumSize() {
    return new Dimension(16, 16);
  }

  /*
   * (non-Javadoc)
   *
   * @see javax.swing.JComponent#getPreferredSize()
   */
  public Dimension getPreferredSize() {
    return getMinimumSize();
  }

  /*
   * (non-Javadoc)
   *
   * @see javax.swing.JComponent#getMaximumSize()
   */
  public Dimension getMaximumSize() {
    return getMinimumSize();
  }
}
