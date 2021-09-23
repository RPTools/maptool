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
package net.rptools.maptool.model.player;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;
import javax.crypto.NoSuchPaddingException;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.player.Player.Role;
import net.rptools.maptool.model.player.PlayerDatabase.AuthMethod;
import net.rptools.maptool.util.threads.ThreadExecutionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Class for interacting with players and player information. */
public class Players {

  /** Return statuses possible when attempting to add a player. */
  public enum ChangePlayerStatus {
    OK,
    ERROR,
    PLAYER_EXISTS,
    NOT_SUPPORTED
  }

  /**
   * Property change event name for when a player is added. Some databases may not support this
   * event, so you will also need to listen to {@link #PROPERTY_CHANGE_DATABASE_CHANGED} for changes
   * to players in the database.
   */
  public static final String PROPERTY_CHANGE_PLAYER_ADDED =
      PlayerDBPropertyChange.PROPERTY_CHANGE_PLAYER_ADDED;

  /**
   * Property change event name for when a player is removed. Some databases may not support this
   * event, so you will also need to listen to {@link #PROPERTY_CHANGE_DATABASE_CHANGED} for changes
   * to players in the database.
   */
  public static final String PROPERTY_CHANGE_PLAYER_REMOVED =
      PlayerDBPropertyChange.PROPERTY_CHANGE_PLAYER_REMOVED;

  /** Property change event name for when a player is changed. */
  public static final String PROPERTY_CHANGE_PLAYER_CHANGED =
      PlayerDBPropertyChange.PROPERTY_CHANGE_PLAYER_CHANGED;
  /**
   * Property change event name for when the database is changed or there are mas updates. Some
   * databases may only support this event and not player added/removed/changed
   */
  public static final String PROPERTY_CHANGE_DATABASE_CHANGED =
      PlayerDBPropertyChange.PROPERTY_CHANGE_DATABASE_CHANGED;

  /** Instance for logging messages. */
  private static final Logger log = LogManager.getLogger(Players.class);

  /** instance variable for property change support. */
  private static final PropertyChangeSupport propertyChangeSupport =
      new PropertyChangeSupport(Players.class);

  private static final PropertyChangeListener databaseChangeListener =
      new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
          propertyChangeSupport.firePropertyChange(evt);
        }
      };

  private static final PropertyChangeListener databaseTypeChangeListener =
      new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
          PlayerDatabase oldDb = (PlayerDatabase) evt.getOldValue();
          PlayerDatabase newDb = (PlayerDatabase) evt.getNewValue();

          if (oldDb instanceof PlayerDBPropertyChange playerdb) {
            playerdb.removePropertyChangeListener(databaseChangeListener);
          }

          if (newDb instanceof PlayerDBPropertyChange playerdb) {
            playerdb.addPropertyChangeListener(databaseChangeListener);
          }
        }
      };

  static {
    PlayerDatabaseFactory.getDatabaseChangeTypeSupport()
        .addPropertyChangeListener(databaseTypeChangeListener);
    if (PlayerDatabaseFactory.getCurrentPlayerDatabase() instanceof PlayerDBPropertyChange pdb) {
      pdb.addPropertyChangeListener(databaseChangeListener);
    }
  }

  /**
   * Return the information about a specific known player.
   *
   * @param name the name of the player to get information about.
   * @return the information about the player.
   */
  public CompletableFuture<PlayerInfo> getPlayer(String name) {
    return CompletableFuture.supplyAsync(() -> getPlayerInfo(name));
  }

  /**
   * Returns the information about the player for the client.
   *
   * @return the information about the player for the client.
   */
  public CompletableFuture<PlayerInfo> getPlayer() {
    return new ThreadExecutionHelper<PlayerInfo>()
        .runOnSwingThread(
            () -> {
              Player player = MapTool.getPlayer();
              return getPlayerInfo(player.getName());
            });
  }

  /**
   * Returns the information about all the currently connected players.
   *
   * @return the information about all the currently connected players.
   */
  public CompletableFuture<Set<PlayerInfo>> getConnectedPlayers() {
    return CompletableFuture.supplyAsync(
        () -> getPlayersInfo().stream().filter(PlayerInfo::connected).collect(Collectors.toSet()));
  }

  /**
   * Returns the information about all known players. IF the player database does not support
   * persisted players then only the players connected will be returned.
   *
   * @return the information about all the known players.
   */
  public CompletableFuture<Set<PlayerInfo>> getDatabasePlayers() {
    return CompletableFuture.supplyAsync(this::getPlayersInfo);
  }

  /**
   * Returns the information about the current player database capabilities.
   *
   * @return the information about the current player database capabilities.
   */
  public CompletableFuture<PlayerDatabaseInfo> getDatabaseCapabilities() {
    return CompletableFuture.supplyAsync(this::getPlayerDatabaseInfo);
  }

  /**
   * Returns the information about the current player databases capabilities.
   *
   * @return the information about the current player databases capabilities.
   */
  private PlayerDatabaseInfo getPlayerDatabaseInfo() {
    PlayerDatabase playerDatabase = PlayerDatabaseFactory.getCurrentPlayerDatabase();
    return new PlayerDatabaseInfo(
        playerDatabase.supportsDisabling(),
        !playerDatabase.supportsRolePasswords(),
        playerDatabase.supportsAsymmetricalKeys(),
        playerDatabase.recordsOnlyConnectedPlayers());
  }

  /**
   * Returns information about the specified player.
   *
   * @param name the name of the player to return the information about.
   * @return the information about the player.
   */
  private PlayerInfo getPlayerInfo(String name) {
    try {
      var playerDatabase = PlayerDatabaseFactory.getCurrentPlayerDatabase();
      if (!playerDatabase.isPlayerRegistered(name)) {
        return null;
      }
      Player player = playerDatabase.getPlayer(name);
      Role role = player.getRole();
      boolean supportsBlocking = playerDatabase.supportsDisabling();
      String blockedReason = "";
      boolean blocked = false;
      if (supportsBlocking) {
        blockedReason = playerDatabase.getDisabledReason(player);
        if (blockedReason.length() > 0) {
          blocked = true;
        }
      }
      boolean connected = PlayerDatabaseFactory.getCurrentPlayerDatabase().isPlayerConnected(name);

      AuthMethod authMethod = playerDatabase.getAuthMethod(player);
      boolean persisted = false;
      if (playerDatabase instanceof PersistedPlayerDatabase persistedPlayerDatabase) {
        persisted = persistedPlayerDatabase.isPersisted(name);
      }

      return new PlayerInfo(name, role, blocked, blockedReason, connected, authMethod, persisted);
    } catch (Exception e) {
      if (e instanceof CompletionException ce) {
        throw ce;
      } else {
        throw new CompletionException(e);
      }
    }
  }

  /**
   * Returns the information about all the known players.
   *
   * @return the information about all the known players.
   */
  private Set<PlayerInfo> getPlayersInfo() {
    Set<PlayerInfo> players = new HashSet<>();
    var playerDatabase = PlayerDatabaseFactory.getCurrentPlayerDatabase();
    try {
      for (Player p : playerDatabase.getAllPlayers()) {
        players.add(getPlayerInfo(p.getName()));
      }
    } catch (Exception e) {
      if (e instanceof CompletionException ce) {
        throw ce;
      } else {
        throw new CompletionException(e);
      }
    }

    return players.stream().filter(Objects::nonNull).collect(Collectors.toSet());
  }

  /**
   * Returns if the current player database only records players that are connected.
   *
   * @return {@code true} if the player database only records players while they are connected.
   */
  public boolean recordsOnlyConnectedPlayers() {
    return PlayerDatabaseFactory.getCurrentPlayerDatabase().recordsOnlyConnectedPlayers();
  }

  /**
   * Returns if this current player database supports per player passwords.
   *
   * @return {@code true} if the player database supports per player passwords.
   */
  public boolean supportsPerPlayerPasswords() {
    return !PlayerDatabaseFactory.getCurrentPlayerDatabase().supportsRolePasswords();
  }

  /**
   * Returns if the current player database supports asymmetric keys.
   *
   * @return {@code true} if the player database supports asymmetric keys.
   */
  public boolean supportsAsymmetricKeys() {
    return PlayerDatabaseFactory.getCurrentPlayerDatabase().supportsAsymmetricalKeys();
  }

  /**
   * Adds a player to the current player database,
   *
   * @param name The name of the player to add.
   * @param role The role for the player.
   * @param password the password for the player
   * @return {@link ChangePlayerStatus#OK} if successful, otherwise the reason for the failure.
   */
  public ChangePlayerStatus addPlayerWithPassword(String name, Role role, String password) {
    if (!supportsPerPlayerPasswords()) {
      log.error(I18N.getText("msg.error.playerDB.cantAddPlayer", name));
      return ChangePlayerStatus.NOT_SUPPORTED;
    }

    var playerDatabase = PlayerDatabaseFactory.getCurrentPlayerDatabase();
    if (playerDatabase instanceof PersistedPlayerDatabase playerDb) {
      try {
        playerDb.addPlayerSharedPassword(name, role, password);
        return ChangePlayerStatus.OK;
      } catch (NoSuchAlgorithmException
          | InvalidKeySpecException
          | PasswordDatabaseException
          | NoSuchPaddingException
          | InvalidKeyException e) {
        log.error(e);
        MapTool.showError(I18N.getText("msg.error.playerDB.errorAdding", name), e);
        return ChangePlayerStatus.ERROR;
      }
    } else {
      log.error(I18N.getText("msg.error.playerDB.cantAddPlayer", name));
      return ChangePlayerStatus.NOT_SUPPORTED;
    }
  }

  /**
   * Adds a player to the current player database,
   *
   * @param name The name of the player to add.
   * @param role The role for the player.
   * @param publicKeyString the public key string for the player.
   * @return {@link ChangePlayerStatus#OK} if successful, otherwise the reason for the failure.
   */
  public ChangePlayerStatus addPlayerWithPublicKey(String name, Role role, String publicKeyString) {
    if (!supportsAsymmetricKeys()) {
      log.error(I18N.getText("msg.error.playerDB.cantAddPlayer", name));
      return ChangePlayerStatus.NOT_SUPPORTED;
    }

    var playerDatabase = PlayerDatabaseFactory.getCurrentPlayerDatabase();
    if (playerDatabase instanceof PersistedPlayerDatabase playerDb) {
      try {
        playerDb.addPlayerAsymmetricKey(name, role, Set.of(publicKeyString));
        return ChangePlayerStatus.OK;
      } catch (NoSuchAlgorithmException
          | InvalidKeySpecException
          | PasswordDatabaseException
          | NoSuchPaddingException
          | InvalidKeyException e) {
        log.error(e);
        MapTool.showError(I18N.getText("msg.error.playerDB.cantAddPlayerPublicKey", name), e);
        return ChangePlayerStatus.ERROR;
      }
    } else {
      log.error(I18N.getText("msg.error.playerDB.cantAddPlayer", name));
      return ChangePlayerStatus.NOT_SUPPORTED;
    }
  }

  public ChangePlayerStatus removePlayer(String name) {

    var playerDatabase = PlayerDatabaseFactory.getCurrentPlayerDatabase();
    if (playerDatabase instanceof PersistedPlayerDatabase playerDb) {
      playerDb.deletePlayer(name);
      return ChangePlayerStatus.OK;
    } else {
      log.error(I18N.getText("msg.error.playerDB.cantAddPlayer", name));
      return ChangePlayerStatus.NOT_SUPPORTED;
    }
  }

  /**
   * Adds a property change listener for player database events.
   *
   * @param listener The property change listener to add.
   */
  public static void addPropertyChangeListener(PropertyChangeListener listener) {
    propertyChangeSupport.addPropertyChangeListener(listener);
  }

  /**
   * Removes a property change listener for player database events.
   *
   * @param listener The property change listener to remove.
   */
  public static void removePropertyChangeListener(PropertyChangeListener listener) {
    propertyChangeSupport.removePropertyChangeListener(listener);
  }

  public void playerSignedIn(Player player) {
    PlayerDatabase playerDatabase = PlayerDatabaseFactory.getCurrentPlayerDatabase();
    var oldInfo = getPlayerInfo(player.getName());
    playerDatabase.playerSignedIn(player);
    var newInfo = getPlayerInfo(player.getName());
    if (newInfo != null) {
      if (oldInfo != null) {
        propertyChangeSupport.firePropertyChange(
            PlayerDBPropertyChange.PROPERTY_CHANGE_PLAYER_CHANGED, oldInfo, newInfo);
      } else {
        propertyChangeSupport.firePropertyChange(PROPERTY_CHANGE_PLAYER_ADDED, null, newInfo);
      }
    }
  }

  public void playerSignedOut(Player player) {
    PlayerDatabase playerDatabase = PlayerDatabaseFactory.getCurrentPlayerDatabase();
    var oldInfo = getPlayerInfo(player.getName());
    playerDatabase.playerSignedOut(player);
    var newInfo = getPlayerInfo(player.getName());
    if (oldInfo != null) {
      if (newInfo != null) {
        propertyChangeSupport.firePropertyChange(
            PlayerDBPropertyChange.PROPERTY_CHANGE_PLAYER_CHANGED, oldInfo, newInfo);
      } else {
        propertyChangeSupport.firePropertyChange(PROPERTY_CHANGE_PLAYER_REMOVED, oldInfo, null);
      }
    }
  }
}
