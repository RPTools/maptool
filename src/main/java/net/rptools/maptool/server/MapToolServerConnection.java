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

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.rptools.clientserver.ConnectionFactory;
import net.rptools.clientserver.simple.MessageHandler;
import net.rptools.clientserver.simple.connection.Connection;
import net.rptools.clientserver.simple.server.Router;
import net.rptools.clientserver.simple.server.Server;
import net.rptools.clientserver.simple.server.ServerObserver;
import net.rptools.maptool.model.player.Player;
import net.rptools.maptool.model.player.ServerSidePlayerDatabase;
import net.rptools.maptool.server.proto.Message;
import net.rptools.maptool.server.proto.PlayerConnectedMsg;
import net.rptools.maptool.server.proto.PlayerDisconnectedMsg;
import net.rptools.maptool.server.proto.SetCampaignMsg;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author trevor
 */
public class MapToolServerConnection implements ServerObserver {
  private static final Logger log = LogManager.getLogger(MapToolServerConnection.class);

  private final MessageHandler messageHandler;
  private final Map<String, Player> playerMap = new ConcurrentHashMap<>();
  private final MapToolServer server;
  private final Router router;
  private final Server connection;
  private final ServerSidePlayerDatabase playerDatabase;
  private final boolean useEasyConnect;

  public MapToolServerConnection(
      MapToolServer server, ServerSidePlayerDatabase playerDatabase, ServerMessageHandler handler) {
    this.messageHandler = handler;
    this.connection = ConnectionFactory.getInstance().createServer(server.getConfig());
    this.router = new Router();
    this.server = server;
    this.playerDatabase = playerDatabase;
    this.useEasyConnect = server.getConfig().getUseEasyConnect();
    addObserver(this);
  }

  public Player getPlayer(String id) {
    for (Player player : playerMap.values()) {
      if (player.getName().equalsIgnoreCase(id)) {
        return player;
      }
    }
    return null;
  }

  public String getConnectionId(String playerId) {
    for (Map.Entry<String, Player> entry : playerMap.entrySet()) {
      if (entry.getValue().getName().equalsIgnoreCase(playerId)) {
        return entry.getKey();
      }
    }
    return null;
  }

  ////
  // SERVER OBSERVER

  /** Handle late connections */
  public void connectionAdded(Connection conn) {
    var handshake = new ServerHandshake(server, conn, playerDatabase, useEasyConnect);

    handshake.whenComplete(
        (player, error) -> {
          if (error != null) {
            log.error("Handshake failure", error);
            log.error("Client closing: bad handshake", error);
            conn.close();
          } else {
            log.debug("About to add new client");
            connectionAccepted(conn, player);
          }
        });
    // Make sure the client is allowed
    handshake.startHandshake();
  }

  private void connectionAccepted(Connection conn, Player connPlayer) {
    playerMap.put(conn.getId().toUpperCase(), connPlayer);

    conn.addMessageHandler(messageHandler);
    conn.addDisconnectHandler(this::connectionRemoved);

    // Make sure any stale connections are gone to avoid conflicts, then add the new one.
    for (var reaped : router.reapClients()) {
      try {
        // TODO I'm not sold on connectionRemoved() being the right call here. Surely our disconnect
        //  handler should be covering this? But let's leave it for now.
        connectionRemoved(reaped);
        reaped.close();
      } catch (Exception e) {
        // Don't want to raise an error if notification of removing a dead connection failed
      }
    }
    router.addConnection(conn);

    server.configureClientConnection(conn);

    Player connectedPlayer = playerMap.get(conn.getId().toUpperCase());
    for (Player player : playerMap.values()) {
      var msg = PlayerConnectedMsg.newBuilder().setPlayer(player.toDto());
      sendMessage(conn.getId(), Message.newBuilder().setPlayerConnectedMsg(msg).build());
    }
    var msg =
        PlayerConnectedMsg.newBuilder().setPlayer(connectedPlayer.getTransferablePlayer().toDto());
    broadcastMessage(Message.newBuilder().setPlayerConnectedMsg(msg).build());

    var msg2 = SetCampaignMsg.newBuilder().setCampaign(server.getCampaign().toDto());
    sendMessage(conn.getId(), Message.newBuilder().setSetCampaignMsg(msg2).build());
  }

  public void connectionRemoved(Connection conn) {
    router.removeConnection(conn);

    server.releaseClientConnection(conn.getId());
    var player = playerMap.get(conn.getId().toUpperCase()).getTransferablePlayer();
    var msg = PlayerDisconnectedMsg.newBuilder().setPlayer(player.toDto());
    broadcastMessage(
        new String[] {conn.getId()}, Message.newBuilder().setPlayerDisconnectedMsg(msg).build());
    playerMap.remove(conn.getId().toUpperCase());
  }

  public void sendMessage(String id, Message message) {
    log.debug(
        server.getConfig().getServerName()
            + " sent to "
            + id
            + ": "
            + message.getMessageTypeCase());
    router.sendMessage(id, message.toByteArray());
  }

  public void sendMessage(String id, Object channel, Message message) {
    log.debug(
        "{} sent to {}: {} ({})",
        server.getName(),
        id,
        message.getMessageTypeCase(),
        channel.toString());
    router.sendMessage(id, channel, message.toByteArray());
  }

  public void broadcastMessage(Message message) {
    log.debug(server.getConfig().getServerName() + " broadcast: " + message.getMessageTypeCase());
    router.broadcastMessage(message.toByteArray());
  }

  public void broadcastMessage(String[] exclude, Message message) {
    log.debug(
        server.getConfig().getServerName()
            + " broadcast: "
            + message.getMessageTypeCase()
            + " except to "
            + String.join(",", exclude));
    router.broadcastMessage(exclude, message.toByteArray());
  }

  public void open() throws IOException {
    connection.start();
  }

  public void close() {
    connection.close();

    for (var connection : router.removeAll()) {
      connection.close();
    }
  }

  public void addObserver(ServerObserver observer) {
    connection.addObserver(observer);
  }

  public void removeObserver(ServerObserver observer) {
    connection.removeObserver(observer);
  }
}
