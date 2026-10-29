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
 * <https://www.gnu.org/licenses/> and specifically the Affero license
 * text at <https://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.client.swing;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.table.TableCellEditor;
import net.rptools.maptool.client.MapTool;

public class TableCellEditorEnumCombo extends AbstractCellEditor
    implements TableCellEditor, ActionListener {
  private final boolean isEnabled;
  private Enum<?> value;
  private final DefaultComboBoxModel<Object> comboBoxModel;
  private final Class<?> enumClass;

  public TableCellEditorEnumCombo(Class<?> enumClass, boolean gmOnly) {
    super();
    if (!enumClass.isEnum()) {
      throw new IllegalArgumentException("Enum class required");
    }
    this.isEnabled = !gmOnly || MapTool.getPlayer().isGM();
    this.enumClass = enumClass;
    comboBoxModel = new DefaultComboBoxModel<>(enumClass.getEnumConstants());
  }

  @Override
  public Object getCellEditorValue() {
    return this.value;
  }

  @Override
  public Component getTableCellEditorComponent(
      JTable table, Object value, boolean isSelected, int row, int column) {
    if (value.getClass().isAssignableFrom(enumClass)) {
      this.value =
          (Enum<?>)
              Arrays.stream(enumClass.getEnumConstants())
                  .filter(e -> e.toString().equalsIgnoreCase(value.toString()))
                  .findFirst()
                  .orElseGet(() -> null);
    }

    JComboBox<?> combo = new JComboBox<>(comboBoxModel);
    combo.setEnabled(isEnabled);
    combo.setEditable(false);
    combo.setSelectedItem(this.value);
    combo.addActionListener(this);

    if (isSelected) {
      combo.setBackground(table.getSelectionBackground());
      combo.setForeground(table.getSelectionForeground());
    } else {
      combo.setBackground(table.getSelectionForeground());
      combo.setForeground(table.getSelectionBackground());
    }

    return combo;
  }

  @Override
  public void actionPerformed(ActionEvent event) {
    JComboBox<?> combo = (JComboBox<?>) event.getSource();
    this.value = (Enum<?>) combo.getSelectedItem();
  }
}
