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
import java.util.function.BiFunction;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RPGGateKeeper extends GateKeeper {
  private static final Logger log = LogManager.getLogger(RPGGateKeeper.class);

  /**
   * Simplistic check for alliance based on GM vs. players where all players on the same side. All
   * PCs and player-owned NPCs are on the same side. Objects tested must be a player and a token.
   */
  protected BiFunction<Object, Object, Boolean> objectAllyTest;

  public RPGGateKeeper() {
    super();
    setObjectAllyTest(
        (Object token_, Object player_) -> {
          Token token;
          Player player;

          // make sure we have a token and a player
          if (token_ instanceof Token t && player_ instanceof Player p) {
            token = t;
            player = p;
          } else if (player_ instanceof Token t && token_ instanceof Player p) {
            token = t;
            player = p;
          } else {
            return false;
          }

          if (!player.isGM() && token.getType().equals(Token.Type.PC)) {
            // All players and PCs are on the same side
            return true;
          } else {
            // For a GM, all GMs; for a player, all players
            List<Player> teamMates =
                MapTool.getPlayerList().stream()
                    .dropWhile(pl -> pl.isGM() != player.isGM())
                    .toList();
            final Set<String> owners = token.getOwners();
            // test token is owned by a teammate
            return !teamMates.stream()
                .filter(pl -> owners.contains(pl.getName()))
                .toList()
                .isEmpty();
          }
        });
    setAccessorTest(
        (Permissions permission, Object player) ->
            // for RPG the only real test is for GM status
            switch (permission) {
              case ALL -> true;
              case GM -> player instanceof Player p && p.isGM();
              default -> false;
            });
    setObjectAccessTest(
        (Permissions permission, Object player, Object token) -> {
          if (permission.equals(Permissions.ALL)) {
            return true;
          } else if (permission.equals(Permissions.NONE) || player == null || token == null) {
            return false;
          }

          if (player instanceof Player p && token instanceof Token t) {
            // need to determine if player, is GM, Owner, Friend, or Opponent
            AccessorType accessorType;
            if (p.isGM()) {
              accessorType = AccessorType.GM;
            } else if (t.isOwner(p.getName())) {
              accessorType = AccessorType.OWNER;
            } else if (objectAllyTest.apply(t, p)) {
              accessorType = AccessorType.ALLY;
            } else {
              accessorType = AccessorType.OPPONENT;
            }
            // See if they are on the list
            return accessorType.accessAllowed(permission);
          }

          log.warn("Bad arguments somewhere this should not be reached");
          return false;
        });
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
