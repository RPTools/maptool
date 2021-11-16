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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.MouseListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import net.rptools.lib.swing.PopupListener;
import net.rptools.maptool.client.AppActions;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.PlayerListModel;
import net.rptools.maptool.model.player.Player;
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
  private final JList<Player> list = new JList<>();
  private final JTabbedPane tabbedPane = new JTabbedPane();

  private final JPanel connectedPanel = new JPanel();
  private final JPanel pendingPannel = new JPanel();

  private class PendingPlayerRenderer extends JPanel
      implements ListCellRenderer<PlayerAwaitingApproval> {

    @Override
    public Component getListCellRendererComponent(
        JList<? extends PlayerAwaitingApproval> list,
        PlayerAwaitingApproval value,
        int index,
        boolean isSelected,
        boolean cellHasFocus) {
      JPanel panel = new JPanel(new BorderLayout());
      JPanel playerPanel = new JPanel(new FlowLayout());
      playerPanel.add(new JLabel(value.name()));
      playerPanel.add(new JLabel(value.pin()));
      JPanel buttonPanel = new JPanel(new FlowLayout());
      JButton cancelButton = new JButton("Cancel");
      JButton approveButton = new JButton("Approve");
      buttonPanel.add(cancelButton);
      buttonPanel.add(approveButton);
      panel.add(playerPanel, BorderLayout.CENTER);
      panel.add(buttonPanel, BorderLayout.SOUTH);
      return panel;
    }
  }

  public ClientConnectionPanel() {
    setLayout(new BorderLayout());
    add(tabbedPane, BorderLayout.CENTER);
    list.setModel(new PlayerListModel(MapTool.getPlayerList()));
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    // setCellRenderer(new DefaultListCellRenderer());

    list.addMouseListener(createPopupListener());

    connectedPanel.setLayout(new BorderLayout());
    connectedPanel.add(new JScrollPane(list), BorderLayout.CENTER);

    tabbedPane.add("Connected", connectedPanel);

    tabbedPane.add("Pending", pendingPannel);
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

  public Player getSelectedPlayer() {
    return list.getSelectedValue();
  }
}
