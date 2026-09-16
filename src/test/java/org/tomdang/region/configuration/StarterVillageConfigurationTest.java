package org.tomdang.region.configuration;

import org.junit.jupiter.api.Test;
import org.tomdang.player.stats.PlayerStatType;
import org.tomdang.region.policy.RegionStatCapConfigurationLoader;
import org.tomdang.region.position.BlockPosition;
import org.tomdang.region.registry.RegionRegistry;
import org.tomdang.region.resolution.RegionResolver;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class StarterVillageConfigurationTest {
	@Test
	void bundledVillageContainsBothCornersAndHasSpeedPolicy() throws Exception {
		RegionRegistry registry = new RegionRegistry();
		try (var regions = new InputStreamReader(
				getClass().getResourceAsStream("/regions.yml"), StandardCharsets.UTF_8)) {
			registry.registerAll(new RegionConfigurationLoader().loadDefinitions(regions).stream()
					.map(new RegionConfigurationConverter()::convert).toList());
		}
		RegionResolver resolver = new RegionResolver(registry);
		assertTrue(resolver.regionsAt(new BlockPosition("world", 107, 68, -171)).stream()
				.anyMatch(region -> region.id().equals("STARTER_VILLAGE")));
		assertTrue(resolver.regionsAt(new BlockPosition("world", 295, 66, 1)).stream()
				.anyMatch(region -> region.id().equals("STARTER_VILLAGE")));
		assertFalse(registry.require("STARTER_VILLAGE").directlyContains(new BlockPosition("world", 296, 66, 1)));
		try (var caps = new InputStreamReader(
				getClass().getResourceAsStream("/region-stat-caps.yml"), StandardCharsets.UTF_8)) {
			assertEquals(200, new RegionStatCapConfigurationLoader().load(caps, registry)
					.get("STARTER_VILLAGE").get(PlayerStatType.SPEED));
		}
	}
}
