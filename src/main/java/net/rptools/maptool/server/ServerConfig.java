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
package net.rptools.maptool.server;

public class ServerConfig {
  public static final int DEFAULT_PORT = 51234;

  private final int port;
  private final String hostPlayerId;
  private final String gmPassword;
  private final String playerPassword;
  private final String serverName;
  private final String hostName;
  private final boolean useEasyConnect;
  private final boolean useWebRTC;

  public ServerConfig(
      String hostPlayerId,
      String gmPassword,
      String playerPassword,
      int port,
      String serverName,
      String hostName,
      boolean useEasyConnect,
      boolean useWebRTC) {
    this.hostPlayerId = hostPlayerId;
    this.gmPassword = gmPassword;
    this.playerPassword = playerPassword;
    this.port = port;
    this.serverName = serverName;
    this.hostName = hostName;
    this.useEasyConnect = useEasyConnect;
    this.useWebRTC = useWebRTC;
  }

  public String getHostPlayerId() {
    return hostPlayerId;
  }

  public boolean isServerRegistered() {
    return serverName != null && !serverName.isEmpty();
  }

  public String getServerName() {
    return serverName;
  }

  public int getPort() {
    return port;
  }

  public String getGmPassword() {
    return gmPassword;
  }

  public String getPlayerPassword() {
    return playerPassword;
  }

  public String getHostName() {
    return hostName;
  }

  public boolean getUseEasyConnect() {
    return useEasyConnect;
  }

  public boolean getUseWebRTC() {
    return useWebRTC;
  }
}
