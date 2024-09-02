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
package net.rptools.maptool.client.ui.addon;

import java.awt.*;
import java.io.IOException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.library.LibraryManager;
import net.rptools.maptool.model.library.addon.ExternalLibraryInfo;

public class ExternalAddOnImportCellEditor extends AbstractCellEditor
    implements TableCellEditor, TableCellRenderer {

  private final JButton button = new JButton();
  private ExternalLibraryInfo info;

  public ExternalAddOnImportCellEditor() {
    button.addActionListener(
        e -> {
          try {
            new LibraryManager().importFromExternal(info.libraryInfo().namespace());
          } catch (IOException ex) {
            MapTool.showError(
                I18N.getText("library.dialog.import.failed", info.libraryInfo().namespace()));
          }
          stopCellEditing();
        });
  }

  @Override
  public Component getTableCellEditorComponent(
      JTable table, Object value, boolean isSelected, int row, int column) {
    return getButton(table, value, isSelected, false, row, column);
  }

  @Override
  public Object getCellEditorValue() {
    return info;
  }

  @Override
  public Component getTableCellRendererComponent(
      JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    return getButton(table, value, isSelected, hasFocus, row, column);
  }

  private Component getButton(
      JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    button.setEnabled(true);
    info = (ExternalLibraryInfo) value;
    String buttonTextKey;
    if (info.isInstalled()) {
      buttonTextKey = "library.dialog.reimport";
    } else {
      buttonTextKey = "library.dialog.import";
    }
    button.setText(I18N.getText(buttonTextKey));

    var panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.add(button, BorderLayout.CENTER);
    panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
    panel.setBorder(new EmptyBorder(1, 5, 2, 5));
    return panel;
  }
}
