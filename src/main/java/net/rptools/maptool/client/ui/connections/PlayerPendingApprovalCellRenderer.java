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

import java.awt.Component;
import java.util.function.BiConsumer;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import net.rptools.maptool.model.player.Player.Role;
import net.rptools.maptool.model.player.PlayerAwaitingApproval;

/** Cell renderer for the {@link PlayerPendingApprovalTableModel}. */
public class PlayerPendingApprovalCellRenderer implements TableCellRenderer {

  /** The callback to call when the user changes the role. */
  private final BiConsumer<PlayerAwaitingApproval, Role> updateRole;

  /**
   * Creates a new {@link PlayerPendingApprovalCellRenderer}.
   *
   * @param updateRole the callback to call when the user changes the role.
   */
  PlayerPendingApprovalCellRenderer(BiConsumer<PlayerAwaitingApproval, Role> updateRole) {
    this.updateRole = updateRole;
  }

  @Override
  public Component getTableCellRendererComponent(
      JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    if (value instanceof PlayerAwaitingApproval player) {
      return new CellContents(player, updateRole).getContents();
    } else {
      return new JLabel();
    }
  }
}
