package org.tomdang.critter.hunting;

public record HuntState(HuntPhase phase, int cluesFound, int alertness, int relocations,
        long captureReadyAtTick, long captureExpiresAtTick) {
    public HuntState {
        if (phase == null || cluesFound < 0 || alertness < 0 || relocations < 0) {
            throw new IllegalArgumentException("Invalid hunt state");
        }
    }

    public static HuntState start() { return new HuntState(HuntPhase.TRACKING, 0, 0, 0, 0, 0); }
}
