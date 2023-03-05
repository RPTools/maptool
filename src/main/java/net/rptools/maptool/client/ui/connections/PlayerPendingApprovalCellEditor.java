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
import javax.swing.AbstractCellEditor;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import net.rptools.maptool.model.player.Player.Role;
import net.rptools.maptool.model.player.PlayerAwaitingApproval;

/** The cell editor for the player awaiting approval table. */
public class PlayerPendingApprovalCellEditor extends AbstractCellEditor implements TableCellEditor {

  /** Call back for when the user changes the role. */
  private final BiConsumer<PlayerAwaitingApproval, Role> updateRole;

  /**
   * Creates a new cell editor.
   *
   * @param updateRole the callback for when the user changes the role.
   */
  PlayerPendingApprovalCellEditor(BiConsumer<PlayerAwaitingApproval, Role> updateRole) {
    this.updateRole = updateRole;
  }

  @Override
  public Component getTableCellEditorComponent(
      JTable table, Object value, boolean isSelected, int row, int column) {
    if (value instanceof PlayerAwaitingApproval player) {
      return new CellContents(player, updateRole).getContents();
    } else {
      return new JLabel();
    }
  }

  @Override
  public Object getCellEditorValue() {
    return null;
  }
}
