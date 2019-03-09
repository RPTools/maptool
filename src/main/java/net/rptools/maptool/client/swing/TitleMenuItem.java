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
import java.awt.FontMetrics;
import java.awt.Graphics;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

/** */
public class TitleMenuItem extends JMenuItem {

  private String title;

  public TitleMenuItem(String title) {
    super(title);
    setEnabled(false);

    this.title = title;
  }

  /*
   * (non-Javadoc)
   *
   * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
   */
  protected void paintComponent(Graphics g) {

    g.setColor(Color.darkGray);
    g.fillRect(0, 0, getSize().width, getSize().height);

    g.setColor(Color.white);
    FontMetrics fm = g.getFontMetrics();

    int x = (getSize().width - SwingUtilities.computeStringWidth(fm, title)) / 2;
    int y = (getSize().height - fm.getHeight()) / 2 + fm.getAscent();

    g.drawString(title, x, y);
  }
}
