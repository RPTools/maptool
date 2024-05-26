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
import net.rptools.clientserver.simple.DisconnectHandler;
import net.rptools.clientserver.simple.connection.Connection;
import net.rptools.maptool.client.ui.ActivityMonitorPanel;
import net.rptools.maptool.model.player.LocalPlayer;
import net.rptools.maptool.server.Handshake;
import net.rptools.maptool.server.proto.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author trevor
 */
public class MapToolConnection {
  public interface HandshakeCompletionObserver {
    void onComplete(boolean success);
  }

  /** Instance used for log messages. */
  private static final Logger log = LogManager.getLogger(MapToolConnection.class);

  private final LocalPlayer player;
  private final Connection connection;
  private final Handshake<Void> handshake;
  private final List<HandshakeCompletionObserver> onCompleted;

  public MapToolConnection(Connection connection, LocalPlayer player, Handshake<Void> handshake) {
    this.connection = connection;
    this.player = player;
    this.handshake = handshake;
    onCompleted = new ArrayList<>();
  }

  public void onCompleted(HandshakeCompletionObserver onCompleted) {
    this.onCompleted.add(onCompleted);
  }

  public void start() throws IOException {
    if (handshake == null) {
      // No handshake required. Transition immediately to connected.
      connection.open();
      for (final var callback : onCompleted) {
        callback.onComplete(true);
      }
    } else {
      handshake.whenComplete(
          (result, exception) -> {
            if (exception != null) {
              // For client side only show the error message as its more likely to make sense
              // for players, the exception is logged just in case more info is required
              log.warn(exception);
              MapTool.showError(exception.getMessage());
              connection.close();
              for (final var callback : onCompleted) {
                callback.onComplete(false);
              }
            } else {
              for (final var callback : onCompleted) {
                callback.onComplete(true);
              }
            }
          });

      // this triggers the handshake from the server side
      connection.open();
      handshake.startHandshake();
    }
  }

  public void addMessageHandler(ClientMessageHandler handler) {
    connection.addMessageHandler(handler);
  }

  public void addActivityListener(ActivityMonitorPanel activityMonitor) {
    connection.addActivityListener(activityMonitor);
  }

  public void addDisconnectHandler(DisconnectHandler serverDisconnectHandler) {
    connection.addDisconnectHandler(serverDisconnectHandler);
  }

  public boolean isAlive() {
    return connection.isAlive();
  }

  public void close() {
    connection.close();
  }

  public void sendMessage(Message msg) {
    log.debug("{} sent {}", player.getName(), msg.getMessageTypeCase());
    connection.sendMessage(msg.toByteArray());
  }
}
