package org.tomdang.region.shape;

import org.junit.jupiter.api.Test;
import org.tomdang.region.position.BlockPosition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompositeRegionShapeTest {
	@Test
	void combinesDisconnectedShapesAcrossARegion() {
		RegionShape west = cuboid(0, 0, 2, 2);
		RegionShape east = cuboid(10, 10, 12, 12);
		CompositeRegionShape composite = new CompositeRegionShape(List.of(west, east));

		assertTrue(composite.contains(position(1, 64, 1)));
		assertTrue(composite.contains(position(11, 64, 11)));
		assertFalse(composite.contains(position(6, 64, 6)));
	}

	@Test
	void defensivelyCopiesShapes() {
		List<RegionShape> mutable = new ArrayList<>();
		mutable.add(cuboid(0, 0, 2, 2));
		CompositeRegionShape composite = new CompositeRegionShape(mutable);
		mutable.clear();

		assertEquals(1, composite.shapes().size());
		assertTrue(composite.contains(position(1, 64, 1)));
		assertThrows(UnsupportedOperationException.class, () -> composite.shapes().clear());
	}

	@Test
	void rejectsInvalidCollectionsAndPositions() {
		assertThrows(IllegalArgumentException.class, () -> new CompositeRegionShape(null));
		assertThrows(IllegalArgumentException.class, () -> new CompositeRegionShape(List.of()));
		assertThrows(IllegalArgumentException.class,
				() -> new CompositeRegionShape(Arrays.asList((RegionShape) null)));
		CompositeRegionShape composite = new CompositeRegionShape(List.of(cuboid(0, 0, 1, 1)));
		assertThrows(IllegalArgumentException.class, () -> composite.contains(null));
	}

	private RegionShape cuboid(int minX, int minZ, int maxX, int maxZ) {
		return new CuboidRegionShape(
				new BlockPosition("world", minX, 0, minZ),
				new BlockPosition("world", maxX, 255, maxZ));
	}

	private BlockPosition position(int x, int y, int z) {
		return new BlockPosition("world", x, y, z);
	}
}
