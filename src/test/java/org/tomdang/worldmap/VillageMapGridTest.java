package org.tomdang.worldmap;

import org.junit.jupiter.api.Test;
import org.tomdang.region.definition.RegionDefinition;
import org.tomdang.region.override.RegionOverrides;
import org.tomdang.region.position.BlockPosition;
import org.tomdang.region.shape.CuboidRegionShape;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class VillageMapGridTest {
	@Test
	void markerMovesWithPlayerAndDisappearsOutsideBounds() {
		VillageMapGrid grid = new VillageMapGrid(new RegionDefinition("VILLAGE", Optional.empty(), 1,
				Set.of(), new CuboidRegionShape(new BlockPosition("world", 0, 0, 0),
				new BlockPosition("world", 89, 100, 89)), new RegionOverrides(Set.of(), Set.of())));
		assertEquals('P', grid.row(new BlockPosition("world", 5, 50, 5), 0, 9).charAt(0));
		assertEquals('P', grid.row(new BlockPosition("world", 85, 50, 85), 8, 9).charAt(8));
		assertFalse(grid.row(new BlockPosition("other", 5, 50, 5), 0, 9).contains("P"));
		assertFalse(grid.row(new BlockPosition("world", 100, 50, 5), 0, 9).contains("P"));
	}
}
