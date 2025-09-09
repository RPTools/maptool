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
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.player.Player;

public class PlayerView {
  /** The role of the player (GM or PLAYER). */
  private final Player.Role role;

  /** Restrict the view to these tokens. Optional. */
  private final List<Token> tokens;

  // Optimization
  private final String hash;

  /**
   * Creates a player view that does not use token views.
   *
   * <p>Calling `isUsingTokenView()` on the new player view will return {@code false} and {@link
   * #getTokens()} should not be called.
   *
   * @param role The player role for the view.
   */
  public PlayerView(Player.Role role) {
    this.role = role;
    this.tokens = null;
    hash = calculateHashcode();
  }

  /**
   * Creates a player view for a token view.
   *
   * <p>Calling `isUsingTokenView()` on the new player view will return {@code false} and {@link
   * #getTokens()} can be called to retrieve the list of tokens.
   *
   * @param role The player role for the view.
   */
  public PlayerView(Player.Role role, List<Token> tokens) {
    this.role = role;
    this.tokens = tokens;
    hash = calculateHashcode();
  }

  public Player.Role getRole() {
    return role;
  }

  public boolean isGMView() {
    return role == Player.Role.GM;
  }

  /**
   * Gets the tokens for this view.
   *
   * <p>This method should only be used when {@link #isUsingTokenView()} returns {@code true}.
   *
   * @return The tokens for this view.
   */
  public List<Token> getTokens() {
    return tokens;
  }

  /**
   * @return true if the view is for some tokens only, false if the view is global
   */
  public boolean isUsingTokenView() {
    return tokens != null;
  }

  @Override
  public int hashCode() {
    return hash.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof PlayerView)) {
      return false;
    }
    PlayerView other = (PlayerView) obj;
    return hash.equals(other.hash);
  }

  private String calculateHashcode() {
    StringBuilder builder = new StringBuilder();
    builder.append(role);
    if (tokens != null) {
      builder.append('|'); // Distinguishes null and empty case.
      for (Token token : tokens) {
        builder.append(token.getId());
      }
    }
    return builder.toString();
  }
}
