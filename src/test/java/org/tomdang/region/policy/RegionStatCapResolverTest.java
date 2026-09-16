package org.tomdang.region.policy;

import org.junit.jupiter.api.Test;
import org.tomdang.player.stats.PlayerStatType;
import org.tomdang.region.definition.RegionDefinition;
import org.tomdang.region.override.RegionOverrides;
import org.tomdang.region.position.BlockPosition;
import org.tomdang.region.registry.RegionRegistry;
import org.tomdang.region.resolution.RegionResolver;
import org.tomdang.region.shape.CuboidRegionShape;

import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RegionStatCapResolverTest {
	@Test
	void choosesHighestPriorityCapAndFallsBackToParent() {
		RegionRegistry regions = registry();
		RegionStatCapResolver caps = new RegionStatCapResolver(new RegionResolver(regions), Map.of(
				"village", Map.of(PlayerStatType.SPEED, 140.0),
				"library", Map.of(PlayerStatType.SPEED, 80.0),
				"overlap", Map.of(PlayerStatType.SPEED, 110.0)));
		assertEquals(80, caps.resolve(position(2), PlayerStatType.SPEED).orElseThrow().value());
		assertEquals("library", caps.resolve(position(2), PlayerStatType.SPEED).orElseThrow().locationId());
		assertTrue(caps.resolve(position(20), PlayerStatType.SPEED).isEmpty());
		assertTrue(caps.resolve(position(2), PlayerStatType.DAMAGE).isEmpty());
	}

	@Test
	void loadsValidatedStatCaps() {
		RegionRegistry regions = registry();
		RegionStatCapConfigurationLoader loader = new RegionStatCapConfigurationLoader();
		var loaded = loader.load(new StringReader("region-stat-caps:\n  library:\n    SPEED: 120\n    DAMAGE: 500\n"), regions);
		assertEquals(120, loaded.get("library").get(PlayerStatType.SPEED));
		assertThrows(IllegalArgumentException.class,
				() -> loader.load(new StringReader("region-stat-caps:\n  missing:\n    SPEED: 100\n"), regions));
		assertThrows(IllegalArgumentException.class,
				() -> loader.load(new StringReader("region-stat-caps:\n  library:\n    SPEED: -1\n"), regions));
	}

	private RegionRegistry registry() {
		RegionRegistry registry = new RegionRegistry();
		registry.registerAll(List.of(region("village", null, 5, 50, 60),
				region("library", "village", 20, 0, 5), region("overlap", null, 10, 0, 5)));
		return registry;
	}

	private RegionDefinition region(String id, String parent, int priority, int from, int to) {
		return new RegionDefinition(id, Optional.ofNullable(parent), priority, Set.of(),
				new CuboidRegionShape(position(from), position(to)), RegionOverrides.empty());
	}

	private BlockPosition position(int x) { return new BlockPosition("world", x, 64, 0); }
}
