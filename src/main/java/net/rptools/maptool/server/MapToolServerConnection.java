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
import net.rptools.clientserver.hessian.server.MethodServerConnection;
import net.rptools.clientserver.simple.client.ClientConnection;
import net.rptools.clientserver.simple.server.HandshakeProvider;
import net.rptools.clientserver.simple.server.ServerObserver;
import net.rptools.maptool.model.player.Player;
import net.rptools.maptool.model.player.PlayerDatabase;
import net.rptools.maptool.server.proto.Message;
import net.rptools.maptool.server.proto.PlayerConnectedMsg;
import net.rptools.maptool.server.proto.PlayerDisconnectedMsg;
import net.rptools.maptool.server.proto.SetCampaignMsg;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** @author trevor */
public class MapToolServerConnection
    implements ServerObserver, HandshakeProvider, HandshakeObserver {
  private static final Logger log = LogManager.getLogger(MapToolServerConnection.class);
  private final Map<String, Player> playerMap = new ConcurrentHashMap<>();
  private final Map<ClientConnection, ServerHandshake> handshakeMap = new ConcurrentHashMap<>();
  private final MapToolServer server;
  private final MethodServerConnection connection;
  private final PlayerDatabase playerDatabase;

  public MapToolServerConnection(MapToolServer server, PlayerDatabase playerDatabase)
      throws IOException {
    this.connection =
        ConnectionFactory.getInstance().createServerConnection(server.getConfig(), this);
    this.server = server;
    this.playerDatabase = playerDatabase;
    addObserver(this);
  }

  /*
   * (non-Javadoc)
   *
   * @see net.rptools.clientserver.simple.server.ServerConnection# handleConnectionHandshake(java.net.Socket)
   */
  public Handshake getConnectionHandshake(ClientConnection conn) {
    var handshake = new ServerHandshake(conn, playerDatabase);
    handshakeMap.put(conn, handshake);
    handshake.addObserver(this);
    conn.addMessageHandler(handshake);
    return handshake;
  }

  @Override
  public void releaseHandshake(ClientConnection conn) {
    var handshake = handshakeMap.get(conn);
    handshakeMap.remove(conn);
    conn.removeMessageHandler(handshake);
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
  public void connectionAdded(ClientConnection conn) {
    server.configureClientConnection(conn);

    Player connectedPlayer = playerMap.get(conn.getId().toUpperCase());
    for (Player player : playerMap.values()) {
      var msg = PlayerConnectedMsg.newBuilder().setPlayer(player.toDto());
      server
          .getConnection()
          .sendMessage(conn.getId(), Message.newBuilder().setPlayerConnectedMsg(msg).build());
    }
    var msg =
        PlayerConnectedMsg.newBuilder().setPlayer(connectedPlayer.getTransferablePlayer().toDto());
    server
        .getConnection()
        .broadcastMessage(Message.newBuilder().setPlayerConnectedMsg(msg).build());

    var msg2 = SetCampaignMsg.newBuilder().setCampaign(server.getCampaign().toDto());
    server
        .getConnection()
        .sendMessage(conn.getId(), Message.newBuilder().setSetCampaignMsg(msg2).build());
  }

  public void connectionRemoved(ClientConnection conn) {
    server.releaseClientConnection(conn.getId());
    var player = playerMap.get(conn.getId().toUpperCase()).getTransferablePlayer();
    var msg = PlayerDisconnectedMsg.newBuilder().setPlayer(player.toDto());
    server
        .getConnection()
        .broadcastMessage(
            new String[] {conn.getId()},
            Message.newBuilder().setPlayerDisconnectedMsg(msg).build());
    playerMap.remove(conn.getId().toUpperCase());
  }

  public void addMessageHandler(ServerMethodHandler handler) {
    connection.addMessageHandler(handler);
  }

  public void broadcastCallMethod(String method, Object... parameters) {
    connection.broadcastCallMethod(method, parameters);
  }

  public void broadcastCallMethod(String[] exclude, String method, Object... parameters) {
    connection.broadcastCallMethod(exclude, method, parameters);
  }

  public void callMethod(String id, String method, Object... parameters) {
    connection.callMethod(id, method, parameters);
  }

  public void callMethod(String id, Object channel, String method, Object... parameters) {
    connection.callMethod(id, channel, method, parameters);
  }

  public void broadcastMessage(byte[] message) {
    connection.broadcastMessage(message);
  }

  public void broadcastMessage(String[] exclude, byte[] message) {
    connection.broadcastMessage(exclude, message);
  }

  public void sendMessage(String id, Message message) {
    connection.sendMessage(id, message.toByteArray());
  }

  public void broadcastMessage(Message message) {
    connection.broadcastMessage(message.toByteArray());
  }

  public void broadcastMessage(String[] exclude, Message message) {
    connection.broadcastMessage(exclude, message.toByteArray());
  }

  public void open() throws IOException {
    connection.open();
  }

  public void close() {
    connection.close();
  }

  public void addObserver(ServerObserver observer) {
    connection.addObserver(observer);
  }

  public void removeObserver(ServerObserver observer) {
    connection.removeObserver(observer);
  }

  @Override
  public void onCompleted(Handshake handshake) {
    handshake.removeObserver(this);
    if (handshake.isSuccessful()) {
      Player player = handshake.getPlayer();

      if (player != null) {
        playerMap.put(handshake.getConnection().getId().toUpperCase(), player);
      }
    } else {
      var exception = handshake.getException();
      if (exception != null) log.error("Handshake failure: " + exception, exception);
    }
  }
}
