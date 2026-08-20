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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolUtil;
import net.rptools.maptool.client.swing.AbeillePanel;
import net.rptools.maptool.client.swing.ButtonKind;
import net.rptools.maptool.client.swing.GenericDialog;
import net.rptools.maptool.client.swing.GenericDialogFactory;
import net.rptools.maptool.client.swing.ImageChooserDialog;
import net.rptools.maptool.client.ui.ImageAssetPanel;
import net.rptools.maptool.client.ui.theme.Icons;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.AssetManager;
import net.rptools.maptool.model.LookupTable;
import net.rptools.maptool.model.LookupTable.LookupEntry;

public class EditLookupTablePanel extends AbeillePanel<LookupTable> {
  private static final int PICKED_COLUMN_INDEX = 0;
  private static final int RANGE_COLUMN_INDEX = 1;
  private static final int VALUE_COLUMN_INDEX = 2;
  private static final int IMAGE_COLUMN_INDEX = 3;

  private final GenericDialogFactory dialogFactory;

  private final EditLookupTablePanelView view;
  private ImageAssetPanel tableImageAssetPanel;
  private int defaultRowHeight;
  private boolean isNew;

  private EditLookupTablePanel(EditLookupTablePanelView view) {
    super(view.getRootComponent());
    this.view = view;
    panelInit();

    dialogFactory =
        GenericDialog.getFactory()
            .setContent(this)
            .makeModal(true)
            .addButton(ButtonKind.ACCEPT)
            .addButton(ButtonKind.CANCEL)
            .setDefaultButton(ButtonKind.ACCEPT)
            .setCloseOperation(WindowConstants.HIDE_ON_CLOSE)
            .onBeforeClose(
                e -> {
                  unbind();
                });
  }

  public EditLookupTablePanel() {
    this(new EditLookupTablePanelView());
  }

  public void showDialog(@Nullable LookupTable lookupTable, boolean isNew) {
    this.isNew = isNew;
    var title =
        isNew || lookupTable == null
            ? I18N.getString("LookupTablePanel.msg.titleNew")
            : I18N.getString("LookupTablePanel.msg.titleEdit");
    dialogFactory.setDialogTitle(title);

    view.getIsVisibleIcon()
        .setIcon(RessourceManager.getSmallIcon(Icons.TABLEPANEL_TABLE_PLAYER_VISIBLE));
    view.getAllowLookupIcon()
        .setIcon(RessourceManager.getSmallIcon(Icons.TABLEPANEL_TABLE_PLAYER_LOOKUP));
    view.getPickOnceIcon().setIcon(RessourceManager.getSmallIcon(Icons.TABLEPANEL_TABLE_PICK_ONCE));

    bind(lookupTable);

    dialogFactory.display();
  }

  public void initPickOnce() {
    JCheckBox pickOnce = view.getPickOnce();
    JButton resetPicks = view.getResetPicks();

    pickOnce.addActionListener(
        event -> {
          var isPickOnce = pickOnce.isSelected();
          view.getDefaultTableRoll().setEnabled(!isPickOnce);
          resetPicks.setEnabled(isPickOnce);

          var model = (LookupTableTableModel) view.getDefinitionTable().getModel();
          model.showPicks(isPickOnce);
        });

    resetPicks.addActionListener(
        e -> {
          var model = (LookupTableTableModel) view.getDefinitionTable().getModel();
          model.clearAllPicks();
        });
  }

  public void initTableDefinitionTable() {
    JTable definitionTable = view.getDefinitionTable();

    defaultRowHeight = definitionTable.getRowHeight();

    definitionTable.setDefaultRenderer(ImageAssetPanel.class, new ImageCellRenderer());
    definitionTable.setModel(new LookupTableTableModel());
    definitionTable.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mousePressed(MouseEvent e) {
            var model = (LookupTableTableModel) definitionTable.getModel();

            int column = model.getCanonicalColumn(definitionTable.columnAtPoint(e.getPoint()));
            if (column != IMAGE_COLUMN_INDEX) {
              return;
            }
            var row = model.getRowAt(definitionTable.rowAtPoint(e.getPoint()));
            String imageIdStr = row.getImageId();

            // HACK: this is a hacky way to figure out if the button was pushed :P
            if (e.getPoint().x > definitionTable.getSize().width - 15) {
              if (imageIdStr == null || imageIdStr.isEmpty()) {
                // Add
                ImageChooserDialog chooserDialog = MapTool.getFrame().getImageChooserDialog();
                chooserDialog.setVisible(true);

                MD5Key imageId = chooserDialog.getImageId();
                if (imageId == null) {
                  return;
                }
                imageIdStr = imageId.toString();
              } else {
                // Cancel
                imageIdStr = null;
              }
            } else if (e.getPoint().x > definitionTable.getSize().width - 30) {
              // Add
              ImageChooserDialog chooserDialog = MapTool.getFrame().getImageChooserDialog();

              chooserDialog.setVisible(true);

              MD5Key imageId = chooserDialog.getImageId();
              if (imageId == null) {
                return;
              }
              imageIdStr = imageId.toString();
            }

            // Commit our changes back.
            row.setImageId(imageIdStr);
          }
        });
  }

  public void initTableImage() {
    tableImageAssetPanel = new ImageAssetPanel();
    tableImageAssetPanel.setPreferredSize(new Dimension(150, 150));
    tableImageAssetPanel.setBorder(BorderFactory.createLineBorder(Color.black));

    replaceComponent(view.getTableImagePlaceholder(), tableImageAssetPanel);
  }

  @Override
  public void bind(LookupTable lookupTable) {
    super.bind(lookupTable);

    view.getTableName().setText(lookupTable.getName());
    view.getDefaultTableRoll()
        .setText(lookupTable.getPickOnce() ? "" : lookupTable.calculateRoll());
    tableImageAssetPanel.setImageId(lookupTable.getTableImage());
    view.getIsVisible().setSelected(lookupTable.getVisible());
    view.getAllowLookup().setSelected(lookupTable.getAllowLookup());

    view.getPickOnce().setSelected(lookupTable.getPickOnce());
    view.getDefaultTableRoll().setEnabled(!lookupTable.getPickOnce());
    view.getResetPicks().setEnabled(lookupTable.getPickOnce());

    view.getTableName().requestFocusInWindow();

    var model = new LookupTableTableModel();
    model.addTableModelListener(e -> updateDefinitionTableRowHeights());
    view.getDefinitionTable().setModel(model);
    populateLookupTableModel(lookupTable, model);
  }

  @Override
  public boolean commit() {
    if (!super.commit()) {
      return false;
    }

    var lookupTable = getModel();

    // Commit any in-process edits
    var definitionTable = view.getDefinitionTable();
    if (definitionTable.isEditing()) {
      definitionTable.getCellEditor().stopCellEditing();
    }
    String name = view.getTableName().getText().trim();
    if (name.isEmpty()) {
      MapTool.showError("EditLookupTablePanel.error.noName");
      return false;
    }
    LookupTable existingTable = MapTool.getCampaign().getLookupTableMap().get(name);
    if (existingTable != null && existingTable != lookupTable) {
      MapTool.showError(I18N.getText("EditLookupTablePanel.error.sameName", name));
      return false;
    }
    var tableModel = (LookupTableTableModel) definitionTable.getModel();
    if (tableModel.getRowCount() < 1) {
      MapTool.showError(I18N.getText("EditLookupTablePanel.error.invalidSize", name));
      return false;
    }
    var isPickOnce = view.getPickOnce().isSelected();

    // Before modifying the table, parse and validate all the entries to avoid partial modification
    // in case of error.
    var entries = new ArrayList<LookupTable.LookupEntry>();
    for (int i = 0; i < tableModel.getRowCount(); i++) {
      var row = tableModel.getRowAt(i);
      var range = row.getRange();
      if (range.isEmpty()) {
        continue;
      }

      int min;
      int max;
      // Allow negative numbers
      int split = range.indexOf('-', range.charAt(0) == '-' ? 1 : 0);
      try {
        if (split < 0) {
          min = Integer.parseInt(range);
          max = min;
        } else {
          min = Integer.parseInt(range.substring(0, split).trim());
          max = Integer.parseInt(range.substring(split + 1).trim());
        }
      } catch (NumberFormatException nfe) {
        MapTool.showError(I18N.getText("EditLookupTablePanel.error.badRange", name, range, i));
        return false;
      }

      MD5Key image = null;
      if (row.getImageId() != null && !row.getImageId().isEmpty()) {
        image = new MD5Key(row.getImageId());
        MapToolUtil.uploadAsset(AssetManager.getAsset(image));
      }

      var entry = new LookupEntry(min, max, row.getValue(), image);
      if (isPickOnce) {
        entry.setPicked(row.isPicked());
      }
      entries.add(entry);
    }

    // save existing name for later removal from LookupTableMap
    String origname = lookupTable.getName();
    lookupTable.setName(name);
    lookupTable.setPickOnce(isPickOnce);
    lookupTable.setRoll(isPickOnce ? null : view.getDefaultTableRoll().getText());
    lookupTable.setTableImage(tableImageAssetPanel.getImageId());
    lookupTable.setVisible(view.getIsVisible().isSelected());
    lookupTable.setAllowLookup(view.getAllowLookup().isSelected());

    lookupTable.clearEntries();
    for (var entry : entries) {
      var copy =
          lookupTable.addEntry(
              entry.getMin(), entry.getMax(), entry.getValue(), entry.getImageId());
      copy.setPicked(entry.getPicked());
    }

    if (!name.equals(origname)) {
      // New name is not the same as the existing name
      MapTool.getCampaign().getLookupTableMap().remove(origname);
      // if not a new table (i.e. create/duplicate) delete the previous named one from the server.
      if (!isNew) {
        MapTool.serverCommand().deleteLookupTable(origname);
      }
    }
    // This will add it if it is new
    MapToolUtil.uploadAsset(AssetManager.getAsset(tableImageAssetPanel.getImageId()));
    MapTool.serverCommand().putLookupTable(lookupTable);

    return true;
  }

  private void updateDefinitionTableRowHeights() {
    JTable table = view.getDefinitionTable();
    var model = (LookupTableTableModel) table.getModel();

    for (int row = 0; row < table.getRowCount(); row++) {
      String imageId = model.getRowAt(row).getImageId();
      table.setRowHeight(row, imageId != null && !imageId.isEmpty() ? 100 : defaultRowHeight);
    }
  }

  private void populateLookupTableModel(LookupTable lookupTable, LookupTableTableModel model) {
    for (LookupEntry entry : lookupTable.getEntryList()) {
      boolean picked = entry.getPicked();
      String range =
          entry.getMax() != entry.getMin()
              ? entry.getMin() + "-" + entry.getMax()
              : "" + entry.getMin();
      String value = entry.getValue();
      MD5Key imageId = entry.getImageId();

      model.addRow(picked, range, value, imageId != null ? imageId.toString() : null);
    }
    model.showPicks(lookupTable.getPickOnce());
  }

  private class ImageCellRenderer extends ImageAssetPanel implements TableCellRenderer {
    public Component getTableCellRendererComponent(
        JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      String s = value == null ? "" : Objects.requireNonNullElse(value.toString(), "");
      setImageId(!s.isEmpty() ? new MD5Key(s) : null, EditLookupTablePanel.this);
      return this;
    }
  }

  private static final class LookupTableRow {
    private final PropertyChangeSupport pcs;
    private boolean picked;
    private String range;
    private String value;
    private @Nullable String imageId;

    public LookupTableRow() {
      this(false, "", "", null);
    }

    public LookupTableRow(boolean picked, String range, String value, @Nullable String imageId) {
      this.picked = picked;
      this.range = range;
      this.value = value;
      this.imageId = imageId;
      this.pcs = new PropertyChangeSupport(this);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
      this.pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
      this.pcs.removePropertyChangeListener(listener);
    }

    public boolean isPicked() {
      return picked;
    }

    public void setPicked(boolean picked) {
      var oldValue = this.picked;
      this.picked = picked;
      this.pcs.firePropertyChange("picked", oldValue, picked);
    }

    public String getRange() {
      return range;
    }

    public void setRange(String range) {
      var oldValue = this.range;
      this.range = range;
      this.pcs.firePropertyChange("range", oldValue, range);
    }

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      var oldValue = this.value;
      this.value = value;
      this.pcs.firePropertyChange("value", oldValue, value);
    }

    public @Nullable String getImageId() {
      return imageId;
    }

    public void setImageId(@Nullable String imageId) {
      var oldValue = this.imageId;
      this.imageId = imageId;
      this.pcs.firePropertyChange("imageId", oldValue, imageId);
    }
  }

  private static final class LookupTableTableModel extends AbstractTableModel
      implements PropertyChangeListener {
    private LookupTableRow newRow = new LookupTableRow();
    private final List<LookupTableRow> rowList;

    private final List<String> columnNames =
        List.of(
            I18N.getText("Label.isPicked"),
            I18N.getText("Label.range"),
            I18N.getText("Label.value"),
            I18N.getText("Label.image"));
    private boolean showPicks = false;

    public LookupTableTableModel() {
      this.rowList = new ArrayList<>();
      newRow.addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      fireTableDataChanged();
    }

    public void addRow(boolean isPicked, String range, String value, @Nullable String imageId) {
      var row = new LookupTableRow(isPicked, range, value, imageId);
      rowList.add(row);
      row.addPropertyChangeListener(this);
      fireTableRowsInserted(rowList.size(), rowList.size());
    }

    public int getCanonicalColumn(int columnIndex) {
      // When the "Picked?" column is not present, the column indices don't line up with our
      // predefined constants. So increment the index to match those.
      return columnIndex + (showPicks ? 0 : 1);
    }

    public void showPicks(boolean show) {
      this.showPicks = show;
      fireTableStructureChanged();
      // This one is needed to make sure the rows all update immediately as well.
      fireTableDataChanged();
    }

    public void clearAllPicks() {
      for (var row : rowList) {
        row.setPicked(false);
      }
      newRow.setPicked(false);
      fireTableRowsUpdated(0, getRowCount());
    }

    public LookupTableRow getRowAt(int rowIndex) {
      if (rowIndex < 0 || rowIndex > rowList.size()) {
        throw new IndexOutOfBoundsException(rowIndex);
      }
      if (rowIndex == rowList.size()) {
        return newRow;
      }
      return rowList.get(rowIndex);
    }

    public int getColumnCount() {
      var columnCount = columnNames.size();
      if (!showPicks) {
        --columnCount;
      }
      return columnCount;
    }

    public int getRowCount() {
      return rowList.size() + 1;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
      columnIndex = getCanonicalColumn(columnIndex);

      LookupTableRow row = rowIndex < rowList.size() ? rowList.get(rowIndex) : newRow;

      return switch (columnIndex) {
        case PICKED_COLUMN_INDEX -> row.isPicked();
        case RANGE_COLUMN_INDEX -> row.getRange();
        case VALUE_COLUMN_INDEX -> row.getValue();
        case IMAGE_COLUMN_INDEX -> row.getImageId();
        default -> "";
      };
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
      columnIndex = getCanonicalColumn(columnIndex);

      LookupTableRow row = rowIndex < rowList.size() ? rowList.get(rowIndex) : newRow;

      switch (columnIndex) {
        case PICKED_COLUMN_INDEX -> row.setPicked((boolean) aValue);
        case RANGE_COLUMN_INDEX -> row.setRange((String) aValue);
        case VALUE_COLUMN_INDEX -> row.setValue((String) aValue);
        case IMAGE_COLUMN_INDEX -> row.setImageId((String) aValue);
      }

      if (row == newRow) {
        // Need to make the row permanent, and add a new placeholder.
        rowList.add(newRow);
        newRow = new LookupTableRow();
        newRow.addPropertyChangeListener(this);
        fireTableRowsInserted(rowList.size(), rowList.size());
      }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
      columnIndex = getCanonicalColumn(columnIndex);

      if (columnIndex == PICKED_COLUMN_INDEX) {
        return Boolean.class;
      } else if (columnIndex == IMAGE_COLUMN_INDEX) {
        return ImageAssetPanel.class;
      } else {
        return String.class;
      }
    }

    @Override
    public String getColumnName(int columnIndex) {
      columnIndex = getCanonicalColumn(columnIndex);

      return columnNames.get(columnIndex);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
      columnIndex = getCanonicalColumn(columnIndex);

      return columnIndex != IMAGE_COLUMN_INDEX;
    }
  }
}
