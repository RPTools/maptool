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
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import javax.swing.JPanel;
import net.rptools.maptool.util.GraphicsUtil;

public abstract class MessageDialog extends JPanel {

  public MessageDialog() {
    addMouseListener(new MouseAdapter() {});
    addMouseMotionListener(new MouseMotionAdapter() {});
  }

  protected abstract String getStatus();

  @Override
  protected void paintComponent(Graphics g) {

    Dimension size = getSize();
    g.setColor(new Color(0, 0, 0, .5f));
    g.fillRect(0, 0, size.width, size.height);

    GraphicsUtil.drawBoxedString((Graphics2D) g, getStatus(), size.width / 2, size.height / 2);
  }
}
