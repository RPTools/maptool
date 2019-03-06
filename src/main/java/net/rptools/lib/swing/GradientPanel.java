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
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Paint;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class GradientPanel extends JPanel {

  private Color c1;
  private Color c2;

  public GradientPanel(Color c1, Color c2) {
    this(c1, c2, new FlowLayout());
  }

  public GradientPanel(Color c1, Color c2, LayoutManager layout) {
    super(layout);

    this.c1 = c1;
    this.c2 = c2;
  }

  @Override
  protected void paintComponent(Graphics g) {

    Graphics2D g2d = (Graphics2D) g;
    Paint p = new GradientPaint(0, 0, c1, getSize().width, 0, c2);
    Paint oldPaint = g2d.getPaint();
    g2d.setPaint(p);
    g2d.fillRect(0, 0, getSize().width, getSize().height);
    g2d.setPaint(oldPaint);
  }
}
