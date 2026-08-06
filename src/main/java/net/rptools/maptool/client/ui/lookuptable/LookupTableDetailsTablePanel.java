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
package net.rptools.maptool.client.ui.lookuptable;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import net.rptools.maptool.model.LookupTable;

/** Implement a scrollable details table based on the underlying model */
public class LookupTableDetailsTablePanel extends JPanel {

  private final JTable detailsTable;
  private final LookupTableDetailsTablePanelModel detailsTableModel;

  // Cache the listener to prevent duplicate leaks on multiple calls
  private MouseMotionListener headerTooltipListener;

  // Save column widths when refreshing the structure, in case the user has changed them
  private final java.util.Map<Integer, Integer> preservedWidths = new java.util.HashMap<>();

  public LookupTableDetailsTablePanel() {

    detailsTableModel = new LookupTableDetailsTablePanelModel();
    detailsTable = new JTable(detailsTableModel);

    // enable row sorting by clicking on the row headers
    detailsTable.setAutoCreateRowSorter(true);

    // enable the horizontal scroll bar
    detailsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    // prevent reordering columns
    detailsTable.getTableHeader().setReorderingAllowed(false);

    setLayout(new BorderLayout());
    configureColumns();
    setRowHeightBasedOnFont();

    // add the table into a scroll pane and then add to the panel
    add(new JScrollPane(detailsTable), BorderLayout.CENTER);

    refreshData();
  }

  /**
   * Set the row height based on the table font. We later use the row height to proportionally scale
   * images
   */
  public void setRowHeightBasedOnFont() {
    Font tableFont = UIManager.getFont("Table.font");
    int rowHeight = tableFont != null ? tableFont.getSize() * 2 : 24;
    detailsTable.setRowHeight(rowHeight);
  }

  /**
   * Retrieve the details table
   *
   * @return the details table
   */
  public JTable getDetailsTable() {
    return detailsTable;
  }

  /**
   * Retrieve the {@link LookupTable} which has been selected in the details {@link JTable}.
   *
   * @return the selected {@link LookupTable}
   */
  public LookupTable getSelectedLookupTable() {
    int row = detailsTable.getSelectedRow();
    if (row < 0) {
      return null;
    }
    row = detailsTable.convertRowIndexToModel(row);
    return detailsTableModel.getLookupTableAt(row);
  }

  /** Refresh the data in the details {@link JTable}. */
  public void refreshData() {
    detailsTableModel.refreshData();
  }

  /** Refresh the structure of the details {@link JTable}. */
  public void refreshStructure() {
    saveColumnWidths();
    detailsTableModel.refreshStructure();
    configureColumns();
  }

  /** Reset the details {@link JTable}. */
  public void reset() {
    saveColumnWidths();
    detailsTableModel.reset();
    configureColumns();
  }

  /** Saves the current visual widths of all columns based on their model index. */
  private void saveColumnWidths() {
    preservedWidths.clear();
    int columnCount = detailsTable.getColumnModel().getColumnCount();

    for (int i = 0; i < columnCount; i++) {
      int modelIndex = detailsTable.convertColumnIndexToModel(i);
      int currentWidth = detailsTable.getColumnModel().getColumn(i).getWidth();
      preservedWidths.put(modelIndex, currentWidth);
    }
  }

  /** Configures column widths, renderers, and header tooltips safely. */
  public void configureColumns() {
    // determine the actual visible column count to prevent index mismatches
    int viewColumnCount = detailsTable.getColumnModel().getColumnCount();
    int modelColumnCount = detailsTableModel.getColumnCount();

    for (int i = 0; i < viewColumnCount; i++) {
      // stop safely if the view has more columns than the current data model state
      if (i >= modelColumnCount) {
        break;
      }

      TableColumn tableColumn = detailsTable.getColumnModel().getColumn(i);

      // map view index to model index (crucial if columns are ever hidden/filtered)
      int modelIndex = detailsTable.convertColumnIndexToModel(i);
      LookupTableDetailsTablePanelModel.DetailsTableColumn column =
          detailsTableModel.getColumns().get(modelIndex);

      // determine column widths
      int preferredWidth;
      if (preservedWidths.containsKey(modelIndex)) {
        preferredWidth = preservedWidths.get(modelIndex);
      } else {
        preferredWidth = column.getPreferredWidth();
      }

      // set BOTH preferred and current width to force Swing to respect it initially
      tableColumn.setPreferredWidth(preferredWidth);
      tableColumn.setWidth(preferredWidth);

      // set image column width
      if (column == LookupTableDetailsTablePanelModel.DetailsTableColumn.IMAGE) {
        tableColumn.setMinWidth(preferredWidth);
        tableColumn.setMaxWidth(preferredWidth);
      }

      // render header specifics
      tableColumn.setHeaderRenderer(
          LookupTableDetailsTableHeaderRenderers.forColumn(detailsTable, column));

      // render column specifics
      tableColumn.setCellRenderer(
          LookupTableDetailsTableColumnRenderers.forColumn(column, detailsTable.getRowHeight()));
    }

    // Force the UI to lay out the table header using the new dimensions
    if (detailsTable.getTableHeader() != null) {
      detailsTable.getTableHeader().resizeAndRepaint();
    }

    // Add the tooltips
    setupHeaderTooltips();
  }

  /** Handles assigning exactly one tooltip listener to the table header. */
  private void setupHeaderTooltips() {
    JTableHeader header = detailsTable.getTableHeader();
    if (header == null) {
      return;
    }

    // clean up previous listener instance to avoid memory growth
    if (headerTooltipListener != null) {
      header.removeMouseMotionListener(headerTooltipListener);
    }

    // instantiate and bind the standalone listener instance
    headerTooltipListener =
        new MouseMotionAdapter() {
          @Override
          public void mouseMoved(MouseEvent e) {
            int viewColumn = header.columnAtPoint(e.getPoint());

            if (viewColumn >= 0) {
              // Map view index back to data model index for accurate tooltip fetch
              int modelColumn = detailsTable.convertColumnIndexToModel(viewColumn);

              if (modelColumn >= 0 && modelColumn < detailsTableModel.getColumns().size()) {
                header.setToolTipText(detailsTableModel.getColumns().get(modelColumn).getTooltip());
                return;
              }
            }
            header.setToolTipText(null);
          }
        };

    header.addMouseMotionListener(headerTooltipListener);
  }
}
