package org.tomdang.region.shape;

import org.junit.jupiter.api.Test;
import org.tomdang.region.position.BlockPosition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CuboidRegionShapeTest {
	@Test
	void containsInteriorAndEveryInclusiveBoundary() {
		CuboidRegionShape shape = shape(position("world", 10, 60, 20), position("world", 20, 70, 30));

		assertTrue(shape.contains(position("world", 15, 65, 25)));
		assertTrue(shape.contains(position("world", 10, 60, 20)));
		assertTrue(shape.contains(position("world", 20, 70, 30)));
		assertTrue(shape.contains(position("world", 10, 70, 30)));
	}

	@Test
	void rejectsPositionsOutsideAnyAxisOrInAnotherWorld() {
		CuboidRegionShape shape = shape(position("world", 10, 60, 20), position("world", 20, 70, 30));

		assertFalse(shape.contains(position("world", 9, 65, 25)));
		assertFalse(shape.contains(position("world", 15, 71, 25)));
		assertFalse(shape.contains(position("world", 15, 65, 31)));
		assertFalse(shape.contains(position("world_nether", 15, 65, 25)));
	}

	@Test
	void normalizesReversedCorners() {
		CuboidRegionShape shape = shape(position("world", 20, 70, 30), position("world", 10, 60, 20));

		assertEquals(position("world", 10, 60, 20), shape.minimum());
		assertEquals(position("world", 20, 70, 30), shape.maximum());
		assertTrue(shape.contains(position("world", 15, 65, 25)));
	}

	@Test
	void rejectsNullAndCrossWorldInputs() {
		BlockPosition corner = position("world", 0, 0, 0);
		assertThrows(IllegalArgumentException.class, () -> shape(null, corner));
		assertThrows(IllegalArgumentException.class, () -> shape(corner, null));
		assertThrows(IllegalArgumentException.class,
				() -> shape(corner, position("world_nether", 1, 1, 1)));
		assertThrows(IllegalArgumentException.class,
				() -> shape(corner, position("world", 1, 1, 1)).contains(null));
	}

	private CuboidRegionShape shape(BlockPosition first, BlockPosition second) {
		return new CuboidRegionShape(first, second);
	}

	private BlockPosition position(String world, int x, int y, int z) {
		return new BlockPosition(world, x, y, z);
	}
}
