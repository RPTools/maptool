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

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
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
import net.rptools.maptool.util.cipher.CipherUtil;
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

  /** Instance for logging messages. */
  private static final Logger log = LogManager.getLogger(Players.class);

  private final PlayerDatabase playerDatabase;

  public Players(PlayerDatabase playerDatabase) {
    this.playerDatabase = playerDatabase;
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
   * Returns information about the specified player.
   *
   * @param name the name of the player to return the information about.
   * @return the information about the player.
   */
  private PlayerInfo getPlayerInfo(String name) {
    try {
      if (!playerDatabase.playerExists(name)) {
        return null;
      }
      Player player;
      if (playerDatabase instanceof DefaultPlayerDatabase dpdb) {
        player =
            dpdb.getAllPlayers().stream()
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .orElse(dpdb.getPlayer(name));
      } else {
        player = playerDatabase.getPlayer(name);
      }

      Role role = player.getRole();
      boolean supportsBlocking = playerDatabase.supportsDisabling();
      String blockedReason = "";
      boolean blocked = false;
      if (supportsBlocking) {
        blockedReason = playerDatabase.getBlockedReason(player);
        if (blockedReason.length() > 0) {
          blocked = true;
        }
      }

      boolean connected = playerDatabase.isPlayerConnected(name);
      AuthMethod authMethod = playerDatabase.getAuthMethod(player);
      boolean persisted = false;
      if (playerDatabase instanceof PersistedPlayerDatabase persistedPlayerDatabase) {
        persisted = persistedPlayerDatabase.isPersisted(name);
      }

      Set<String> pkeys = playerDatabase.getEncodedPublicKeys(name);

      return new PlayerInfo(
          name, role, blocked, blockedReason, connected, authMethod, pkeys, persisted);
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
   * Returns if this current player database supports per player passwords.
   *
   * @return {@code true} if the player database supports per player passwords.
   */
  private boolean supportsPerPlayerPasswords() {
    return !playerDatabase.supportsRolePasswords();
  }

  /**
   * Returns if the current player database supports asymmetric keys.
   *
   * @return {@code true} if the player database supports asymmetric keys.
   */
  private boolean supportsAsymmetricKeys() {
    return playerDatabase.supportsAsymmetricalKeys();
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
        MapTool.showError(I18N.getText("msg.error.playerDB.errorAddingPlayer", name), e);
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
      log.error(I18N.getText("msg.error.playerDB.cantAddPlayerPublicKey", name));
      return ChangePlayerStatus.NOT_SUPPORTED;
    }
    var pkeys = new HashSet<>(Arrays.asList(CipherUtil.splitPublicKeys(publicKeyString)));

    if (playerDatabase instanceof PersistedPlayerDatabase playerDb) {
      try {
        playerDb.addPlayerAsymmetricKey(name, role, pkeys);
        return ChangePlayerStatus.OK;
      } catch (NoSuchAlgorithmException
          | InvalidAlgorithmParameterException
          | InvalidKeySpecException
          | PasswordDatabaseException
          | NoSuchPaddingException
          | InvalidKeyException e) {
        log.error(e);
        MapTool.showError(I18N.getText("msg.error.playerDB.errorAddingPlayer", name), e);
        return ChangePlayerStatus.ERROR;
      }
    } else {
      log.error(I18N.getText("msg.error.playerDB.cantAddPlayer", name));
      return ChangePlayerStatus.NOT_SUPPORTED;
    }
  }

  /**
   * Sets the public keys for the specified player.
   *
   * @param name the name of the player.
   * @param publicKeyString the public keys for the player.
   * @return {@link ChangePlayerStatus#OK} if successful, otherwise the reason for the failure.
   */
  public ChangePlayerStatus setPublicKeys(String name, String publicKeyString) {
    if (!supportsAsymmetricKeys()) {
      log.error(I18N.getText("msg.error.playerDB.cantUpdatePlayer", name));
      return ChangePlayerStatus.NOT_SUPPORTED;
    }
    var pkeys = new HashSet<>(Arrays.asList(CipherUtil.splitPublicKeys(publicKeyString)));

    if (playerDatabase instanceof PersistedPlayerDatabase playerDb) {
      try {
        playerDb.setAsymmetricKeys(name, pkeys);
      } catch (NoSuchPaddingException
          | NoSuchAlgorithmException
          | InvalidAlgorithmParameterException
          | InvalidKeySpecException
          | PasswordDatabaseException
          | InvalidKeyException e) {
        MapTool.showError(I18N.getText("msg.error.playerDB.errorUpdatingPlayer", name), e);
        return ChangePlayerStatus.ERROR;
      }
    } else {
      log.error(I18N.getText("msg.error.playerDB.cantUpdatePlayer", name));
      return ChangePlayerStatus.NOT_SUPPORTED;
    }
    return ChangePlayerStatus.OK;
  }

  /**
   * Sets the password for the specified player.
   *
   * @param name the name of the player to change the password for.
   * @param password the password for the player.
   * @return {@link ChangePlayerStatus#OK} if successful, otherwise the reason for the failure.
   */
  public ChangePlayerStatus setPassword(String name, String password) {
    if (playerDatabase instanceof PersistedPlayerDatabase playerDb) {
      try {
        playerDb.setSharedPassword(name, password);
        return ChangePlayerStatus.OK;
      } catch (NoSuchAlgorithmException
          | InvalidKeySpecException
          | PasswordDatabaseException
          | NoSuchPaddingException
          | InvalidKeyException e) {
        log.error(e);
        MapTool.showError(I18N.getText("msg.error.playerDB.errorUpdatingPlayer", name), e);
        return ChangePlayerStatus.ERROR;
      }
    } else {
      log.error(I18N.getText("msg.error.playerDB.cantUpdatePlayer", name));
      return ChangePlayerStatus.NOT_SUPPORTED;
    }
  }

  /**
   * Sets the role for a player in the database. This will not change the role of a currently logged
   * in player, they will have to log out and log back in for it to take effect.
   *
   * @param name the name of the player.
   * @param role the role for the player.
   * @return {@link ChangePlayerStatus#OK} if successful, otherwise the reason for the failure.
   */
  public ChangePlayerStatus setRole(String name, Role role) {
    if (playerDatabase instanceof PersistedPlayerDatabase playerDb) {
      playerDb.setRole(name, role);
      return ChangePlayerStatus.OK;
    } else {
      log.error(I18N.getText("msg.error.playerDB.cantUpdatePlayer", name));
      return ChangePlayerStatus.NOT_SUPPORTED;
    }
  }

  /**
   * Blocks a player and sets the reason they are blocked. This will not remove a player from the
   * game, it will only stop them from logging in.
   *
   * @param name the name of the player to block.
   * @param reason the reason for the block.
   * @return {@link ChangePlayerStatus#OK} if successful, otherwise the reason for the failure.
   */
  public ChangePlayerStatus blockPlayer(String name, String reason) {
    if (playerDatabase instanceof PersistedPlayerDatabase playerDb) {
      playerDb.blockPlayer(name, reason);
      return ChangePlayerStatus.OK;
    } else {
      log.error(I18N.getText("msg.error.playerDB.cantUpdatePlayer", name));
      return ChangePlayerStatus.NOT_SUPPORTED;
    }
  }

  /**
   * Removes a block from a player.
   *
   * @param name the name of the player to unblock.
   * @return {@link ChangePlayerStatus#OK} if successful, otherwise the reason for the failure.
   */
  public ChangePlayerStatus unblockPlayer(String name) {
    if (playerDatabase instanceof PersistedPlayerDatabase playerDb) {
      playerDb.unblockPlayer(name);
      return ChangePlayerStatus.OK;
    } else {
      log.error(I18N.getText("msg.error.playerDB.cantUpdatePlayer", name));
      return ChangePlayerStatus.NOT_SUPPORTED;
    }
  }
}
