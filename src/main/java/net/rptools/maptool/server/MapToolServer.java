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

import static net.rptools.maptool.model.player.PlayerDatabaseFactory.PlayerDatabaseType.PERSONAL_SERVER;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import javax.swing.SwingUtilities;
import net.rptools.clientserver.simple.client.ClientConnection;
import net.rptools.clientserver.simple.server.ServerObserver;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolRegistry;
import net.rptools.maptool.client.ui.connectioninfodialog.ConnectionInfoDialog;
import net.rptools.maptool.common.MapToolConstants;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Campaign;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.TextMessage;
import net.rptools.maptool.model.player.PlayerDatabase;
import net.rptools.maptool.model.player.PlayerDatabaseFactory;
import net.rptools.maptool.server.proto.Message;
import net.rptools.maptool.server.proto.UpdateAssetTransferMsg;
import net.rptools.maptool.transfer.AssetProducer;
import net.rptools.maptool.transfer.AssetTransferManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author drice
 */
public class MapToolServer {
  private static final Logger log = LogManager.getLogger(MapToolServer.class);
  private static final int ASSET_CHUNK_SIZE = 5 * 1024;

  private final MapToolServerConnection conn;
  private final ServerMessageHandler handler;
  private final ServerConfig config;
  private final PlayerDatabase playerDatabase;

  private final Map<String, AssetTransferManager> assetManagerMap =
      Collections.synchronizedMap(new HashMap<String, AssetTransferManager>());
  private final Map<String, ClientConnection> connectionMap =
      Collections.synchronizedMap(new HashMap<String, ClientConnection>());
  private final AssetProducerThread assetProducerThread;

  private Campaign campaign;
  private ServerPolicy policy;
  private HeartbeatThread heartbeatThread;

  public MapToolServer(ServerConfig config, ServerPolicy policy, PlayerDatabase playerDb)
      throws IOException {
    this.config = config;
    this.policy = policy;
    handler = new ServerMessageHandler(this);
    playerDatabase = playerDb;
    conn = new MapToolServerConnection(this, playerDatabase);
    conn.addMessageHandler(handler);

    campaign = new Campaign();

    assetProducerThread = new AssetProducerThread();
    assetProducerThread.start();

    // Start a heartbeat if requested
    if (config.isServerRegistered()) {
      heartbeatThread = new HeartbeatThread();
      heartbeatThread.start();
    }
  }

  public void configureClientConnection(ClientConnection connection) {
    String id = connection.getId();
    assetManagerMap.put(id, new AssetTransferManager());
    connectionMap.put(id, connection);
  }

  public ClientConnection getClientConnection(String id) {
    return connectionMap.get(id);
  }

  public String getConnectionId(String playerId) {
    return conn.getConnectionId(playerId);
  }

  /**
   * Forceably disconnects a client and cleans up references to it
   *
   * @param id the connection ID
   */
  public void releaseClientConnection(String id) {
    ClientConnection connection = getClientConnection(id);
    if (connection != null) {
      connection.close();
    }
    assetManagerMap.remove(id);
    connectionMap.remove(id);
  }

  public void addAssetProducer(String connectionId, AssetProducer producer) {
    AssetTransferManager manager = assetManagerMap.get(connectionId);
    manager.addProducer(producer);
  }

  public void addObserver(ServerObserver observer) {
    if (observer != null) {
      conn.addObserver(observer);
    }
  }

  public void removeObserver(ServerObserver observer) {
    conn.removeObserver(observer);
  }

  public boolean isHostId(String playerId) {
    return config.getHostPlayerId() != null && config.getHostPlayerId().equals(playerId);
  }

  public MapToolServerConnection getConnection() {
    return conn;
  }

  public boolean isPlayerConnected(String id) {
    return conn.getPlayer(id) != null;
  }

  public void updatePlayerStatus(String playerName, GUID zoneId, boolean loaded) {
    var player = conn.getPlayer(playerName);
    player.setLoaded(loaded);
    player.setZoneId(zoneId);
  }

  public void setCampaign(Campaign campaign) {
    // Don't allow null campaigns, but allow the campaign to be cleared out
    if (campaign == null) {
      campaign = new Campaign();
    }
    this.campaign = campaign;
  }

  public Campaign getCampaign() {
    return campaign;
  }

  public ServerPolicy getPolicy() {
    return policy;
  }

  public void updateServerPolicy(ServerPolicy policy) {
    this.policy = policy;
  }

  public ServerMessageHandler getMethodHandler() {
    return handler;
  }

  public ServerConfig getConfig() {
    return config;
  }

  public void stop() {
    conn.close();
    if (heartbeatThread != null) {
      heartbeatThread.shutdown();
    }
    if (assetProducerThread != null) {
      assetProducerThread.shutdown();
    }
  }

  private static final Random random = new Random();

  public void start() throws IOException {
    conn.open();
  }

  private class HeartbeatThread extends Thread {
    private boolean stop = false;
    private static final int HEARTBEAT_DELAY = 10 * 60 * 1000; // 10 minutes
    private static final int HEARTBEAT_FLUX = 20 * 1000; // 20 seconds

    private boolean ever_had_an_error = false;

    @Override
    public void run() {
      int WARNING_TIME = 2; // number of heartbeats before popup warning
      int errors = 0;
      String IP_addr = ConnectionInfoDialog.getExternalAddress();
      int port = getConfig().getPort();

      while (!stop) {
        try {
          Thread.sleep(HEARTBEAT_DELAY + (int) (HEARTBEAT_FLUX * random.nextFloat()));
          // Pulse
          MapToolRegistry.getInstance().heartBeat();
          // If the heartbeat worked, reset the counter if the last one failed
          if (errors != 0) {
            String msg = I18N.getText("msg.info.heartbeat.registrySuccess", errors);
            SwingUtilities.invokeLater(
                () -> {
                  // Write to the GM's console. (Code taken from client.functions.ChatFunction)
                  MapTool.serverCommand().message(TextMessage.gm(null, msg));
                  // Write to our console. (Code taken from client.functions.ChatFunction)
                  MapTool.addServerMessage(TextMessage.me(null, msg));
                });
            errors = 0;
            WARNING_TIME = 2;
          }
        } catch (InterruptedException ie) {
          // This means we are being stopped from the outside, between heartbeats
          break;
        } catch (Exception e) {
          // Any other exception is a problem with the Hessian protocol and/or a network issue
          // Regardless, we will count the number of consecutive errors and display a dialog
          // at appropriate times, but otherwise ignore it. The purpose of the heartbeat is
          // to let the website registry know this server is still running so that clients can
          // easily connect. If it breaks in the middle of a game, the clients are already connected
          // so it's not *that* terrible. However, our dialog to the user should tell them where
          // the connection info can be found so they can give it to a client, if needed.
          errors++;
          if ((errors % WARNING_TIME) == 0) {
            WARNING_TIME = Math.min(WARNING_TIME * 3, 10);
            // It's been X heartbeats since we last talked to the registry successfully. Let
            // the user know we'll keep trying, but there may be an unrecoverable problem.
            // We use a linear backoff so we don't inundate the user with popups!

            String msg = I18N.getText("msg.info.heartbeat.registryFailure", IP_addr, port, errors);
            SwingUtilities.invokeLater(
                () -> {
                  // Write to the GM's console. (Code taken from client.functions.ChatFunction)
                  MapTool.serverCommand().message(TextMessage.gm(null, msg));
                  // Write to our console. (Code taken from client.functions.ChatFunction)
                  MapTool.addServerMessage(TextMessage.me(null, msg));

                  // This is the first time the heartbeat has failed in this stretch of time.
                  // Only writes to the log on the first error. Should it always add an entry?
                  if (!ever_had_an_error) {
                    ever_had_an_error = true;
                    // Uses a popup to tell the user what's going on. Includes a 'Logger.warn()'
                    // message.
                    MapTool.showWarning(msg, e);
                  }
                });
          }
        }
      }
    }

    public void shutdown() {
      stop = true;
      interrupt();
    }
  }

  ////
  // CLASSES
  private class AssetProducerThread extends Thread {
    private boolean stop = false;

    public AssetProducerThread() {
      setName("AssetProducerThread");
    }

    @Override
    public void run() {
      while (!stop) {
        Entry<String, AssetTransferManager> entryForException = null;
        try {
          boolean lookForMore = false;
          for (Entry<String, AssetTransferManager> entry : assetManagerMap.entrySet()) {
            entryForException = entry;
            var chunk = entry.getValue().nextChunk(ASSET_CHUNK_SIZE);
            if (chunk != null) {
              lookForMore = true;
              var msg = UpdateAssetTransferMsg.newBuilder().setChunk(chunk);
              getConnection()
                  .sendMessage(
                      entry.getKey(),
                      MapToolConstants.Channel.IMAGE,
                      Message.newBuilder().setUpdateAssetTransferMsg(msg).build());
            }
          }
          if (lookForMore) {
            continue;
          }
          // Sleep for a bit
          synchronized (this) {
            Thread.sleep(500);
          }
        } catch (Exception e) {
          log.warn("Couldn't retrieve AssetChunk for " + entryForException.getKey(), e);
          // keep on going
        }
      }
    }

    public void shutdown() {
      stop = true;
    }
  }

  ////
  // STANDALONE SERVER
  public static void main(String[] args) throws IOException {
    // This starts the server thread.
    PlayerDatabaseFactory.setCurrentPlayerDatabase(PERSONAL_SERVER);
    PlayerDatabase playerDatabase = PlayerDatabaseFactory.getCurrentPlayerDatabase();
    MapToolServer server =
        new MapToolServer(new ServerConfig(), new ServerPolicy(), playerDatabase);
  }
}
