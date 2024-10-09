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
package net.rptools.maptool.client.ui.connections;

import com.google.common.eventbus.Subscribe;
import java.awt.BorderLayout;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import net.rptools.maptool.client.AppActions;
import net.rptools.maptool.client.events.PlayerConnected;
import net.rptools.maptool.client.events.PlayerDisconnected;
import net.rptools.maptool.client.events.PlayerStatusChanged;
import net.rptools.maptool.client.events.ServerDisconnected;
import net.rptools.maptool.client.swing.PopupListener;
import net.rptools.maptool.events.MapToolEventBus;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.player.Player;
import net.rptools.maptool.model.player.Player.Role;
import net.rptools.maptool.model.player.PlayerAwaitingApproval;

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
public class ClientConnectionPanel extends JPanel {
  /** List of connected players. */
  private final JList<Player> list = new JList<>();

  private final DefaultListModel<Player> listModel;

  /** List of players awaiting approval. */
  private final List<PlayerAwaitingApproval> awaitingApprovalList;

  /**
   * JTable for players awaiting approval, a table with a single column is used rather than a list
   * as a swing list doesn't allow interactive components.
   */
  private final JTable awaitingApprovalTable = new JTable();

  /** The table model for the awaiting approval table. */
  private final PlayerPendingApprovalTableModel awaitingApprovalTableModel;

  /** The tabbed pane for the current connections and those awaiting approval. */
  private final JTabbedPane tabbedPane;

  /** Creates a new instance of {@code ClientConnectionPanel}. */
  public ClientConnectionPanel() {
    setLayout(new BorderLayout());
    tabbedPane = new JTabbedPane();
    add(tabbedPane, BorderLayout.CENTER);

    listModel = new DefaultListModel<>();
    list.setModel(listModel);
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    list.setCellRenderer(new PlayerListCellRenderer());

    list.addMouseListener(createPopupListener());

    JPanel connectedPanel = new JPanel();
    connectedPanel.setLayout(new BorderLayout());
    connectedPanel.add(new JScrollPane(list), BorderLayout.CENTER);

    tabbedPane.add(I18N.getString("connections.tab.connected"), connectedPanel);

    awaitingApprovalList = Collections.synchronizedList(new ArrayList<>());
    awaitingApprovalTableModel = new PlayerPendingApprovalTableModel(awaitingApprovalList);
    awaitingApprovalTable.setModel(awaitingApprovalTableModel);
    awaitingApprovalTable.setDefaultRenderer(
        PlayerAwaitingApproval.class, new PlayerPendingApprovalCellRenderer(this::updateRole));
    awaitingApprovalTable.setRowHeight(100);
    awaitingApprovalTable.setDefaultEditor(
        PlayerAwaitingApproval.class, new PlayerPendingApprovalCellEditor(this::updateRole));
    tabbedPane.add(
        I18N.getString("connections.tab.pending"), new JScrollPane(awaitingApprovalTable));
    tabbedPane.setEnabledAt(1, false);

    new MapToolEventBus().getMainEventBus().register(this);
  }

  @Subscribe
  private void onPlayerConnected(PlayerConnected event) {
    if (event.isLocal()) {
      return;
    }

    listModel.addElement(event.player());
  }

  @Subscribe
  private void onPlayerStatusChanged(PlayerStatusChanged event) {
    var index = listModel.indexOf(event.player());
    if (index != -1) {
      listModel.set(index, event.player());
    }
  }

  @Subscribe
  private void onPlayerDisconnected(PlayerDisconnected event) {
    listModel.removeElement(event.player());
  }

  @Subscribe
  private void onServerDisconnected(ServerDisconnected event) {
    listModel.clear();
  }

  /**
   * Update the role of the player in the pending player list.
   *
   * @param player the player to update.
   * @param role the new role.
   */
  private void updateRole(PlayerAwaitingApproval player, Role role) {
    for (int i = 0; i < awaitingApprovalList.size(); i++) {
      var storedPlayer = awaitingApprovalList.get(i);
      if (player.name().equals(storedPlayer.name())) {
        if (role != storedPlayer.role()) {
          awaitingApprovalList.set(
              i,
              new PlayerAwaitingApproval(
                  storedPlayer.name(),
                  storedPlayer.pin(),
                  role,
                  storedPlayer.publicKey(),
                  storedPlayer.approveCallback(),
                  storedPlayer.denyCallback()));
        }
        break;
      }
    }
    awaitingApprovalTable.revalidate();
  }

  /**
   * Creates a mouse listener for the connected players list.
   *
   * @return a mouse listener for the connected players list.
   */
  private MouseListener createPopupListener() {
    PopupListener listener = new PopupListener(createPopupMenu());
    return listener;
  }

  /**
   * Creates a popup menu for the connected players list.
   *
   * @return a popup menu for the connected players list.
   */
  private JPopupMenu createPopupMenu() {
    JPopupMenu menu = new JPopupMenu();
    menu.add(new JMenuItem(AppActions.BOOT_CONNECTED_PLAYER));
    menu.add(new JMenuItem(AppActions.WHISPER_PLAYER));
    return menu;
  }

  /**
   * Returns the currently selected player in the connected players list.
   *
   * @return the currently selected player in the connected players list.
   */
  public Player getSelectedPlayer() {
    return list.getSelectedValue();
  }

  /**
   * Adds a player to the list of players awaiting approval.
   *
   * @param player the player to add.
   */
  public void addAwaitingApproval(PlayerAwaitingApproval player) {
    awaitingApprovalList.add(player);
    awaitingApprovalTableModel.fireTableStructureChanged();
  }

  /**
   * Removes a player from the list of players awaiting approval.
   *
   * @param name the name of the player to remove.
   */
  public void removeAwaitingApproval(String name) {
    awaitingApprovalList.removeIf(p -> p.name().equals(name));
    awaitingApprovalTableModel.fireTableStructureChanged();
  }

  /** Sets up the connection panel for the hosting server. */
  public void startHosting() {
    tabbedPane.setEnabledAt(1, true);
  }

  /** Sets up the connection panel for a client. */
  public void stopHosting() {
    awaitingApprovalList.clear();
    awaitingApprovalTableModel.fireTableStructureChanged();
    tabbedPane.setSelectedIndex(0);
    tabbedPane.setEnabledAt(1, false);
  }
}
