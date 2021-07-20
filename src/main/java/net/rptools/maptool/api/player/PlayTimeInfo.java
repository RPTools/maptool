package net.rptools.maptool.api.player;

import java.time.DayOfWeek;
import java.time.LocalTime;
import net.rptools.maptool.model.player.PlayTime;

public record PlayTimeInfo(
    DayOfWeek dayOfWeek,
    LocalTime startTime,
    LocalTime endTime
) {
  public PlayTimeInfo(PlayTime playTime) {
    this(playTime.dayOfWeek(), playTime.startTime(), playTime.endTime());
  }
}
