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
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.annotation.Nullable;
import net.rptools.clientserver.simple.connection.Connection;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Campaign;
import net.rptools.maptool.model.CampaignFactory;
import net.rptools.maptool.model.campaign.CampaignManager;
import net.rptools.maptool.model.player.LocalPlayer;
import net.rptools.maptool.model.player.PlayerDatabase;
import net.rptools.maptool.model.player.PlayerDatabaseFactory;
import net.rptools.maptool.server.MapToolServer;
import net.rptools.maptool.server.PersonalServer;
import net.rptools.maptool.server.ServerCommand;
import net.rptools.maptool.server.ServerConfig;
import net.rptools.maptool.server.ServerPolicy;

/**
 * The client side of a client-server channel.
 *
 * <p>This has nothing to do with the GUI, but represents those parts of the client that are needed
 * to interact with a server. Most of this used to exist as global state in {@link
 * net.rptools.maptool.client.MapTool} and elsewhere.
 */
public class MapToolClient {
  private final LocalPlayer player;
  private final PlayerDatabase playerDatabase;
  private final IMapToolConnection conn;
  private Campaign campaign;
  private ServerPolicy serverPolicy;
  private final ServerCommand serverCommand;

  private boolean disconnectExpected = false;

  private MapToolClient(
      boolean isForLocalServer,
      LocalPlayer player,
      PlayerDatabase playerDatabase,
      ServerPolicy serverPolicy,
      @Nullable ServerConfig serverConfig) {
    this.campaign = new Campaign();
    this.player = player;
    this.playerDatabase = playerDatabase;
    this.serverPolicy = serverPolicy;

    this.conn =
        serverConfig == null
            ? new NilMapToolConnection()
            : new MapToolConnection(this, serverConfig, player);

    this.serverCommand = new ServerCommandClientImpl(this);

    this.conn.addDisconnectHandler(conn -> onDisconnect(isForLocalServer, conn));
    this.conn.onCompleted(
        () -> {
          this.conn.addMessageHandler(new ClientMessageHandler(this));
        });
  }

  /**
   * Creates a client for use with a personal server.
   *
   * @param server The personal server that will run with this client.
   */
  public MapToolClient(PersonalServer server) {
    this(true, server.getLocalPlayer(), server.getPlayerDatabase(), new ServerPolicy(), null);
  }

  /**
   * Creates a client for use with a remote hosted server.
   *
   * @param player The player connecting to the server.
   * @param config The configuration details needed to connect to the server.
   */
  public MapToolClient(LocalPlayer player, ServerConfig config) {
    this(
        false,
        player,
        PlayerDatabaseFactory.getLocalPlayerDatabase(player),
        new ServerPolicy(),
        config);
  }

  /**
   * Creates a client for a server hosted in the same MapTool process.
   *
   * @param player The player who started the server.
   * @param server The local server.
   */
  public MapToolClient(LocalPlayer player, MapToolServer server) {
    this(true, player, server.getPlayerDatabase(), server.getPolicy(), server.getConfig());
  }

  public void start() throws IOException {
    conn.start();
  }

  public void close() throws IOException {
    if (conn.isAlive()) {
      conn.close();
    }
  }

  public void expectDisconnection() {
    disconnectExpected = true;
  }

  public ServerCommand getServerCommand() {
    return serverCommand;
  }

  public LocalPlayer getPlayer() {
    return player;
  }

  public PlayerDatabase getPlayerDatabase() {
    return playerDatabase;
  }

  public IMapToolConnection getConnection() {
    return conn;
  }

  public ServerPolicy getServerPolicy() {
    return serverPolicy;
  }

  public Campaign getCampaign() {
    return this.campaign;
  }

  public void setCampaign(Campaign campaign) {
    this.campaign = campaign;
  }

  public void setServerPolicy(ServerPolicy serverPolicy, boolean sendToServer) {
    this.serverPolicy = serverPolicy;
    if (sendToServer) {
      this.serverCommand.setServerPolicy(serverPolicy);
    }
  }

  private void onDisconnect(boolean isLocalServer, Connection connection) {
    /*
     * Three main cases:
     * 1. Expected disconnect. This will be part of a broader shutdown sequence and we don't need to
     *    do anything to clean up client or server state.
     * 2. Unexpected disconnect for remote server. Common case due to remote server shutdown or
     *    other lost connection. We need to clean up the connection, show an error to the user, and
     *    start a new personal server with a blank campaign.
     * 3. Unexpected disconnect for local server. A rare case where we lost connection without
     *    shutting down the server. We need to clean up the connection, stop the server, show an
     *    error to the user, and start a new personal server with the current campaign.
     */

    if (!disconnectExpected) {
      // Make sure the connection state is cleaned up since we can't count on it having been done.
      MapTool.disconnect();
      if (isLocalServer) {
        MapTool.stopServer();
      }

      var errorText = I18N.getText("msg.error.server.disconnected");
      var connectionError = connection.getError();
      var errorMessage = errorText + (connectionError != null ? (": " + connectionError) : "");
      MapTool.showError(errorMessage);

      // hide map so player doesn't get a brief GM view
      MapTool.getFrame().setCurrentZoneRenderer(null);
      MapTool.getFrame().getToolbarPanel().getMapselect().setVisible(true);
      MapTool.getFrame().getAssetPanel().enableAssets();
      new CampaignManager().clearCampaignData();
      MapTool.getFrame().getToolbarPanel().setTokenSelectionGroupEnabled(true);

      // Keep any local campaign around in the new personal server.
      final var campaign = isLocalServer ? getCampaign() : CampaignFactory.createBasicCampaign();
      try {
        MapTool.startPersonalServer(campaign);
      } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
        MapTool.showError(I18N.getText("msg.error.server.cantrestart"), e);
      }
    } else if (!isLocalServer) {
      // expected disconnect from someone else's server
      // hide map so player doesn't get a brief GM view
      MapTool.getFrame().setCurrentZoneRenderer(null);
    }
  }
}
