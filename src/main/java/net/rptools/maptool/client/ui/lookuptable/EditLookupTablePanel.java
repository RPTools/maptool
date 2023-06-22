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
import java.awt.EventQueue;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolUtil;
import net.rptools.maptool.client.swing.AbeillePanel;
import net.rptools.maptool.client.swing.ImageChooserDialog;
import net.rptools.maptool.client.ui.ImageAssetPanel;
import net.rptools.maptool.client.ui.lookuptable.EditLookupTablePanel.LookupTableTableModel;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.AssetManager;
import net.rptools.maptool.model.LookupTable;
import net.rptools.maptool.model.LookupTable.LookupEntry;

public class EditLookupTablePanel extends AbeillePanel<LookupTableTableModel> {
  private static final long serialVersionUID = 2341539768448195059L;

  private LookupTable lookupTable;
  private ImageAssetPanel tableImageAssetPanel;
  private int defaultRowHeight;

  private boolean accepted = false;
  private boolean newTable = false;

  public EditLookupTablePanel() {
    super(new EditLookupTablePanelView().getRootComponent());
    panelInit();
  }

  public void initTableDefinitionTable() {
    defaultRowHeight = getTableDefinitionTable().getRowHeight();

    getTableDefinitionTable().setDefaultRenderer(ImageAssetPanel.class, new ImageCellRenderer());
    getTableDefinitionTable().setModel(createLookupTableModel(new LookupTable()));
    getTableDefinitionTable()
        .addMouseListener(
            new MouseAdapter() {
              @Override
              public void mousePressed(MouseEvent e) {
                int column = getTableDefinitionTable().columnAtPoint(e.getPoint());
                if (column < 2) {
                  return;
                }
                int row = getTableDefinitionTable().rowAtPoint(e.getPoint());
                String imageIdStr =
                    (String) getTableDefinitionTable().getModel().getValueAt(row, column);

                // HACK: this is a hacky way to figure out if the button was pushed :P
                if (e.getPoint().x > getTableDefinitionTable().getSize().width - 15) {
                  if (imageIdStr == null || imageIdStr.length() == 0) {
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
                } else if (e.getPoint().x > getTableDefinitionTable().getSize().width - 30) {
                  // Add
                  ImageChooserDialog chooserDialog = MapTool.getFrame().getImageChooserDialog();

                  chooserDialog.setVisible(true);

                  MD5Key imageId = chooserDialog.getImageId();
                  if (imageId == null) {
                    return;
                  }
                  imageIdStr = imageId.toString();
                }
                getTableDefinitionTable().getModel().setValueAt(imageIdStr, row, column);
                updateDefinitionTableRowHeights();
                getTableDefinitionTable().repaint();
              }
            });
  }

  public void initTableImage() {
    tableImageAssetPanel = new ImageAssetPanel();
    tableImageAssetPanel.setPreferredSize(new Dimension(150, 150));
    tableImageAssetPanel.setBorder(BorderFactory.createLineBorder(Color.black));
    replaceComponent("mainForm", "tableImage", tableImageAssetPanel);
  }

  public void attach(LookupTable luTable) {
    newTable = luTable == null;
    lookupTable = newTable ? new LookupTable() : luTable;
    accepted = false;

    getTableNameTextField().setText(this.lookupTable.getName());
    getTableRollTextField().setText(this.lookupTable.getRoll());
    tableImageAssetPanel.setImageId(this.lookupTable.getTableImage());
    getVisibleCheckbox().setSelected(this.lookupTable.getVisible());
    getAllowLookupCheckbox().setSelected(this.lookupTable.getAllowLookup());

    getTableNameTextField().requestFocusInWindow();

    EventQueue.invokeLater(
        () -> {
          getTableDefinitionTable()
              .setModel(createLookupTableModel(EditLookupTablePanel.this.lookupTable));
          updateDefinitionTableRowHeights();
        });
  }

  public boolean accepted() {
    return accepted;
  }

  public JTextField getTableNameTextField() {
    return (JTextField) getComponent("tableName");
  }

  public JTextField getTableRollTextField() {
    return (JTextField) getComponent("defaultTableRoll");
  }

  public JTable getTableDefinitionTable() {
    return (JTable) getComponent("definitionTable");
  }

  public JList getTableList() {
    return (JList) getComponent("tableList");
  }

  public JCheckBox getVisibleCheckbox() {
    return (JCheckBox) getComponent("visibleCheckbox");
  }

  public JCheckBox getAllowLookupCheckbox() {
    return (JCheckBox) getComponent("allowLookupCheckbox");
  }

  public void initCancelButton() {
    JButton button = (JButton) getComponent("cancelButton");
    button.addActionListener(
        e -> {
          accepted = false;
          close();
        });
  }

  public void initAcceptButton() {
    JButton button = (JButton) getComponent("acceptButton");
    button.addActionListener(
        e -> {
          // Commit any in-process edits
          if (getTableDefinitionTable().isEditing()) {
            getTableDefinitionTable().getCellEditor().stopCellEditing();
          }
          String name = getTableNameTextField().getText().trim();
          if (name.length() == 0) {
            MapTool.showError("EditLookupTablePanel.error.noName");
            return;
          }
          LookupTable existingTable = MapTool.getCampaign().getLookupTableMap().get(name);
          if (existingTable != null && existingTable != lookupTable) {
            MapTool.showError(I18N.getText("EditLookupTablePanel.error.sameName", name));
            return;
          }
          TableModel tableModel = getTableDefinitionTable().getModel();
          if (tableModel.getRowCount() < 1) {
            MapTool.showError(I18N.getText("EditLookupTablePanel.error.invalidSize", name));
            return;
          }
          String origname =
              lookupTable.getName(); // save existing name for later removal from LookupTableMap
          lookupTable.setName(name);
          lookupTable.setRoll(getTableRollTextField().getText());
          lookupTable.setTableImage(tableImageAssetPanel.getImageId());
          lookupTable.setVisible(getVisibleCheckbox().isSelected());
          lookupTable.setAllowLookup(getAllowLookupCheckbox().isSelected());
          lookupTable.clearEntries();
          for (int i = 0; i < tableModel.getRowCount(); i++) {
            String range = ((String) tableModel.getValueAt(i, 0)).trim();
            if (range.length() == 0) {
              continue;
            }
            String value = ((String) tableModel.getValueAt(i, 1)).trim();
            String imageId = (String) tableModel.getValueAt(i, 2);

            int min = 0;
            int max = 0;

            int split =
                range.indexOf('-', range.charAt(0) == '-' ? 1 : 0); // Allow negative numbers
            try {
              if (split < 0) {
                min = Integer.parseInt(range);
                max = min;
              } else {
                min = Integer.parseInt(range.substring(0, split).trim());
                max = Integer.parseInt(range.substring(split + 1).trim());
              }
            } catch (NumberFormatException nfe) {
              MapTool.showError(
                  I18N.getText("EditLookupTablePanel.error.badRange", name, range, i));
              return;
            }
            MD5Key image = null;
            if (imageId != null && imageId.length() > 0) {
              image = new MD5Key(imageId);
              MapToolUtil.uploadAsset(AssetManager.getAsset(image));
            }
            lookupTable.addEntry(min, max, value, image);
          }
          if (!name.equals(origname)) {
            // New name is not the same as the existing name
            MapTool.getCampaign().getLookupTableMap().remove(origname);
          }
          // This will add it if it is new
          MapToolUtil.uploadAsset(AssetManager.getAsset(tableImageAssetPanel.getImageId()));
          MapTool.getCampaign().getLookupTableMap().put(name, lookupTable);
          MapTool.serverCommand().updateCampaign(MapTool.getCampaign().getCampaignProperties());
          accepted = true;
          close();
        });
  }

  private void close() {
    SwingUtilities.getWindowAncestor(this).setVisible(false);
  }

  private void updateDefinitionTableRowHeights() {
    JTable table = getTableDefinitionTable();
    for (int row = 0; row < table.getRowCount(); row++) {
      String imageId = (String) table.getModel().getValueAt(row, 2);
      table.setRowHeight(row, imageId != null && imageId.length() > 0 ? 100 : defaultRowHeight);
    }
  }

  private LookupTableTableModel createLookupTableModel(LookupTable lookupTable) {
    List<List<String>> rows = new ArrayList<List<String>>();
    if (lookupTable != null) {
      for (LookupEntry entry : lookupTable.getEntryList()) {
        String range =
            entry.getMax() != entry.getMin()
                ? entry.getMin() + "-" + entry.getMax()
                : "" + entry.getMin();
        String value = entry.getValue();
        MD5Key imageId = entry.getImageId();

        rows.add(Arrays.asList(range, value, imageId != null ? imageId.toString() : null));
      }
    }
    return new LookupTableTableModel(
        rows,
        I18N.getText("Label.range"),
        I18N.getText("Label.value"),
        I18N.getText("Label.image"));
  }

  private class ImageCellRenderer extends ImageAssetPanel implements TableCellRenderer {
    private static final long serialVersionUID = 183503471819640825L;

    public Component getTableCellRendererComponent(
        JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      setImageId(
          value != null && ((String) value).length() > 0 ? new MD5Key((String) value) : null,
          EditLookupTablePanel.this);
      return this;
    }
  }

  static class LookupTableTableModel extends AbstractTableModel {
    private static final long serialVersionUID = -6310344745803084970L;

    private List<String> newRow = new ArrayList<String>();
    private final List<List<String>> rowList;
    private final String[] cols;

    public LookupTableTableModel(List<List<String>> rowList, String... cols) {
      this.rowList = rowList;
      this.cols = cols;
    }

    public int getColumnCount() {
      return cols.length;
    }

    public int getRowCount() {
      return rowList.size() + 1;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
      List<String> row = null;

      // Existing value
      if (rowIndex < rowList.size()) {
        row = rowList.get(rowIndex);
      } else {
        row = newRow;
      }
      return columnIndex < row.size() ? row.get(columnIndex) : "";
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
      boolean hasNewRow = false;
      List<String> row = null;
      if (rowIndex < rowList.size()) {
        row = rowList.get(rowIndex);
      } else {
        row = newRow;
        rowList.add(newRow);
        newRow = new ArrayList<String>();
        hasNewRow = true;
      }
      while (columnIndex >= row.size()) {
        row.add("");
      }
      row.set(columnIndex, (String) aValue);
      if (hasNewRow) {
        fireTableRowsInserted(rowList.size(), rowList.size());
      }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
      return columnIndex < 2 ? String.class : ImageAssetPanel.class;
    }

    @Override
    public String getColumnName(int column) {
      return cols[column];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
      return columnIndex != 2;
    }
  }

  public void initToolTips() {
    getVisibleCheckbox().setToolTipText(I18N.getString("EditLookupTablePanel.tooltip.visible"));
    getAllowLookupCheckbox()
        .setToolTipText(I18N.getString("EditLookupTablePanel.tooltip.allowLookup"));
  }
}
