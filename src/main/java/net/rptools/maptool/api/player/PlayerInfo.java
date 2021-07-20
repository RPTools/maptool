package net.rptools.maptool.api.player;

import net.rptools.maptool.api.ApiData;
import net.rptools.maptool.model.player.Player.Role;

public record PlayerInfo(
    String name,
    Role role,
    boolean individualPassword,
    boolean supportsBlocking,
    boolean blocked,
    String blockedReason,
    boolean supportsPlayTimes,
    PlayTimeInfo[] playTimes,
    boolean connected
)  implements ApiData {

}
