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
import net.rptools.maptool.model.TokenProperty;

/** Table model for the token properties type table. */
public class TokenPropertiesTableModel extends AbstractTableModel {

  /**
   * Record to hold strings that can also be edited via the macro editor. The strings are "wrapped"
   * in this class so we can register a cell editor for them.
   */
  public record LargeEditableText(String text) {}
  ;

  @Serial private static final long serialVersionUID = 3256444702936019250L;

  /**
   * Copy of the token type map from the campaign properties. This is used to populate the table. We
   * create an empty map to being with so that we don't get a null pointer exception when the table
   * is first displayed.
   */
  private Map<String, List<TokenProperty>> tokenTypeMap = new HashMap<>();

  /** The token type that is currently displayed in the table. */
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
    var properties = tokenTypeMap.get(tokenType);
    return properties == null ? 0 : properties.size();
  }

  @Override
  public int getColumnCount() {
    return 7;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    var properties = tokenTypeMap.get(tokenType);
    var property = properties.get(rowIndex);
    return switch (columnIndex) {
      case 0 -> property.getName();
      case 1 -> property.getShortName();
      case 2 -> {
        var displayName = property.getDisplayName();
        yield displayName == null || displayName.isBlank() ? null : displayName;
      }
      case 3 -> property.getDefaultValue();
      case 4 -> property.isShowOnStatSheet();
      case 5 -> property.isGMOnly() & property.isShowOnStatSheet();
      case 6 -> property.isOwnerOnly() & property.isShowOnStatSheet();
      default -> null;
    };
  }

  @Override
  public String getColumnName(int column) {
    String cName = "";
    String htmlWrap = "<html><table><th style=\"font-weight:normal;\">###</th></table></html>";
    switch (column) {
      case 0 -> cName = I18N.getText("campaignPropertiesTable.column.name");
      case 1 -> cName = I18N.getText("campaignPropertiesTable.column.shortName");
      case 2 -> cName = I18N.getText("campaignPropertiesTable.column.displayName");
      case 3 -> cName = I18N.getText("campaignPropertiesTable.column.defaultValue");
      case 4 -> cName = I18N.getText("campaignPropertiesTable.column.onStatSheet");
      case 5 -> cName = I18N.getText("campaignPropertiesTable.column.gmStatSheet");
      case 6 -> cName = I18N.getText("campaignPropertiesTable.column.ownerStatSheet");
    }
    ;
    return htmlWrap.replace("###", cName);
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return switch (columnIndex) {
      case 0, 1, 2 -> String.class;
      case 3 -> LargeEditableText.class;
      case 4, 5, 6 -> Boolean.class;
      default -> null;
    };
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    var properties = tokenTypeMap.get(tokenType);
    var tokenProperty = properties.get(rowIndex);
    return switch (columnIndex) {
      case 5, 6 -> tokenProperty
          .isShowOnStatSheet(); // GM, Owner only editable if show on stat sheet is set
      default -> true;
    };
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    var properties = tokenTypeMap.get(tokenType);
    var tokenProperty = properties.get(rowIndex);
    switch (columnIndex) {
      case 0 -> tokenProperty.setName((String) aValue);
      case 1 -> tokenProperty.setShortName((String) aValue);
      case 2 -> tokenProperty.setDisplayName((String) aValue);
      case 3 -> tokenProperty.setDefaultValue((String) aValue);
      case 4 -> {
        tokenProperty.setShowOnStatSheet((Boolean) aValue);
        fireTableRowsUpdated(rowIndex, rowIndex);
      }
      case 5 -> tokenProperty.setGMOnly((Boolean) aValue);
      case 6 -> tokenProperty.setOwnerOnly((Boolean) aValue);
    }
  }

  /** Adds a new token property, with a generated name. */
  public void addProperty() {
    var properties = tokenTypeMap.get(tokenType);

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
        properties.add(prop);
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
    var properties = tokenTypeMap.get(tokenType);
    properties.remove(selectedRow);
    fireTableRowsDeleted(selectedRow, selectedRow);
  }

  public void movePropertyUp(int selectedRow) {
    var properties = tokenTypeMap.get(tokenType);
    if (selectedRow <= 0 || selectedRow >= properties.size()) {
      // Either already at the top or a nonsense index.
      throw new ArrayIndexOutOfBoundsException(selectedRow);
    }

    Collections.swap(properties, selectedRow - 1, selectedRow);
    fireTableRowsUpdated(selectedRow - 1, selectedRow);
  }

  public void movePropertyDown(int selectedRow) {
    var properties = tokenTypeMap.get(tokenType);
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
