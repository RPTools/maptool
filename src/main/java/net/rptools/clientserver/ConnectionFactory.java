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
package net.rptools.clientserver;

import java.awt.EventQueue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.rptools.clientserver.simple.connection.Connection;
import net.rptools.clientserver.simple.connection.SocketConnection;
import net.rptools.clientserver.simple.connection.WebRTCConnection;
import net.rptools.clientserver.simple.server.NilServer;
import net.rptools.clientserver.simple.server.Server;
import net.rptools.clientserver.simple.server.SocketServer;
import net.rptools.clientserver.simple.server.WebRTCServer;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.RemoteServerConfig;
import net.rptools.maptool.server.ServerConfig;

public class ConnectionFactory {
  private static ConnectionFactory instance = new ConnectionFactory();

  public static ConnectionFactory getInstance() {
    return instance;
  }

  @Nonnull
  public Connection createConnection(@Nonnull String id, @Nonnull RemoteServerConfig config) {
    return switch (config) {
      case RemoteServerConfig.Socket(String hostName, int port) ->
          new SocketConnection(id, hostName, port);
      case RemoteServerConfig.WebRTC(String serverName) ->
          new WebRTCConnection(
              id,
              serverName,
              new WebRTCConnection.Listener() {
                @Override
                public void onLoginError() {
                  MapTool.showError("Handshake.msg.playerAlreadyConnected");
                }
              });
    };
  }

  @Nonnull
  public Server createServer(@Nullable ServerConfig config) {
    if (config == null) {
      return new NilServer();
    }

    if (!config.getUseWebRTC()) {
      return new SocketServer(config.getPort());
    }

    return new WebRTCServer(
        config.getServerName(),
        new WebRTCServer.Listener() {
          @Override
          public void onLoginError() {
            EventQueue.invokeLater(
                () -> {
                  MapTool.showError("ServerDialog.error.serverAlreadyExists");
                });
          }

          @Override
          public void onUnexpectedClose() {
            MapTool.stopServer();
          }
        });
  }
}
