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

import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingUtilities;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.util.cipher.CipherUtil;

/**
 * This interface is implemented by all classes that provide information about which players are
 * able to join.
 */
public interface PlayerDatabase {

  public enum AuthMethod {
    PASSWORD,
    ASYMMETRIC_KEY
  };

  /**
   * Returns {@code true} if a player with the given name is known.
   *
   * @param playerName the name of the player to get the information about from the database
   * @return {@code true} if the player exists.
   * @note if the "database" allows any player name to connect (e.g. default) then this will always
   *     return true. If you want to check if there is actually a player of this name in the
   *     database then use {@link #isPlayerRegistered(String)}.
   */
  boolean playerExists(String playerName);

  /**
   * Returns the player information from the database.
   *
   * @param playerName the name of the player to retrieve.
   * @return the player information.
   */
  Player getPlayer(String playerName) throws NoSuchAlgorithmException, InvalidKeySpecException;

  /**
   * Returns the {@link CipherUtil.Key} for the player. If the database only supports role based
   * passwords the returned value will be empty.
   *
   * @param playerName The name of the player to check.
   * @return the {@link CipherUtil.Key} to use.
   */
  Optional<CipherUtil.Key> getPlayerPassword(String playerName);

  /**
   * Returns the salt used for the player's password.
   *
   * @param playerName the name of the player to get the password salt for.
   * @return the salt used for the password.
   */
  byte[] getPlayerPasswordSalt(String playerName);

  /**
   * Returns the player overriding the role in the database with the specified role.
   *
   * @param playerName The name of the player to retrieve.
   * @param role The role for the player.
   * @return The player.
   */
  Player getPlayerWithRole(String playerName, Player.Role role)
      throws NoSuchAlgorithmException, InvalidKeySpecException;

  /**
   * Returns the password required for the role. If role authentication is not supported this will
   * be empty.
   *
   * @param role The role to retrieve the password for.
   * @return The password for the role.
   */
  Optional<CipherUtil.Key> getRolePassword(Player.Role role);

  /**
   * Returns if this player database supports disabling players.
   *
   * @return {@code true} if this player database supports disabling players.
   */
  boolean supportsDisabling();

  boolean supportsAsymmetricalKeys();

  /**
   * Returns if this player database supports role based passwords.
   *
   * @return {@code true} if this player database supports role based passwords.
   */
  boolean supportsRolePasswords();

  /**
   * Disables the specified player. This will not boot the player from the server.
   *
   * @param player The player to disable.
   * @param reason The reason that the player is disabled, this can be a key in i18n properties.
   * @throws PasswordDatabaseException If the password database does not support disabling players.
   */
  void disablePlayer(Player player, String reason) throws PasswordDatabaseException;

  /**
   * Returns if the player has been disabled.
   *
   * @param player {@code true} the player to check if they have been disabled.
   * @return {@code true} if the player has been disabled.
   */
  boolean isDisabled(Player player);

  /**
   * Returns the reason tha the player has been disabled, if the player has not been disabled then
   * an empty string is returned.
   *
   * @param player the player to get the disabled reason for.
   * @return the reason that the player has been disabled, or empty string if they have not.
   */
  String getDisabledReason(Player player);

  /**
   * Returns the known players. For many player databases this will be the players that are
   * currently connected.
   *
   * @return The players that are known to the database.
   */
  default Set<Player> getAllPlayers() throws InterruptedException, InvocationTargetException {
    return getOnlinePlayers();
  }

  /**
   * Returns all the players currently connected.
   *
   * @return The players that are currently connected.
   */
  default Set<Player> getOnlinePlayers() throws InterruptedException, InvocationTargetException {
    Set<Player> players = new HashSet<>();
    if (SwingUtilities.isEventDispatchThread()) {
      MapTool.getPlayerList().forEach(players::add);
    } else {
      SwingUtilities.invokeAndWait(() -> MapTool.getPlayerList().forEach(players::add));
    }

    return players;
  }

  /**
   * Adds a player to the database if the
   *
   * @param player
   */
  // void addPlayer(Player player);

  /**
   * Returns the authentication method for the player.
   *
   * @param player the player to get the authentication method for.
   * @return the authentication method for the player.
   */
  AuthMethod getAuthMethod(Player player);

  /**
   * Returns the public key for a player that matches the MD5Key specified. The MD5Key is generated
   * based on the text representation of the public key as returned by * {@link
   * CipherUtil#getEncodedPublicKeyText()}
   *
   * @param player The player to get the public key for.
   * @param md5key The {@link MD5Key} of the public key.
   * @return A {@link CompletableFuture} which returns a {@link CipherUtil} that can be used to
   *     decode with * the public key, the {@code CompletableFuture} can return {@code null} if
   *     there is no public key.
   */
  CompletableFuture<CipherUtil> getPublicKey(Player player, MD5Key md5key)
      throws ExecutionException, InterruptedException;

  /**
   * Checks to see if the player is defined in the database, unlike {@link #playerExists(String)}
   * this will not return {@code true} for every input but only for those that are logged on.
   *
   * @param name the name of the player to check for.
   * @return {@code true} if the player is registered, {@code false} otherwise.
   */
  boolean isPlayerRegistered(String name) throws InterruptedException, InvocationTargetException;
}
