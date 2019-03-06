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

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;

public class TabPopupMenu extends JPopupMenu {

  // private final JComponent parent;
  private int index;

  // TODO: replace index with Tab.TABNAME.index
  public TabPopupMenu(JComponent parent, int index) {
    // this.parent = parent;
    this.index = index;
    add(new AddNewButtonAction());
  }

  private class AddNewButtonAction extends AbstractAction {
    public AddNewButtonAction() {
      putValue(Action.NAME, "New Tab");
    }

    public void actionPerformed(ActionEvent event) {}
  }
}
