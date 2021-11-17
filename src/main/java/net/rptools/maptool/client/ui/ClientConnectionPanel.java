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
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
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

  private final List<PlayerAwaitingApproval> awaitingApprovalList;

  private final DefaultListModel<PlayerAwaitingApproval> awaitingApprovalModel =
      new DefaultListModel<>();

  private static class PendingPlayers extends JPanel {

    private final List<PlayerAwaitingApproval> awaitingApprovalList;
    private final JPanel pendingPanel = new JPanel();

    private PendingPlayers(List<PlayerAwaitingApproval> awaitingApprovalList) {
      this.awaitingApprovalList = awaitingApprovalList;
      setLayout(new BorderLayout());
      pendingPanel.setLayout(new BoxLayout(pendingPanel, BoxLayout.Y_AXIS));
      add(new JScrollPane(pendingPanel), BorderLayout.CENTER);
    }

    @Override
    public void repaint() {
      if (awaitingApprovalList != null) {
        for (PlayerAwaitingApproval player : awaitingApprovalList) {
          JPanel panel = new JPanel();
          panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
          JPanel playerPanel = new JPanel(new FlowLayout());
          JLabel playerLabel = new JLabel(player.name());
          Font font = playerLabel.getFont();
          font = font.deriveFont(Font.BOLD).deriveFont(font.getSize() + 6.0f);
          playerLabel.setFont(font);
          JLabel pinLabel = new JLabel(player.pin());
          pinLabel.setFont(font);
          playerPanel.add(playerLabel);
          playerPanel.add(pinLabel);
          JPanel buttonPanel = new JPanel(new FlowLayout());
          JButton cancelButton = new JButton("Cancel");
          JButton approveButton = new JButton("Approve");
          JCheckBox gmCheckBox = new JCheckBox("is GM?");
          buttonPanel.add(cancelButton);
          buttonPanel.add(approveButton);
          buttonPanel.add(gmCheckBox);
          panel.add(playerPanel);
          panel.add(buttonPanel);
          panel.setBorder(BorderFactory.createEmptyBorder(5, 1, 5, 1));
          pendingPanel.add(panel);
        }
      }
    }
  }

  public ClientConnectionPanel() {
    setLayout(new BorderLayout());
    JTabbedPane tabbedPane = new JTabbedPane();
    add(tabbedPane, BorderLayout.CENTER);
    list.setModel(new PlayerListModel(MapTool.getPlayerList()));
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    list.addMouseListener(createPopupListener());

    JPanel connectedPanel = new JPanel();
    connectedPanel.setLayout(new BorderLayout());
    connectedPanel.add(new JScrollPane(list), BorderLayout.CENTER);

    tabbedPane.add("Connected", connectedPanel);

    awaitingApprovalList = new ArrayList<>();
    tabbedPane.add("Pending", new PendingPlayers(awaitingApprovalList));

    addAwaitingApproval(new PlayerAwaitingApproval("Bob", "1234", Player.Role.PLAYER));
    addAwaitingApproval(new PlayerAwaitingApproval("Bob", "1234", Player.Role.PLAYER));
    addAwaitingApproval(new PlayerAwaitingApproval("Bob", "1234", Player.Role.PLAYER));
    addAwaitingApproval(new PlayerAwaitingApproval("Bob", "1234", Player.Role.PLAYER));
    addAwaitingApproval(new PlayerAwaitingApproval("Bob", "1234", Player.Role.PLAYER));
    addAwaitingApproval(new PlayerAwaitingApproval("Bob", "1234", Player.Role.PLAYER));
    addAwaitingApproval(new PlayerAwaitingApproval("Bob", "1234", Player.Role.PLAYER));
    addAwaitingApproval(new PlayerAwaitingApproval("Bob", "1234", Player.Role.PLAYER));
    addAwaitingApproval(new PlayerAwaitingApproval("Bob", "1234", Player.Role.PLAYER));
    addAwaitingApproval(new PlayerAwaitingApproval("Bob", "1234", Player.Role.PLAYER));
    addAwaitingApproval(new PlayerAwaitingApproval("Bob", "1234", Player.Role.PLAYER));
    addAwaitingApproval(new PlayerAwaitingApproval("Bob", "1234", Player.Role.PLAYER));
    addAwaitingApproval(new PlayerAwaitingApproval("Bob", "1234", Player.Role.PLAYER));
    addAwaitingApproval(new PlayerAwaitingApproval("Bob", "1234", Player.Role.PLAYER));
    addAwaitingApproval(new PlayerAwaitingApproval("Bob", "1234", Player.Role.PLAYER));
    addAwaitingApproval(new PlayerAwaitingApproval("Bob", "1234", Player.Role.PLAYER));
    addAwaitingApproval(new PlayerAwaitingApproval("Bob", "1234", Player.Role.PLAYER));
    addAwaitingApproval(new PlayerAwaitingApproval("Bob", "1234", Player.Role.PLAYER));
    addAwaitingApproval(new PlayerAwaitingApproval("Bob", "1234", Player.Role.PLAYER));
    addAwaitingApproval(new PlayerAwaitingApproval("Bob", "1234", Player.Role.PLAYER));
    addAwaitingApproval(new PlayerAwaitingApproval("Bob", "1234", Player.Role.PLAYER));
    addAwaitingApproval(new PlayerAwaitingApproval("Bob", "1234", Player.Role.PLAYER));
    addAwaitingApproval(new PlayerAwaitingApproval("Bob", "1234", Player.Role.PLAYER));
    addAwaitingApproval(new PlayerAwaitingApproval("Bob", "1234", Player.Role.PLAYER));
    addAwaitingApproval(new PlayerAwaitingApproval("Bob", "1234", Player.Role.PLAYER));
    addAwaitingApproval(new PlayerAwaitingApproval("Bob", "1234", Player.Role.PLAYER));
    addAwaitingApproval(new PlayerAwaitingApproval("Bob", "1234", Player.Role.PLAYER));
    addAwaitingApproval(new PlayerAwaitingApproval("Bob", "1234", Player.Role.PLAYER));
    addAwaitingApproval(new PlayerAwaitingApproval("Bob", "1234", Player.Role.PLAYER));
    addAwaitingApproval(new PlayerAwaitingApproval("Bob", "1234", Player.Role.PLAYER));
    addAwaitingApproval(new PlayerAwaitingApproval("Bob", "1234", Player.Role.PLAYER));
    addAwaitingApproval(new PlayerAwaitingApproval("Bob", "1234", Player.Role.PLAYER));
    addAwaitingApproval(new PlayerAwaitingApproval("Bob", "1234", Player.Role.PLAYER));
    addAwaitingApproval(new PlayerAwaitingApproval("Bob", "1234", Player.Role.PLAYER));
    addAwaitingApproval(new PlayerAwaitingApproval("Bob", "1234", Player.Role.PLAYER));
    addAwaitingApproval(new PlayerAwaitingApproval("Bob", "1234", Player.Role.PLAYER));
    addAwaitingApproval(new PlayerAwaitingApproval("Bob", "1234", Player.Role.PLAYER));
    addAwaitingApproval(new PlayerAwaitingApproval("Bob", "1234", Player.Role.PLAYER));
    addAwaitingApproval(new PlayerAwaitingApproval("Bob", "1234", Player.Role.PLAYER));
    addAwaitingApproval(new PlayerAwaitingApproval("Bob", "1234", Player.Role.PLAYER));
    addAwaitingApproval(new PlayerAwaitingApproval("Bob", "1234", Player.Role.PLAYER));
    addAwaitingApproval(new PlayerAwaitingApproval("Bob", "1234", Player.Role.PLAYER));
    addAwaitingApproval(new PlayerAwaitingApproval("Bob", "1234", Player.Role.PLAYER));
    addAwaitingApproval(new PlayerAwaitingApproval("Bob", "1234", Player.Role.PLAYER));
    addAwaitingApproval(new PlayerAwaitingApproval("Bob", "1234", Player.Role.PLAYER));
    addAwaitingApproval(new PlayerAwaitingApproval("Bob", "1234", Player.Role.PLAYER));
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

  public void addAwaitingApproval(PlayerAwaitingApproval player) {
    awaitingApprovalList.add(player);
    repaint();
  }
}
