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
import net.rptools.maptool.api.util.NoData;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.player.PasswordDatabaseException;
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
    return CompletableFuture.supplyAsync(() -> {
      try {
        return new ApiListResult<>(
            getPlayersInfo().stream().filter(PlayerInfo::connected).collect(Collectors.toList())
        );
      } catch (InterruptedException  | InvocationTargetException | NoSuchAlgorithmException | InvalidKeySpecException e) {
        return new ApiListResult<>(new ApiException("err.internal", e));
        // TODO: CDW: log error
      }
    });
  }

  public CompletableFuture<ApiListResult<PlayerInfo>> getDatabasePlayers() {
    return CompletableFuture.supplyAsync(() -> {
      try {
        return new ApiListResult<>(getPlayersInfo());
      } catch (InterruptedException  | InvocationTargetException | NoSuchAlgorithmException | InvalidKeySpecException e) {
        return new ApiListResult<>(new ApiException("err.internal", e));
        // TODO: CDW: log error
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
        // TODO: CDW: log error
      }
    });
  }
  */

  private PlayerDatabaseInfo getPlayerDatabaseInfo() {
    PlayerDatabase playerDatabase = PlayerDatabaseFactory.getCurrentPlayerDatabase();
    return new PlayerDatabaseInfo(playerDatabase.supportsDisabling(),
        !playerDatabase.supportsRolePasswords(), playerDatabase.supportsAsymmetricalKeys());
  }


  private PlayerInfo getPlayerInfo(String name)
      throws NoSuchAlgorithmException, InvalidKeySpecException, InterruptedException, InvocationTargetException {
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
        blocked,
        blockedReason,
        connected
    );
  }

  private List<PlayerInfo> getPlayersInfo()
      throws InterruptedException, InvocationTargetException, NoSuchAlgorithmException, InvalidKeySpecException {
    List<PlayerInfo> players = new ArrayList<>();
    PlayerDatabase playerDatabase = PlayerDatabaseFactory.getCurrentPlayerDatabase();
    for (Player p : playerDatabase.getAllPlayers()) {
      players.add(getPlayerInfo(p.getName()));
    }

    return players;
  }
}

