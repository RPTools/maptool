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
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.rptools.clientserver.ActivityListener;
import net.rptools.clientserver.ConnectionFactory;
import net.rptools.clientserver.hessian.server.IMethodServerConnection;
import net.rptools.clientserver.hessian.server.MethodServerConnection;
import net.rptools.clientserver.simple.AbstractConnection;
import net.rptools.clientserver.simple.DisconnectHandler;
import net.rptools.clientserver.simple.MessageHandler;
import net.rptools.clientserver.simple.client.IClientConnection;
import net.rptools.clientserver.simple.server.IHandshake;
import net.rptools.clientserver.simple.server.ServerObserver;
import net.rptools.maptool.client.ClientCommand;
import net.rptools.maptool.common.MapToolConstants;
import net.rptools.maptool.model.Player;
import net.rptools.maptool.transfer.AssetChunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** @author trevor */
public class MapToolServerConnection implements ServerObserver, IHandshake {
  private static final Logger log = LogManager.getLogger(MapToolServerConnection.class);
  private final Map<String, Player> playerMap = new ConcurrentHashMap<String, Player>();
  private final MapToolServer server;
  private final IMethodServerConnection connection;

  public MapToolServerConnection(MapToolServer server) throws IOException {
    this.connection = ConnectionFactory.getInstance().createServerConnection(server.getConfig(), this);
    this.server = server;
    addObserver(this);
  }

  /*
   * (non-Javadoc)
   *
   * @see net.rptools.clientserver.simple.server.ServerConnection# handleConnectionHandshake(java.net.Socket)
   */
  public boolean handleConnectionHandshake(IClientConnection conn) {
    try {
      Player player = Handshake.receiveHandshake(server, conn);

      if (player != null) {
        playerMap.put(conn.getId().toUpperCase(), player);
        return true;
      }
    } catch (IOException ioe) {
      log.error("Handshake failure: " + ioe, ioe);
    } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    return false;
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
  public void connectionAdded(IClientConnection conn) {
    server.configureClientConnection(conn);

    Player connectedPlayer = playerMap.get(conn.getId().toUpperCase());
    for (Player player : playerMap.values()) {
      server
          .getConnection()
          .callMethod(conn.getId(), ClientCommand.COMMAND.playerConnected.name(), player);
    }
    server
        .getConnection()
        .broadcastCallMethod(ClientCommand.COMMAND.playerConnected.name(), connectedPlayer);
    // if (!server.isHostId(player.getName())) {
    // Don't bother sending the campaign file if we're hosting it ourselves
    server
        .getConnection()
        .callMethod(conn.getId(), ClientCommand.COMMAND.setCampaign.name(), server.getCampaign());
    // }
  }

  public void connectionRemoved(IClientConnection conn) {
    server.releaseClientConnection(conn.getId());
    server
        .getConnection()
        .broadcastCallMethod(
            new String[] {conn.getId()},
            ClientCommand.COMMAND.playerDisconnected.name(),
            playerMap.get(conn.getId().toUpperCase()));
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


  public void close() throws IOException {
    connection.close();
  }

  public void addObserver(ServerObserver observer) {
    connection.addObserver(observer);
  }

  public void removeObserver(ServerObserver observer) {
    connection.removeObserver(observer);
  }
}
