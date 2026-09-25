package org.tomdang.entityai.navigation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.tomdang.entityai.core.AiCapability;
import org.tomdang.entityai.core.AiVector;

class NativeGroundNavigatorTest {
    @Test void delegatesToNativePathfinderAndCancels() {
        GroundAgent agent = new GroundAgent();
        NativeGroundNavigator navigator = new NativeGroundNavigator();
        NavigationRequest request = new NavigationRequest(new AiVector(4, 0, 0), 1, .25, 8, 0);
        assertEquals(NavigationStatus.RUNNING, navigator.navigate(agent, request, 1));
        assertEquals(request, agent.request);
        navigator.cancel(agent);
        assertTrue(agent.stopped);
    }

    @Test void reportsArrivalWithoutInvokingPathfinder() {
        GroundAgent agent = new GroundAgent();
        assertEquals(NavigationStatus.ARRIVED, new NativeGroundNavigator().navigate(agent,
                new NavigationRequest(new AiVector(0, 0, 0), 1, .25, 8, 0), 1));
    }

    private static final class GroundAgent implements NativeGroundNavigator.NativeGroundAgent {
        private final UUID id = UUID.randomUUID(); private NavigationRequest request; private boolean stopped;
        @Override public UUID id() { return id; }
        @Override public String worldId() { return "world"; }
        @Override public AiVector position() { return new AiVector(0, 0, 0); }
        @Override public AiVector home() { return position(); }
        @Override public Set<AiCapability> capabilities() { return Set.of(AiCapability.GROUND); }
        @Override public boolean valid() { return true; }
        @Override public boolean navigateNative(NavigationRequest request) { this.request = request; return true; }
        @Override public void stopNativeNavigation() { stopped = true; }
    }
}
