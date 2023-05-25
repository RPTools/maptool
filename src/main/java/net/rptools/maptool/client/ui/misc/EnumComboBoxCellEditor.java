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

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import net.rptools.maptool.util.I18nName;

/**
 * Class that implements the Combo Box renderer for enums. If the enum implements {@link I18nName}
 * then the i18n name is used, otherwise <code>toString()</code> is used for the label.
 *
 * @param <T> The enum type.
 */
public class EnumComboBoxCellEditor<T extends Enum<T>> extends AbstractCellEditor
    implements TableCellEditor {
  /** The list of values to include in the combo box. */
  private final List<T> values = new ArrayList<>();

  private T value;

  /**
   * Creates a new instance of the renderer for the given enum class but limiting the values to
   * include in the combo box to the given list.
   *
   * @param enumClass The enum class.
   * @param values The list of values to include in the combo box.
   */
  public EnumComboBoxCellEditor(Class<T> enumClass, List<T> values) {
    this.values.addAll(values);
  }

  /**
   * Creates a new instance of the renderer for the given enum class.
   *
   * @param enumClass The enum class.
   */
  public EnumComboBoxCellEditor(Class<T> enumClass) {
    this(enumClass, List.of(enumClass.getEnumConstants()));
  }

  @Override
  public Component getTableCellEditorComponent(
      JTable table, Object value, boolean isSelected, int row, int column) {
    var comboBox = new JComboBox<T>();
    comboBox.setRenderer(new EnumComboBoxRenderer<>());
    values.forEach(comboBox::addItem);
    comboBox.setSelectedItem(value);
    comboBox.addActionListener(
        e -> {
          this.value = (T) comboBox.getSelectedItem();
          fireEditingStopped();
        });
    return comboBox;
  }

  @Override
  public Object getCellEditorValue() {
    return value;
  }
}
