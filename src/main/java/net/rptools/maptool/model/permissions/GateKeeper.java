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
 * <https://www.gnu.org/licenses/> and specifically the Affero license
 * text at <https://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.model.permissions;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.player.Player;

public abstract class GateKeeper {
  public GateKeeper() {}

  /** Does an accessing object match the required permission */
  public abstract boolean testAccess(Permissions permission, Object accessor);

  /** Does an accessor have permission to access the target */
  public abstract boolean testAccess(Permissions permission, Object accessor, Object target);

  /** Are two objects allied */
  public abstract boolean testAllied(Object first, Object second);

  public static class RPGInstance extends GateKeeper {

    /** Does an accessing object match the required permission @param permission */
    @Override
    public boolean testAccess(Permissions permission, Object accessor) {
      // for RPG the only real test is for a Player's GM status
      return switch (permission) {
        case ALL -> true;
        case GM -> accessor instanceof Player p && p.isGM();
        default -> false;
      };
    }

    /** Does an accessor have permission to access the target @param permission */
    @Override
    public boolean testAccess(Permissions permission, Object accessor, Object target) {
      if (permission.equals(Permissions.ALL)) {
        return true;
      } else if (permission.equals(Permissions.NONE) || accessor == null || target == null) {
        return false;
      } else if (accessor instanceof Player player && target instanceof Token token) {
        return testAccess(permission, token, player);
      } else if (accessor instanceof Token token && target instanceof Player player) {
        return testAccess(permission, token, player);
      } else {
        return false;
      }
    }

    private boolean testAccess(Permissions permission, Token token, Player player) {
      // need to determine if player, is GM, Owner, Friend, or Opponent
      AccessorType accessorType;
      if (player.isGM()) {
        accessorType = AccessorType.GM;
      } else if (token.isOwner(player.getName())) {
        accessorType = AccessorType.OWNER;
      } else if (testAllied(token, player)) {
        accessorType = AccessorType.ALLY;
      } else {
        accessorType = AccessorType.OPPONENT;
      }
      // See if they are on the list
      return accessorType.accessAllowed(permission);
    }

    /** Are two objects allied @param first */
    @Override
    public boolean testAllied(Object first, Object second) {
      if (first == null || second == null) {
        return false;
      } else if (first instanceof Player p1 && second instanceof Player p2) {
        return p1.isGM() == p2.isGM();
      } else if (first instanceof Player player && second instanceof Token token) {
        return testAllied(token, player);
      } else if (first instanceof Token token && second instanceof Player player) {
        return testAllied(token, player);
      }
      return false;
    }

    private boolean testAllied(Token token, Player player) {
      if (!player.isGM() && token.getType().equals(Token.Type.PC)) {
        // All players and PCs are on the same side
        return true;
      } else {
        // For a GM, all GMs; for a player, all players
        List<Player> teamMates =
            MapTool.getPlayerList().stream().dropWhile(pl -> pl.isGM() != player.isGM()).toList();
        final Set<String> owners = token.getOwners();
        // test token is owned by a teammate
        return !teamMates.stream().filter(pl -> owners.contains(pl.getName())).toList().isEmpty();
      }
    }

    /**
     *
     *
     * <h4>Just a convenient holder for allowable permissions lookup</h4>
     */
    private enum AccessorType {
      GM(EnumSet.of(Permissions.GM, Permissions.OWNER, Permissions.ALLIED, Permissions.ALL)),
      OWNER(
          EnumSet.of(
              Permissions.OWNER,
              Permissions.ALL,
              Permissions.OWNER_ONLY,
              Permissions.OWNER_ALLIED_ONLY)),
      ALLY(
          EnumSet.of(
              Permissions.ALLIED,
              Permissions.ALL,
              Permissions.ALLIED_ONLY,
              Permissions.OWNER_ALLIED_ONLY)),
      OPPONENT(EnumSet.of(Permissions.ALL, Permissions.OPPONENT_ONLY));

      final EnumSet<Permissions> access;

      AccessorType(EnumSet<Permissions> access) {
        this.access = access;
      }

      boolean accessAllowed(Permissions permission) {
        return access.contains(permission);
      }
    }
  }
}
