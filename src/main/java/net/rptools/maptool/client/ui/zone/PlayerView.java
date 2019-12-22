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
package net.rptools.maptool.client.ui.zone;

import java.util.List;
import net.rptools.maptool.model.Player;
import net.rptools.maptool.model.Token;

public class PlayerView {
  /** The role of the player (GM or PLAYER). */
  private final Player.Role role;
  /** Restrict the view to these tokens. Optional. */
  private final List<Token> tokens;

  // Optimization
  private final String hash;

  public PlayerView(Player.Role role) {
    this(role, null);
  }

  public PlayerView(Player.Role role, List<Token> tokens) {
    this.role = role;
    this.tokens = tokens != null && !tokens.isEmpty() ? tokens : null;
    hash = calculateHashcode();
  }

  public Player.Role getRole() {
    return role;
  }

  public boolean isGMView() {
    return role == Player.Role.GM;
  }

  public List<Token> getTokens() {
    return tokens;
  }

  /** @return true if the view is for some tokens only, false if the view is global */
  public boolean isUsingTokenView() {
    return tokens != null;
  }

  @Override
  public int hashCode() {
    return hash.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof PlayerView)) {
      return false;
    }
    PlayerView other = (PlayerView) obj;
    return hash.equals(other.hash);
  }

  private String calculateHashcode() {
    StringBuilder builder = new StringBuilder();
    builder.append(role);
    if (tokens != null) {
      for (Token token : tokens) {
        builder.append(token.getId());
      }
    }
    return builder.toString();
  }
}
