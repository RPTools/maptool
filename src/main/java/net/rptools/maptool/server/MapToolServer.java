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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import javax.swing.SwingUtilities;
import net.rptools.clientserver.simple.client.ClientConnection;
import net.rptools.clientserver.simple.server.ServerObserver;
import net.rptools.maptool.client.ClientCommand;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolRegistry;
import net.rptools.maptool.client.ui.ConnectionInfoDialog;
import net.rptools.maptool.common.MapToolConstants;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Campaign;
import net.rptools.maptool.model.TextMessage;
import net.rptools.maptool.transfer.AssetChunk;
import net.rptools.maptool.transfer.AssetProducer;
import net.rptools.maptool.transfer.AssetTransferManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** @author drice */
public class MapToolServer {
  private static final Logger log = LogManager.getLogger(MapToolServer.class);
  private static final int ASSET_CHUNK_SIZE = 5 * 1024;

  private final MapToolServerConnection conn;
  private final ServerMethodHandler handler;
  private final ServerConfig config;

  private final Map<String, AssetTransferManager> assetManagerMap =
      Collections.synchronizedMap(new HashMap<String, AssetTransferManager>());
  private final Map<String, ClientConnection> connectionMap =
      Collections.synchronizedMap(new HashMap<String, ClientConnection>());
  private final AssetProducerThread assetProducerThread;

  private Campaign campaign;
  private ServerPolicy policy;
  private HeartbeatThread heartbeatThread;

  public MapToolServer(ServerConfig config, ServerPolicy policy) throws IOException {
    handler = new ServerMethodHandler(this);
    conn = new MapToolServerConnection(this, config.getPort());
    conn.addMessageHandler(handler);

    campaign = new Campaign();

    assetProducerThread = new AssetProducerThread();
    assetProducerThread.start();

    this.config = config;
    this.policy = policy;

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
      try {
        connection.close();
      } catch (IOException e) {
        log.error("Could not release connection: " + id, e);
      }
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

  public ServerMethodHandler getMethodHandler() {
    return handler;
  }

  public ServerConfig getConfig() {
    return config;
  }

  public void stop() {
    try {
      conn.close();
      if (heartbeatThread != null) {
        heartbeatThread.shutdown();
      }
      if (assetProducerThread != null) {
        assetProducerThread.shutdown();
      }
    } catch (IOException e) {
      // Not too concerned about this
      log.info("Couldn't close connection", e);
    }
  }

  private static final Random random = new Random();

  private class HeartbeatThread extends Thread {
    private boolean stop = false;
    private static final int HEARTBEAT_DELAY = 7 * 60 * 1000; // 7 minutes
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
          MapToolRegistry.heartBeat(config.getPort());
          // If the heartbeat worked, reset the counter if the last one failed
          if (errors != 0) {
            String msg = I18N.getText("msg.info.heartbeat.registrySuccess", errors);
            SwingUtilities.invokeLater(
                new Runnable() {
                  public void run() {
                    // Write to the GM's console. (Code taken from client.functions.ChatFunction)
                    MapTool.serverCommand().message(TextMessage.gm(null, msg));
                    // Write to our console. (Code taken from client.functions.ChatFunction)
                    MapTool.addServerMessage(TextMessage.me(null, msg));
                  }
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
                new Runnable() {
                  public void run() {
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

    @Override
    public void run() {
      while (!stop) {
        Entry<String, AssetTransferManager> entryForException = null;
        try {
          boolean lookForMore = false;
          for (Entry<String, AssetTransferManager> entry : assetManagerMap.entrySet()) {
            entryForException = entry;
            AssetChunk chunk = entry.getValue().nextChunk(ASSET_CHUNK_SIZE);
            if (chunk != null) {
              lookForMore = true;
              getConnection()
                  .callMethod(
                      entry.getKey(),
                      MapToolConstants.Channel.IMAGE,
                      ClientCommand.COMMAND.updateAssetTransfer.name(),
                      chunk);
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
          log.info("Couldn't retrieve AssetChunk for " + entryForException.getKey(), e);
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
    MapToolServer server = new MapToolServer(new ServerConfig(), new ServerPolicy());
  }
}
