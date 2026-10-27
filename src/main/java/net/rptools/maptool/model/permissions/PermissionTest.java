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

import static net.rptools.maptool.model.permissions.PermissionTest.Mode.*;
import static net.rptools.maptool.model.permissions.Permissions.*;

import com.google.common.eventbus.Subscribe;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.events.PlayerConnected;
import net.rptools.maptool.client.events.PlayerDisconnected;
import net.rptools.maptool.events.MapToolEventBus;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;

public class PermissionTest {
  private static final Logger log = LogManager.getLogger(PermissionTest.class);

  public enum Mode {
    SIMPLE,
    GM_VS_PLAYERS,
    TEAMS
  }

  protected static Mode mode = GM_VS_PLAYERS;
  private static final List<Player> playerList;

  static {
    playerList = MapTool.getPlayerList();
    MapToolEventBus.get().register(new PermissionTest());
  }

  public static boolean hasPermission(
      @NonNull Permissions permission,
      @Nullable Object accessor,
      @Nullable Object restrictedObject) {
    if (permission.equals(NONE) || accessor == null || restrictedObject == null) {
      return false;
    }
    if (permission.equals(ALL)) {
      return true;
    }
    switch (mode) {
      case SIMPLE -> {
        if (accessor instanceof Player p) {
          if (restrictedObject instanceof Token t) {
            return simplePlayerAccessTest(permission, p, t.isOwner(p.getName()));
          }
          return simplePlayerAccessTest(permission, p, false);
        }
      }
      case GM_VS_PLAYERS, TEAMS -> {
        if (accessor instanceof Player p && restrictedObject instanceof Token t) {
          return gmVsPlayersTokenAccessTest(permission, p, t);
        }
      }
    }
    log.warn("Bad arguments somewhere this should not be reached");
    return false;
  }

  public static boolean simplePlayerAccessTest(
      @NonNull Permissions permission, Player player, Boolean isOwner) {
    return switch (permission) {
      case ALL -> true;
      case NONE, ALLIED, ALLIED_ONLY -> false;
      case GM -> player.isGM();
      case OWNER, OWNER_ONLY, OWNER_ALLIED_ONLY -> player.isGM() || isOwner;
      case OPPONENT_ONLY -> !player.isGM() && !isOwner;
    };
  }

  public static boolean gmVsPlayersTokenAccessTest(
      @NonNull Permissions permission, Player player, Token token) {
    return switch (permission) {
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
  private static boolean isAllied(Token token, Player player) {
    if (!player.isGM() && token.getType().equals(Token.Type.PC)) {
      return true;
    } else {
      List<Player> teamMates =
          MapTool.getPlayerList().stream().dropWhile(p -> p.isGM() != player.isGM()).toList();
      final Set<String> owners = token.getOwners();
      return !teamMates.stream().filter(p -> owners.contains(p.getName())).toList().isEmpty();
    }
  }

  public static Mode getMode() {
    return mode;
  }

  public static void setMode(Mode mode) {
    PermissionTest.mode = mode;
  }

  @Subscribe
  void playerAdded(PlayerConnected event) {
    PermissionTest.playerList.add(event.player());
  }

  @Subscribe
  void playerRemoved(PlayerDisconnected event) {
    PermissionTest.playerList.remove(event.player());
  }
}
