package org.tomdang.region.override;

import org.junit.jupiter.api.Test;
import org.tomdang.region.position.BlockPosition;
import org.tomdang.region.shape.CuboidRegionShape;
import org.tomdang.region.shape.RegionShape;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegionOverridesTest {
	@Test
	void exclusionWinsOverShapeMembershipAndExplicitInclusion() {
		BlockPosition position = position(1);
		RegionOverrides overrides = new RegionOverrides(Set.of(position), Set.of(position));

		assertFalse(overrides.resolve(position, true));
		assertFalse(overrides.resolve(position, false));
	}

	@Test
	void inclusionAddsDisconnectedPositionOutsideShape() {
		BlockPosition included = position(100);
		RegionOverrides overrides = new RegionOverrides(Set.of(included), Set.of());

		assertTrue(overrides.resolve(included, false));
		assertFalse(overrides.resolve(position(101), false));
	}

	@Test
	void supportsExactABASandwichMembership() {
		BlockPosition left = position(0);
		BlockPosition middle = position(1);
		BlockPosition right = position(2);
		RegionShape broadA = new CuboidRegionShape(left, right);
		RegionOverrides regionA = new RegionOverrides(Set.of(), Set.of(middle));
		RegionOverrides regionB = new RegionOverrides(Set.of(middle), Set.of());

		assertTrue(regionA.resolve(left, broadA.contains(left)));
		assertFalse(regionA.resolve(middle, broadA.contains(middle)));
		assertTrue(regionA.resolve(right, broadA.contains(right)));
		assertTrue(regionB.resolve(middle, false));
		assertFalse(regionB.resolve(left, false));
		assertFalse(regionB.resolve(right, false));
	}

	@Test
	void defensivelyCopiesOverridesAndReturnsImmutableViews() {
		Set<BlockPosition> mutable = new HashSet<>();
		mutable.add(position(1));
		RegionOverrides overrides = new RegionOverrides(mutable, Set.of());
		mutable.clear();

		assertTrue(overrides.resolve(position(1), false));
		assertThrows(UnsupportedOperationException.class, () -> overrides.inclusions().clear());
	}

	@Test
	void rejectsInvalidCollectionsAndPositions() {
		assertThrows(IllegalArgumentException.class, () -> new RegionOverrides(null, Set.of()));
		assertThrows(IllegalArgumentException.class, () -> new RegionOverrides(Set.of(), null));
		assertThrows(IllegalArgumentException.class,
				() -> new RegionOverrides(new HashSet<>(Arrays.asList((BlockPosition) null)), Set.of()));
		assertThrows(IllegalArgumentException.class,
				() -> new RegionOverrides(Set.of(), new HashSet<>(Arrays.asList((BlockPosition) null))));
		assertThrows(IllegalArgumentException.class, () -> RegionOverrides.empty().resolve(null, false));
	}

	private BlockPosition position(int x) {
		return new BlockPosition("world", x, 64, 0);
	}
}
