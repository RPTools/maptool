package net.rptools.maptool.model.player;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import net.rptools.maptool.util.CipherUtil;

import java.util.Optional;

/**
 * This interface is implemented by all classes that provide information about which players are able to join.
 */
public interface PlayerDatabase {

  /**
   * Returns {@code true} if a player with the given name is known.
   *
   * @param playerName
   * @return
   * @note if the "database" allows any player name to connect (e.g. default) then this will always
   *       return true.
   */
  boolean playerExists(String playerName);

  /**
   * Returns the player information from the database.
   * @param playerName the name of the player to retrieve.
   * @return the player information.
   */
  Player getPlayer(String playerName) throws NoSuchAlgorithmException, InvalidKeySpecException;

  /**
   * Returns the {@link CipherUtil.Key} for the player. If the database only supports role based passwords
   * the returned value will be empty.
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
}
