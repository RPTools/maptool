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

import java.awt.GridLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.rptools.maptool.client.swing.StatusPanel;
import net.rptools.maptool.client.ui.theme.IconMap;
import net.rptools.maptool.client.ui.theme.Icons;
import net.rptools.maptool.language.I18N;

public class ConnectionStatusPanel extends JPanel {
  public enum Status {
    connected,
    disconnected,
    server
  }

  public static Icon disconnectedIcon;
  public static Icon connectedIcon;
  public static Icon serverIcon;

  private final JLabel iconLabel = new JLabel();

  static {
    disconnectedIcon = IconMap.getIcon(Icons.STATUSBAR_SERVER_DISCONNECTED, StatusPanel.ICON_W_H);
    connectedIcon = IconMap.getIcon(Icons.STATUSBAR_SERVER_CONNECTED, StatusPanel.ICON_W_H);
    serverIcon = IconMap.getIcon(Icons.STATUSBAR_SERVER_RUNNING, StatusPanel.ICON_W_H);
  }

  public ConnectionStatusPanel() {
    setLayout(new GridLayout(1, 1));
    setStatus(Status.disconnected);
    add(iconLabel);
  }

  public void setStatus(Status status) {
    Icon icon = null;
    String tip = null;
    switch (status) {
      case connected:
        icon = connectedIcon;
        tip = "ConnectionStatusPanel.serverConnected"; // $NON-NLS-1$
        break;
      case server:
        icon = serverIcon;
        tip = "ConnectionStatusPanel.runningServer"; // $NON-NLS-1$
        break;
      default:
        icon = disconnectedIcon;
        tip = "ConnectionStatusPanel.notConnected"; // $NON-NLS-1$
    }
    iconLabel.setIcon(icon);
    setToolTipText(I18N.getString(tip));
  }
}
