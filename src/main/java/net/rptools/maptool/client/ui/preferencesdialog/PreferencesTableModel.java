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
package net.rptools.maptool.client.ui.preferencesdialog;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;

/** Table model for the token properties type table. */
public class PreferencesTableModel extends AbstractTableModel {
  protected record PrefComponentPane(JComponent component) {}

  /**
   * Copy of the token type map from the campaign properties. This is used to populate the table. We
   * create an empty map to begin with so that we don't get a null pointer exception when the table
   * is first displayed.
   */
  private final Object[][] tableData;

  private final String[] columnNames = {
    "section", "group", "label", "control", "searchWords", "collated"
  };
  private final Class<?>[] columnClasses = {
    Classify.Section.class,
    Classify.Group.class,
    PrefComponentPane.class,
    PrefComponentPane.class,
    String.class,
    Classify.Collated.class
  };
  private final int rowCount;
  private final int columnCount;

  protected PreferencesTableModel() {
    super();

    tableData = getTableData();
    rowCount = tableData.length;
    columnCount = columnNames.length;
  }

  protected Object[][] getTableData() {
    if (tableData != null) {
      return tableData;
    }
    Classify.Collated[] values = Classify.Collated.values();
    Object[][] data = new Object[values.length][];
    for (int i = 0; i < values.length; i++) {
      Classify.Collated collated = values[i];
      Component[] components = PrefParts.createComponentsFor(collated);
      data[i] =
          new Object[] {
            collated.section,
            collated.group,
            components[0],
            components[1],
            collated.searchWords,
            collated
          };
    }
    return data;
  }

  protected String[] getColumnNames() {
    return columnNames;
  }

  @Override
  public int getRowCount() {
    return rowCount;
  }

  @Override
  public int getColumnCount() {
    return columnCount;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    return tableData[rowIndex][columnIndex];
  }

  @Override
  public String getColumnName(int column) {
    return columnNames[column];
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return columnClasses[columnIndex];
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return columnClasses[columnIndex] == PrefComponentPane.class;
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    //    var properties = tokenTypeMap.get(tokenType);
    //    var tokenProperty = properties.get(rowIndex);
    //    switch (columnIndex) {
    //      case 0 -> tokenProperty.setName((String) aValue);
    //      case 1 -> tokenProperty.setShortName((String) aValue);
    //      case 2 -> tokenProperty.setDisplayName((String) aValue);
    //      case 3 -> tokenProperty.setDefaultValue((String) aValue);
    //      case 4 -> {
    //        tokenProperty.setShowOnStatSheet((Boolean) aValue);
    //        fireTableRowsUpdated(rowIndex, rowIndex);
    //      }
    //      case 5 -> tokenProperty.setGMOnly((Boolean) aValue);
    //      case 6 -> tokenProperty.setOwnerOnly((Boolean) aValue);
    //    }
  }
}
