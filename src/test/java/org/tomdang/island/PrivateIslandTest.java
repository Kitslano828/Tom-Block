package org.tomdang.island;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrivateIslandTest {
	@Test void starterUsesStableSafeWorldNamingConvention() {
		UUID owner = UUID.randomUUID();
		PrivateIsland island = PrivateIsland.starter(owner);
		assertEquals(owner, island.ownerId());
		assertEquals("PRIVATE_STARTER", island.presetKey());
		assertTrue(island.worldName().matches("island_[a-f0-9]{32}"));
	}

	@Test void serviceReturnsTheSameIslandForAnOwner() {
		PrivateIslandService service = new PrivateIslandService(new InMemoryPrivateIslandRepository());
		UUID owner = UUID.randomUUID();
		assertSame(service.getOrCreate(owner), service.getOrCreate(owner));
	}
}
