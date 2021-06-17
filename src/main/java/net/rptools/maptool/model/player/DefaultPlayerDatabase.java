package net.rptools.maptool.model.player;

import net.rptools.maptool.util.CipherUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This class provides the implementation for the default player database, where any one can connect as long as they
 * know the role password. This follows the standard behaviour for 1.9 and earlier.
 */
public class DefaultPlayerDatabase implements PlayerDatabase {

  private final Map<Player.Role, CipherUtil.Key> rolePasswordMap = new HashMap<>();

  DefaultPlayerDatabase() {

  }

  @Override
  public boolean playerExists(String playerName) {
    return true; // The player will always "exist" in the database as any player name is possible.
  }

  @Override
  public Player getPlayer(String playerName) {
    // If role is not specified always return player!
    return new Player(playerName, Player.Role.PLAYER, rolePasswordMap.get(Player.Role.PLAYER));
  }

  @Override
  public Optional<CipherUtil.Key> getPlayerPassword(String playerName) {
    return Optional.empty(); // Only supports role based passwords.
  }

  @Override
  public Player getPlayerWithRole(String playerName, Player.Role role) {
    return new Player(playerName, role, rolePasswordMap.get(role));
  }

  @Override
  public Optional<CipherUtil.Key> getRolePassword(Player.Role role) {
    return Optional.of(rolePasswordMap.get(role));
  }
}
