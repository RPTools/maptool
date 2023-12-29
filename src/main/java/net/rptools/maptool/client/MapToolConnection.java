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
import java.util.ArrayList;
import java.util.List;
import net.rptools.clientserver.ConnectionFactory;
import net.rptools.clientserver.simple.DisconnectHandler;
import net.rptools.clientserver.simple.connection.Connection;
import net.rptools.maptool.client.ui.ActivityMonitorPanel;
import net.rptools.maptool.model.player.LocalPlayer;
import net.rptools.maptool.server.ClientHandshake;
import net.rptools.maptool.server.ServerConfig;
import net.rptools.maptool.server.proto.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author trevor
 */
public class MapToolConnection implements IMapToolConnection {

  /** Instance used for log messages. */
  private static final Logger log = LogManager.getLogger(MapToolConnection.class);

  private final LocalPlayer player;
  private Connection connection;
  private ClientHandshake handshake;
  private List<Runnable> onCompleted;

  public MapToolConnection(ServerConfig config, LocalPlayer player) {

    this.connection = ConnectionFactory.getInstance().createConnection(player.getName(), config);
    this.player = player;
    this.handshake = new ClientHandshake(connection, player);
    onCompleted = new ArrayList<>();
  }

  @Override
  public void onCompleted(Runnable onCompleted) {
    this.onCompleted.add(onCompleted);
  }

  @Override
  public void start() throws IOException {
    connection.addMessageHandler(handshake);
    handshake.addObserver(
        (ignore) -> {
          connection.removeMessageHandler(handshake);
          if (handshake.isSuccessful()) {
            for (final var callback : onCompleted) {
              callback.run();
            }
          } else {
            // For client side only show the error message as its more likely to make sense
            // for players, the exception is logged just in case more info is required
            var exception = handshake.getException();
            if (exception != null) {
              log.warn(exception);
            }
            MapTool.showError(handshake.getErrorMessage());
            connection.close();
            for (final var callback : onCompleted) {
              callback.run();
            }
            AppActions.disconnectFromServer();
          }
        });
    // this triggers the handshake from the server side
    connection.open();
    handshake.startHandshake();
  }

  @Override
  public void addMessageHandler(ClientMessageHandler handler) {
    connection.addMessageHandler(handler);
  }

  @Override
  public void addActivityListener(ActivityMonitorPanel activityMonitor) {
    connection.addActivityListener(activityMonitor);
  }

  @Override
  public void addDisconnectHandler(DisconnectHandler serverDisconnectHandler) {
    connection.addDisconnectHandler(serverDisconnectHandler);
  }

  @Override
  public boolean isAlive() {
    return connection.isAlive();
  }

  @Override
  public void close() throws IOException {
    connection.close();
  }

  @Override
  public void sendMessage(Message msg) {
    log.debug(player.getName() + " sent " + msg.getMessageTypeCase());
    connection.sendMessage(msg.toByteArray());
  }
}
