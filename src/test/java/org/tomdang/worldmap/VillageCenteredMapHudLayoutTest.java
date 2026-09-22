package org.tomdang.worldmap;

import org.junit.jupiter.api.Test;
import org.tomdang.region.position.BlockPosition;

import static org.junit.jupiter.api.Assertions.*;

class VillageCenteredMapHudLayoutTest {
	private final VillageCenteredMapHudLayout layout = new VillageCenteredMapHudLayout();

	@Test
	void choosesDenserVillageCropsAndKeepsMarkerAtCenter() {
		BlockPosition first = new BlockPosition("world", 73, 70, -213);
		BlockPosition next = new BlockPosition("world", 89, 70, -213);
		assertEquals(0, layout.indexAt(first));
		assertEquals(1, layout.indexAt(next));
		assertEquals(17, layout.indexAt(new BlockPosition("world", 73, 70, -197)));
		assertEquals(-1, layout.indexAt(new BlockPosition("world", 72, 70, -213)));
		assertNotEquals(layout.compose(first, 0), layout.compose(next, 0));
		assertNotEquals(layout.compose(first, 0), layout.compose(first, 45));
	}
}
