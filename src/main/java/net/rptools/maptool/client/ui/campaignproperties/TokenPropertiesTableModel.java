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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.table.AbstractTableModel;
import net.rptools.maptool.model.TokenProperty;

public class TokenPropertiesTableModel extends AbstractTableModel {

  public record LargeEditableText(String text) {}
  ;

  @Serial private static final long serialVersionUID = 3256444702936019250L;

  private Map<String, List<TokenProperty>> tokenTypeMap = new HashMap<>();
  private String tokenType = "";

  public void setPropertyType(String propertyType) {
    tokenType = propertyType;
    if (propertyType != null && !propertyType.isEmpty()) {}
    fireTableDataChanged();
  }

  public int getRowCount() {
    var properties = tokenTypeMap.get(tokenType);
    return properties == null ? 0 : properties.size();
  }

  public int getColumnCount() {
    return 6;
  }

  public Object getValueAt(int rowIndex, int columnIndex) {
    var properties = tokenTypeMap.get(tokenType);
    var property = properties.get(rowIndex);
    return switch (columnIndex) {
      case 0 -> property.getName();
      case 1 -> property.getShortName();
      case 2 -> property.isShowOnStatSheet();
      case 3 -> property.isGMOnly();
      case 4 -> property.isOwnerOnly();
      case 5 -> property.getDefaultValue();
      default -> null;
    };
  }

  @Override
  public String getColumnName(int column) {
    return switch (column) {
      case 0 -> "Name";
      case 1 -> "Short Name";
      case 2 -> "On Stat Sheet";
      case 3 -> "GM Only";
      case 4 -> "Owner Only";
      case 5 -> "Default Value";
      default -> null;
    };
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return switch (columnIndex) {
      case 0, 1 -> String.class;
      case 2, 3, 4 -> Boolean.class;
      case 5 -> LargeEditableText.class;
      default -> null;
    };
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return true;
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    var properties = tokenTypeMap.get(tokenType);
    var tokenProperty = properties.get(rowIndex);
    switch (columnIndex) {
      case 0 -> tokenProperty.setName((String) aValue);
      case 1 -> tokenProperty.setShortName((String) aValue);
      case 2 -> tokenProperty.setShowOnStatSheet((Boolean) aValue);
      case 3 -> tokenProperty.setGMOnly((Boolean) aValue);
      case 4 -> tokenProperty.setOwnerOnly((Boolean) aValue);
      case 5 -> tokenProperty.setDefaultValue((String) aValue);
    }
  }

  public void addProperty() {
    var properties = tokenTypeMap.get(tokenType);
    var prop = new TokenProperty("New");
    properties.add(prop);
    fireTableRowsInserted(properties.size() - 1, properties.size() - 1);
  }

  public void deleteProperty(int selectedRow) {
    var properties = tokenTypeMap.get(tokenType);
    properties.remove(selectedRow);
    fireTableRowsDeleted(selectedRow, selectedRow);
  }

  public void setPropertyTypeMap(Map<String, List<TokenProperty>> tokenTypeMap) {
    this.tokenTypeMap = tokenTypeMap;
  }
}
