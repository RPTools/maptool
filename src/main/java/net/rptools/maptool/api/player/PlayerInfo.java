package net.rptools.maptool.api.player;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.rptools.maptool.api.ApiData;
import net.rptools.maptool.model.player.Player.Role;

public record PlayerInfo(
    String name,
    Role role,
    boolean blocked,
    String blockedReason,
    boolean connected
)  implements ApiData {

}
