package org.tomdang.region.registry;

import org.junit.jupiter.api.Test;
import org.tomdang.region.definition.RegionDefinition;
import org.tomdang.region.override.RegionOverrides;
import org.tomdang.region.position.BlockPosition;
import org.tomdang.region.shape.CuboidRegionShape;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RegionRegistryTest {
	@Test
	void registersParentAndChildInEitherBatchOrder() {
		RegionRegistry registry = new RegionRegistry();
		RegionDefinition child = definition("library", "village");
		RegionDefinition parent = definition("village", null);

		registry.registerAll(List.of(child, parent));

		assertSame(child, registry.require("library"));
		assertEquals(List.of(child, parent), registry.all().stream().toList());
	}

	@Test
	void rejectsDuplicatesAndUnknownParentsWithoutPartiallyMutatingRegistry() {
		RegionRegistry registry = new RegionRegistry();
		RegionDefinition existing = definition("swamp", null);
		registry.register(existing);

		assertThrows(IllegalArgumentException.class, () -> registry.register(definition("swamp", null)));
		assertThrows(IllegalArgumentException.class,
				() -> registry.registerAll(List.of(definition("village", "missing"))));

		assertEquals(List.of(existing), registry.all().stream().toList());
		assertFalse(registry.find("village").isPresent());
	}

	@Test
	void rejectsSelfParentAndLongerParentCyclesAtomically() {
		RegionRegistry registry = new RegionRegistry();

		assertThrows(IllegalArgumentException.class,
				() -> registry.register(definition("village", "village")));
		assertThrows(IllegalArgumentException.class, () -> registry.registerAll(List.of(
				definition("a", "b"), definition("b", "c"), definition("c", "a"))));

		assertEquals(0, registry.all().size());
	}

	@Test
	void exposesReadOnlySnapshotsAndHandlesLookupValidation() {
		RegionRegistry registry = new RegionRegistry();
		registry.register(definition("village", null));

		assertEquals("village", registry.find(" village ").orElseThrow().id());
		assertFalse(registry.find(null).isPresent());
		assertFalse(registry.find(" ").isPresent());
		assertThrows(IllegalArgumentException.class, () -> registry.require("missing"));
		assertThrows(UnsupportedOperationException.class, () -> registry.all().clear());
		assertThrows(IllegalArgumentException.class, () -> registry.registerAll(null));
		assertThrows(IllegalArgumentException.class,
				() -> registry.registerAll(java.util.Arrays.asList((RegionDefinition) null)));
	}

	private RegionDefinition definition(String id, String parentId) {
		BlockPosition point = new BlockPosition("world", 0, 64, 0);
		return new RegionDefinition(id, Optional.ofNullable(parentId), 0, Set.of(),
				new CuboidRegionShape(point, point), RegionOverrides.empty());
	}
}
