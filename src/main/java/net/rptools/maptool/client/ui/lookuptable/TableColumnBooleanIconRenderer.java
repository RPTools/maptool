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

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

public class TableColumnBooleanIconRenderer extends DefaultTableCellRenderer {

  private final Icon trueIcon;
  private final Icon falseIcon;

  public TableColumnBooleanIconRenderer(Icon trueIcon, Icon falseIcon, boolean isEnabled) {
    this.trueIcon = trueIcon;
    this.falseIcon = falseIcon;

    setHorizontalAlignment(SwingConstants.CENTER);
    setEnabled(isEnabled);
  }

  @Override
  public Component getTableCellRendererComponent(
      JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

    super.getTableCellRendererComponent(table, null, isSelected, hasFocus, row, column);
    setIcon(Boolean.TRUE.equals(value) ? trueIcon : falseIcon);

    return this;
  }
}
