package org.tomdang.worldtime.event;

import org.tomdang.worldtime.WorldTimeSnapshot;

public record WorldTimeAdvanced(WorldTimeSnapshot previous, WorldTimeSnapshot current) implements WorldTimeEvent { }
