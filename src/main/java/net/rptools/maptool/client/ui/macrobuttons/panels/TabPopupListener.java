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
package net.rptools.maptool.client.ui.macrobuttons.panels;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

public class TabPopupListener extends MouseAdapter {

  private JComponent component;
  private int index;

  public TabPopupListener(JComponent component, int index) {
    this.component = component;
    this.index = index;
  }

  public void mousePressed(MouseEvent e) {
    if (SwingUtilities.isRightMouseButton(e)) {
      new TabPopupMenu(component, index).show(component, e.getX(), e.getY());
    } else {
      // System.out.println("Tab index: " + ((JTabbedPane) component).indexAtLocation(e.getX(),
      // e.getY()));
    }
  }
}
