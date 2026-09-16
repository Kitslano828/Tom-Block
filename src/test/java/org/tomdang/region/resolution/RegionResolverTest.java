package org.tomdang.region.resolution;

import org.junit.jupiter.api.Test;
import org.tomdang.region.definition.RegionDefinition;
import org.tomdang.region.override.RegionOverrides;
import org.tomdang.region.position.BlockPosition;
import org.tomdang.region.registry.RegionRegistry;
import org.tomdang.region.shape.CuboidRegionShape;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegionResolverTest {
	@Test
	void resolvesDirectRegionAndItsSemanticAncestorsWithoutDuplicatingGeometry() {
		RegionRegistry registry = new RegionRegistry();
		RegionDefinition swamp = definition("swamp", null, 10, 100, 110);
		RegionDefinition village = definition("village", "swamp", 20, 200, 210);
		RegionDefinition library = definition("library", "village", 30, 0, 10);
		registry.registerAll(List.of(library, village, swamp));
		RegionResolver resolver = new RegionResolver(registry);

		assertEquals(List.of("library"), ids(resolver.directRegionsAt(position(5))));
		assertEquals(List.of("library", "village", "swamp"), ids(resolver.regionsAt(position(5))));
		assertEquals("library", resolver.primaryRegionAt(position(5)).orElseThrow().id());
	}

	@Test
	void resolvesIndependentOverlapsByPriorityThenIdentifier() {
		RegionRegistry registry = new RegionRegistry();
		RegionDefinition low = definition("low", null, 5, 0, 10);
		RegionDefinition beta = definition("beta", null, 10, 0, 10);
		RegionDefinition alpha = definition("alpha", null, 10, 0, 10);
		registry.registerAll(List.of(low, beta, alpha));
		RegionResolver resolver = new RegionResolver(registry);

		assertEquals(List.of("alpha", "beta", "low"), ids(resolver.directRegionsAt(position(5))));
		assertEquals(List.of("alpha", "beta", "low"), ids(resolver.regionsAt(position(5))));
		assertEquals("alpha", resolver.primaryRegionAt(position(5)).orElseThrow().id());
	}

	@Test
	void deduplicatesRegionThatIsBothDirectAndInherited() {
		RegionRegistry registry = new RegionRegistry();
		RegionDefinition parent = definition("village", null, 5, 0, 10);
		RegionDefinition child = definition("library", "village", 10, 0, 10);
		registry.registerAll(List.of(parent, child));
		RegionResolver resolver = new RegionResolver(registry);

		assertEquals(List.of("library", "village"), ids(resolver.regionsAt(position(5))));
	}

	@Test
	void returnsEmptyResultsWhenNothingMatches() {
		RegionRegistry registry = new RegionRegistry();
		registry.register(definition("village", null, 10, 0, 10));
		RegionResolver resolver = new RegionResolver(registry);

		assertTrue(resolver.directRegionsAt(position(50)).isEmpty());
		assertTrue(resolver.regionsAt(position(50)).isEmpty());
		assertTrue(resolver.primaryRegionAt(position(50)).isEmpty());
	}

	@Test
	void rejectsInvalidDependenciesAndPositions() {
		assertThrows(IllegalArgumentException.class, () -> new RegionResolver(null));
		RegionResolver resolver = new RegionResolver(new RegionRegistry());
		assertThrows(IllegalArgumentException.class, () -> resolver.directRegionsAt(null));
		assertThrows(IllegalArgumentException.class, () -> resolver.regionsAt(null));
		assertThrows(IllegalArgumentException.class, () -> resolver.primaryRegionAt(null));
	}

	private RegionDefinition definition(String id, String parentId, int priority, int minimumX, int maximumX) {
		return new RegionDefinition(id, Optional.ofNullable(parentId), priority, Set.of(),
				new CuboidRegionShape(position(minimumX), position(maximumX)), RegionOverrides.empty());
	}

	private List<String> ids(List<RegionDefinition> definitions) {
		return definitions.stream().map(RegionDefinition::id).toList();
	}

	private BlockPosition position(int x) {
		return new BlockPosition("world", x, 64, 0);
	}
}
