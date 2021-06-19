package net.rptools.maptool.model.player;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;
import net.rptools.maptool.model.player.Player.Role;
import net.rptools.maptool.util.CipherUtil.Key;

/**
 * This class provides the implementation for the "database" for the client local player.
 */
public class LocalPlayerDatabase implements PlayerDatabase {


  private final LocalPlayer localPlayer;


  public LocalPlayerDatabase(LocalPlayer player) {
    localPlayer = player;
  }

  @Override
  public boolean playerExists(String playerName) {
    return localPlayer.getName().equals(playerName);
  }

  @Override
  public Player getPlayer(String playerName) {
    if (playerExists(playerName)) {
      return localPlayer;
    }
    return null;
  }

  @Override
  public Optional<Key> getPlayerPassword(String playerName) {
    return Optional.of(localPlayer.getPassword());
  }

  @Override
  public byte[] getPlayerPasswordSalt(String playerName) {
    return localPlayer.getPassword().salt();
  }

  @Override
  public Player getPlayerWithRole(String playerName, Role role)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    if (playerExists(playerName)) {
      localPlayer.setRole(role);
      return localPlayer;
    }
    return null;
  }

  @Override
  public Optional<Key> getRolePassword(Role role) {
    return Optional.empty();
  }
}
