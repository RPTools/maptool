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

import java.awt.Font;
import java.util.function.BiConsumer;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.player.Player;
import net.rptools.maptool.model.player.Player.Role;
import net.rptools.maptool.model.player.PlayerAwaitingApproval;

public class CellContents {

  private final JPanel panel = new JPanel();
  private final JLabel name;
  private final JLabel pin;
  private final JCheckBox gmCheckBox;

  private final BiConsumer<PlayerAwaitingApproval, Player.Role> updateRoleCCallback;

  public CellContents(
      PlayerAwaitingApproval player, BiConsumer<PlayerAwaitingApproval, Role> updateRole) {
    updateRoleCCallback = updateRole;
    Font font = panel.getFont().deriveFont(Font.BOLD);
    // Name
    JLabel nameLabel = new JLabel("Name");
    nameLabel.setFont(font);
    nameLabel.setOpaque(false);
    name = new JLabel("");
    name.setFont(font);
    name.setOpaque(false);
    name.setText(player.name());

    JPanel namePanel = new JPanel();
    namePanel.setOpaque(false);
    namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.X_AXIS));
    namePanel.add(nameLabel);
    namePanel.add(Box.createHorizontalGlue());
    namePanel.add(name);

    // Pin
    JLabel pinLabel = new JLabel("Pin");
    pinLabel.setFont(font);
    pinLabel.setOpaque(false);
    pin = new JLabel("");
    pin.setFont(font);
    pin.setOpaque(false);

    JPanel pinPanel = new JPanel();
    pinPanel.setOpaque(false);
    pinPanel.setLayout(new BoxLayout(pinPanel, BoxLayout.X_AXIS));
    pinPanel.add(pinLabel);
    pinPanel.add(Box.createHorizontalGlue());
    pinPanel.add(pin);
    pin.setText(player.pin());

    // Role Panel
    JLabel roleLabel = new JLabel("Role");
    roleLabel.setOpaque(false);
    roleLabel.setFont(font);
    gmCheckBox = new JCheckBox("GM");
    gmCheckBox.setFont(font);
    gmCheckBox.setOpaque(false);

    JPanel rolePanel = new JPanel();
    rolePanel.setOpaque(false);
    rolePanel.setLayout(new BoxLayout(rolePanel, BoxLayout.X_AXIS));
    rolePanel.add(roleLabel);
    rolePanel.add(Box.createHorizontalGlue());
    rolePanel.add(gmCheckBox);
    if (player.role() == Player.Role.GM) {
      gmCheckBox.setSelected(true);
    }

    // Buttons

    JPanel buttonPanel = new JPanel();
    buttonPanel.setOpaque(false);
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
    JButton approveButton = new JButton("Approve");
    approveButton.addActionListener(l -> player.approveCallback().accept(player));
    buttonPanel.add(approveButton);
    buttonPanel.add(Box.createHorizontalStrut(40));
    JButton denyButton = new JButton("Deny");
    denyButton.addActionListener(l -> player.denyCallback().accept(player));
    buttonPanel.add(denyButton);

    gmCheckBox.addActionListener(
        e -> {
          updateRole(player, gmCheckBox.isSelected() ? Role.GM : Role.PLAYER);
        });
    approveButton.addActionListener(
        l -> approve(player, gmCheckBox.isSelected() ? Role.GM : Role.PLAYER));
    denyButton.addActionListener(l -> deny(player));

    panel.setOpaque(false);
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.add(namePanel);
    panel.add(pinPanel);
    panel.add(rolePanel);
    panel.add(buttonPanel);
  }

  private void updateRole(PlayerAwaitingApproval player, Role role) {
    updateRoleCCallback.accept(player, role);
  }

  public JComponent getContents() {
    return panel;
  }

  private void approve(PlayerAwaitingApproval player, Player.Role role) {
    MapTool.getFrame().getConnectionPanel().removeAwaitingApproval(player.name());
    player
        .approveCallback()
        .accept(
            new PlayerAwaitingApproval(
                player.name(),
                player.pin(),
                role,
                player.publicKey(),
                player.approveCallback(),
                player.denyCallback()));
  }

  private void deny(PlayerAwaitingApproval player) {
    MapTool.getFrame().getConnectionPanel().removeAwaitingApproval(player.name());
    player.denyCallback().accept(player);
  }
}
