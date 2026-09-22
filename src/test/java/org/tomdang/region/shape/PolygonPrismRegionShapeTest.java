package org.tomdang.region.shape;

import org.junit.jupiter.api.Test;
import org.tomdang.region.position.BlockPosition;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PolygonPrismRegionShapeTest {
	private final PolygonPrismRegionShape shape = new PolygonPrismRegionShape("world", -64, 319,
			List.of(new RegionPolygonVertex(0, 0), new RegionPolygonVertex(10, 0),
					new RegionPolygonVertex(10, 10), new RegionPolygonVertex(5, 5),
					new RegionPolygonVertex(0, 10)));

	@Test
	void includesInteriorAndBoundaryButExcludesConcaveCutout() {
		assertTrue(shape.contains(at(2, 2, 64)));
		assertTrue(shape.contains(at(0, 5, -64)));
		assertTrue(shape.contains(at(5, 5, 319)));
		assertFalse(shape.contains(at(5, 9, 64)));
		assertFalse(shape.contains(at(11, 5, 64)));
	}

	@Test
	void rejectsWrongWorldAndHeight() {
		assertFalse(shape.contains(new BlockPosition("other", 2, 64, 2)));
		assertFalse(shape.contains(at(2, 2, -65)));
		assertFalse(shape.contains(at(2, 2, 320)));
	}

	@Test
	void validatesVerticesAndBounds() {
		assertThrows(IllegalArgumentException.class, () -> new PolygonPrismRegionShape(
				"world", 10, 0, shape.vertices()));
		assertThrows(IllegalArgumentException.class, () -> new PolygonPrismRegionShape(
				"world", 0, 10, shape.vertices().subList(0, 2)));
		assertThrows(IllegalArgumentException.class, () -> shape.contains(null));
	}

	private BlockPosition at(int x, int z, int y) {
		return new BlockPosition("world", x, y, z);
	}
}
