package org.tomdang.worldmap;

import org.junit.jupiter.api.Test;
import org.tomdang.region.position.BlockPosition;

import static org.junit.jupiter.api.Assertions.*;

class VillageMapHudLayoutTest {
	private final VillageMapHudLayout layout = new VillageMapHudLayout();

	@Test
	void markerTracksCalibratedVillageTile() {
		assertEquals(new VillageMapHudLayout.Marker(0, 0),
				layout.markerAt(new BlockPosition("world", 73, 80, -213)));
		assertEquals(new VillageMapHudLayout.Marker(32, 8),
				layout.markerAt(new BlockPosition("world", 201, 80, -85)));
		assertEquals(new VillageMapHudLayout.Marker(63, 15),
				layout.markerAt(new BlockPosition("world", 328, 80, 42)));
	}

	@Test
	void switchesToNeighboringTilesAndPreservesLocalMarkerCoordinates() {
		assertEquals(new VillageMapHudLayout.Tile(7, 4, 63),
				layout.tileAt(new BlockPosition("world", 73, 80, -213)));
		assertEquals(new VillageMapHudLayout.Tile(8, 4, 64),
				layout.tileAt(new BlockPosition("world", 329, 80, -213)));
		assertEquals(new VillageMapHudLayout.Marker(0, 0),
				layout.markerAt(new BlockPosition("world", 329, 80, -213)));
		assertEquals(new VillageMapHudLayout.Tile(6, 4, 62),
				layout.tileAt(new BlockPosition("world", 72, 80, -213)));
		assertEquals(new VillageMapHudLayout.Tile(7, 5, 77),
				layout.tileAt(new BlockPosition("world", 73, 80, 43)));
	}

	@Test
	void worldEdgesRemainCoveredAndOtherWorldsAreNotMapped() {
		assertNotNull(layout.tileAt(new BlockPosition("world", -1536, 80, -1152)));
		assertNotNull(layout.tileAt(new BlockPosition("world", 1663, 80, 1151)));
		assertNull(layout.tileAt(new BlockPosition("world", -1537, 80, -1152)));
		assertNull(layout.markerAt(new BlockPosition("other", 201, 80, -85)));
	}
}
