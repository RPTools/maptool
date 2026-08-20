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
import net.rptools.maptool.client.ui.theme.RessourceManager;

/** For a given column decide which renderer to use. */
public final class LookupTableDetailsTableColumnRenderers {

  public static TableCellRenderer forColumn(
      LookupTableDetailsTablePanelModel.DetailsTableColumn column, int rowHeight) {

    // proportionally scale images to fit in the cell
    if (column == LookupTableDetailsTablePanelModel.DetailsTableColumn.IMAGE) {
      return new TableColumnIconRenderer(column.getPreferredWidth(), rowHeight);
    }

    // boolean columns can optionally display icons, or default to a checkbox
    if (column.getColumnClass() == Boolean.class) {
      Icon iconTrue = RessourceManager.getSmallIcon(column.getIconTrue());
      Icon iconFalse = RessourceManager.getSmallIcon(column.getIconFalse());

      if (iconTrue != null || iconFalse != null) {
        // n.b. if one of the icons is null nothing will be displayed for that boolean value
        return new TableColumnBooleanIconRenderer(iconTrue, iconFalse, true);
      } else {
        return new TableColumnBooleanRenderer(false);
      }
    }

    // otherwise just align the cell values as specified in the column enum
    return new TableColumnAlignedRenderer(column.getAlignment());
  }
}
