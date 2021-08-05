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
package net.rptools.maptool.client;

import java.io.IOException;
import net.rptools.clientserver.ConnectionFactory;
import net.rptools.clientserver.hessian.client.IMethodClientConnection;
import net.rptools.maptool.client.ui.ActivityMonitorPanel;
import net.rptools.maptool.model.Player;
import net.rptools.maptool.server.Handshake;
import net.rptools.maptool.server.ServerConfig;

/** @author trevor */
public class MapToolConnection {
  private final Player player;
  private IMethodClientConnection connection;
  private Handshake handshake;
  private Runnable onCompleted;

  public MapToolConnection(ServerConfig config, Player player) throws IOException {
    this.connection =
        ConnectionFactory.getInstance().createClientConnection(player.getName(), config);
    this.player = player;
    this.handshake = new Handshake(connection, player);
    onCompleted = () -> {};
  }

  public void setOnCompleted(Runnable onCompleted) {
    if (onCompleted == null) this.onCompleted = () -> {};
    else this.onCompleted = onCompleted;
  }

  public void start() throws IOException {
    connection.addMessageHandler(handshake);
    handshake.addObserver(
        (ignore) -> {
          connection.removeMessageHandler(handshake);
          if (handshake.isSuccessful()) {
            onCompleted.run();
          } else {
            var exception = handshake.getException();
            if (exception != null) MapTool.showError("Handshake.msg.encodeInitFail", exception);
            else MapTool.showError("ERROR: " + handshake.getErrorMessage());
            connection.close();
            onCompleted.run();
          }
        });
    // this triggers the handshake from the server side
    connection.open();
  }

  public void addMessageHandler(ClientMethodHandler handler) {
    connection.addMessageHandler(handler);
  }

  public void addActivityListener(ActivityMonitorPanel activityMonitor) {
    connection.addActivityListener(activityMonitor);
  }

  public void addDisconnectHandler(ServerDisconnectHandler serverDisconnectHandler) {
    connection.addDisconnectHandler(serverDisconnectHandler);
  }

  public boolean isAlive() {
    return connection.isAlive();
  }

  public void close() throws IOException {
    connection.close();
  }

  public void callMethod(String name, Object[] params) {
    connection.callMethod(name, params);
  }
}
