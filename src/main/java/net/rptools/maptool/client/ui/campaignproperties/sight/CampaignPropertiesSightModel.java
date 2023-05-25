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
package net.rptools.maptool.client.ui.campaignproperties.sight;

import javax.swing.table.AbstractTableModel;

public class CampaignPropertiesSightModel extends AbstractTableModel {
  @Override
  public int getRowCount() {
    return 2;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    return "1";
  }

  @Override
  public int getColumnCount() {
    return 10;
  }

  @Override
  public String getColumnName(int column) {
    return switch (column) {
      case 0 -> "Name";
      case 1 -> "Shape";
      case 2 -> "Distance";
      case 3 -> "Measure From";
      case 4 -> "Arc";
      case 5 -> "Offset";
      case 6 -> "Light Multiplier";
      case 7 -> "Personal Distance";
      case 8 -> "Color";
      case 9 -> "Lumens";
      default -> null;
    };
  }
}
