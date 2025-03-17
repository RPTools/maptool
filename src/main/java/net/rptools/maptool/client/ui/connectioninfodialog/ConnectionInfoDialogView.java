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

import javax.swing.*;

public class ConnectionInfoDialogView {
  private JPanel mainPanel;
  private JLabel nameLabel;
  private JLabel LANIDLabel;
  private JLabel localIpv4AddressLabel;
  private JLabel localIpv6AddressLabel;
  private JLabel externalAddressLabel;
  private JLabel portLabel;
  private JTextField nameTextField;
  private JTextField serviceIdentifierTextField;
  private JTextField localIpv4AddressTextField;
  private JTextField localIpv6AddressTextField;
  private JTextField externalAddressTextField;
  private JTextField portTextField;
  private JButton registryHttpUrlCopyButton;
  private JButton registryUriCopyButton;
  private JButton lanUriCopyButton;
  private JButton lanHttpUrlCopyButton;
  private JButton localIpv4UriCopyButton;
  private JButton localIpv4HttpUrlCopyButton;
  private JButton localIpv6UriCopyButton;
  private JButton localIpv6HttpUrlCopyButton;
  private JButton externalUriCopyButton;
  private JButton externalHttpUrlCopyButton;
  private JButton okButton;

  public JComponent getRootComponent() {
    return mainPanel;
  }
}
