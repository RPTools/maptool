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
package net.rptools.maptool.api.player;

import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.rptools.maptool.api.ApiException;
import net.rptools.maptool.api.util.ApiCall;
import net.rptools.maptool.api.util.ApiListResult;
import net.rptools.maptool.api.util.ApiResult;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.player.Player;
import net.rptools.maptool.model.player.Player.Role;
import net.rptools.maptool.model.player.PlayerDatabase;
import net.rptools.maptool.model.player.PlayerDatabaseFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PlayerApi {

  private static final Logger log = LogManager.getLogger(PlayerApi.class);

  public CompletableFuture<ApiResult<PlayerInfo>> getPlayer(String name) {
    return new ApiCall<PlayerInfo>().runOnSwingThread(() -> getPlayerInfo(name));
  }

  public CompletableFuture<ApiResult<PlayerInfo>> getPlayer() {
    return new ApiCall<PlayerInfo>()
        .runOnSwingThread(
            () -> {
              Player player = MapTool.getPlayer();
              return getPlayerInfo(player.getName());
            });
  }

  public CompletableFuture<ApiListResult<PlayerInfo>> getConnectedPlayers() {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            return new ApiListResult<>(
                getPlayersInfo().stream()
                    .filter(PlayerInfo::connected)
                    .collect(Collectors.toList()));
          } catch (InterruptedException
              | InvocationTargetException
              | NoSuchAlgorithmException
              | InvalidKeySpecException e) {
            log.error(e);
            return new ApiListResult<>(new ApiException("err.internal", e));
          }
        });
  }

  public CompletableFuture<ApiListResult<PlayerInfo>> getDatabasePlayers() {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            return new ApiListResult<>(getPlayersInfo());
          } catch (InterruptedException
              | InvocationTargetException
              | NoSuchAlgorithmException
              | InvalidKeySpecException e) {
            log.error(e);
            return new ApiListResult<>(new ApiException("err.internal", e));
          }
        });
  }

  public CompletableFuture<ApiResult<PlayerDatabaseInfo>> getDatabaseCapabilities() {
    return CompletableFuture.supplyAsync(() -> new ApiResult<>(getPlayerDatabaseInfo()));
  }

  /*
  public CompletableFuture<ApiResult<NoData>> disablePlayer(String playerName, String reason) {
    return CompletableFuture.supplyAsync(() -> {
      PlayerDatabase playerDatabase = PlayerDatabaseFactory.getCurrentPlayerDatabase();
      try {
        Player player = playerDatabase.getPlayer(playerName);
        if (player == null) {
          return CompletableFuture.completedFuture(ApiResult.NOT_FOUND);
        }
        playerDatabase.disablePlayer(player, reason);
        return new ApiResult<NoData>();
      } catch (NoSuchAlgorithmException | InvalidKeySpecException | PasswordDatabaseException e) {
        return CompletableFuture.completedFuture(new ApiResult<>(new ApiException("err.internal",
            e)));
      }
    });
  }
  */

  private PlayerDatabaseInfo getPlayerDatabaseInfo() {
    PlayerDatabase playerDatabase = PlayerDatabaseFactory.getCurrentPlayerDatabase();
    return new PlayerDatabaseInfo(
        playerDatabase.supportsDisabling(),
        !playerDatabase.supportsRolePasswords(),
        playerDatabase.supportsAsymmetricalKeys());
  }

  private PlayerInfo getPlayerInfo(String name)
      throws NoSuchAlgorithmException, InvalidKeySpecException, InterruptedException,
          InvocationTargetException {
    PlayerDatabase playerDatabase = PlayerDatabaseFactory.getCurrentPlayerDatabase();
    if (!playerDatabase.isPlayerRegistered(name)) {
      return null;
    }
    Player player = playerDatabase.getPlayer(name);
    Role role = player.getRole();
    boolean supportsBlocking = playerDatabase.supportsDisabling();
    String blockedReason = "";
    boolean blocked = false;
    if (supportsBlocking) {
      blockedReason = playerDatabase.getDisabledReason(player);
      if (blockedReason.length() > 0) {
        blocked = true;
      }
    }
    boolean connected = false;
    for (Player p : MapTool.getPlayerList()) {
      if (name.equals(p.getName())) {
        connected = true;
        break;
      }
    }

    return new PlayerInfo(name, role, blocked, blockedReason, connected);
  }

  private List<PlayerInfo> getPlayersInfo()
      throws InterruptedException, InvocationTargetException, NoSuchAlgorithmException,
          InvalidKeySpecException {
    List<PlayerInfo> players = new ArrayList<>();
    PlayerDatabase playerDatabase = PlayerDatabaseFactory.getCurrentPlayerDatabase();
    for (Player p : playerDatabase.getAllPlayers()) {
      players.add(getPlayerInfo(p.getName()));
    }

    return players;
  }
}
