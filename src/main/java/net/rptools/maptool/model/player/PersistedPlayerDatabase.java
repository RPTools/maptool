package net.rptools.maptool.model.player;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Set;
import javax.crypto.NoSuchPaddingException;
import net.rptools.maptool.model.player.Player.Role;

/**
 * Interface implemented by player databases that persist their information between runs.
 */
public interface PersistedPlayerDatabase {

  /**
   * Disables the specified player. This will not boot the player from the server.
   *
   * @param player The name of the player to disable.
   * @param reason The reason that the player is disabled, this can be a key in i18n properties.
   * @throws PasswordDatabaseException If the password database does not support disabling players.
   */
  void disablePlayer(String player, String reason) throws PasswordDatabaseException;

  /**
   * Adds a new player to the database with a shared password.
   * If the player already exists then a {@link PasswordDatabaseException} will be thrown.
   *
   * @param name the name of the player.
   * @param role the role of the player.
   * @param password the shared password to set for the player.
   *
   * @throws PasswordDatabaseException If there is an error adding the player.
   * @throws NoSuchAlgorithmException If there is an error hashing the password.
   * @throws InvalidKeySpecException If there is an error hashing the password.
   * @throws NoSuchPaddingException If there is an error hashing the password.
   * @throws InvalidKeyException If there is an error hashing the password.
   * @throws IllegalStateException  If there is an error hashing the password.
   */
  void addPlayerSharedPassword(String name, Role role, String password)
      throws NoSuchAlgorithmException, InvalidKeySpecException, PasswordDatabaseException,
      NoSuchPaddingException, InvalidKeyException;

  /**
   * Adds a new player to the database with multiple public asymmetric key.
   * If the player already exists then a {@link PasswordDatabaseException} will be thrown.
   *
   * @param name the name of the player.
   * @param role the role of the player.
   * @param publicKeyStrings the shared encoded public key strings.
   *
   * @throws PasswordDatabaseException If there is an error adding the player.
   * @throws NoSuchAlgorithmException If there is an error hashing the password.
   * @throws InvalidKeySpecException If there is an error hashing the password.
   * @throws NoSuchPaddingException If there is an error hashing the password.
   * @throws InvalidKeyException If there is an error hashing the password.
   * @throws IllegalStateException  If there is an error hashing the password.
   */
  void addPlayerAsymmetricKey(String name, Role role, Set<String> publicKeyStrings)
      throws NoSuchAlgorithmException, InvalidKeySpecException, PasswordDatabaseException,
      NoSuchPaddingException, InvalidKeyException;

  /**
   * Sets the shared password for the specified player. If the player does not exist then a
   *  {@link PasswordDatabaseException} will be thrown. This will remove any asymmetric keys
   *  associated with the player.
   *
   * @param name The name of the player to set the password for.
   * @param password the password to set.
   *
   * @throws PasswordDatabaseException If the player does not exist.
   * @throws NoSuchAlgorithmException If there is an error hashing the password.
   * @throws InvalidKeySpecException If there is an error hashing the password.
   * @throws NoSuchPaddingException If there is an error hashing the password.
   * @throws InvalidKeyException If there is an error hashing the password.
   * @throws IllegalStateException  If there is an error hashing the password.
   */
  void setSharedPassword(String name, String password)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException,
      PasswordDatabaseException, InvalidKeyException;


  /**
   * Sets the asymmetric keys for the player. This will remove any shared password keys for
   * the player if they exist. If the player is not found then {@link PasswordDatabaseException}
   *
   *
   * @param name The name of the player to set the keys for.
   * @param keys The keys to set for the player.
   *
   * @throws PasswordDatabaseException If the player does not exist.
   * @throws NoSuchAlgorithmException If there is an error hashing the password.
   * @throws InvalidKeySpecException If there is an error hashing the password.
   * @throws NoSuchPaddingException If there is an error hashing the password.
   * @throws InvalidKeyException If there is an error hashing the password.
   * @throws IllegalStateException  If there is an error hashing the password.
   */
  void setAsymmetricKeys(String name, Set<String> keys)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException,
      PasswordDatabaseException, InvalidKeyException;

  /**
   * Adds the keys to the existing keys for the specified player. This will remove any shared key
   * for the player if one exists. If the player does not exist then {@link PasswordDatabaseException}
   * will be thrown.
   *
   * @param name The name of the player to add the keys for.
   * @param keys The keys to add to the player.
   *
   * @throws PasswordDatabaseException If the player does not exist.
   * @throws NoSuchAlgorithmException If there is an error hashing the password.
   * @throws InvalidKeySpecException If there is an error hashing the password.
   * @throws NoSuchPaddingException If there is an error hashing the password.
   * @throws InvalidKeyException If there is an error hashing the password.
   * @throws IllegalStateException  If there is an error hashing the password.
   */
  void addAsymmetricKeys(String name, Set<String> keys)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException,
      PasswordDatabaseException, InvalidKeyException;

}
