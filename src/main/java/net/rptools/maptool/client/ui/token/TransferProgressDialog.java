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
package net.rptools.maptool.client.ui.token;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.swing.AbeillePanel;
import net.rptools.maptool.client.swing.GenericDialog;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.transfer.AssetConsumer;
import net.rptools.maptool.transfer.ConsumerListener;

/** This dialog is used to display all of the assets being transferred */
public class TransferProgressDialog extends AbeillePanel<Token> implements ConsumerListener {

  private GenericDialog dialog;

  public TransferProgressDialog() {
    super("net/rptools/maptool/client/ui/forms/transferProgressDialog.xml");

    panelInit();
  }

  public void showDialog() {
    dialog =
        new GenericDialog("Assets in Transit", MapTool.getFrame(), this, false) {
          @Override
          public void showDialog() {
            MapTool.getAssetTransferManager().addConsumerListener(TransferProgressDialog.this);
            super.showDialog();
          }

          @Override
          public void closeDialog() {
            MapTool.getAssetTransferManager().removeConsumerListener(TransferProgressDialog.this);
            super.closeDialog();
          }
        };

    getRootPane().setDefaultButton(getCloseButton());
    dialog.showDialog();
  }

  public JButton getCloseButton() {
    return (JButton) getComponent("closeButton");
  }

  public JTable getTransferTable() {
    return (JTable) getComponent("transferTable");
  }

  public void initCloseButton() {
    getCloseButton()
        .addActionListener(
            new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                dialog.closeDialog();
              }
            });
  }

  private void updateTransferTable() {

    final TransferTableModel model = new TransferTableModel();
    EventQueue.invokeLater(
        new Runnable() {
          public void run() {
            getTransferTable().setModel(model);

            TableColumnModel colModel = getTransferTable().getColumnModel();
            colModel.getColumn(1).setMaxWidth(100);
            colModel.getColumn(2).setMaxWidth(75);
          }
        });
  }

  public void initTransferTable() {
    getTransferTable().setBackground(Color.white);
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

      if (consumerList.size() == 0) {
        return columnIndex == 0 ? "None" : "";
      }

      AssetConsumer consumer = consumerList.get(rowIndex);

      switch (columnIndex) {
        case 0:
          return consumer.getId();
        case 1:
          return formatSize(consumer.getSize());
        case 2:
          return NumberFormat.getPercentInstance().format(consumer.getPercentComplete());
      }

      return null;
    }

    private String formatSize(long size) {

      return NumberFormat.getIntegerInstance().format(size / 1024) + "k";
    }

    @Override
    public String getColumnName(int column) {
      switch (column) {
        case 0:
          return "ID";
        case 1:
          return "Size";
        case 2:
          return "Progress";
      }
      return "";
    }
  }

  ////
  // CONSUMER LISTENER
  public void assetComplete(Serializable id, String name, File data) {
    updateTransferTable();
  }

  public void assetUpdated(Serializable id) {
    getTransferTable().repaint();
  }

  public void assetAdded(Serializable id) {
    updateTransferTable();
  }
}
