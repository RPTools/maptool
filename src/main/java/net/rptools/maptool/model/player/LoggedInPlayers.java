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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Class used for tracking the logged in players. */
class LoggedInPlayers {

  /** Concurrent set of logged in players. */
  private final Set<Player> players = ConcurrentHashMap.newKeySet();

  /**
   * Records a player as logged in.
   *
   * @param player the player that logged in.
   */
  public void playerSignedIn(Player player) {
    players.add(player);
  }

  /**
   * Records a player as logged out.
   *
   * @param player the player that logged out.
   */
  public void playerSignedOut(Player player) {
    players.remove(player);
  }

  /**
   * Returns the players that are logged in.
   *
   * @return the players that are logged in.
   */
  public Set<Player> getPlayers() {
    return new HashSet<>(players);
  }

  /**
   * Returns if the player is logged in or not.
   *
   * @param name the name of the player to check for.
   * @return if the player is logged in or not.
   */
  public boolean isLoggedIn(String name) {
    return players.stream().anyMatch(p -> p.getName().equals(name));
  }
}
