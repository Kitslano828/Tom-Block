package org.tomdang.worldtime.event;

public sealed interface WorldTimeEvent permits WorldTimeAdvanced, GameDayStarted, GameMonthStarted, GameSeasonChanged { }
