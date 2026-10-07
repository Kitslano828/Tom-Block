package org.tomdang.worldtime.event;

import org.tomdang.worldtime.Season;
import org.tomdang.worldtime.WorldTimeSnapshot;

public record GameSeasonChanged(Season previous, WorldTimeSnapshot current) implements WorldTimeEvent { }
