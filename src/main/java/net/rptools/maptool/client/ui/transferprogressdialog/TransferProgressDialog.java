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
package net.rptools.maptool.client.ui.transferprogressdialog;

import java.awt.EventQueue;
import java.io.File;
import java.text.NumberFormat;
import java.util.List;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.swing.AbeillePanel;
import net.rptools.maptool.client.swing.ButtonKind;
import net.rptools.maptool.client.swing.GenericDialog;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.transfer.AssetConsumer;
import net.rptools.maptool.transfer.ConsumerListener;

/** This dialog is used to display all the assets being transferred */
public class TransferProgressDialog extends AbeillePanel<Token> implements ConsumerListener {
  public TransferProgressDialog() {
    super(new TransferProgressDialogView().getRootComponent());
    panelInit();
  }

  public void showDialog() {
    GenericDialog.getFactory()
        .setDialogTitle(I18N.getText("TransferProgressDialog.title"))
        .setContent(this)
        .onBeforeShow(
            e -> MapTool.getAssetTransferManager().addConsumerListener(TransferProgressDialog.this))
        .onBeforeClose(
            e ->
                MapTool.getAssetTransferManager()
                    .removeConsumerListener(TransferProgressDialog.this))
        .addButton(ButtonKind.CLOSE)
        .setDefaultButton(ButtonKind.CLOSE)
        .display();
  }

  public JTable getTransferTable() {
    return (JTable) getComponent("transferTable");
  }

  private void updateTransferTable() {

    final TransferTableModel model = new TransferTableModel();
    EventQueue.invokeLater(
        () -> {
          getTransferTable().setModel(model);

          TableColumnModel colModel = getTransferTable().getColumnModel();
          colModel.getColumn(1).setMaxWidth(100);
          colModel.getColumn(2).setMaxWidth(75);
        });
  }

  @SuppressWarnings("unused")
  public void initTransferTable() {
    updateTransferTable();
  }

  private static class TransferTableModel extends AbstractTableModel {

    private final List<AssetConsumer> consumerList;

    public TransferTableModel() {
      consumerList = MapTool.getAssetTransferManager().getAssetConsumers();
    }

    public int getColumnCount() {
      return 3;
    }

    public int getRowCount() {
      return Math.max(consumerList.size(), 1);
    }

    public Object getValueAt(int rowIndex, int columnIndex) {

      if (consumerList.isEmpty()) {
        return columnIndex == 0 ? I18N.getText("AddResourcesDialog.label.none") : "";
      }

      AssetConsumer consumer = consumerList.get(rowIndex);

      return switch (columnIndex) {
        case 0 -> consumer.getId();
        case 1 -> formatSize(consumer.getSize());
        case 2 -> NumberFormat.getPercentInstance().format(consumer.getPercentComplete());
        default -> null;
      };
    }

    private String formatSize(long size) {
      return NumberFormat.getIntegerInstance().format(size / 1024) + "k";
    }

    @Override
    public String getColumnName(int column) {
      return switch (column) {
        case 0 -> I18N.getText("EditTokenDialog.msg.speech.colID");
        case 1 -> I18N.getText("token.popup.menu.size");
        case 2 -> I18N.getText("Label.progress");
        default -> "";
      };
    }
  }

  ////
  // CONSUMER LISTENER
  public void assetComplete(MD5Key id, String name, File data) {
    updateTransferTable();
  }

  public void assetUpdated(MD5Key id) {
    getTransferTable().repaint();
  }

  public void assetAdded(MD5Key id) {
    updateTransferTable();
  }
}
