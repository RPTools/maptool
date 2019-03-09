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

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import net.rptools.maptool.client.AppActions.ClientAction;

/**
 * This little baby will keep the menu items selected state intact. Not the most elegant, but works
 */
public class RPCheckBoxMenuItem extends JCheckBoxMenuItem implements MenuListener {

  public RPCheckBoxMenuItem(Action action, JMenu parentMenu) {
    super(action);

    parentMenu.addMenuListener(this);
  }

  public void menuSelected(MenuEvent e) {
    Action action = getAction();
    if (action instanceof ClientAction) {
      setSelected(((ClientAction) action).isSelected());
    }
  }

  public void menuCanceled(MenuEvent e) {}

  public void menuDeselected(MenuEvent e) {}
}
