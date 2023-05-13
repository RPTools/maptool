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

import java.awt.*;
import javax.swing.*;
import javax.swing.table.TableCellEditor;
import net.rptools.maptool.client.ui.macrobuttons.dialog.MacroEditorDialog;

/**
 * Class that implements a Table Cell editor that allows editing either through text box or macro dialog if the
 * user clicks on the "..." button.
 */
public class TextFieldEditorButtonTableCellEditor extends AbstractCellEditor
    implements TableCellEditor {

  private JTextField textField = new JTextField();
  private JPanel panel = new JPanel(new GridBagLayout());

  /**
   * Creates a new <code>TextFieldEditorButtonTableCellEditor</code>.
   */
  public TextFieldEditorButtonTableCellEditor() {
    textField.addActionListener(l -> fireEditingStopped());
    panel.add(textField);
    JButton button = new JButton("...");
    button.addActionListener(
        l ->
            MacroEditorDialog.createModalDialog(
                    c -> {
                      if (c != null) {
                        textField.setText(c);
                        fireEditingStopped();
                      } else {
                        fireEditingCanceled();
                      }
                    })
                .show("Default", textField.getText()));
    panel.add(button);
  }

  @Override
  public Component getTableCellEditorComponent(
      JTable table, Object value, boolean isSelected, int row, int column) {
    textField.setText(value == null ? "" : value.toString());
    return panel;
  }

  @Override
  public Object getCellEditorValue() {
    return textField.getText();
  }
}
