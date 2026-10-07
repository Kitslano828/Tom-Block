package org.tomdang.worldtime;

import java.util.Set;

/** Reusable calendar predicate for stats, ecology, schedules, quests, and future content. Empty sets mean any. */
public record TemporalCondition(Set<Integer> months, Set<Season> seasons, Set<DayPeriod> periods) {
    public TemporalCondition {
        months = months == null ? Set.of() : Set.copyOf(months);
        seasons = seasons == null ? Set.of() : Set.copyOf(seasons);
        periods = periods == null ? Set.of() : Set.copyOf(periods);
        if (months.stream().anyMatch(month -> month < 1)) throw new IllegalArgumentException("Months are one-based");
    }
    public boolean matches(WorldTimeSnapshot time) {
        if (time == null) throw new IllegalArgumentException("World time is required");
        return (months.isEmpty() || months.contains(time.date().month()))
                && (seasons.isEmpty() || seasons.contains(time.season()))
                && (periods.isEmpty() || periods.contains(time.period()));
    }
}
