package org.tomdang.worldtime;

import java.util.List;

public record WorldCalendarSettings(long realMillisPerDay, long daylightRealMillis, int daysPerMonth,
        List<CalendarMonth> months, GameDate initialDate, int initialHour, int initialMinute, long synchronizationTicks) {
    public WorldCalendarSettings {
        if (realMillisPerDay < 60_000 || daylightRealMillis <= 0 || daylightRealMillis >= realMillisPerDay)
            throw new IllegalArgumentException("Calendar day and daylight durations are invalid");
        if (daysPerMonth < 1 || months == null || months.isEmpty()) throw new IllegalArgumentException("Calendar months are required");
        if (initialDate == null || initialDate.month() > months.size() || initialDate.day() > daysPerMonth)
            throw new IllegalArgumentException("Initial date is outside the calendar");
        if (initialHour < 0 || initialHour > 23 || initialMinute < 0 || initialMinute > 59 || synchronizationTicks < 1)
            throw new IllegalArgumentException("Initial time or synchronization interval is invalid");
        months = List.copyOf(months);
    }

    public long daysPerYear() { return (long) daysPerMonth * months.size(); }
    public double daylightRatio() { return (double) daylightRealMillis / realMillisPerDay; }
}
