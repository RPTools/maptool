package net.rptools.maptool.api.player;

import com.google.gson.JsonObject;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import net.rptools.maptool.api.ApiData;
import net.rptools.maptool.model.player.PlayTime;

public record PlayTimeInfo(
    DayOfWeek dayOfWeek,
    LocalTime startTime,
    LocalTime endTime
) implements ApiData {
  public PlayTimeInfo(PlayTime playTime) {
    this(playTime.dayOfWeek(), playTime.startTime(), playTime.endTime());
  }

  @Override
  public JsonObject asJsonObject() {
    JsonObject json = new JsonObject();
    json.addProperty("dayOfWeek", dayOfWeek.getValue());
    json.addProperty("startTime", startTime.format(DateTimeFormatter.ISO_LOCAL_TIME));
    json.addProperty("endTime", startTime.format(DateTimeFormatter.ISO_LOCAL_TIME));
    return json;
  }
}
