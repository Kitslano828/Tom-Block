package org.tomdang.entityai.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.tomdang.entityai.navigation.NavigationStatus;
import org.tomdang.entityai.navigation.Navigator;

class AiBrainTest {
    @Test void selectsHighestPriorityAndInterruptsWhenItBecomesEligible() {
        TestAgent agent = new TestAgent();
        Toggle urgent = new Toggle();
        TestGoal idle = new TestGoal("IDLE", 0, () -> true);
        TestGoal flee = new TestGoal("FLEE", 100, () -> urgent.enabled);
        AiBrain brain = new AiBrain(agent, (ignored, tick) -> PerceptionSnapshot.empty(tick),
                new NoopNavigator(), List.of(idle, flee));

        brain.tick(1);
        assertEquals("IDLE", brain.activeGoal().orElseThrow());
        urgent.enabled = true;
        brain.tick(2);
        assertEquals("FLEE", brain.activeGoal().orElseThrow());
        assertEquals(1, idle.interruptions);
    }

    @Test void equalPrioritiesResolveByStableGoalId() {
        AiBrain brain = new AiBrain(new TestAgent(), (ignored, tick) -> PerceptionSnapshot.empty(tick),
                new NoopNavigator(), List.of(new TestGoal("ZETA", 10, () -> true),
                        new TestGoal("ALPHA", 10, () -> true)));
        brain.tick(1);
        assertEquals("ALPHA", brain.activeGoal().orElseThrow());
    }

    @Test void rejectsDuplicateGoalIds() {
        assertThrows(IllegalArgumentException.class, () -> new AiBrain(new TestAgent(),
                (ignored, tick) -> PerceptionSnapshot.empty(tick), new NoopNavigator(),
                List.of(new TestGoal("SAME", 1, () -> true), new TestGoal("SAME", 0, () -> true))));
    }

    @Test void exposesImmutableDiagnosticsWithoutLeakingBrainState() {
        TestAgent agent = new TestAgent();
        AiBrain brain = new AiBrain(agent, (ignored, tick) -> PerceptionSnapshot.empty(tick),
                new NoopNavigator(), List.of(new TestGoal("IDLE", 0, () -> true)));
        brain.memory().put("mode", "test");
        brain.tick(20);
        AiDiagnosticsSnapshot snapshot = brain.diagnostics();
        assertEquals(agent.id(), snapshot.id());
        assertEquals("IDLE", snapshot.activeGoal().orElseThrow().id());
        assertEquals("test", snapshot.memory().get("mode"));
        assertThrows(UnsupportedOperationException.class, () -> snapshot.memory().put("bad", true));
    }

    private static final class Toggle { boolean enabled; }
    private static final class TestAgent implements AiAgent {
        private final UUID id = UUID.randomUUID();
        @Override public UUID id() { return id; }
        @Override public String worldId() { return "world"; }
        @Override public AiVector position() { return new AiVector(0, 0, 0); }
        @Override public AiVector home() { return position(); }
        @Override public Set<AiCapability> capabilities() { return Set.of(); }
        @Override public boolean valid() { return true; }
    }
    private static final class TestGoal implements AiGoal {
        private final String id; private final int priority; private final java.util.function.BooleanSupplier condition;
        private int interruptions;
        private TestGoal(String id, int priority, java.util.function.BooleanSupplier condition) {
            this.id = id; this.priority = priority; this.condition = condition;
        }
        @Override public String id() { return id; }
        @Override public int priority() { return priority; }
        @Override public boolean canStart(AiContext context) { return condition.getAsBoolean(); }
        @Override public AiBehavior start(AiContext context) { return new AiBehavior() {
            @Override public AiBehaviorStatus tick(AiContext ignored) { return AiBehaviorStatus.RUNNING; }
            @Override public void stop(AiContext ignored, boolean interrupted) { if (interrupted) interruptions++; }
        }; }
    }
    private static final class NoopNavigator implements Navigator {
        @Override public NavigationStatus navigate(AiAgent agent,
                org.tomdang.entityai.navigation.NavigationRequest request, long tick) {
            return NavigationStatus.RUNNING;
        }
        @Override public void cancel(AiAgent agent) {}
    }
}
