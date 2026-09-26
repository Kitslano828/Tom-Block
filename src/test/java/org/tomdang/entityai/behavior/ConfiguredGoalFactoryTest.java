package org.tomdang.entityai.behavior;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.tomdang.entityai.configuration.AiGoalDefinition;
import org.tomdang.entityai.configuration.AiProfile;
import org.tomdang.entityai.core.AiAgent;
import org.tomdang.entityai.core.AiBrain;
import org.tomdang.entityai.core.AiCapability;
import org.tomdang.entityai.core.AiVector;
import org.tomdang.entityai.core.PerceptionSnapshot;
import org.tomdang.entityai.navigation.NavigationRequest;
import org.tomdang.entityai.navigation.NavigationStatus;
import org.tomdang.entityai.navigation.Navigator;

class ConfiguredGoalFactoryTest {
    @Test void seekCoverSelectsAuthoredPointAwayFromThreatAndSettles() {
        var profile = new AiProfile("NATIVE_GROUND", List.of(
                new AiGoalDefinition("SEEK_COVER", "SEEK_COVER", 90,
                        Map.of("speed", "1.0", "territory-radius", "16")),
                new AiGoalDefinition("IDLE", "IDLE", 0, Map.of())));
        RecordingNavigator navigator = new RecordingNavigator();
        AiBrain brain = new AiBrain(new Agent(), (ignored, tick) -> PerceptionSnapshot.empty(tick),
                navigator, new ConfiguredGoalFactory().create(profile));
        brain.memory().flag("seek-cover", true);
        brain.memory().put("threat-position", new AiVector(0, 0, 0));
        brain.memory().put("cover-points", List.of(new AiVector(2, 0, 0), new AiVector(10, 0, 0)));

        brain.tick(1);

        assertEquals(new AiVector(10, 0, 0), navigator.request.destination());
        assertEquals(new AiVector(10, 0, 0), brain.memory().get("cover-target", AiVector.class).orElseThrow());
        assertTrue(brain.memory().flag("settled"));
        assertFalse(brain.memory().flag("seek-cover"));
    }

    private static final class Agent implements AiAgent {
        private final UUID id = UUID.randomUUID();
        @Override public UUID id() { return id; }
        @Override public String worldId() { return "world"; }
        @Override public AiVector position() { return new AiVector(0, 0, 0); }
        @Override public AiVector home() { return position(); }
        @Override public Set<AiCapability> capabilities() { return Set.of(AiCapability.GROUND); }
        @Override public boolean valid() { return true; }
    }

    private static final class RecordingNavigator implements Navigator {
        private NavigationRequest request;
        @Override public NavigationStatus navigate(AiAgent agent, NavigationRequest request, long tick) {
            this.request = request;
            return NavigationStatus.ARRIVED;
        }
        @Override public void cancel(AiAgent agent) {}
    }
}
