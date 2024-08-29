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

import java.util.List;
import javax.swing.table.AbstractTableModel;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.library.LibraryManager;
import net.rptools.maptool.model.library.addon.ExternalLibraryInfo;

public class ExternalAddOnLibrariesTableModel extends AbstractTableModel {

  @Override
  public int getRowCount() {
    return new LibraryManager().getExternalAddOnLibraries().size();
  }

  @Override
  public int getColumnCount() {
    return 5;
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return columnIndex == 4 ? ExternalLibraryInfo.class : String.class;
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return columnIndex == 4;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    List<ExternalLibraryInfo> addons = new LibraryManager().getExternalAddOnLibraries();

    var info = addons.get(rowIndex);
    return switch (columnIndex) {
      case 0 -> info.libraryInfo().name();
      case 1 -> info.libraryInfo().version();
      case 2 -> info.libraryInfo().namespace();
      case 3 -> info.libraryInfo().backingDirectory();
      case 4 -> info;
      default -> null;
    };
  }

  @Override
  public String getColumnName(int column) {
    return switch (column) {
      case 0 -> I18N.getText("library.dialog.addon.name");
      case 1 -> I18N.getText("library.dialog.addon.version");
      case 2 -> I18N.getText("library.dialog.addon.namespace");
      case 3 -> I18N.getText("library.dialog.addon.subdir");
      case 4 -> I18N.getText("library.dialog.addon.refresh");
      default -> null;
    };
  }

  void refresh(int rowIndex) {
    // TODO: CDW
  }
}
