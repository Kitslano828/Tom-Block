package org.tomdang.region.visualization;

import org.junit.jupiter.api.Test;
import org.tomdang.region.definition.RegionDefinition;
import org.tomdang.region.override.RegionOverrideRepository;
import org.tomdang.region.override.RegionOverrideService;
import org.tomdang.region.override.RegionOverrideState;
import org.tomdang.region.override.RegionOverrides;
import org.tomdang.region.position.BlockPosition;
import org.tomdang.region.registry.RegionRegistry;
import org.tomdang.region.shape.CompositeRegionShape;
import org.tomdang.region.shape.CuboidRegionShape;
import org.tomdang.region.shape.RegionShape;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegionVisualizationServiceTest {
	@Test
	void samplesCuboidEdgesWithoutFillingInterior() {
		Fixture fixture = fixture(new CuboidRegionShape(position(0, 0, 0), position(4, 4, 4)),
				new RegionVisualizationSettings(10, 20, 1, 1000, 20));

		List<RegionVisualizationMarker> markers = fixture.visualization.markers("REGION", position(2, 2, 2));

		assertTrue(markers.stream().anyMatch(marker -> marker.position().equals(position(0, 0, 0))));
		assertFalse(markers.stream().anyMatch(marker -> marker.position().equals(position(2, 2, 2))));
		assertTrue(markers.stream().allMatch(marker -> marker.type() == RegionVisualizationMarkerType.BOUNDARY));
	}

	@Test
	void limitsMarkersByRadiusAndConfiguredMaximum() {
		Fixture fixture = fixture(new CuboidRegionShape(position(0, 0, 0), position(100, 100, 100)),
				new RegionVisualizationSettings(10, 5, 1, 3, 20));

		List<RegionVisualizationMarker> markers = fixture.visualization.markers("REGION", position(0, 0, 0));

		assertEquals(3, markers.size());
		assertTrue(markers.stream().allMatch(marker -> distanceSquared(marker.position(), position(0, 0, 0)) <= 25));
	}

	@Test
	void runtimeOverridesArePrioritizedAheadOfBoundaries() {
		Fixture fixture = fixture(new CuboidRegionShape(position(0, 0, 0), position(10, 10, 10)),
				new RegionVisualizationSettings(10, 20, 1, 1, 20));
		BlockPosition included = position(5, 5, 5);
		fixture.overrides.setState("REGION", included, RegionOverrideState.INCLUSION);

		List<RegionVisualizationMarker> markers = fixture.visualization.markers("REGION", position(5, 5, 5));

		assertEquals(List.of(new RegionVisualizationMarker(included, RegionVisualizationMarkerType.INCLUSION)), markers);
	}

	@Test
	void supportsDisconnectedCompositeCuboids() {
		RegionShape shape = new CompositeRegionShape(List.of(
				new CuboidRegionShape(position(0, 0, 0), position(2, 2, 2)),
				new CuboidRegionShape(position(10, 0, 0), position(12, 2, 2))));
		Fixture fixture = fixture(shape, new RegionVisualizationSettings(10, 20, 1, 1000, 20));

		List<RegionVisualizationMarker> markers = fixture.visualization.markers("REGION", position(6, 1, 1));

		assertTrue(markers.stream().anyMatch(marker -> marker.position().x() == 0));
		assertTrue(markers.stream().anyMatch(marker -> marker.position().x() == 12));
	}

	private Fixture fixture(RegionShape shape, RegionVisualizationSettings settings) {
		RegionRegistry registry = new RegionRegistry();
		registry.register(new RegionDefinition("REGION", Optional.empty(), 0, Set.of(), shape, RegionOverrides.empty()));
		RegionOverrideRepository repository = new RegionOverrideRepository() {
			@Override public Map<String, RegionOverrides> load() { return Map.of(); }
			@Override public void save(Map<String, RegionOverrides> overrides) { }
		};
		RegionOverrideService overrides = new RegionOverrideService(registry, repository);
		return new Fixture(overrides, new RegionVisualizationService(registry, overrides, settings));
	}

	private long distanceSquared(BlockPosition first, BlockPosition second) {
		long x = first.x() - second.x();
		long y = first.y() - second.y();
		long z = first.z() - second.z();
		return x * x + y * y + z * z;
	}

	private BlockPosition position(int x, int y, int z) {
		return new BlockPosition("world", x, y, z);
	}

	private record Fixture(RegionOverrideService overrides, RegionVisualizationService visualization) { }
}
