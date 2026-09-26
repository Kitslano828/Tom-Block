package org.tomdang.critter.hunting;

/** Deterministic hunt rules. Bukkit presentation supplies movement, interactions, and time. */
public final class HuntEngine {
    private final HuntRules rules;

    public HuntEngine(HuntRules rules) {
        if (rules == null) throw new IllegalArgumentException("Hunt rules are required");
        this.rules = rules;
    }

    public HuntUpdate inspectClue(HuntState state) {
        requireActive(state);
        if (state.phase() != HuntPhase.TRACKING) return same(state);
        int found = Math.min(rules.cluesRequired(), state.cluesFound() + 1);
        HuntPhase phase = found == rules.cluesRequired() ? HuntPhase.TRAP_PLACEMENT : HuntPhase.TRACKING;
        return update(new HuntState(phase, found, state.alertness(), state.relocations(), 0, 0),
                phase == HuntPhase.TRAP_PLACEMENT ? HuntTransition.TRAIL_COMPLETED : HuntTransition.CLUE_FOUND);
    }

    public HuntUpdate armTrap(HuntState state) {
        requireActive(state);
        if (state.phase() != HuntPhase.TRAP_PLACEMENT) return same(state);
        return update(new HuntState(HuntPhase.APPROACH, state.cluesFound(), 0,
                state.relocations(), 0, 0), HuntTransition.TRAP_ARMED);
    }

    public HuntUpdate sampleApproach(HuntState state, boolean reckless) {
        requireActive(state);
        if (state.phase() != HuntPhase.APPROACH) return same(state);
        int alertness = reckless
                ? Math.min(rules.maximumAlertness(), state.alertness() + rules.recklessAlertness())
                : Math.max(0, state.alertness() - rules.calmRecovery());
        if (alertness < rules.maximumAlertness()) {
            return update(new HuntState(state.phase(), state.cluesFound(), alertness,
                    state.relocations(), 0, 0), HuntTransition.ALERTNESS_CHANGED);
        }
        return update(new HuntState(HuntPhase.APPROACH, state.cluesFound(), alertness,
                state.relocations(), 0, 0), HuntTransition.FLUSH_READY);
    }

    public HuntUpdate missTrap(HuntState state) {
        requireActive(state);
        int relocations = state.relocations() + 1;
        if (relocations > rules.maximumRelocations()) {
            return update(new HuntState(HuntPhase.ESCAPED, state.cluesFound(), rules.maximumAlertness(),
                    relocations, 0, 0), HuntTransition.ESCAPED);
        }
        return update(new HuntState(HuntPhase.TRAP_PLACEMENT, state.cluesFound(), 0,
                relocations, 0, 0), HuntTransition.TRAP_MISSED);
    }

    public HuntUpdate trapCaptured(HuntState state) {
        requireActive(state);
        if (state.phase() != HuntPhase.APPROACH) return same(state);
        return update(new HuntState(HuntPhase.COMPLETED, state.cluesFound(), 0,
                state.relocations(), 0, 0), HuntTransition.CAPTURED);
    }

    public HuntUpdate openCaptureWindow(HuntState state, long tick, boolean concealed) {
        requireActive(state);
        if (state.phase() != HuntPhase.APPROACH || !concealed || state.alertness() > 0) return same(state);
        return update(new HuntState(HuntPhase.CAPTURE_WINDOW, state.cluesFound(), 0,
                state.relocations(), tick + rules.captureReadyTicks(), tick + rules.captureWindowTicks()),
                HuntTransition.WINDOW_OPENED);
    }

    public HuntUpdate capture(HuntState state, long tick) {
        requireActive(state);
        if (state.phase() != HuntPhase.CAPTURE_WINDOW) return same(state);
        if (tick > state.captureExpiresAtTick()) {
            return update(new HuntState(HuntPhase.ESCAPED, state.cluesFound(), rules.maximumAlertness(),
                    state.relocations(), 0, 0), HuntTransition.ESCAPED);
        }
        if (tick < state.captureReadyAtTick()) return update(state, HuntTransition.TOO_EARLY);
        return update(new HuntState(HuntPhase.COMPLETED, state.cluesFound(), 0,
                state.relocations(), 0, 0), HuntTransition.CAPTURED);
    }

    public HuntUpdate tick(HuntState state, long tick) {
        if (state.phase() == HuntPhase.CAPTURE_WINDOW && tick > state.captureExpiresAtTick()) {
            return update(new HuntState(HuntPhase.ESCAPED, state.cluesFound(), rules.maximumAlertness(),
                    state.relocations(), 0, 0), HuntTransition.ESCAPED);
        }
        return same(state);
    }

    private void requireActive(HuntState state) {
        if (state == null) throw new IllegalArgumentException("Hunt state is required");
        if (state.phase().terminal()) throw new IllegalStateException("Hunt is already terminal");
    }
    private HuntUpdate same(HuntState state) { return update(state, HuntTransition.NONE); }
    private HuntUpdate update(HuntState state, HuntTransition transition) { return new HuntUpdate(state, transition); }
}
