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

import java.util.List;
import javax.swing.table.AbstractTableModel;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.player.PlayerAwaitingApproval;

/** The Table Model for the Player Pending Approval Table. */
class PlayerPendingApprovalTableModel extends AbstractTableModel {

  /** The list of players awaiting approval. */
  private final List<PlayerAwaitingApproval> players;

  /**
   * Creates a new instance of the PlayerPendingApprovalTableModel.
   *
   * @param players the list of players awaiting approval.
   */
  public PlayerPendingApprovalTableModel(List<PlayerAwaitingApproval> players) {
    this.players = players;
  }

  @Override
  public int getRowCount() {
    return players.size();
  }

  @Override
  public int getColumnCount() {
    return 1;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    return players.get(rowIndex);
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return true;
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return PlayerAwaitingApproval.class;
  }

  @Override
  public String getColumnName(int columnIndex) {
    return I18N.getString("pendingConnection.column.title");
  }
}
