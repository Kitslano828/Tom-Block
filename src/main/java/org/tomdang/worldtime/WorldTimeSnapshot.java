package org.tomdang.worldtime;

public record WorldTimeSnapshot(GameDate date, String monthName, Season season, DayPeriod period,
        int hour, int minute, long minecraftTime, double dayProgress, boolean paused, double speed) {
    public WorldTimeSnapshot {
        if (date == null || monthName == null || season == null || period == null) throw new IllegalArgumentException("Complete time context is required");
        if (hour < 0 || hour > 23 || minute < 0 || minute > 59) throw new IllegalArgumentException("Invalid game time");
        if (minecraftTime < 0 || minecraftTime >= 24000 || dayProgress < 0 || dayProgress >= 1) throw new IllegalArgumentException("Invalid cycle position");
    }

    public String displayDate() { return monthName + " " + date.day() + ", Year " + date.year(); }
    public String displayTime() { return "%02d:%02d".formatted(hour, minute); }
}
