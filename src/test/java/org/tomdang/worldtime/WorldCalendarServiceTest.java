package org.tomdang.worldtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WorldCalendarServiceTest {
    private AtomicLong now;
    private MemoryStore store;
    private WorldCalendarService calendar;

    @BeforeEach void setUp() {
        now = new AtomicLong(1_000_000);
        store = new MemoryStore();
        calendar = new WorldCalendarService(settings(), store, now::get);
    }

    @Test void mapsTwentyMinuteDaylightAndTenMinuteNightToMinecraftTime() {
        assertTime(6, 0, 0);
        advanceMinutes(10); assertTime(12, 0, 6000);
        advanceMinutes(10); assertTime(18, 0, 12000);
        advanceMinutes(5); assertTime(0, 0, 18000);
        assertEquals(new GameDate(1, 1, 2), calendar.snapshot().date());
        advanceMinutes(5); assertTime(6, 0, 0);
    }

    @Test void rollsMonthsAndYearsUsingConfiguredCalendar() {
        now.addAndGet(28L * 1_800_000);
        assertEquals(new GameDate(1, 2, 1), calendar.snapshot().date());
        now.addAndGet(11L * 28 * 1_800_000);
        assertEquals(new GameDate(2, 1, 1), calendar.snapshot().date());
    }

    @Test void persistedAnchorIncludesOfflineElapsedTime() {
        calendar.save();
        now.addAndGet(1_800_000);
        var restarted = new WorldCalendarService(settings(), store, now::get);
        assertEquals(new GameDate(1, 1, 2), restarted.snapshot().date());
        assertEquals(6, restarted.snapshot().hour());
    }

    @Test void pauseFreezesAndResumeContinuesFromFrozenInstant() {
        advanceMinutes(10);
        calendar.pause();
        now.addAndGet(10_000_000);
        assertEquals(12, calendar.snapshot().hour());
        assertTrue(calendar.snapshot().paused());
        calendar.resume();
        advanceMinutes(10);
        assertEquals(18, calendar.snapshot().hour());
        assertFalse(calendar.snapshot().paused());
    }

    @Test void administrativeSetAndSpeedReanchorWithoutJumping() {
        calendar.set(new GameDate(3, 7, 14), 23, 30);
        assertEquals(new GameDate(3, 7, 14), calendar.snapshot().date());
        assertEquals(23, calendar.snapshot().hour());
        assertEquals(30, calendar.snapshot().minute());
        calendar.set(new GameDate(3, 7, 14), 6, 0);
        calendar.setSpeed(2);
        advanceMinutes(5);
        assertEquals(new GameDate(3, 7, 14), calendar.snapshot().date());
        assertEquals(12, calendar.snapshot().hour());
    }

    private void assertTime(int hour, int minute, long minecraft) {
        assertEquals(hour, calendar.snapshot().hour());
        assertEquals(minute, calendar.snapshot().minute());
        assertEquals(minecraft, calendar.snapshot().minecraftTime());
    }
    private void advanceMinutes(long minutes) { now.addAndGet(minutes * 60_000); }
    private static WorldCalendarSettings settings() {
        return new WorldCalendarSettings(1_800_000, 1_200_000, 28, List.of(
                new CalendarMonth("January", Season.WINTER), new CalendarMonth("February", Season.WINTER),
                new CalendarMonth("March", Season.SPRING), new CalendarMonth("April", Season.SPRING),
                new CalendarMonth("May", Season.SPRING), new CalendarMonth("June", Season.SUMMER),
                new CalendarMonth("July", Season.SUMMER), new CalendarMonth("August", Season.SUMMER),
                new CalendarMonth("September", Season.AUTUMN), new CalendarMonth("October", Season.AUTUMN),
                new CalendarMonth("November", Season.AUTUMN), new CalendarMonth("December", Season.WINTER)),
                new GameDate(1, 1, 1), 6, 0, 20);
    }
    private static final class MemoryStore implements WorldClockStateStore {
        private WorldClockState state;
        @Override public WorldClockState load(WorldClockState fallback) { return state == null ? fallback : state; }
        @Override public void save(WorldClockState state) { this.state = state; }
    }
}
