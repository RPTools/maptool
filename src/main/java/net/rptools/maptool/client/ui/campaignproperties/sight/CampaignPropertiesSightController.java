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

import java.util.List;
import javax.swing.*;
import net.rptools.maptool.client.ui.misc.EnumComboBoxCellEditor;
import net.rptools.maptool.client.ui.misc.EnumComboBoxCellRenderer;
import net.rptools.maptool.model.ShapeType;

/** Controller for the Sight tab of the Campaign Properties dialog. */
public class CampaignPropertiesSightController {

  /**
   * Creates a new instance of the controller.
   *
   * @param sightTable The table that displays the sight types.
   */
  public CampaignPropertiesSightController(JTable sightTable) {
    sightTable.setModel(new CampaignPropertiesSightModel());
    sightTable.setDefaultRenderer(
        ShapeType.class,
        new EnumComboBoxCellRenderer<>(
            ShapeType.class,
            List.of(ShapeType.CIRCLE, ShapeType.CONE, ShapeType.SQUARE, ShapeType.GRID)));
    sightTable.setDefaultEditor(
        ShapeType.class,
        new EnumComboBoxCellEditor<>(
            ShapeType.class,
            List.of(ShapeType.CIRCLE, ShapeType.CONE, ShapeType.SQUARE, ShapeType.GRID)));
  }
}
