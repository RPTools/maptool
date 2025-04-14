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
package net.rptools.maptool.client.ui.connectioninfodialog;

import static java.util.concurrent.CompletableFuture.completedFuture;

import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.net.URI;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ServerAddress;
import net.rptools.maptool.client.swing.AbeillePanel;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.server.MapToolServer;
import net.rptools.maptool.util.NetUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConnectionInfoDialog extends JDialog {
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

    AbeillePanel panel = new AbeillePanel(new ConnectionInfoDialogView().getRootComponent());

    JTextField nameLabel = panel.getTextField("name");
    JTextField serviceIdentifierLabel = panel.getTextField("serviceIdentifier");
    JTextField localv4AddressLabel = panel.getTextField("localv4Address");
    JTextField localv6AddressLabel = panel.getTextField("localv6Address");
    JTextField portLabel = panel.getTextField("port");
    externalAddressLabel = panel.getTextField("externalAddress");

    String name = server.getName();
    if (name == null || name.isEmpty()) {
      name = "---";
    }
    String serviceIdentifier = Objects.toString(server.getServiceIdentifier(), "---");

    int port = server.getPort();
    String portString = port < 0 ? "---" : Integer.toString(port);

    nameLabel.setText(name);
    serviceIdentifierLabel.setText(serviceIdentifier);
    localv4AddressLabel.setText("Unknown");
    localv6AddressLabel.setText("Unknown");
    externalAddressLabel.setText(I18N.getText("ConnectionInfoDialog.discovering"));
    portLabel.setText(portString);

    NetUtil.getInstance()
        .getLocalAddresses()
        .thenAccept(
            localAddresses -> {
              if (!localAddresses.ipv4().isEmpty()) {
                localv4AddressLabel.setText(NetUtil.formatAddress(localAddresses.ipv4().get(0)));
              }
              if (!localAddresses.ipv6().isEmpty()) {
                localv6AddressLabel.setText(NetUtil.formatAddress(localAddresses.ipv6().get(0)));
              }
            });

    Supplier<CompletableFuture<ServerAddress.Registry>> getServerName =
        () -> completedFuture(new ServerAddress.Registry(server.getName()));
    Supplier<CompletableFuture<ServerAddress.Lan>> getServiceIdentifier =
        () -> completedFuture(new ServerAddress.Lan(server.getServiceIdentifier()));
    Supplier<CompletableFuture<ServerAddress.Tcp>> getLocalV4 =
        () ->
            NetUtil.getInstance()
                .getLocalAddresses()
                .thenApply(
                    localAddresses -> {
                      if (localAddresses.ipv4().isEmpty()) {
                        return null;
                      }
                      return new ServerAddress.Tcp(
                          NetUtil.formatAddress(localAddresses.ipv4().get(0)), server.getPort());
                    });
    Supplier<CompletableFuture<ServerAddress.Tcp>> getLocalV6 =
        () ->
            NetUtil.getInstance()
                .getLocalAddresses()
                .thenApply(
                    localAddresses -> {
                      if (localAddresses.ipv6().isEmpty()) {
                        return null;
                      }
                      return new ServerAddress.Tcp(
                          NetUtil.formatAddress(localAddresses.ipv6().get(0)), server.getPort());
                    });
    Supplier<CompletableFuture<ServerAddress.Tcp>> getExternal =
        () ->
            NetUtil.getInstance()
                .getExternalAddress()
                .thenApply(
                    address -> {
                      if (address == null) {
                        return null;
                      }
                      return new ServerAddress.Tcp(
                          NetUtil.formatAddress(address), server.getPort());
                    });
    registerCopyButton(panel, "registryUriCopyButton", getServerName, ServerAddress::toUri);
    registerCopyButton(panel, "registryHttpUrlCopyButton", getServerName, ServerAddress::toHttpUrl);
    registerCopyButton(panel, "lanUriCopyButton", getServiceIdentifier, ServerAddress::toUri);
    registerCopyButton(
        panel, "lanHttpUrlCopyButton", getServiceIdentifier, ServerAddress::toHttpUrl);
    registerCopyButton(panel, "localIpv4UriCopyButton", getLocalV4, ServerAddress::toUri);
    registerCopyButton(panel, "localIpv4HttpUrlCopyButton", getLocalV4, ServerAddress::toHttpUrl);
    registerCopyButton(panel, "localIpv6UriCopyButton", getLocalV6, ServerAddress::toUri);
    registerCopyButton(panel, "localIpv6HttpUrlCopyButton", getLocalV6, ServerAddress::toHttpUrl);
    registerCopyButton(panel, "externalUriCopyButton", getExternal, ServerAddress::toUri);
    registerCopyButton(panel, "externalHttpUrlCopyButton", getExternal, ServerAddress::toHttpUrl);
    if (panel.getButton("okButton") instanceof JButton okButton) {
      okButton.addActionListener(e -> setVisible(false));
    }

    setLayout(new GridLayout());
    ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    add(panel);

    NetUtil.getInstance()
        .getExternalAddress()
        .thenAccept(
            address -> {
              if (address != null) {
                SwingUtilities.invokeLater(
                    () -> externalAddressLabel.setText(NetUtil.formatAddress(address)));
              }
            });
  }

  private <T extends ServerAddress> void registerCopyButton(
      @Nonnull AbeillePanel panel,
      @Nonnull String buttonId,
      @Nonnull Supplier<CompletableFuture<T>> connectionSupplier,
      @Nonnull Function<T, URI> specToUri) {
    if (!(panel.getButton(buttonId) instanceof JButton button)) {
      return;
    }
    button.addActionListener(
        e -> {
          var future = connectionSupplier.get();
          future.thenAccept(
              connectionSpec -> {
                var url = specToUri.apply(connectionSpec).toString();
                Toolkit.getDefaultToolkit()
                    .getSystemClipboard()
                    .setContents(new StringSelection(url), null);
              });
        });
  }

  @Override
  public void setVisible(boolean b) {
    if (b) {
      SwingUtil.centerOver(this, MapTool.getFrame());
    }
    super.setVisible(b);
  }
}
