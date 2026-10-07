package org.tomdang.hud.calendar;

import org.tomdang.hud.presentation.HudViewModel;
import org.tomdang.worldtime.DayPeriod;
import org.tomdang.worldtime.Season;

public record CalendarHudModel(String monthName, int day, Season season, DayPeriod period,
        int hour, int minute) implements HudViewModel {
    public CalendarHudModel {
        if (monthName == null || monthName.isBlank() || season == null || period == null)
            throw new IllegalArgumentException("Complete calendar HUD data is required");
    }
}
