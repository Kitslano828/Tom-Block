package org.tomdang.entityai.navigation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.tomdang.entityai.core.AiCapability;
import org.tomdang.entityai.core.AiVector;

class DirectFlightNavigatorTest {
    @Test void advancesFlyingAgentAndEventuallyArrives() {
        TestAgent agent = new TestAgent(new AiVector(0, 0, 0));
        DirectFlightNavigator navigator = new DirectFlightNavigator();
        NavigationRequest request = new NavigationRequest(new AiVector(1, 0, 0), 20, .01, 4, 0);
        assertEquals(NavigationStatus.RUNNING, navigator.navigate(agent, request, 1));
        assertEquals(request, navigator.diagnostics(agent).orElseThrow().request());
        assertEquals(NavigationStatus.RUNNING, navigator.diagnostics(agent).orElseThrow().status());
        assertEquals(new AiVector(1, 0, 0), agent.position);
        assertEquals(NavigationStatus.ARRIVED, navigator.navigate(agent, request, 2));
    }

    @Test void refusesDestinationOutsideHomeBoundary() {
        TestAgent agent = new TestAgent(new AiVector(0, 0, 0));
        NavigationStatus status = new DirectFlightNavigator().navigate(agent,
                new NavigationRequest(new AiVector(10, 0, 0), 1, .2, 5, 0), 1);
        assertEquals(NavigationStatus.BLOCKED, status);
        assertEquals(new AiVector(0, 0, 0), agent.position);
    }

    @Test void detoursWhenDirectStepIsOccupied() {
        TestAgent agent = new TestAgent(new AiVector(0, 0, 0));
        agent.blockDirect = true;
        assertEquals(NavigationStatus.RUNNING, new DirectFlightNavigator().navigate(agent,
                new NavigationRequest(new AiVector(2, 0, 0), 20, .1, 4, 0), 1));
        assertTrue(agent.position.y() != 0 || agent.position.z() != 0);
    }

    private static final class TestAgent implements MovableAiAgent {
        private final UUID id = UUID.randomUUID(); private final AiVector home; private AiVector position;
        private boolean blockDirect;
        private TestAgent(AiVector position) { this.position = position; this.home = position; }
        @Override public UUID id() { return id; }
        @Override public String worldId() { return "world"; }
        @Override public AiVector position() { return position; }
        @Override public AiVector home() { return home; }
        @Override public Set<AiCapability> capabilities() { return Set.of(AiCapability.FLYING); }
        @Override public boolean valid() { return true; }
        @Override public boolean canOccupy(AiVector candidate) {
            return !blockDirect || candidate.y() != 0 || candidate.z() != 0;
        }
        @Override public void move(AiVector position, AiVector direction) { this.position = position; }
    }
}
