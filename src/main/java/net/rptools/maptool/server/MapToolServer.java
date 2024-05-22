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
import javax.annotation.Nullable;
import javax.swing.SwingUtilities;
import net.rptools.clientserver.ConnectionFactory;
import net.rptools.clientserver.simple.DisconnectHandler;
import net.rptools.clientserver.simple.MessageHandler;
import net.rptools.clientserver.simple.connection.Connection;
import net.rptools.clientserver.simple.server.Router;
import net.rptools.clientserver.simple.server.Server;
import net.rptools.clientserver.simple.server.ServerObserver;
import net.rptools.maptool.client.AppConstants;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolRegistry;
import net.rptools.maptool.client.ui.connectioninfodialog.ConnectionInfoDialog;
import net.rptools.maptool.common.MapToolConstants;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Campaign;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.TextMessage;
import net.rptools.maptool.model.player.Player;
import net.rptools.maptool.model.player.ServerSidePlayerDatabase;
import net.rptools.maptool.server.proto.Message;
import net.rptools.maptool.server.proto.PlayerConnectedMsg;
import net.rptools.maptool.server.proto.PlayerDisconnectedMsg;
import net.rptools.maptool.server.proto.SetCampaignMsg;
import net.rptools.maptool.server.proto.UpdateAssetTransferMsg;
import net.rptools.maptool.transfer.AssetProducer;
import net.rptools.maptool.transfer.AssetTransferManager;
import net.rptools.maptool.util.UPnPUtil;
import net.tsc.servicediscovery.ServiceAnnouncer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author drice
 */
public class MapToolServer {
  private static final Logger log = LogManager.getLogger(MapToolServer.class);
  private static final int ASSET_CHUNK_SIZE = 5 * 1024;

  public enum State {
    New,
    Started,
    Stopped
  }

  private final Server server;
  private final MessageHandler messageHandler;
  private final Router router;
  private final ServerConfig config;
  private final ServerSidePlayerDatabase playerDatabase;

  /** Maps connection IDs to their associated player */
  private final Map<String, Player> playerMap = Collections.synchronizedMap(new HashMap<>());

  private final Map<String, AssetTransferManager> assetManagerMap =
      Collections.synchronizedMap(new HashMap<String, AssetTransferManager>());
  private final AssetProducerThread assetProducerThread;

  private final boolean useUPnP;
  private final ServiceAnnouncer announcer;
  private Campaign campaign;
  private ServerPolicy policy;
  private HeartbeatThread heartbeatThread;
  private final DisconnectHandler onConnectionDisconnected;
  private final ServerObserver serverObserver;

  private State currentState;

  public MapToolServer(
      String id,
      Campaign campaign,
      @Nullable ServerConfig config,
      boolean useUPnP,
      ServerPolicy policy,
      ServerSidePlayerDatabase playerDb) {
    this.config = config;
    this.useUPnP = useUPnP;
    this.policy = new ServerPolicy(policy);
    this.playerDatabase = playerDb;

    this.announcer =
        config == null
            ? null
            : new ServiceAnnouncer(id, config.getPort(), AppConstants.SERVICE_GROUP);

    server = ConnectionFactory.getInstance().createServer(this.config);
    messageHandler = new ServerMessageHandler(this);
    this.router = new Router();

    // Make sure the server has a different copy than the client.
    this.campaign = new Campaign(campaign);

    assetProducerThread = new AssetProducerThread();

    currentState = State.New;

    this.onConnectionDisconnected = this::releaseClientConnection;
    this.serverObserver = this::connectionAdded;
  }

  /**
   * Transition from any state except {@code newState} to {@code newState}.
   *
   * @param newState The new state to set.
   */
  private boolean transitionToState(State newState) {
    if (currentState == newState) {
      log.warn(
          "Failed to transition to state {} because that is already the current state", newState);
      return false;
    } else {
      currentState = newState;
      return true;
    }
  }

  /**
   * Transition from {@code expectedState} to {@code newState}.
   *
   * @param expectedState The state to transition from
   * @param newState The new state to set.
   */
  private boolean transitionToState(State expectedState, State newState) {
    if (currentState != expectedState) {
      log.warn(
          "Failed to transition from state {} to state {} because the current state is actually {}",
          expectedState,
          newState,
          currentState);
      return false;
    } else {
      currentState = newState;
      return true;
    }
  }

  public State getState() {
    return currentState;
  }

  private String getConnectionId(String playerId) {
    synchronized (playerMap) {
      for (Map.Entry<String, Player> entry : playerMap.entrySet()) {
        if (entry.getValue().getName().equalsIgnoreCase(playerId)) {
          return entry.getKey();
        }
      }
    }
    return null;
  }

  private Player getPlayer(String playerId) {
    synchronized (playerMap) {
      for (Player player : playerMap.values()) {
        if (player.getName().equalsIgnoreCase(playerId)) {
          return player;
        }
      }
    }
    return null;
  }

  public boolean isPersonalServer() {
    return config == null;
  }

  public boolean isServerRegistered() {
    return config == null ? false : config.isServerRegistered();
  }

  public String getName() {
    return config == null ? "" : config.getServerName();
  }

  public int getPort() {
    return config == null ? -1 : config.getPort();
  }

  private void connectionAdded(Connection conn) {
    var handshake =
        new ServerHandshake(
            this, conn, playerDatabase, config != null && config.getUseEasyConnect());

    handshake.whenComplete(
        (player, error) -> {
          if (error != null) {
            log.error("Client closing: bad handshake", error);
            releaseClientConnection(conn);
          } else {
            log.debug("About to add new client");
            addRemoteConnection(conn, player);
          }
        });
    // Make sure the client is allowed
    handshake.startHandshake();
  }

  private void installConnection(Connection conn, Player player) {
    playerMap.put(conn.getId().toUpperCase(), player);

    conn.addMessageHandler(messageHandler);
    conn.addDisconnectHandler(onConnectionDisconnected);

    // Make sure any stale connections are gone to avoid conflicts, then add the new one.
    for (var reaped : router.reapClients()) {
      try {
        releaseClientConnection(reaped);
      } catch (Exception e) {
        // Don't want to raise an error if notification of removing a dead connection failed
      }
    }
    router.addConnection(conn);

    assetManagerMap.put(conn.getId(), new AssetTransferManager());
  }

  public void addLocalConnection(Connection conn, Player localPlayer) {
    installConnection(conn, localPlayer);
  }

  private void addRemoteConnection(Connection conn, Player connPlayer) {
    installConnection(conn, connPlayer);

    synchronized (playerMap) {
      for (Player player : playerMap.values()) {
        var msg = PlayerConnectedMsg.newBuilder().setPlayer(player.toDto());
        sendMessage(conn.getId(), Message.newBuilder().setPlayerConnectedMsg(msg).build());
      }
    }
    var msg = PlayerConnectedMsg.newBuilder().setPlayer(connPlayer.getTransferablePlayer().toDto());
    broadcastMessage(Message.newBuilder().setPlayerConnectedMsg(msg).build());

    var msg2 = SetCampaignMsg.newBuilder().setCampaign(campaign.toDto());
    sendMessage(conn.getId(), Message.newBuilder().setSetCampaignMsg(msg2).build());
  }

  public void bootPlayer(String playerId) {
    var connectionId = getConnectionId(playerId);
    var connection = router.getConnection(connectionId);
    if (connection == null) {
      return;
    }

    releaseClientConnection(connection);
  }

  /**
   * Cleans up references to a disconnected client connection.
   *
   * @param connection the connection to release
   */
  private void releaseClientConnection(Connection connection) {
    connection.removeDisconnectHandler(onConnectionDisconnected);

    connection.close();
    router.removeConnection(connection);
    assetManagerMap.remove(connection.getId());

    // Notify everyone else about the disconnection.
    var player = playerMap.remove(connection.getId().toUpperCase());
    if (player != null) {
      var msg =
          PlayerDisconnectedMsg.newBuilder().setPlayer(player.getTransferablePlayer().toDto());
      broadcastMessage(
          new String[] {connection.getId()},
          Message.newBuilder().setPlayerDisconnectedMsg(msg).build());
    }
  }

  public void addAssetProducer(String connectionId, AssetProducer producer) {
    AssetTransferManager manager = assetManagerMap.get(connectionId);
    manager.addProducer(producer);
  }

  public boolean isPlayerConnected(String playerId) {
    return getPlayer(playerId) != null;
  }

  public void updatePlayerStatus(String playerName, GUID zoneId, boolean loaded) {
    var player = getPlayer(playerName);
    if (player != null) {
      player.setLoaded(loaded);
      player.setZoneId(zoneId);
    }
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
    return new ServerPolicy(policy);
  }

  public void updateServerPolicy(ServerPolicy policy) {
    this.policy = new ServerPolicy(policy);
  }

  public void stop() {
    if (!transitionToState(State.Stopped)) {
      return;
    }

    server.close();
    for (var connection : router.removeAll()) {
      connection.removeDisconnectHandler(onConnectionDisconnected);
      connection.close();
    }

    assetManagerMap.clear();

    if (heartbeatThread != null) {
      heartbeatThread.shutdown();
    }
    if (assetProducerThread != null) {
      assetProducerThread.shutdown();
    }

    if (announcer != null) {
      announcer.stop();
    }

    // Unregister ourselves
    if (config != null && config.isServerRegistered()) {
      try {
        MapToolRegistry.getInstance().unregisterInstance();
      } catch (Throwable t) {
        MapTool.showError("While unregistering server instance", t);
      }
    }

    // Close UPnP port mapping if used
    if (useUPnP && config != null) {
      int port = config.getPort();
      UPnPUtil.closePort(port);
    }
  }

  public void start() throws IOException {
    if (!transitionToState(State.New, State.Started)) {
      return;
    }

    server.addObserver(serverObserver);
    try {
      server.start();
    } catch (IOException e) {
      // Make sure we're in a reasonable state before propagating.
      log.error("Failed to start server", e);
      transitionToState(State.Stopped);
      server.removeObserver(serverObserver);
      throw e;
    }

    // Use UPnP to open port in router
    if (useUPnP && config != null) {
      UPnPUtil.openPort(config.getPort());
    }

    // Registered ?
    if (config != null && config.isServerRegistered()) {
      try {
        MapToolRegistry.RegisterResponse result =
            MapToolRegistry.getInstance()
                .registerInstance(config.getServerName(), config.getPort(), config.getUseWebRTC());
        if (result == MapToolRegistry.RegisterResponse.NAME_EXISTS) {
          MapTool.showError("msg.error.alreadyRegistered");
        } else {
          heartbeatThread = new HeartbeatThread(config.getPort());
          heartbeatThread.start();
        }
        // TODO: I don't like this
      } catch (Exception e) {
        MapTool.showError("msg.error.failedCannotRegisterServer", e);
      }
    }

    if (announcer != null) {
      announcer.start();
    }

    assetProducerThread.start();
  }

  public void sendMessage(String id, Message message) {
    log.debug("{} sent to {}: {}", getName(), id, message.getMessageTypeCase());
    router.sendMessage(id, message.toByteArray());
  }

  public void sendMessage(String id, Object channel, Message message) {
    log.debug(
        "{} sent to {}: {} ({})", getName(), id, message.getMessageTypeCase(), channel.toString());
    router.sendMessage(id, channel, message.toByteArray());
  }

  public void broadcastMessage(Message message) {
    log.debug("{} broadcast: {}", getName(), message.getMessageTypeCase());
    router.broadcastMessage(message.toByteArray());
  }

  public void broadcastMessage(String[] exclude, Message message) {
    log.debug(
        "{} broadcast: {} except to {}",
        getName(),
        message.getMessageTypeCase(),
        String.join(",", exclude));
    router.broadcastMessage(exclude, message.toByteArray());
  }

  private class HeartbeatThread extends Thread {
    private static final Random random = new Random();

    private final int port;
    private boolean stop = false;
    private static final int HEARTBEAT_DELAY = 10 * 60 * 1000; // 10 minutes
    private static final int HEARTBEAT_FLUX = 20 * 1000; // 20 seconds

    private boolean ever_had_an_error = false;

    public HeartbeatThread(int port) {
      this.port = port;
    }

    @Override
    public void run() {
      int WARNING_TIME = 2; // number of heartbeats before popup warning
      int errors = 0;
      String IP_addr = ConnectionInfoDialog.getExternalAddress();

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
              sendMessage(
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
}
