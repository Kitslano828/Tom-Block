package org.tomdang.worldmap;

import org.junit.jupiter.api.Test;
import org.tomdang.region.position.BlockPosition;

import static org.junit.jupiter.api.Assertions.*;

class VillageFollowingMapHudLayoutTest {
	private final VillageFollowingMapHudLayout layout = new VillageFollowingMapHudLayout();

	@Test
	void followsPlayerInOverlappingViews() {
		assertEquals(new VillageFollowingMapHudLayout.Selection(0, 32, 8),
				layout.select(new BlockPosition("world", 73, 80, -213)));
		assertEquals(0, layout.select(new BlockPosition("world", 88, 80, -213)).index());
		assertEquals(1, layout.select(new BlockPosition("world", 89, 80, -213)).index());
		assertEquals(32, layout.select(new BlockPosition("world", 105, 80, -213)).markerX());
		assertEquals(9, layout.select(new BlockPosition("world", 73, 80, -181)).index());
	}

	@Test
	void limitsPrototypeToVillageAndIgnoresOtherWorlds() {
		assertNotNull(layout.select(new BlockPosition("world", 328, 80, 42)));
		assertNull(layout.select(new BlockPosition("world", 329, 80, 42)));
		assertNull(layout.select(new BlockPosition("other", 73, 80, -213)));
	}
}
