package org.tomdang.region.override;

import org.junit.jupiter.api.Test;
import org.tomdang.region.definition.RegionDefinition;
import org.tomdang.region.position.BlockPosition;
import org.tomdang.region.registry.RegionRegistry;
import org.tomdang.region.shape.CuboidRegionShape;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegionOverrideServiceTest {
	@Test
	void editsAreMutuallyExclusiveAndClearingRestoresNone() throws Exception {
		MemoryRepository repository = new MemoryRepository();
		RegionOverrideService service = service(repository);
		BlockPosition position = position(5);

		assertEquals(RegionOverrideState.NONE,
				service.setState("VILLAGE", position, RegionOverrideState.INCLUSION));
		assertEquals(RegionOverrideState.INCLUSION, service.state("VILLAGE", position));
		assertEquals(RegionOverrideState.INCLUSION,
				service.setState("VILLAGE", position, RegionOverrideState.EXCLUSION));
		assertEquals(RegionOverrideState.EXCLUSION, service.state("VILLAGE", position));
		assertEquals(RegionOverrideState.EXCLUSION,
				service.setState("VILLAGE", position, RegionOverrideState.NONE));
		assertEquals(RegionOverrideState.NONE, service.state("VILLAGE", position));
		assertTrue(service.isDirty());
	}

	@Test
	void loadAndSaveUseImmutableSnapshotsAndDirtyTracking() throws Exception {
		MemoryRepository repository = new MemoryRepository();
		repository.stored = Map.of("VILLAGE", new RegionOverrides(Set.of(position(1)), Set.of()));
		RegionOverrideService service = service(repository);

		service.load();
		assertEquals(RegionOverrideState.INCLUSION, service.state("VILLAGE", position(1)));
		assertFalse(service.isDirty());
		service.setState("VILLAGE", position(2), RegionOverrideState.EXCLUSION);
		service.save();

		assertFalse(service.isDirty());
		assertEquals(Set.of(position(2)), repository.stored.get("VILLAGE").exclusions());
		assertThrows(UnsupportedOperationException.class, () -> service.snapshot().clear());
	}

	@Test
	void rejectsOverridesForUnknownRegionsOnLoadAndMutation() {
		MemoryRepository repository = new MemoryRepository();
		repository.stored = Map.of("MISSING", RegionOverrides.empty());
		RegionOverrideService service = service(repository);

		assertThrows(IllegalArgumentException.class, service::load);
		assertThrows(IllegalArgumentException.class,
				() -> service.setState("MISSING", position(1), RegionOverrideState.INCLUSION));
	}

	private RegionOverrideService service(MemoryRepository repository) {
		RegionRegistry registry = new RegionRegistry();
		registry.register(new RegionDefinition("VILLAGE", Optional.empty(), 0, Set.of(),
				new CuboidRegionShape(position(0), position(10)), RegionOverrides.empty()));
		return new RegionOverrideService(registry, repository);
	}

	private BlockPosition position(int x) {
		return new BlockPosition("world", x, 64, 0);
	}

	private static final class MemoryRepository implements RegionOverrideRepository {
		private Map<String, RegionOverrides> stored = Map.of();

		@Override
		public Map<String, RegionOverrides> load() {
			return stored;
		}

		@Override
		public void save(Map<String, RegionOverrides> overrides) throws IOException {
			stored = Map.copyOf(overrides);
		}
	}
}
