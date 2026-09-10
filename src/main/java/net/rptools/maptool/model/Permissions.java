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
package net.rptools.maptool.model;

import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.player.Player;

/**
 *
 *
 * <h4>Defined Player permissions</h4>
 *
 * <li>Accommodates ordinary RPG use for GM vs Players
 * <li>Adds a level {@link #ALLIED} where friendlies have permission as a step before {@link #ALL}
 * <li>This opens the door for Teams of Players
 * <li>Introduces exclusive permissions beyond GM Only
 *
 *     <p>Used in {@link TokenProperty} for determining who can see a property on a pop-up attribute
 *     sheet, and whether a property appears in the token property editor.
 */
public enum Permissions {
  // Order follows increasing visibility for accumulated permissions

  /** Nobody has permission */
  NONE("permission.displayName.none"),
  /** Only the GMs have permission */
  GM("permission.displayName.gm"),
  /** The GMs and the Owners have permission */
  OWNER("permission.displayName.owner"),
  /** The GMs, Owners, and the Owners' Allies have permission */
  ALLIED("permission.displayName.allies"),
  /** Everyone has permission */
  ALL("permission.displayName.all"),

  // Exclusive permissions, i.e. Can exclude the GM, etc.
  /** Only the Owners have permission */
  OWNER_ONLY("permission.displayName.owner.discrete"),
  /** Only the Owners' Allies have permission */
  ALLIED_ONLY("permission.displayName.allies.discrete"),
  /** Only the Owners and the Owners' Allies have permission */
  OWNER_ALLIED_ONLY("permission.displayName.allies.discrete"),
  /** Only the Owners' Opponents have permission */
  OPPONENT_ONLY("permission.displayName.opponent");

  final String displayName;

  Permissions(String i18nKey) {
    this.displayName = I18N.getText(i18nKey);
  }

  public boolean hasPermission(@Nullable Player player, @Nullable Token token) {
    if (player == null || token == null) {
      return this.equals(ALL);
    }
    return switch (this) {
      case ALL -> true;
      case NONE -> false;
      case GM -> player.isGM();
      case OWNER -> player.isGM() || token.isOwner(player.getName());
      case ALLIED -> player.isGM() || token.isOwner(player.getName()) || isAllied(token, player);
      case OWNER_ONLY -> token.isOwner(player.getName());
      case ALLIED_ONLY -> isAllied(token, player);
      case OWNER_ALLIED_ONLY -> token.isOwner(player.getName()) || isAllied(token, player);
      case OPPONENT_ONLY -> !token.isOwner(player.getName()) && !isAllied(token, player);
    };
  }

  /**
   * Simplistic check for alliance based on GM vs. players where all players on the same side. All
   * PCs and player-owned NPCs are on the same side.
   *
   * @param token to check is ally
   * @param player whose side we are checking for
   * @return if token belongs to the same side
   */
  private boolean isAllied(Token token, Player player) {
    if (!player.isGM() && token.getType().equals(Token.Type.PC)) {
      return true;
    } else {
      List<Player> teamMates =
          MapTool.getPlayerList().stream().dropWhile(p -> p.isGM() != player.isGM()).toList();
      final Set<String> owners = token.getOwners();
      return !teamMates.stream().filter(p -> owners.contains(p.getName())).toList().isEmpty();
    }
  }

  @Override
  public String toString() {
    return displayName;
  }
}
