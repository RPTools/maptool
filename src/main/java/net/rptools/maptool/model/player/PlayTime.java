package net.rptools.maptool.model.player;

import java.time.DayOfWeek;
import java.time.LocalTime;


public record PlayTime(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {}
