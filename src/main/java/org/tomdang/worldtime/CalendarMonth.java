package org.tomdang.worldtime;

public record CalendarMonth(String name, Season season) {
    public CalendarMonth {
        if (name == null || name.isBlank() || season == null) throw new IllegalArgumentException("Month name and season are required");
    }
}
