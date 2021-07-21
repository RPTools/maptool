package net.rptools.maptool.api.player;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.rptools.maptool.api.ApiData;
import net.rptools.maptool.model.player.PlayTime;
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

  @Override
  public JsonObject asJsonObject() {
    JsonObject json = new JsonObject();
    json.addProperty("name", name);
    json.addProperty("individualPassword", individualPassword);
    json.addProperty("supportsBlocking", supportsBlocking);
    json.addProperty("blocked", blocked);
    if (blocked) {
      json.addProperty("blockedReason", blockedReason);
    }
    json.addProperty("supportsPlayTimes", supportsPlayTimes);
    if (supportsPlayTimes && playTimes != null && playTimes.length > 0) {
      JsonArray jsonPlayTimes = new JsonArray();
      for (PlayTimeInfo pt : playTimes) {
        jsonPlayTimes.add(pt.asJsonObject());
      }
      json.add("playTimes", jsonPlayTimes);
    }
    json.addProperty("connected", connected);

    return json;
  }
}
