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

import static net.rptools.maptool.server.proto.Message.MessageTypeCase.HEARTBEAT_MSG;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import net.rptools.clientserver.ConnectionFactory;
import net.rptools.clientserver.simple.client.ClientConnection;
import net.rptools.maptool.client.ui.ActivityMonitorPanel;
import net.rptools.maptool.model.player.LocalPlayer;
import net.rptools.maptool.server.ClientHandshake;
import net.rptools.maptool.server.Handshake;
import net.rptools.maptool.server.ServerConfig;
import net.rptools.maptool.server.proto.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** @author trevor */
public class MapToolConnection {

  /** Instance used for log messages. */
  private static final Logger log = LogManager.getLogger(MapToolConnection.class);

  private final LocalPlayer player;
  private ClientConnection connection;
  private Handshake handshake;
  private Runnable onCompleted;

  public MapToolConnection(ServerConfig config, LocalPlayer player) throws IOException {

    this.connection =
        ConnectionFactory.getInstance().createClientConnection(player.getName(), config);
    this.player = player;
    this.handshake = new ClientHandshake(connection, player);
    onCompleted = () -> {};
  }

  public void setOnCompleted(Runnable onCompleted) {
    if (onCompleted == null) this.onCompleted = () -> {};
    else this.onCompleted = onCompleted;
  }

  public void start() throws IOException, ExecutionException, InterruptedException {
    connection.addMessageHandler(handshake);
    handshake.addObserver(
        (ignore) -> {
          connection.removeMessageHandler(handshake);
          if (handshake.isSuccessful()) {
            onCompleted.run();
          } else {
            // For client side only show the error message as its more likely to make sense
            // for players, the exception is logged just in case more info is required
            var exception = handshake.getException();
            if (exception != null) {
              log.warn(exception);
            }
            MapTool.showError(handshake.getErrorMessage());
            connection.close();
            onCompleted.run();
            AppActions.disconnectFromServer();
          }
        });
    // this triggers the handshake from the server side
    connection.open();
    handshake.startHandshake();
  }

  public void addMessageHandler(ClientMessageHandler handler) {
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

  public void sendMessage(Message msg) {
    var msgType = msg.getMessageTypeCase();
    var logText = player.getName() + " sent " + msg.getMessageTypeCase();
    if (msgType == HEARTBEAT_MSG) {
      log.debug(logText);
    } else {
      log.info(logText);
    }
    connection.sendMessage(msg.toByteArray());
  }
}
