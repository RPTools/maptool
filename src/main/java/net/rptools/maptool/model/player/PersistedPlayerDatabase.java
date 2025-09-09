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
import java.util.Set;
import javax.crypto.NoSuchPaddingException;
import net.rptools.maptool.model.player.Player.Role;

/** Interface implemented by player databases that persist their information between runs. */
public interface PersistedPlayerDatabase extends PlayerDatabase, PlayerDBPropertyChange {

  /**
   * Disables the specified player. This will not boot the player from the server.
   *
   * @param player The name of the player to disable.
   * @param reason The reason that the player is blocked, this can be a key in i18n properties.
   * @throws PasswordDatabaseException If the password database does not support disabling players.
   */
  void disablePlayer(String player, String reason) throws PasswordDatabaseException;

  /**
   * Adds a new player to the database with a shared password. If the player already exists then a
   * {@link PasswordDatabaseException} will be thrown.
   *
   * @param name the name of the player.
   * @param role the role of the player.
   * @param password the shared password to set for the player.
   * @throws PasswordDatabaseException If there is an error adding the player.
   * @throws NoSuchAlgorithmException If there is an error hashing the password.
   * @throws InvalidKeySpecException If there is an error hashing the password.
   * @throws NoSuchPaddingException If there is an error hashing the password.
   * @throws InvalidKeyException If there is an error hashing the password.
   * @throws IllegalStateException If there is an error hashing the password.
   */
  void addPlayerSharedPassword(String name, Role role, String password)
      throws NoSuchAlgorithmException,
          InvalidKeySpecException,
          PasswordDatabaseException,
          NoSuchPaddingException,
          InvalidKeyException;

  /**
   * Adds a new player to the database with multiple public asymmetric key. If the player already
   * exists then a {@link PasswordDatabaseException} will be thrown.
   *
   * @param name the name of the player.
   * @param role the role of the player.
   * @param publicKeyStrings the shared encoded public key strings.
   * @throws PasswordDatabaseException If there is an error adding the player.
   * @throws NoSuchAlgorithmException If there is an error hashing the password.
   * @throws InvalidKeySpecException If there is an error hashing the password.
   * @throws NoSuchPaddingException If there is an error hashing the password.
   * @throws InvalidKeyException If there is an error hashing the password.
   * @throws IllegalStateException If there is an error hashing the password.
   */
  void addPlayerAsymmetricKey(String name, Role role, Set<String> publicKeyStrings)
      throws NoSuchAlgorithmException,
          InvalidKeySpecException,
          PasswordDatabaseException,
          NoSuchPaddingException,
          InvalidKeyException,
          InvalidAlgorithmParameterException;

  /**
   * Sets the shared password for the specified player. If the player does not exist then a {@link
   * PasswordDatabaseException} will be thrown. This will remove any asymmetric keys associated with
   * the player.
   *
   * @param name The name of the player to set the password for.
   * @param password the password to set.
   * @throws PasswordDatabaseException If the player does not exist.
   * @throws NoSuchAlgorithmException If there is an error hashing the password.
   * @throws InvalidKeySpecException If there is an error hashing the password.
   * @throws NoSuchPaddingException If there is an error hashing the password.
   * @throws InvalidKeyException If there is an error hashing the password.
   * @throws IllegalStateException If there is an error hashing the password.
   */
  void setSharedPassword(String name, String password)
      throws NoSuchPaddingException,
          NoSuchAlgorithmException,
          InvalidKeySpecException,
          PasswordDatabaseException,
          InvalidKeyException;

  /**
   * Sets the asymmetric keys for the player. This will remove any shared password keys for the
   * player if they exist. If the player is not found then {@link PasswordDatabaseException}
   *
   * @param name The name of the player to set the keys for.
   * @param keys The keys to set for the player.
   * @throws PasswordDatabaseException If the player does not exist.
   * @throws NoSuchAlgorithmException If there is an error hashing the password.
   * @throws InvalidKeySpecException If there is an error hashing the password.
   * @throws NoSuchPaddingException If there is an error hashing the password.
   * @throws InvalidKeyException If there is an error hashing the password.
   * @throws IllegalStateException If there is an error hashing the password.
   */
  void setAsymmetricKeys(String name, Set<String> keys)
      throws NoSuchPaddingException,
          NoSuchAlgorithmException,
          InvalidKeySpecException,
          PasswordDatabaseException,
          InvalidKeyException,
          InvalidAlgorithmParameterException;

  /**
   * Adds the keys to the existing keys for the specified player. This will remove any shared key
   * for the player if one exists. If the player does not exist then {@link
   * PasswordDatabaseException} will be thrown.
   *
   * @param name The name of the player to add the keys for.
   * @param keys The keys to add to the player.
   * @throws PasswordDatabaseException If the player does not exist.
   * @throws NoSuchAlgorithmException If there is an error hashing the password.
   * @throws InvalidKeySpecException If there is an error hashing the password.
   * @throws NoSuchPaddingException If there is an error hashing the password.
   * @throws InvalidKeyException If there is an error hashing the password.
   * @throws IllegalStateException If there is an error hashing the password.
   */
  void addAsymmetricKeys(String name, Set<String> keys)
      throws NoSuchPaddingException,
          NoSuchAlgorithmException,
          InvalidKeySpecException,
          PasswordDatabaseException,
          InvalidKeyException,
          InvalidAlgorithmParameterException;

  /**
   * Returns if the specified player is persisted or not. Persisted players include those that are
   * not yet committed but will be persisted when committed.
   *
   * @param name the name of the player to check.
   * @return if the player is/will be persisted.
   */
  boolean isPersisted(String name);

  /**
   * Deletes a player from the database.
   *
   * @param name the name of the player to delete.
   */
  void deletePlayer(String name);

  /**
   * Sets the status of the player to blocked.
   *
   * @param name the name of the player to block.
   * @param reason the reason that the player is blocked.
   */
  void blockPlayer(String name, String reason);

  /**
   * Removes the blocked status from the player.
   *
   * @param name the name of the player to removed te blocked status from.
   */
  void unblockPlayer(String name);

  /**
   * Sets the role for the player.
   *
   * @param name the name of the player to set the role for.
   * @param role the role to set.
   */
  void setRole(String name, Role role);

  /**
   * Commits the pending changes writing them out to the persistent storage.
   *
   * @throws NoSuchPaddingException if there is an error hashing the password.
   * @throws NoSuchAlgorithmException if there is an error hashing the password.
   * @throws InvalidKeySpecException if there is an error hashing the password.
   * @throws PasswordDatabaseException if there is an error adding the player to the file.
   * @throws InvalidKeyException if there is an error hashing the password.
   */
  void commitChanges()
      throws NoSuchPaddingException,
          NoSuchAlgorithmException,
          InvalidKeySpecException,
          PasswordDatabaseException,
          InvalidKeyException;

  /**
   * Rolls back any pending changes that haven't been written to the file.
   *
   * @throws NoSuchPaddingException if there is an error hashing the password.
   * @throws NoSuchAlgorithmException if there is an error hashing the password.
   * @throws InvalidKeySpecException if there is an error hashing the password.
   * @throws PasswordDatabaseException if there is an error adding the player to the file.
   * @throws InvalidKeyException i
   */
  void rollbackChanges()
      throws NoSuchPaddingException,
          NoSuchAlgorithmException,
          InvalidKeySpecException,
          PasswordDatabaseException,
          InvalidKeyException;
}
