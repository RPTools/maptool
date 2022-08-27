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
package net.rptools.maptool.client.ui;

import com.jeta.forms.components.panel.FormPanel;
import java.awt.GridLayout;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import net.rptools.lib.swing.SwingUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolRegistry;
import net.rptools.maptool.client.swing.FormPanelI18N;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.server.MapToolServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConnectionInfoDialog extends JDialog {
  private static String externalAddress =
      "Unknown"; // Used to be "Discovering ..." -- note that this is a UX change
  private static JTextField externalAddressLabel;

  private static final Logger log = LogManager.getLogger(ConnectionInfoDialog.class);

  /**
   * This is the default constructor
   *
   * @param server the server instance for the connection dialog
   */
  public ConnectionInfoDialog(MapToolServer server) {
    super(MapTool.getFrame(), I18N.getText("ConnectionInfoDialog.title"), true);
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    setSize(275, 275);

    FormPanel panel =
        new FormPanelI18N("net/rptools/maptool/client/ui/forms/connectionInfoDialog.xml");

    JTextField nameLabel = panel.getTextField("name");
    JTextField localAddressLabel = panel.getTextField("localAddress");
    JTextField portLabel = panel.getTextField("port");
    externalAddressLabel = panel.getTextField("externalAddress");

    String name = server.getConfig().getServerName();
    if (name == null) {
      name = "---";
    }
    String localAddress = "Unknown";
    try {
      InetAddress rptools = InetAddress.getByName("www.rptools.net");
      try {
        InetAddress localAddy = InetAddress.getLocalHost();
        localAddress = localAddy.getHostAddress();
      } catch (IOException e) { // Socket|UnknownHost
        log.warn("Can't resolve 'www.rptools.net' or our own IP address!?", e);
      }
    } catch (UnknownHostException e) {
      log.warn("Can't resolve 'www.rptools.net' or our own IP address!?", e);
    }
    String port =
        MapTool.isPersonalServer() ? "---" : Integer.toString(server.getConfig().getPort());

    nameLabel.setText(name);
    localAddressLabel.setText(localAddress);
    externalAddressLabel.setText(I18N.getText("ConnectionInfoDialog.discovering"));
    portLabel.setText(port);

    JButton okButton = (JButton) panel.getButton("okButton");
    bindOKButtonActions(okButton);

    setLayout(new GridLayout());
    ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    add(panel);

    (new Thread(new ExternalAddressFinder(externalAddressLabel))).start();
  }

  @Override
  public void setVisible(boolean b) {
    if (b) {
      SwingUtil.centerOver(this, MapTool.getFrame());
    }
    super.setVisible(b);
  }

  private static FutureTask<String> getExternalAddressFinderResult() {
    ExternalAddressFinder finder = new ExternalAddressFinder(externalAddressLabel);
    FutureTask<String> future = new FutureTask<>(finder);
    Executor executor = Executors.newSingleThreadExecutor();
    executor.execute(future);
    return future;
  }

  public static String getExternalAddress() {
    if (externalAddress.equals("Unknown")) {
      FutureTask<String> future = getExternalAddressFinderResult();
      try {
        externalAddress = future.get();
      } catch (Exception e) {
        // if there's an exception, we just keep the string 'Unknown'
      }
    }
    return externalAddress;
  }

  /**
   * This method initializes okButton
   *
   * @return javax.swing.JButton
   */
  private void bindOKButtonActions(JButton okButton) {
    okButton.addActionListener(e -> setVisible(false));
  }

  private static class ExternalAddressFinder implements Callable<String>, Runnable {
    private final JTextField myLabel;

    public ExternalAddressFinder(JTextField label) {
      myLabel = label;
    }

    @Override
    public String call() {
      String address = MapToolRegistry.getInstance().getAddress();
      if (address == null || address.length() == 0) {
        address = "Unknown";
      }
      return address;
    }

    @Override
    public void run() {
      String result = call();
      SwingUtilities.invokeLater(() -> myLabel.setText(result));
    }
  }
}
