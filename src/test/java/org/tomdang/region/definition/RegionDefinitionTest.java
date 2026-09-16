package org.tomdang.region.definition;

import org.junit.jupiter.api.Test;
import org.tomdang.region.override.RegionOverrides;
import org.tomdang.region.position.BlockPosition;
import org.tomdang.region.shape.CuboidRegionShape;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegionDefinitionTest {
	@Test
	void resolvesShapeMembershipThroughOverrides() {
		BlockPosition included = position(20);
		BlockPosition excluded = position(5);
		RegionDefinition definition = new RegionDefinition(
				" village ", Optional.empty(), 10, Set.of(" safe "),
				new CuboidRegionShape(position(0), position(10)),
				new RegionOverrides(Set.of(included), Set.of(excluded)));

		assertEquals("village", definition.id());
		assertEquals(Set.of("safe"), definition.tags());
		assertTrue(definition.directlyContains(position(1)));
		assertFalse(definition.directlyContains(excluded));
		assertTrue(definition.directlyContains(included));
	}

	@Test
	void defensivelyCopiesTags() {
		Set<String> tags = new HashSet<>(Set.of("safe"));
		RegionDefinition definition = definition("village", Optional.empty(), tags);
		tags.clear();

		assertEquals(Set.of("safe"), definition.tags());
		assertThrows(UnsupportedOperationException.class, () -> definition.tags().clear());
	}

	@Test
	void rejectsInvalidDefinitionData() {
		assertThrows(IllegalArgumentException.class, () -> definition(" ", Optional.empty(), Set.of()));
		assertThrows(IllegalArgumentException.class, () -> definition("village", null, Set.of()));
		assertThrows(IllegalArgumentException.class,
				() -> definition("village", Optional.of(" "), Set.of()));
		assertThrows(IllegalArgumentException.class, () -> definition("village", Optional.empty(), null));
		assertThrows(IllegalArgumentException.class,
				() -> definition("village", Optional.empty(), new HashSet<>(java.util.Arrays.asList((String) null))));
		assertThrows(IllegalArgumentException.class, () -> definition("village", Optional.empty(), Set.of(" ")));
		assertThrows(IllegalArgumentException.class, () -> new RegionDefinition(
				"village", Optional.empty(), 0, Set.of(), null, RegionOverrides.empty()));
		assertThrows(IllegalArgumentException.class, () -> new RegionDefinition(
				"village", Optional.empty(), 0, Set.of(), shape(), null));
		assertThrows(IllegalArgumentException.class, () -> definition("village", Optional.empty(), Set.of())
				.directlyContains(null));
	}

	private RegionDefinition definition(String id, Optional<String> parentId, Set<String> tags) {
		return new RegionDefinition(id, parentId, 0, tags, shape(), RegionOverrides.empty());
	}

	private CuboidRegionShape shape() {
		return new CuboidRegionShape(position(0), position(10));
	}

	private BlockPosition position(int x) {
		return new BlockPosition("world", x, 64, 0);
	}
}
