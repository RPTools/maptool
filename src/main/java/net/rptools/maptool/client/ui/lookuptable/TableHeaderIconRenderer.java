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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

public class TableHeaderIconRenderer extends DefaultTableCellRenderer {

  private final TableCellRenderer delegate;
  private final Icon icon;
  private final int alignment;

  TableHeaderIconRenderer(TableCellRenderer delegate, Icon icon, int alignment) {
    this.delegate = delegate;
    this.icon = icon;
    this.alignment = alignment;
  }

  @Override
  public java.awt.Component getTableCellRendererComponent(
      JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

    JLabel label =
        (JLabel)
            delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    label.setIcon(icon);
    label.setHorizontalTextPosition(SwingConstants.RIGHT);
    label.setHorizontalAlignment(alignment);
    return label;
  }
}
