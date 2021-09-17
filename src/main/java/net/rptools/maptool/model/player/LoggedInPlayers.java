package net.rptools.maptool.model.player;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LoggedInPlayers {

  private final Set<Player> players = new ConcurrentHashMap<Player, Boolean>().keySet();

  public void playerSignedIn(Player player) {
    players.add(player);
  }

  public void playerSignedOut(Player player) {
    players.remove(player);
  }

  public Set<Player> getPlayers() {
    return new HashSet<>(players);
  }


}
