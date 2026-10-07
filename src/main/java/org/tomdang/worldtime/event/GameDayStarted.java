package org.tomdang.worldtime.event;

import org.tomdang.worldtime.WorldTimeSnapshot;

public record GameDayStarted(WorldTimeSnapshot current) implements WorldTimeEvent { }
