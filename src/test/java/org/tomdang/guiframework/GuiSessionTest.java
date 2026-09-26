package org.tomdang.guiframework;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GuiSessionTest {
    @Test
    void navigationRetainsHistoryAndBackRestoresPriorState() {
        GuiSession session = new GuiSession(UUID.randomUUID(), new GuiRoute("INDEX", Map.of("page", 2)));
        session.navigate(new GuiRoute("DETAIL", Map.of("critter", "MOSSBACK")));

        assertEquals("DETAIL", session.current().screenId());
        assertEquals(1, session.historyDepth());
        assertTrue(session.back());
        assertEquals("INDEX", session.current().screenId());
        assertEquals(2, session.current().state().get("page"));
        assertFalse(session.back());
    }

    @Test
    void revisionsAdvanceMonotonically() {
        GuiSession session = new GuiSession(UUID.randomUUID(), new GuiRoute("INDEX", Map.of()));
        assertEquals(1, session.nextRevision());
        assertEquals(2, session.nextRevision());
        assertEquals(2, session.revision());
    }

    @Test
    void tokenPreventsStaleSessionClosure() {
        GuiSessionRegistry registry = new GuiSessionRegistry();
        UUID player = UUID.randomUUID();
        GuiSession old = registry.open(player, new GuiRoute("ONE", Map.of()));
        GuiSession current = registry.open(player, new GuiRoute("TWO", Map.of()));

        assertFalse(registry.close(player, old.token()));
        assertSame(current, registry.find(player).orElseThrow());
        assertTrue(registry.close(player, current.token()));
    }
}
