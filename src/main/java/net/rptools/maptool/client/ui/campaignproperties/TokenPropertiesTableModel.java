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
package net.rptools.maptool.client.ui.campaignproperties;

import java.io.Serial;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.table.AbstractTableModel;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Permissions;
import net.rptools.maptool.model.TokenProperty;

/** Table model for the token properties type table. */
public class TokenPropertiesTableModel extends AbstractTableModel {

  /**
   * Record to hold strings that can also be edited via the macro editor. The strings are "wrapped"
   * in this class so we can register a cell editor for them.
   */
  public record LargeEditableText(String text) {}

  @Serial private static final long serialVersionUID = 3256444702936019250L;

  /**
   * Copy of the token type map from the campaign properties. This is used to populate the table. We
   * create an empty map to being with so that we don't get a null pointer exception when the table
   * is first displayed.
   */
  private Map<String, List<TokenProperty>> tokenTypeMap = new HashMap<>();

  /** The token property type on display in the table. */
  private String tokenType = "";

  /**
   * Set the token type to display in the table.
   *
   * @param propertyType the token type to display.
   */
  public void setPropertyType(String propertyType) {
    tokenType = propertyType;
    fireTableDataChanged();
  }

  @Override
  public int getRowCount() {
    List<TokenProperty> properties = tokenTypeMap.get(tokenType);
    return properties == null ? 0 : properties.size();
  }

  @Override
  public int getColumnCount() {
    return 7;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    List<TokenProperty> properties = tokenTypeMap.get(tokenType);
    TokenProperty property = properties.get(rowIndex);
    return switch (columnIndex) {
      case 0 -> property.getName();
      case 1 -> property.getShortName();
      case 2 -> property.getDisplayName();
      case 3 -> property.getDefaultValue();
      case 4 -> property.getEditorViewPermission().equals(Permissions.OWNER);
      case 5 -> property.getEditorEditPermission().equals(Permissions.OWNER);
      case 6 -> property.getStatSheetViewPermission();
      default -> null;
    };
  }

  public String getColumnTooltipText(int column) {
    return switch (column) {
      case 0 -> I18N.getText("campaignPropertiesTable.column.name.description");
      case 1 -> I18N.getText("campaignPropertiesTable.column.shortName.description");
      case 2 -> I18N.getText("campaignPropertiesTable.column.displayName.description");
      case 3 -> I18N.getText("campaignPropertiesTable.column.default.description");
      case 4 -> I18N.getText("campaignPropertiesTable.column.playerViewable.description");
      case 5 -> I18N.getText("campaignPropertiesTable.column.playerEditable.description");
      case 6 -> I18N.getText("campaignPropertiesTable.column.statSheet.description");
      default -> "";
    };
  }

  @Override
  public String getColumnName(int column) {
    return switch (column) {
      case 0 -> I18N.getText("campaignPropertiesTable.column.name");
      case 1 -> I18N.getText("campaignPropertiesTable.column.shortName");
      case 2 -> I18N.getText("campaignPropertiesTable.column.displayName");
      case 3 -> I18N.getText("campaignPropertiesTable.column.defaultValue");
      case 4 -> I18N.getText("campaignPropertiesTable.column.playerViewable");
      case 5 -> I18N.getText("campaignPropertiesTable.column.playerEditable");
      case 6 -> I18N.getText("campaignPropertiesTable.column.statSheetVisibility");
      default -> "";
    };
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return switch (columnIndex) {
      case 0, 1, 2 -> String.class;
      case 3 -> LargeEditableText.class;
      case 4, 5 -> Boolean.class;
      case 6 -> Permissions.class;
      default -> null;
    };
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    if (columnIndex == 5) {
      return (boolean) getValueAt(rowIndex, 4);
    }
    return true;
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    List<TokenProperty> properties = tokenTypeMap.get(tokenType);
    TokenProperty tokenProperty = properties.get(rowIndex);

    switch (columnIndex) {
      case 0 -> tokenProperty.setName((String) aValue);
      case 1 -> tokenProperty.setShortName((String) aValue);
      case 2 -> tokenProperty.setDisplayName((String) aValue);
      case 3 -> tokenProperty.setDefaultValue((String) aValue);
      case 4 -> tokenProperty.setEditorViewPermission((boolean) aValue);
      case 5 -> tokenProperty.setEditorEditPermission((boolean) aValue);
      case 6 -> tokenProperty.setStatSheetViewPermission((Permissions) aValue);
    }
  }

  /** Adds a new token property, with a generated name. */
  public void addProperty(int selectedRow) {
    List<TokenProperty> properties = tokenTypeMap.get(tokenType);

    // First find a unique name, there are so few entries we don't have to worry
    // about being fancy
    int seq = 1;
    while (true) {
      boolean free = true;
      String newName = I18N.getText("campaignPropertiesDialog.newTokenPropertyDefaultName", seq);
      for (var p : properties) {
        if (newName.equals(p.getName())) {
          free = false;
          break;
        }
      }

      if (free) {
        var prop = new TokenProperty(newName);
        // append if there is no selection, otherwise insert at selectedRow
        if (selectedRow == -1) {
          properties.add(prop);
        } else {
          properties.add(selectedRow, prop);
        }
        break;
      }
      seq++;
    }

    fireTableRowsInserted(properties.size() - 1, properties.size() - 1);
  }

  /**
   * Deletes the selected token property.
   *
   * @param selectedRow the selected row to delete.
   */
  public void deleteProperty(int selectedRow) {
    List<TokenProperty> properties = tokenTypeMap.get(tokenType);
    properties.remove(selectedRow);
    fireTableRowsDeleted(selectedRow, selectedRow);
  }

  public void movePropertyUp(int selectedRow) {
    List<TokenProperty> properties = tokenTypeMap.get(tokenType);
    if (selectedRow <= 0 || selectedRow >= properties.size()) {
      // Either already at the top or a nonsense index.
      throw new ArrayIndexOutOfBoundsException(selectedRow);
    }

    Collections.swap(properties, selectedRow - 1, selectedRow);
    fireTableRowsUpdated(selectedRow - 1, selectedRow);
  }

  public void movePropertyDown(int selectedRow) {
    List<TokenProperty> properties = tokenTypeMap.get(tokenType);
    if (selectedRow < 0 || selectedRow >= properties.size() - 1) {
      // Either already at the bottom or a nonsense index.
      throw new ArrayIndexOutOfBoundsException(selectedRow);
    }

    Collections.swap(properties, selectedRow, selectedRow + 1);
    fireTableRowsUpdated(selectedRow, selectedRow + 1);
  }

  /**
   * Sets the token type map used to populate the table.
   *
   * @param tokenTypeMap the token type map.
   */
  public void setPropertyTypeMap(Map<String, List<TokenProperty>> tokenTypeMap) {
    this.tokenTypeMap = tokenTypeMap;
  }
}
