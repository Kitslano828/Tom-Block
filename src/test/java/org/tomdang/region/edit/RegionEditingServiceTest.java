package org.tomdang.region.edit;

import org.junit.jupiter.api.Test;
import org.tomdang.region.definition.RegionDefinition;
import org.tomdang.region.override.RegionOverrideRepository;
import org.tomdang.region.override.RegionOverrideService;
import org.tomdang.region.override.RegionOverrideState;
import org.tomdang.region.override.RegionOverrides;
import org.tomdang.region.position.BlockPosition;
import org.tomdang.region.registry.RegionRegistry;
import org.tomdang.region.shape.CuboidRegionShape;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegionEditingServiceTest {
	@Test
	void appliesAndUndoesChangesForSelectedRegion() {
		Fixture fixture = fixture(10);
		UUID playerId = UUID.randomUUID();
		BlockPosition position = position(20);
		fixture.editing.begin(playerId, "VILLAGE");

		fixture.editing.apply(playerId, position, RegionOverrideState.INCLUSION);
		assertEquals(RegionOverrideState.INCLUSION, fixture.overrides.state("VILLAGE", position));
		assertTrue(fixture.editing.hasUnsavedChanges());

		RegionEditAction undone = fixture.editing.undo(playerId).orElseThrow();
		assertEquals(RegionOverrideState.NONE, undone.previousState());
		assertEquals(RegionOverrideState.NONE, fixture.overrides.state("VILLAGE", position));
		assertTrue(fixture.editing.undo(playerId).isEmpty());
	}

	@Test
	void rejectsUnknownRegionsAndPlayersWithoutSessions() {
		Fixture fixture = fixture(10);
		UUID playerId = UUID.randomUUID();

		assertThrows(IllegalArgumentException.class, () -> fixture.editing.begin(playerId, "MISSING"));
		assertThrows(IllegalStateException.class,
				() -> fixture.editing.apply(playerId, position(1), RegionOverrideState.INCLUSION));
		assertThrows(IllegalStateException.class, () -> fixture.editing.undo(playerId));
	}

	@Test
	void sessionHistoryIsBounded() {
		Fixture fixture = fixture(2);
		UUID playerId = UUID.randomUUID();
		RegionEditSession session = fixture.editing.begin(playerId, "VILLAGE");

		fixture.editing.apply(playerId, position(20), RegionOverrideState.INCLUSION);
		fixture.editing.apply(playerId, position(21), RegionOverrideState.INCLUSION);
		fixture.editing.apply(playerId, position(22), RegionOverrideState.INCLUSION);

		assertEquals(2, session.historySize());
		assertEquals(position(22), fixture.editing.undo(playerId).orElseThrow().position());
		assertEquals(position(21), fixture.editing.undo(playerId).orElseThrow().position());
		assertTrue(fixture.editing.undo(playerId).isEmpty());
	}

	private Fixture fixture(int historyLimit) {
		RegionRegistry registry = new RegionRegistry();
		registry.register(new RegionDefinition("VILLAGE", Optional.empty(), 0, Set.of(),
				new CuboidRegionShape(position(0), position(10)), RegionOverrides.empty()));
		RegionOverrideRepository repository = new RegionOverrideRepository() {
			@Override public Map<String, RegionOverrides> load() { return Map.of(); }
			@Override public void save(Map<String, RegionOverrides> overrides) { }
		};
		RegionOverrideService overrides = new RegionOverrideService(registry, repository);
		return new Fixture(overrides, new RegionEditingService(
				registry, overrides, new RegionEditSessionRegistry(historyLimit)));
	}

	private BlockPosition position(int x) {
		return new BlockPosition("world", x, 64, 0);
	}

	private record Fixture(RegionOverrideService overrides, RegionEditingService editing) { }
}
