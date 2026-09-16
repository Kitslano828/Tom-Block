package org.tomdang.region.tracking;

import org.junit.jupiter.api.Test;
import org.tomdang.region.definition.RegionDefinition;
import org.tomdang.region.override.RegionOverrides;
import org.tomdang.region.position.BlockPosition;
import org.tomdang.region.registry.RegionRegistry;
import org.tomdang.region.resolution.RegionResolver;
import org.tomdang.region.shape.CuboidRegionShape;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PlayerRegionTrackingServiceTest {
	private final UUID player = UUID.randomUUID();
	private final List<RegionMembershipTransition> changes = new ArrayList<>();

	@Test
	void tracksOverlappingAndInheritedRegionsAndPrimaryChanges() {
		RegionRegistry registry = new RegionRegistry();
		registry.registerAll(List.of(region("parent", null, 1, "world", 100, 101),
				region("child", "parent", 20, "world", 0, 5),
				region("overlap", null, 10, "world", 3, 8)));
		PlayerRegionTrackingService service = service(registry);
		service.update(player, position("world", 2));
		assertEquals(List.of("child", "parent"), changes.getLast().entered());
		assertEquals(List.of("child"), service.snapshot(player).orElseThrow().direct());
		service.update(player, position("world", 4));
		assertEquals(List.of("overlap"), changes.getLast().entered());
		assertFalse(changes.getLast().primaryChanged());
		service.update(player, position("world", 7));
		assertEquals(List.of("child", "parent"), changes.getLast().left());
		assertEquals(Optional.of("overlap"), changes.getLast().current().primary());
	}

	@Test
	void ignoresSameBlockButRefreshesAfterAnEdit() {
		RegionRegistry registry = new RegionRegistry();
		registry.register(region("area", null, 1, "world", 0, 5));
		PlayerRegionTrackingService service = service(registry);
		service.update(player, position("world", 1));
		service.update(player, position("world", 1));
		service.refresh(player, position("world", 1));
		assertEquals(1, changes.size());
		service.clear(player);
		assertTrue(service.snapshot(player).isEmpty());
	}

	@Test
	void teleportAndWorldChangeLeaveOldRegions() {
		RegionRegistry registry = new RegionRegistry();
		registry.registerAll(List.of(region("world_area", null, 1, "world", 0, 5),
				region("nether_area", null, 1, "nether", 0, 5)));
		PlayerRegionTrackingService service = service(registry);
		service.update(player, position("world", 1));
		service.update(player, position("world", 20));
		assertEquals(List.of("world_area"), changes.getLast().left());
		service.update(player, position("nether", 1));
		assertEquals(List.of("nether_area"), changes.getLast().entered());
	}

	private PlayerRegionTrackingService service(RegionRegistry registry) {
		return new PlayerRegionTrackingService(new RegionResolver(registry), (id, change) -> changes.add(change));
	}

	private RegionDefinition region(String id, String parent, int priority, String world, int from, int to) {
		return new RegionDefinition(id, Optional.ofNullable(parent), priority, Set.of(),
				new CuboidRegionShape(position(world, from), position(world, to)), RegionOverrides.empty());
	}

	private BlockPosition position(String world, int x) {
		return new BlockPosition(world, x, 64, 0);
	}
}
