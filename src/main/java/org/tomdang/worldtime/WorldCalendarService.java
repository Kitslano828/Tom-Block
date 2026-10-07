package org.tomdang.worldtime;

import java.util.function.LongSupplier;

/** Server-authoritative, real-elapsed-time calendar. Its state continues across restarts without tick drift. */
public final class WorldCalendarService {
    private final WorldCalendarSettings settings;
    private final WorldClockStateStore store;
    private final LongSupplier now;
    private WorldClockState state;

    public WorldCalendarService(WorldCalendarSettings settings, WorldClockStateStore store) {
        this(settings, store, System::currentTimeMillis);
    }

    WorldCalendarService(WorldCalendarSettings settings, WorldClockStateStore store, LongSupplier now) {
        if (settings == null || store == null || now == null) throw new IllegalArgumentException("Calendar dependencies are required");
        this.settings = settings;
        this.store = store;
        this.now = now;
        long current = now.getAsLong();
        double initial = virtualFor(settings.initialDate(), settings.initialHour(), settings.initialMinute());
        state = store.load(new WorldClockState(initial, current, false, 1));
        store.save(state);
    }

    public WorldTimeSnapshot snapshot() { return snapshotAt(now.getAsLong()); }

    WorldTimeSnapshot snapshotAt(long realMillis) {
        double virtual = virtualAt(realMillis);
        long dayMillis = settings.realMillisPerDay();
        long cycle = floor(virtual / dayMillis);
        double within = virtual - cycle * dayMillis;
        if (within < 0) { cycle--; within += dayMillis; }
        double progress = within / dayMillis;
        double daylight = settings.daylightRatio();
        double solarMinutes;
        if (progress < daylight) solarMinutes = 360 + progress / daylight * 720;
        else solarMinutes = 1080 + (progress - daylight) / (1 - daylight) * 720;
        solarMinutes %= 1440;
        long dateOffset = cycle + (progress >= daylight + (1 - daylight) / 2 ? 1 : 0);
        GameDate date = dateFromOffset(dateOffset);
        int hour = (int) solarMinutes / 60;
        int minute = (int) solarMinutes % 60;
        CalendarMonth month = settings.months().get(date.month() - 1);
        long minecraft = Math.floorMod(Math.round((solarMinutes - 360) / 1440.0 * 24000), 24000);
        return new WorldTimeSnapshot(date, month.name(), month.season(), period(hour), hour, minute,
                minecraft, progress, state.paused(), state.speed());
    }

    public void pause() { reanchor(true, state.speed()); }
    public void resume() { reanchor(false, state.speed()); }
    public void setSpeed(double speed) {
        if (!Double.isFinite(speed) || speed <= 0 || speed > 1000) throw new IllegalArgumentException("Speed must be between 0 and 1000");
        reanchor(state.paused(), speed);
    }
    public void set(GameDate date, int hour, int minute) {
        validate(date, hour, minute);
        state = new WorldClockState(virtualFor(date, hour, minute), now.getAsLong(), state.paused(), state.speed());
        store.save(state);
    }
    public void save() {
        long current = now.getAsLong();
        state = new WorldClockState(virtualAt(current), current, state.paused(), state.speed());
        store.save(state);
    }
    public WorldCalendarSettings settings() { return settings; }

    private void reanchor(boolean paused, double speed) {
        long current = now.getAsLong();
        state = new WorldClockState(virtualAt(current), current, paused, speed);
        store.save(state);
    }

    private double virtualAt(long realMillis) {
        return state.baseVirtualMillis() + (state.paused() ? 0 : (realMillis - state.anchorRealMillis()) * state.speed());
    }

    private double virtualFor(GameDate date, int hour, int minute) {
        validate(date, hour, minute);
        long dateOffset = ordinal(date) - ordinal(settings.initialDate());
        int solar = hour * 60 + minute;
        double daylight = settings.daylightRatio();
        double progress;
        long cycle = dateOffset;
        if (solar >= 360 && solar < 1080) progress = (solar - 360) / 720.0 * daylight;
        else {
            int sinceDusk = solar >= 1080 ? solar - 1080 : solar + 360;
            progress = daylight + sinceDusk / 720.0 * (1 - daylight);
            if (solar < 360) cycle--;
        }
        return (cycle + progress) * settings.realMillisPerDay();
    }

    private GameDate dateFromOffset(long offset) {
        long ordinal = ordinal(settings.initialDate()) + offset;
        if (ordinal < 0) throw new IllegalStateException("World calendar predates Year 1");
        long year = ordinal / settings.daysPerYear() + 1;
        long inYear = ordinal % settings.daysPerYear();
        int month = (int) (inYear / settings.daysPerMonth()) + 1;
        int day = (int) (inYear % settings.daysPerMonth()) + 1;
        return new GameDate(year, month, day);
    }

    private long ordinal(GameDate date) {
        return Math.addExact(Math.multiplyExact(date.year() - 1, settings.daysPerYear()),
                (long) (date.month() - 1) * settings.daysPerMonth() + date.day() - 1);
    }

    private void validate(GameDate date, int hour, int minute) {
        if (date == null || date.month() > settings.months().size() || date.day() > settings.daysPerMonth()
                || hour < 0 || hour > 23 || minute < 0 || minute > 59) throw new IllegalArgumentException("Date or time is outside the configured calendar");
    }

    private static DayPeriod period(int hour) {
        if (hour >= 5 && hour < 7) return DayPeriod.DAWN;
        if (hour >= 7 && hour < 17) return DayPeriod.DAY;
        if (hour >= 17 && hour < 19) return DayPeriod.DUSK;
        return DayPeriod.NIGHT;
    }

    private static long floor(double value) { return (long) Math.floor(value); }
}
