package org.tomdang.collection;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CollectionConfigurationLoaderTest {
	@Test void loadsBundledCollectionsAndMilestones() {
		CollectionRegistry registry = new CollectionConfigurationLoader().load(
				getClass().getClassLoader().getResourceAsStream("collections.yml"));
		CollectionDefinition oak = registry.require("OAK_LOG");
		assertEquals(Material.OAK_LOG, oak.material());
		assertEquals("FORAGING:OAK_LOGS_BROKEN", oak.counterKey().value());
		assertEquals(250L, oak.nextMilestone(100));
		assertNull(oak.nextMilestone(5000));
	}

	@Test void rejectsDescendingMilestones() {
		assertThrows(IllegalArgumentException.class, () -> new CollectionDefinition("BAD", "Bad", "TEST",
				Material.STONE, org.tomdang.player.counter.CounterKey.of("TEST:BAD"), java.util.List.of(10L, 5L)));
	}
}
