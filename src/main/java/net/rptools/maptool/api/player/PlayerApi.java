package net.rptools.maptool.api.player;


import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.CompletableFuture;
import net.rptools.maptool.api.util.ApiCall;
import net.rptools.maptool.api.util.ApiListResult;
import net.rptools.maptool.api.util.ApiResult;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.player.Player;
import net.rptools.maptool.model.player.Player.Role;
import net.rptools.maptool.model.player.PlayerDatabase;
import net.rptools.maptool.model.player.PlayerDatabaseFactory;

public class PlayerApi {

  public CompletableFuture<ApiResult<PlayerInfo>> getPlayer(String name) {
    return new ApiCall<PlayerInfo>().runOnSwingThread(() -> getPlayerInfo(name));
  }

  public CompletableFuture<ApiResult<PlayerInfo>> getPlayer() {
    return new ApiCall<PlayerInfo>().runOnSwingThread(() -> {
      Player player = MapTool.getPlayer();
      return getPlayerInfo(player.getName());
    });
  }

  public CompletableFuture<ApiListResult<PlayerInfo>> getConnectedPlayers() {
    return null; // TODO: CDW:

  }

  public CompletableFuture<ApiListResult<PlayerInfo>> getDatabasePlayers() {
    return null; // TODO: CDW:
  }


  private PlayerInfo getPlayerInfo(String name)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    PlayerDatabase playerDatabase = PlayerDatabaseFactory.getCurrentPlayerDatabase();
    Player player = playerDatabase.getPlayer(name);
    Role role = player.getRole();
    boolean individualPassword = playerDatabase.supportsRolePasswords();
    boolean supportsBlocking = playerDatabase.supportsDisabling();
    String blockedReason = "";
    boolean blocked = false;
    if (supportsBlocking) {
      blockedReason = playerDatabase.getDisabledReason(player);
      if (blockedReason.length() > 0) {
        blocked = true;
      }
    }
    boolean supportsPlayTimes = playerDatabase.supportsPlayTimes();

    PlayTimeInfo[] playTimes;
    if (supportsPlayTimes || playerDatabase.allowedAnyPlayTime(player)) {
      playTimes = new PlayTimeInfo[0];
    } else {
      playTimes =
          playerDatabase.getPlayTimes(player).stream().map(pt -> new PlayTimeInfo(pt.dayOfWeek(),
              pt.startTime(), pt.endTime())).toArray(PlayTimeInfo[]::new);
    }

    boolean connected = false;
    for (Player p : MapTool.getPlayerList()) {
      if (name.equals(p.getName())) {
        connected = true;
        break;
      }
    }


    return new PlayerInfo(
        name,
        role,
        individualPassword,
        supportsBlocking,
        blocked,
        blockedReason,
        supportsPlayTimes,
        playTimes,
        connected
    );
  }
}

