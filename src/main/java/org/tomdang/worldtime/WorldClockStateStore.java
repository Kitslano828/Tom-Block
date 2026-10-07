package org.tomdang.worldtime;

public interface WorldClockStateStore {
    WorldClockState load(WorldClockState fallback);
    void save(WorldClockState state);
}
