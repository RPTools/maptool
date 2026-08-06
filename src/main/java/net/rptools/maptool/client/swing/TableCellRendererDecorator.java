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
package net.rptools.maptool.client.swing;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

/**
 * A customizable, theme-respecting table cell renderer.
 *
 * <p>This is primarily intended for customizing header cells. Since Java does not expose the
 * default header cell renderer implementation, we cannot simply create new instances and customize
 * them as we can with regular table cells. This makes it difficult to customize header cells while
 * respecting the UI theme.
 *
 * <p>This cell renderer allows customizing header cells by decorating the pre-existing header cell
 * renderer, then modifying the component returned in {@link #getTableCellRendererComponent(JTable,
 * Object, boolean, boolean, int, int)}.
 *
 * <p>For now, only the text alignment can be customized, but support for other properties may be
 * added in the future.
 */
public class TableCellRendererDecorator implements TableCellRenderer {
  private final TableCellRenderer decorated;
  private int horizontalAlignment = SwingConstants.LEADING;
  private int verticalAlignment = SwingConstants.CENTER;

  public TableCellRendererDecorator(TableCellRenderer decorated) {
    this.decorated = decorated;
  }

  public int getHorizontalAlignment() {
    return horizontalAlignment;
  }

  public void setHorizontalAlignment(int horizontalAlignment) {
    this.horizontalAlignment = horizontalAlignment;
  }

  public int getVerticalAlignment() {
    return verticalAlignment;
  }

  public void setVerticalAlignment(int verticalAlignment) {
    this.verticalAlignment = verticalAlignment;
  }

  @Override
  public Component getTableCellRendererComponent(
      JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    var component =
        decorated.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

    /* Typical renderers use a label, which just so happens to be the component that allows us to
     * support alignment as we want. */
    if (component instanceof JLabel label) {
      label.setHorizontalAlignment(horizontalAlignment);
      label.setVerticalAlignment(verticalAlignment);
    }

    return component;
  }
}
