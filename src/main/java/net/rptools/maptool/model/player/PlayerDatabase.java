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
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.util.cipher.CipherUtil;

/**
 * This interface is implemented by all classes that provide information about which players are
 * able to join.
 */
public interface PlayerDatabase {

  /** The type of authentication for the player. */
  enum AuthMethod {
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
   * Returns if this player database supports disabling players.
   *
   * @return {@code true} if this player database supports disabling players.
   */
  boolean supportsDisabling();

  /**
   * Returns {@code true} if the database supports asymmetric keys for authentication.
   *
   * @return {@code true} if the database supports asymmetric keys for authentication.
   */
  boolean supportsAsymmetricalKeys();

  /**
   * Returns if this player database supports role based passwords.
   *
   * @return {@code true} if this player database supports role based passwords.
   */
  boolean supportsRolePasswords();

  /**
   * Returns if the player has been disabled.
   *
   * @param player {@code true} the player to check if they have been disabled.
   * @return {@code true} if the player has been disabled.
   */
  boolean isBlocked(Player player);

  /**
   * Returns the reason tha the player has been disabled, if the player has not been disabled then
   * an empty string is returned.
   *
   * @param player the player to get the disabled reason for.
   * @return the reason that the player has been disabled, or empty string if they have not.
   */
  String getBlockedReason(Player player);

  /**
   * Returns the known players. For many player databases this will be the players that are
   * currently connected.
   *
   * @return The players that are known to the database.
   */
  default Set<Player> getAllPlayers() {
    return getOnlinePlayers();
  }

  /**
   * Returns all the players currently connected.
   *
   * @return The players that are currently connected.
   */
  Set<Player> getOnlinePlayers();

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
  CompletableFuture<CipherUtil.Key> getPublicKey(Player player, MD5Key md5key)
      throws ExecutionException, InterruptedException;

  /**
   * Returns the {@link String} encoding of the public keys for a player.
   *
   * @param name The name of the player to return the public keys of.
   * @return the {@link String} encoding of the public keys for a player.
   */
  Set<String> getEncodedPublicKeys(String name);

  /**
   * Returns if the player has the specified public key.
   *
   * @param player The player to check if they have the specified public key.
   * @param md5key The {@link MD5Key} of the public key.
   * @return {@code true} if the player has the specified public key.
   */
  CompletableFuture<Boolean> hasPublicKey(Player player, MD5Key md5key);

  /**
   * Checks to see if the player is defined in the database, unlike {@link #playerExists(String)}
   * this will not return {@code true} for every input but only for those that are actually known.
   *
   * @param name the name of the player to check for.
   * @return {@code true} if the player is registered, {@code false} otherwise.
   */
  boolean isPlayerRegistered(String name) throws InterruptedException, InvocationTargetException;

  /**
   * Inform the database that the player has signed in.
   *
   * @param player the player that has signed in.
   */
  void playerSignedIn(Player player);

  /**
   * Inform the database that the player has signed out.
   *
   * @param player the player that has signed out.
   */
  void playerSignedOut(Player player);

  /**
   * Returns if a player is connected or not.
   *
   * @param name the player to check.
   * @return if a player is connected or not.
   */
  boolean isPlayerConnected(String name);
}
