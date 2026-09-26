package org.tomdang.critter.hunting;

/** Content-facing tuning for a tracked hunt. Tick values keep the domain independent of Bukkit. */
public record HuntRules(int cluesRequired, int maximumAlertness, int recklessAlertness,
        int calmRecovery, int maximumRelocations, long captureReadyTicks, long captureWindowTicks) {
    public HuntRules {
        if (cluesRequired < 1 || maximumAlertness < 1 || recklessAlertness < 1 || calmRecovery < 0
                || maximumRelocations < 0 || captureReadyTicks < 0 || captureWindowTicks < 1) {
            throw new IllegalArgumentException("Invalid hunt rules");
        }
    }
}
