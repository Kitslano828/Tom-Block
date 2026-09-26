package org.tomdang.critter.hunting;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HuntEngineTest {
    private final HuntRules rules = new HuntRules(3, 100, 50, 25, 2, 20, 80);
    private final HuntEngine engine = new HuntEngine(rules);

    @Test void cluesUnlockApproachOnlyAfterTheAuthoredCount() {
        HuntUpdate first = engine.inspectClue(HuntState.start());
        HuntUpdate second = engine.inspectClue(first.state());
        HuntUpdate third = engine.inspectClue(second.state());
        assertEquals(HuntPhase.TRACKING, second.state().phase());
        assertEquals(HuntTransition.TRAIL_COMPLETED, third.transition());
        assertEquals(HuntPhase.TRAP_PLACEMENT, third.state().phase());
    }

    @Test void missedTrapsRelocateThenEventuallyEscape() {
        HuntState state = approach();
        for (int relocation = 0; relocation < 2; relocation++) {
            HuntUpdate update = engine.missTrap(state);
            assertEquals(HuntTransition.TRAP_MISSED, update.transition());
            assertEquals(HuntPhase.TRAP_PLACEMENT, update.state().phase());
            state = engine.armTrap(update.state()).state();
        }
        HuntUpdate escaped = engine.missTrap(state);
        assertEquals(HuntTransition.ESCAPED, escaped.transition());
        assertEquals(HuntPhase.ESCAPED, escaped.state().phase());
    }

    @Test void pressureMakesTheCritterReadyToFlush() {
        HuntState state = engine.sampleApproach(approach(), true).state();
        HuntUpdate update = engine.sampleApproach(state, true);
        assertEquals(HuntTransition.FLUSH_READY, update.transition());
        assertEquals(HuntPhase.APPROACH, update.state().phase());
    }

    @Test void trapCaptureCompletesWithoutASecondInteraction() {
        HuntUpdate captured = engine.trapCaptured(approach());
        assertEquals(HuntTransition.CAPTURED, captured.transition());
        assertEquals(HuntPhase.COMPLETED, captured.state().phase());
    }

    @Test void concealedCalmApproachCreatesAReadableTimingWindowForOtherArchetypes() {
        HuntUpdate opened = engine.openCaptureWindow(approach(), 100, true);
        assertEquals(HuntTransition.WINDOW_OPENED, opened.transition());
        assertEquals(HuntTransition.TOO_EARLY, engine.capture(opened.state(), 119).transition());
        assertEquals(HuntTransition.CAPTURED, engine.capture(opened.state(), 120).transition());
    }

    @Test void missedCaptureWindowEscapes() {
        HuntState window = engine.openCaptureWindow(approach(), 100, true).state();
        assertEquals(HuntTransition.ESCAPED, engine.tick(window, 181).transition());
    }

    @Test void relocationCountProducesStableRewardGrades() {
        assertEquals(HuntGrade.CLEAN, HuntGrade.fromRelocations(0));
        assertEquals(HuntGrade.STANDARD, HuntGrade.fromRelocations(2));
        assertEquals(HuntGrade.SCRAPPY, HuntGrade.fromRelocations(3));
    }

    private HuntState approach() {
        HuntState state = HuntState.start();
        for (int i = 0; i < rules.cluesRequired(); i++) state = engine.inspectClue(state).state();
        return engine.armTrap(state).state();
    }
}
