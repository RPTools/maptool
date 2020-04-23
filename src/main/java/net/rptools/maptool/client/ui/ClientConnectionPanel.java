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

import java.awt.event.MouseListener;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;
import net.rptools.lib.swing.PopupListener;
import net.rptools.maptool.client.AppActions;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.PlayerListModel;

/**
 * Implements the contents of the Window {@code ->} Connections status panel. Previously this class
 * only displayed a list of connected clients, but it is being extended to include other information
 * as well:
 *
 * <ul>
 *   <li>current map name,
 *   <li>viewing range of current map (as a rectangle of grid coordinates),
 *   <li>whether a macro is running (?),
 *   <li>IP address (for ping/traceroute tests?)
 *   <li>others?
 * </ul>
 */
public class ClientConnectionPanel extends JList {
  public ClientConnectionPanel() {
    setModel(new PlayerListModel(MapTool.getPlayerList()));
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    // setCellRenderer(new DefaultListCellRenderer());

    addMouseListener(createPopupListener());
  }

  private MouseListener createPopupListener() {
    PopupListener listener = new PopupListener(createPopupMenu());
    return listener;
  }

  private JPopupMenu createPopupMenu() {
    JPopupMenu menu = new JPopupMenu();
    menu.add(new JMenuItem(AppActions.BOOT_CONNECTED_PLAYER));
    menu.add(new JMenuItem(AppActions.WHISPER_PLAYER));
    return menu;
  }
}
