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

import com.formdev.flatlaf.FlatLaf;
import com.jidesoft.grid.*;
import com.jidesoft.swing.JideScrollPane;
import com.jidesoft.swing.Resizable;
import com.jidesoft.utils.PortingUtils;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.*;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.swing.searchable.SearchableEx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PreferencesTable extends CategorizedTable {
  private static final Logger log = LogManager.getLogger(PreferencesTable.class);
  private final StringProperty filterText = new SimpleStringProperty("");
  private final JideScrollPane tableScrollPane;
  private int minimumWidth = -1;
  private static final PreferencesTableModel PREFERENCES_TABLE_MODEL = new PreferencesTableModel();
  private static SearchableEx searchableEx;
  private static final DefaultGroupTableModel DEFAULT_GROUP_TABLE_MODEL =
      new DefaultGroupTableModel(PREFERENCES_TABLE_MODEL);

  private final TableRowSorter<PreferencesTableModel> SORTER = getTableRowSorter();

  protected SearchableEx getSearchableEx() {
    return searchableEx;
  }

  PreferencesTable(JideScrollPane tableScrollPane) {
    super(DEFAULT_GROUP_TABLE_MODEL);
    searchableEx = SearchableEx.getSearchable(this);
    setUseTableRendererForCategoryRow(false);
    this.tableScrollPane = tableScrollPane;
    initScrollPane();

    this.filterText.addListener(observable -> newRowFilter());

    CellRendererManager.registerRenderer(
        PreferencesTableModel.PrefComponentPane.class, new ComponentCellRenderer());
    CellEditorManager.registerEditor(
        PreferencesTableModel.PrefComponentPane.class,
        (CellEditorFactory) new ComponentCellEditor());

    DEFAULT_GROUP_TABLE_MODEL.setDisplayGroupColumns(false);
    DEFAULT_GROUP_TABLE_MODEL.setRemoveNullGrouper(false);
    DEFAULT_GROUP_TABLE_MODEL.addGroupColumn(0);
    DEFAULT_GROUP_TABLE_MODEL.addGroupColumn(1);
    this.removeColumn(columnModel.getColumn(3));
    this.removeColumn(columnModel.getColumn(2));

    this.setColumnResizable(true);
    this.setAutoResizeMode(AUTO_RESIZE_FILL);
    this.setRowResizable(true);
    this.setRowAutoResizes(true);
    this.setFillsViewportHeight(true);

    this.setShowVerticalLines(false);
    this.setNestedTableHeader(true);

    this.setRowSelectionAllowed(true);
    this.setRowAutoResizes(true);

    this.setAutoCreateColumnsFromModel(true);

    //        setTableStyleProvider(new StyleProvider());
    //        setShowTreeLines(true);
    //        setShowLeafNodeTreeLines(true);
    //        setExpandAllAllowed(true);
    //

    //        prettify();
    DEFAULT_GROUP_TABLE_MODEL.groupAndRefresh();
  }

  protected TableRowSorter<PreferencesTableModel> getTableRowSorter() {
    return SORTER;
  }

  protected void newRowFilter() {
    RowFilter<PreferencesTableModel, Object> rf = null;
    // If current expression doesn't parse, don't update.
    try {
      rf = RowFilter.regexFilter(filterText.get(), 0);
    } catch (java.util.regex.PatternSyntaxException e) {
      return;
    }
    SORTER.setRowFilter(rf);
    DEFAULT_GROUP_TABLE_MODEL.refresh();
  }

  private void initScrollPane() {
    tableScrollPane.setViewportView(this);
    tableScrollPane.setKeepCornerVisible(true);
    tableScrollPane.setColumnHeaderView(tableHeader);
    setPreferredSize(new Dimension(getMinimumWidth(), getPreferredSize().height));

    Resizable.ResizeCorner cornerLR = new Resizable.ResizeCorner(Resizable.LOWER_RIGHT);
    Resizable.ResizeCorner cornerLL = new Resizable.ResizeCorner(Resizable.LOWER_LEFT);
    tableScrollPane.setCorner(ScrollPaneConstants.LOWER_TRAILING_CORNER, cornerLR);
    tableScrollPane.setCorner(ScrollPaneConstants.LOWER_LEADING_CORNER, cornerLL);
    Resizable _resizable =
        new Resizable(getTableScrollPane()) {
          public void resizing(int resizeDir, int newX, int newY, int newW, int newH) {
            PortingUtils.setPreferredSize(getTableScrollPane(), new Dimension(newW, newH));
            PreferencesTable.this.setBounds(newX, newY, newW, newH);
          }
        };
    _resizable.setResizeCornerSize(18);
    _resizable.setResizableCorners(Resizable.LOWER_LEFT | Resizable.LOWER_RIGHT);
  }

  public void setFilterText(String filterText) {
    this.filterText.set(filterText);
  }

  public void finalizeCellEditing() {
    if (this.isEditing()) {
      this.getCellEditor().stopCellEditing();
    }
  }

  public JScrollPane getTableScrollPane() {
    Component parent = this.getParent();
    while (!(parent instanceof JScrollPane)) {
      parent = parent.getParent();
    }
    return (JScrollPane) parent;
  }

  public void valueChanged(ListSelectionEvent e) {
    ListSelectionModel lsm = (ListSelectionModel) e.getSource();
    int visibleRowIndex = this.getSelectedRow(); // Get the index of the selected row
    // Convert from the selection index based on the visible items to the
    // internal index for all elements.
    int internalRowIndex = convertRowIndexToModel(visibleRowIndex);
  }

  /**
   * Returns an appropriate renderer for the cell specified by this row and column. If the <code>
   * TableColumn</code> for this column has a non-null renderer, returns that. If not, finds the
   * class of the data in this column (using <code>getColumnClass</code>) and returns the default
   * renderer for this type of data.
   *
   * <p><b>Note:</b> Throughout the table package, the internal implementations always use this
   * method to provide renderers so that this default behavior can be safely overridden by a
   * subclass.
   *
   * @param row the row of the cell to render, where 0 is the first row
   * @param column the column of the cell to render, where 0 is the first column
   * @return the assigned renderer; if <code>null</code> returns the default renderer for this type
   *     of object
   * @see DefaultTableCellRenderer
   * @see TableColumn#setCellRenderer
   * @see #setDefaultRenderer
   */
  @Override
  public TableCellRenderer getCellRenderer(int row, int column) {
    return super.getCellRenderer(row, column);
  }

  @Override
  public Dimension getMinimumSize() {
    Dimension superMin = super.getMinimumSize();
    return new Dimension(
        getMinimumWidth() + tableScrollPane.getVerticalScrollBar().getPreferredSize().width,
        superMin.height);
  }

  private int getMinimumWidth() {
    if (minimumWidth == -1) {
      minimumWidth = getColumnWidths().stream().reduce(0, Integer::sum);
    }
    return minimumWidth;
  }

  private final List<Integer> columnWidths = new ArrayList<>();

  private List<Integer> getColumnWidths() {
    if (!columnWidths.isEmpty()) {
      return columnWidths;
    }
    Font font = this.getFont();
    FontMetrics fm = getFontMetrics(font);
    // string widths
    int width = 0;
    for (Classify.Section section : Classify.Section.values()) {
      width = Math.max(width, fm.stringWidth(section.toString()));
    }
    columnWidths.add(width + this.getRowMargin());
    width = 0;
    for (Classify.Group group : Classify.Group.values()) {
      width = Math.max(width, fm.stringWidth(group.toString()));
    }
    columnWidths.add(width + this.getRowMargin());
    width = 0;
    int width1 = 0;
    //        DefaultGroupRow
    for (int i = 0; i < PREFERENCES_TABLE_MODEL.getRowCount(); i++) {
      width =
          Math.max(
              width, fm.stringWidth(((JLabel) PREFERENCES_TABLE_MODEL.getValueAt(i, 2)).getText()));
      width1 =
          Math.max(
              width1,
              ((JComponent) PREFERENCES_TABLE_MODEL.getValueAt(i, 3)).getPreferredSize().width);
    }
    columnWidths.add(width + this.getRowMargin());
    columnWidths.add(width1);
    return columnWidths;
  }

  @Override
  public FontMetrics getFontMetrics(Font font) {
    if (font == null) {
      font = Font.decode(FlatLaf.getPreferredFontFamily());
    }
    if (this.isVisible()) {
      return super.getFontMetrics(font);
    } else {
      return MapTool.getFrame()
          .getGraphicsConfiguration()
          .getDevice()
          .getDefaultConfiguration()
          .createCompatibleVolatileImage(1, 1)
          .getGraphics()
          .getFontMetrics(font);
    }
  }

  public void prettify() {
    final List<Integer> headerSizes = getColumnWidths();

    //        for (int i = 0; i < getColumnCount(); i++) {
    //            getColumnModel().getColumn(i).setHeaderValue(getModel().getColumnName(i));
    //            getColumnModel().getColumn(i).setMinWidth(headerSizes.get(i));
    //            getColumnModel().getColumn(i).setMaxWidth(headerSizes.get(i) / 2 * 3);
    //            getColumnModel().getColumn(i).setPreferredWidth(headerSizes.get(i) + 12);
    //        }
    /* fix text areas to look like labels
     * dig down to the appropriate container level
     * then set the backgrounds to transparent
     */
    //        JPanel jPanel = (JPanel) this.getParent();
    //        List<Component> jPanels =
    //                Arrays.stream(jPanel.getComponents()).filter(c -> c instanceof
    // JPanel).toList();
    //
    //        final Color CLEAR = new Color(1f,1f,1f, 1);
    //        for (Component panel : jPanels) {
    //            JPanel jp = (JPanel) panel;
    //            Component[] components = jp.getComponents();
    //            Arrays.stream(components).toList().forEach(c -> c.setBackground(CLEAR));
    //        }

    /* prettify - take cell background colour and adjust the luminance for cell contrast.
    change the hue and saturation for the grid line colour
     */
    Color bg, bgSmall, gridColour;
    bg = this.getTableHeader().getComponent(0).getBackground(); // get background colour
    float[] hsbComponents = new float[3];
    Color.RGBtoHSB(bg.getRed(), bg.getGreen(), bg.getBlue(), hsbComponents); // convert to HSB

    boolean lighten = hsbComponents[2] < 0.5f; // to determine direction of change
    hsbComponents[2] =
        lighten
            ? hsbComponents[2] + 0.015f
            : hsbComponents[2] - 0.025f; // small change in brilliance
    bgSmall = new Color(Color.HSBtoRGB(hsbComponents[0], hsbComponents[1], hsbComponents[2]));

    hsbComponents[2] =
        lighten
            ? hsbComponents[2] + 0.04f
            : hsbComponents[2] - 0.02f; // bigger change in brilliance
    bg = new Color(Color.HSBtoRGB(hsbComponents[0], hsbComponents[1], hsbComponents[2]));

    hsbComponents[0] =
        hsbComponents[0] < 0.5
            ? hsbComponents[0] + 0.5f
            : hsbComponents[0] - 0.5f; // change hue 180 degrees
    hsbComponents[1] =
        hsbComponents[1] < 0.25
            ? hsbComponents[1] + 0.25f // increase saturation if it is low
            : hsbComponents[1];
    gridColour = new Color(Color.HSBtoRGB(hsbComponents[0], hsbComponents[1], hsbComponents[2]));

    DefaultTableCellRenderer cellRenderer =
        new DefaultTableCellRenderer(); // cell renderer for contrasting cells
    cellRenderer.setBackground(bgSmall);
    cellRenderer.setHorizontalAlignment(DefaultTableCellRenderer.LEFT);

    // cell renderer for contrasting headings
    Color finalBg = bg;
    Function<Integer, DefaultTableCellRenderer> headerRenderer =
        column -> {
          DefaultTableCellRenderer hr = new DefaultTableCellRenderer();
          if ((column & 1) == 1) {
            hr.setBackground(finalBg);
          }
          hr.setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
          hr.setVerticalAlignment(
              column == 1 || column == 4 ? SwingConstants.TOP : SwingConstants.CENTER);
          //                    hr.setToolTipText(
          //                            ((PreferencesTableModel)
          // jTable.getModel()).getColumnTooltipText(column));
          return hr;
        };

    this.setGridColor(gridColour);
    this.setIntercellSpacing(new Dimension(2, 2));
    this.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    this.setShowHorizontalLines(true);
    this.setFillsViewportHeight(true);

    for (int i = 0; i < this.getColumnCount(); i++) {
      this.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer.apply(i));
      switch (i) { // set column shading
        case 1, 3 -> this.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
      }
    }
    doLayout();
  }

  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);
    //        this.prettify();
  }

  private static final Border margin = BorderFactory.createEmptyBorder(1, 2, 1, 2);

  private static class EditorRenderer extends AbstractTableCellEditorRenderer {
    @Override
    public Component createTableCellEditorRendererComponent(JTable table, int row, int column) {
      JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder(margin);
      return panel;
    }

    @Override
    public void configureTableCellEditorRendererComponent(
        JTable table,
        Component editorRendererComponent,
        boolean forRenderer,
        Object value,
        boolean isSelected,
        boolean hasFocus,
        int row,
        int column) {
      if (table.getColumnClass(column).equals(PreferencesTableModel.PrefComponentPane.class)) {
        ((JPanel) editorRendererComponent).add((JComponent) value, BorderLayout.CENTER);
      }
    }

    /**
     * Returns the value contained in the editor.
     *
     * @return the value contained in the editor
     */
    @Override
    public Object getCellEditorValue() {
      return null;
    }
  }

  private static class ComponentCellEditor extends AbstractCellEditor
      implements CellEditorFactory, TableCellEditor {
    public ComponentCellEditor() {}

    public Component getTableCellEditorComponent(
        JTable table, Object value, boolean isSelected, int row, int column) {
      if (value instanceof JComponent jComponent) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(margin);
        panel.add(jComponent, BorderLayout.CENTER);
        return panel;
      } else {
        return new DefaultTableCellRenderer();
      }
    }

    @Override
    public Object getCellEditorValue() {
      return null;
    }

    @Override
    public CellEditor create() {
      return new ComponentCellEditor();
    }
  }

  private static class ComponentCellRenderer implements TableCellRenderer {
    ComponentCellRenderer() {}

    @Override
    public Component getTableCellRendererComponent(
        JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      JPanel panel = new JPanel(new BorderLayout());
      panel.setBorder(margin);
      if (value instanceof JComponent jComponent) {
        panel.add(jComponent, BorderLayout.CENTER);
      }
      return panel;
    }
  }
}
