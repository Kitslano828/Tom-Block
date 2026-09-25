package org.tomdang.hud.protocol;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class HudPackStateRegistryTest {
	@Test void renderingRequiresConfirmedLoadedPack() {
		HudPackStateRegistry states = new HudPackStateRegistry();
		UUID player = UUID.randomUUID();
		assertFalse(states.canRender(player));
		states.update(player, HudPackState.ACCEPTED);
		assertFalse(states.canRender(player));
		states.update(player, HudPackState.LOADED);
		assertTrue(states.canRender(player));
		states.update(player, HudPackState.ACCEPTED);
		assertEquals(HudPackState.LOADED, states.state(player));
		states.remove(player);
		assertEquals(HudPackState.UNKNOWN, states.state(player));
	}
}
