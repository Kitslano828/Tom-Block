package org.tomdang.entityai.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.tomdang.entityai.core.AiAgent;
import org.tomdang.entityai.core.AiBrain;

class EntityAiRuntimeTest {
    @Test void honorsBudgetAndRoundRobinsAcrossBrains() {
        Plugin plugin = mock(Plugin.class);
        when(plugin.getLogger()).thenReturn(Logger.getAnonymousLogger());
        AiBrain first = brain(true); AiBrain second = brain(true); AiBrain third = brain(true);
        EntityAiRuntime runtime = new EntityAiRuntime(plugin, 2);
        runtime.register(first); runtime.register(second); runtime.register(third);
        assertEquals(2, runtime.budget());
        assertEquals(3, runtime.all().size());

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::getCurrentTick).thenReturn(50);
            runtime.tick();
            verify(first).tick(50); verify(second).tick(50); verify(third, never()).tick(50);
            runtime.tick();
            verify(third).tick(50);
        }
        runtime.close();
        assertEquals(0, runtime.size());
    }

    @Test void removesInvalidAgentWithoutTickingIt() {
        Plugin plugin = mock(Plugin.class);
        when(plugin.getLogger()).thenReturn(Logger.getAnonymousLogger());
        AiBrain invalid = brain(false);
        EntityAiRuntime runtime = new EntityAiRuntime(plugin, 1);
        runtime.register(invalid);
        runtime.tick();
        verify(invalid, never()).tick(org.mockito.ArgumentMatchers.anyLong());
        assertEquals(0, runtime.size());
    }

    private AiBrain brain(boolean valid) {
        AiAgent agent = mock(AiAgent.class);
        when(agent.id()).thenReturn(UUID.randomUUID());
        when(agent.valid()).thenReturn(valid);
        AiBrain brain = mock(AiBrain.class);
        when(brain.agent()).thenReturn(agent);
        return brain;
    }
}
