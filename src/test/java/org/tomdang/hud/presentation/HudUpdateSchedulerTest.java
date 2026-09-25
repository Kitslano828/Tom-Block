package org.tomdang.hud.presentation;

import org.junit.jupiter.api.Test;
import org.tomdang.hud.composition.HudElementId;
import org.tomdang.hud.presentation.scheduling.*;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class HudUpdateSchedulerTest {
	@Test void publishesOnlySemanticChangesAndHonorsThrottle() {
		HudUpdateScheduler scheduler = new HudUpdateScheduler();
		UUID player = UUID.randomUUID();
		HudElementId id = HudElementId.of("test", "status");
		HudUpdatePolicy policy = new HudUpdatePolicy(5);
		assertTrue(scheduler.shouldPublish(player, id, 1, 10, policy));
		assertFalse(scheduler.shouldPublish(player, id, 1, 20, policy));
		assertFalse(scheduler.shouldPublish(player, id, 2, 12, policy));
		assertTrue(scheduler.shouldPublish(player, id, 2, 15, policy));
		scheduler.forget(player, id);
		assertTrue(scheduler.shouldPublish(player, id, 2, 16, policy));
	}
}
