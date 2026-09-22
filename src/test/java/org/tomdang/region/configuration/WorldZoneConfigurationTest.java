package org.tomdang.region.configuration;

import org.junit.jupiter.api.Test;
import org.tomdang.region.position.BlockPosition;
import org.tomdang.region.registry.RegionRegistry;
import org.tomdang.region.resolution.RegionResolver;
import org.tomdang.region.shape.PolygonPrismRegionShape;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class WorldZoneConfigurationTest {
	@Test
	void bundledOutlinesResolveLocationsAheadOfWilderness() throws Exception {
		RegionRegistry registry = new RegionRegistry();
		try (var reader = new InputStreamReader(getClass().getResourceAsStream("/regions.yml"),
				StandardCharsets.UTF_8)) {
			registry.registerAll(new RegionConfigurationLoader().loadDefinitions(reader).stream()
					.map(new RegionConfigurationConverter()::convert).toList());
		}
		assertEquals(23, registry.all().size());
		assertInstanceOf(PolygonPrismRegionShape.class, registry.require("JELLYFISH_HUNTING_GROUNDS").shape());
		RegionResolver resolver = new RegionResolver(registry);
		assertEquals("Mushroom Island", primaryName(resolver, 1230, -150));
		assertEquals("Starter Village", primaryName(resolver, 190, -100));
		assertEquals("Desert Settlement", primaryName(resolver, -1100, -700));
		assertEquals("Southwest Island", primaryName(resolver, -950, 400));
		assertEquals("Cherry Town", primaryName(resolver, 850, 150));
		assertEquals("Central Meadows", primaryName(resolver, 400, 300));
		assertTrue(resolver.primaryRegionAt(new BlockPosition("world", 0, 70, -1000)).isEmpty());
		assertTrue(resolver.primaryRegionAt(new BlockPosition("other", 1230, 70, -150)).isEmpty());
	}

	private String primaryName(RegionResolver resolver, int x, int z) {
		return resolver.primaryRegionAt(new BlockPosition("world", x, 70, z))
				.orElseThrow().displayName();
	}
}
