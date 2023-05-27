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
package net.rptools.maptool.client.ui.misc;

import com.jidesoft.combobox.ColorExComboBox;
import com.jidesoft.grid.AbstractTableCellEditorRenderer;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;

/** Editor for a color in table cell using Jide ColorExComboBox. */
public class ColorComboBoxCellEditor extends AbstractTableCellEditorRenderer {

  /** The value of the cell being edited. */
  private Color value;

  @Override
  public Component createTableCellEditorRendererComponent(JTable jTable, int i, int i1) {
    var combo = new ColorExComboBox();
    combo.addPropertyChangeListener(
        l -> {
          if (l.getPropertyName().equals("selectedItem")) {
            value = combo.getSelectedColor();
            stopCellEditing();
          }
        });
    value = (Color) jTable.getValueAt(i, i1);
    combo.setSelectedColor(value);
    return combo;
  }
  ;

  @Override
  public void configureTableCellEditorRendererComponent(
      JTable jTable,
      Component component,
      boolean b,
      Object o,
      boolean b1,
      boolean b2,
      int i,
      int i1) {}

  @Override
  public Object getCellEditorValue() {
    return value;
  }
}
