package org.tomdang.worldtime;

public record WorldClockState(double baseVirtualMillis, long anchorRealMillis, boolean paused, double speed) {
    public WorldClockState {
        if (!Double.isFinite(baseVirtualMillis) || !Double.isFinite(speed) || speed <= 0)
            throw new IllegalArgumentException("Clock state values must be finite and speed must be positive");
    }
}
