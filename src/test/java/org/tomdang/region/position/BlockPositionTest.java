package org.tomdang.region.position;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BlockPositionTest {
	@Test
	void storesImmutableWorldAndBlockCoordinates() {
		BlockPosition position = new BlockPosition(" world ", 10, 64, -5);
		assertEquals("world", position.worldId());
		assertEquals(10, position.x());
		assertEquals(64, position.y());
		assertEquals(-5, position.z());
	}

	@Test
	void rejectsMissingWorldIdentity() {
		assertThrows(IllegalArgumentException.class, () -> new BlockPosition(null, 0, 0, 0));
		assertThrows(IllegalArgumentException.class, () -> new BlockPosition(" ", 0, 0, 0));
	}
}
