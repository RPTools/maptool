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
package net.rptools.maptool.client.ui.connecttoserverdialog;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import net.rptools.maptool.client.AppConstants;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolRegistry;
import net.rptools.maptool.client.MapToolRegistry.SeverConnectionDetails;
import net.rptools.maptool.client.swing.AbeillePanel;
import net.rptools.maptool.client.swing.GenericDialog;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.language.I18N;
import net.tsc.servicediscovery.AnnouncementListener;
import net.tsc.servicediscovery.ServiceFinder;
import yasb.Binder;

/**
 * @author trevor
 */
public class ConnectToServerDialog extends AbeillePanel<ConnectToServerDialogPreferences>
    implements AnnouncementListener {
  private static ServiceFinder finder;

  static {
    finder = new ServiceFinder(AppConstants.SERVICE_GROUP);
  }

  private boolean accepted;
  private GenericDialog dialog;
  private SeverConnectionDetails connectionDetails = new SeverConnectionDetails();

  /** This is the default constructor */
  public ConnectToServerDialog() {
    super(new ConnectToServerDialogView().getRootComponent());
    setPreferredSize(new Dimension(600, 500));
    panelInit();
  }

  @Override
  protected void preModelBind() {
    Binder.setFormat(getPortTextField(), new DecimalFormat("####"));
  }

  public int getPort() {
    return connectionDetails.port;
  }

  public String getServer() {
    return connectionDetails.address;
  }

  public boolean getUseWebRTC() {
    return connectionDetails.webrtc;
  }

  public void showDialog() {
    dialog =
        new GenericDialog(
            I18N.getText("ConnectToServerDialog.msg.title"), MapTool.getFrame(), this);
    bind(new ConnectToServerDialogPreferences());
    getRootPane().setDefaultButton(getOKButton());
    getUsePublicKeyCheckBox()
        .addItemListener(
            l -> {
              boolean usePublicKey = getUsePublicKeyCheckBox().isSelected();
              getPasswordTextField().setEnabled(!usePublicKey);
            });

    boolean usePublicKey = getUsePublicKeyCheckBox().isSelected();
    getPasswordTextField().setEnabled(!usePublicKey);

    dialog.showDialog();
  }

  public JButton getOKButton() {
    return (JButton) getComponent("okButton");
  }

  @Override
  public void bind(ConnectToServerDialogPreferences model) {
    finder.addAnnouncementListener(this);

    updateLocalServerList();
    updateRemoteServerList();

    super.bind(model);
  }

  @Override
  public void unbind() {
    // Shutting down
    finder.removeAnnouncementListener(this);
    finder.dispose();

    super.unbind();
  }

  public JButton getCancelButton() {
    return (JButton) getComponent("cancelButton");
  }

  public void initCancelButton() {
    getCancelButton()
        .addActionListener(
            e -> {
              accepted = false;
              dialog.closeDialog();
            });
  }

  public void initOKButton() {
    getOKButton().addActionListener(e -> handleOK());
  }

  public boolean accepted() {
    return accepted;
  }

  public void initLocalServerList() {
    getLocalServerList().setModel(new DefaultListModel());
    getLocalServerList().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    getLocalServerList()
        .addMouseListener(
            new MouseAdapter() {
              @Override
              public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                  handleOK();
                }
              }
            });
  }

  public JList getLocalServerList() {
    return (JList) getComponent("localServerList");
  }

  private void updateLocalServerList() {
    finder.find();
  }

  private void updateRemoteServerList() {
    new SwingWorker<Object, Object>() {
      RemoteServerTableModel model = null;

      @Override
      protected Object doInBackground() {
        model = new RemoteServerTableModel(MapToolRegistry.getInstance().findAllInstances());
        return null;
      }

      @Override
      protected void done() {
        if (model != null) {
          getRemoteServerTable().setModel(model);
        }
        TableColumn column = getRemoteServerTable().getColumnModel().getColumn(1);
        column.setPreferredWidth(70);
        column.setMaxWidth(70);
        column.setCellRenderer(
            new DefaultTableCellRenderer() {
              {
                setHorizontalAlignment(RIGHT);
              }
            });
      }
    }.execute();
  }

  public void initRemoteServerTable() {
    getRemoteServerTable().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    getRemoteServerTable().setModel(new RemoteServerTableModel(new ArrayList<String>()));
    getRemoteServerTable()
        .addMouseListener(
            new MouseAdapter() {
              @Override
              public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                  JTable rem = getRemoteServerTable();
                  getServerNameTextField()
                      .setText(rem.getModel().getValueAt(rem.getSelectedRow(), 0).toString());
                  if (e.getClickCount() == 2) handleOK();
                }
              }
            });
  }

  public JTable getRemoteServerTable() {
    return (JTable) getComponent("aliasTable");
  }

  public JButton getRescanButton() {
    return (JButton) getComponent("rescanButton");
  }

  public JButton getRefreshButton() {
    return (JButton) getComponent("refreshButton");
  }

  public void initRescanButton() {
    getRescanButton()
        .addActionListener(
            e -> {
              ((DefaultListModel) getLocalServerList().getModel()).clear();
              finder.find();
            });
  }

  public void initRefreshButton() {
    getRefreshButton().addActionListener(e -> updateRemoteServerList());
  }

  public JTextField getUsernameTextField() {
    return (JTextField) getComponent("@username");
  }

  public JTextField getPasswordTextField() {
    return (JTextField) getComponent("@password");
  }

  public JTextField getPortTextField() {
    return (JTextField) getComponent("@port");
  }

  public JTextField getHostTextField() {
    return (JTextField) getComponent("@host");
  }

  public JTextField getServerNameTextField() {
    return (JTextField) getComponent("@serverName");
  }

  public JTabbedPane getTabPane() {
    return (JTabbedPane) getComponent("tabPane");
  }

  public JCheckBox getUsePublicKeyCheckBox() {
    return (JCheckBox) getComponent("@usePublicKey");
  }

  private void handleOK() {
    String username = getUsernameTextField().getText().trim();
    if (username.length() == 0) {
      MapTool.showError("ServerDialog.error.username"); // $NON-NLS-1$
      return;
    }
    if (!getUsePublicKeyCheckBox().isSelected() && getPasswordTextField().getText().length() == 0) {
      MapTool.showError("ServerDialog.error.noConnectPassword"); // $NON-NLS-1$
      return;
    }
    getUsernameTextField().setText(username);

    String externalAddress = "Unknown";
    try {
      externalAddress = MapToolRegistry.getInstance().getAddress();
      if (externalAddress == null || externalAddress.length() == 0) {
        externalAddress = "Unknown";
      }
    } catch (Exception e) {
      // Oh well, might not be connected
    }
    // System.out.println("External address is: " + externalAddress);

    JComponent selectedPanel = (JComponent) getTabPane().getSelectedComponent();
    if (SwingUtil.hasComponent(selectedPanel, "lanPanel")) {
      if (getLocalServerList().getSelectedIndex() < 0) {
        MapTool.showError("ServerDialog.error.server"); // $NON-NLS-1$
        return;
      }
      // OK
      ServerInfo info = (ServerInfo) getLocalServerList().getSelectedValue();
      connectionDetails.port = info.port;
      connectionDetails.address = info.address.getHostAddress();
    }
    if (SwingUtil.hasComponent(selectedPanel, "directPanel")) {
      // TODO: put these into a validation method
      if (getPortTextField().getText().length() == 0) {
        MapTool.showError("ServerDialog.error.port");
        return;
      }
      int portTemp = -1;
      try {
        portTemp = Integer.parseInt(getPortTextField().getText());
      } catch (NumberFormatException nfe) {
        MapTool.showError("ServerDialog.error.port");
        return;
      }

      String host = getHostTextField().getText().trim();
      if (host.length() == 0) {
        MapTool.showError("ServerDialog.error.server");
        return;
      }
      getHostTextField().setText(host);

      // OK
      connectionDetails.port = portTemp;
      connectionDetails.address = host;
    }
    if (SwingUtil.hasComponent(selectedPanel, "rptoolsPanel")) {
      String serverName = getServerNameTextField().getText().trim();
      if (serverName.length() == 0) {
        MapTool.showError("ServerDialog.error.server");
        return;
      }
      getServerNameTextField().setText(serverName);

      // Do the lookup
      SeverConnectionDetails serverInfo = MapToolRegistry.getInstance().findInstance(serverName);
      if (serverInfo == null || serverInfo.address == null || serverInfo.address.length() == 0) {
        MapTool.showError(I18N.getText("ServerDialog.error.serverNotFound", serverName));
        return;
      }
      connectionDetails = serverInfo;
    }
    try {
      InetAddress server = InetAddress.getByName(connectionDetails.address);
      InetAddress extAddress = InetAddress.getByName(externalAddress);
      if (extAddress != null && extAddress.equals(server) && !connectionDetails.webrtc) {
        boolean yes =
            MapTool.confirm(
                "ConnectToServerDialog.warning.doNotUseExternalAddress",
                I18N.getString("menu.file"),
                I18N.getString("action.showConnectionInfo"));
        if (!yes) return;
      }
    } catch (UnknownHostException e) {
      // If an exception occurs, don't bother doing the comparison. But otherwise it's not an error.
    }
    if (commit()) {
      accepted = true;
      dialog.closeDialog();
    }
  }

  @Override
  public boolean commit() {
    ConnectToServerDialogPreferences prefs = new ConnectToServerDialogPreferences();

    // Not bindable .. yet
    prefs.setTab(getTabPane().getSelectedIndex());
    return super.commit();
  }

  private static class RemoteServerTableModel extends AbstractTableModel {
    private final List<String[]> data;

    public RemoteServerTableModel(List<String> encodedData) {
      // Simple but sufficient
      encodedData.sort(String.CASE_INSENSITIVE_ORDER);

      data = new ArrayList<String[]>(encodedData.size());
      for (String line : encodedData) {
        String[] row = line.split(":");
        if (row.length == 1) {
          row = new String[] {row[0], "Unknown"};
        }
        data.add(row);
      }
    }

    @Override
    public String getColumnName(int column) {
      switch (column) {
        case 0:
          return I18N.getText("ConnectToServerDialog.msg.headingServer");
        case 1:
          return I18N.getText("ConnectToServerDialog.msg.headingVersion");
      }
      return "";
    }

    public int getColumnCount() {
      return 2;
    }

    public int getRowCount() {
      return data.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
      String[] row = data.get(rowIndex);
      return row[columnIndex];
    }
  }

  // ANNOUNCEMENT LISTENER
  public void serviceAnnouncement(String type, InetAddress address, int port, byte[] data) {
    ((DefaultListModel) getLocalServerList().getModel())
        .addElement(new ServerInfo(new String(data), address, port));
  }

  private static class ServerInfo {
    String id;
    InetAddress address;
    int port;

    public ServerInfo(String id, InetAddress address, int port) {
      this.id = id.trim();
      this.address = address;
      this.port = port;
    }

    @Override
    public String toString() {
      return id;
    }
  }
}
