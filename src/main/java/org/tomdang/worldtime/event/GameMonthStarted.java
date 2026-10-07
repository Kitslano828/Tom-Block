package org.tomdang.worldtime.event;

import org.tomdang.worldtime.WorldTimeSnapshot;

public record GameMonthStarted(WorldTimeSnapshot current) implements WorldTimeEvent { }
