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
package net.rptools.maptool.client.ui.lookuptable;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import net.rptools.maptool.client.ui.theme.Icons;
import net.rptools.maptool.client.ui.theme.RessourceManager;

/** For a given header decide which renderer to use. */
public final class LookupTableDetailsTableHeaderRenderers {

  private LookupTableDetailsTableHeaderRenderers() {}

  public static TableCellRenderer forColumn(
      JTable table, LookupTableDetailsTablePanelModel.DetailsTableColumn column) {

    TableCellRenderer defaultHeaderRenderer = table.getTableHeader().getDefaultRenderer();
    Icons iconHeader = column.getIconHeader();

    if (iconHeader != null) {
      Icon icon = RessourceManager.getSmallIcon(iconHeader);
      return new TableHeaderIconRenderer(defaultHeaderRenderer, icon, column.getAlignment());
    }

    // otherwise just align the header as specified in the column enum
    return new TableHeaderAlignedRenderer(defaultHeaderRenderer, column.getAlignment());
  }
}
