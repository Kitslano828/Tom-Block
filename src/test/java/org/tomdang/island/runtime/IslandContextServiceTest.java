package org.tomdang.island.runtime;

import org.junit.jupiter.api.Test;
import org.tomdang.island.PrivateIsland;
import org.tomdang.island.preset.IslandPresetConfigurationLoader;
import org.tomdang.island.preset.IslandPresetRegistry;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class IslandContextServiceTest {
	@Test void resolvesPublicAndOwnedPrivateWorldsWithoutDatabaseQueries() {
		IslandPresetRegistry presets = new IslandPresetConfigurationLoader().load(
				getClass().getClassLoader().getResourceAsStream("island-presets.yml"));
		IslandContextService service = new IslandContextService(presets);
		UUID owner = UUID.randomUUID();
		PrivateIsland island = PrivateIsland.starter(owner);
		service.register(island, presets.require(island.presetKey()));
		assertEquals(IslandRole.OWNER, service.resolve(island.worldName(), owner).orElseThrow().role());
		assertEquals(IslandRole.VISITOR, service.resolve(island.worldName(), UUID.randomUUID()).orElseThrow().role());
		assertEquals("SOUTHWEST_PUBLIC", service.runtime("world").orElseThrow().preset().id());
	}
}
