package net.rptools.maptool.model.player;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class used for tracking the logged in players.
 */
class LoggedInPlayers {

  /** Concurrent set of logged in players. */
  private final Set<Player> players = ConcurrentHashMap.newKeySet();

  /**
   * Records a player as logged in.
   * @param player the player that logged in.
   */
  public void playerSignedIn(Player player) {
    players.add(player);
  }

  /**
   * Records a player as logged out.
   * @param player the player that logged out.
   */
  public void playerSignedOut(Player player) {
    players.remove(player);
  }

  /**
   * Returns the players that are logged in.
   * @return the players that are logged in.
   */
  public Set<Player> getPlayers() {
    return new HashSet<>(players);
  }

  /**
   * Returns if the player is logged in or not.
   * @param name the name of the player to check for.
   * @return if the player is logged in or not.
   */
  public boolean isLoggedIn(String name) {
    return players.stream().anyMatch(p -> p.getName().equals(name));
  }
}
