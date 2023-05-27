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

import java.awt.Color;
import java.awt.Dimension;
import java.util.List;
import javax.swing.*;
import net.rptools.maptool.client.ui.misc.ColorComboBoxCellEditor;
import net.rptools.maptool.client.ui.misc.EnumComboBoxCellEditor;
import net.rptools.maptool.client.ui.misc.I18NLabelTableCellRenderer;
import net.rptools.maptool.client.ui.misc.MultiLineTableHeaderRenderer;
import net.rptools.maptool.model.ShapeType;
import net.rptools.maptool.model.SightType;

/** Controller for the Sight tab of the Campaign Properties dialog. */
public class CampaignPropertiesSightController {

  /** The model for the table that displays the sight types. */
  private final CampaignPropertiesSightModel model = new CampaignPropertiesSightModel();

  /**
   * Creates a new instance of the controller.
   *
   * @param sightTable The table that displays the sight types.
   */
  public CampaignPropertiesSightController(JTable sightTable) {
    sightTable.setModel(model);
    sightTable.getColumnModel().getColumn(0).setPreferredWidth(250);
    sightTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
    sightTable.setDefaultRenderer(ShapeType.class, new I18NLabelTableCellRenderer());
    sightTable.setDefaultEditor(
        ShapeType.class,
        new EnumComboBoxCellEditor<>(
            ShapeType.class,
            List.of(ShapeType.CIRCLE, ShapeType.CONE, ShapeType.SQUARE, ShapeType.GRID)));
    sightTable.setDefaultRenderer(
        Color.class,
        (table, value, isSelected, hasFocus, row, column) -> {
          var label = new JLabel();
          if (value != null) {
            label.setBackground((Color) value);
            label.setOpaque(true);
          }
          return label;
        });
    sightTable.setDefaultEditor(Color.class, new ColorComboBoxCellEditor());
    sightTable.getTableHeader().setDefaultRenderer(new MultiLineTableHeaderRenderer());
    /*
     * Who would have thought that setting the height of a table header contained in a scroll pane
     * would be so difficult.  Since the viewport for the table header is managed separately from
     * the viewport for the table, we have to create a new viewport for the table header and
     * override the getPreferredSize() method to return the height of the tallest column header.
     */
    if (sightTable.getParent() instanceof JViewport
        && sightTable.getParent().getParent() instanceof JScrollPane scrollPane) {
      scrollPane.setColumnHeader(
          new JViewport() {
            @Override
            public Dimension getPreferredSize() {
              int height = 0;
              for (int i = 0; i < sightTable.getColumnCount(); i++) {
                var col = sightTable.getColumnModel().getColumn(i);
                if (col != null) {
                  var comp =
                      sightTable
                          .getTableHeader()
                          .getDefaultRenderer()
                          .getTableCellRendererComponent(
                              sightTable, col.getHeaderValue(), false, false, 0, i);
                  height = Math.max(height, comp.getPreferredSize().height);
                }
              }
              Dimension d = super.getPreferredSize();
              d.height = height;
              return d;
            }
          });
    }
  }

  /**
   * Returns the list of sight types from the table.
   *
   * @return The list of sight types from the table.
   */
  public List<SightType> getSightTypes() {
    return model.getSightTypes();
  }
}
