package org.tomdang.region.visualization;

import org.tomdang.region.definition.RegionDefinition;
import org.tomdang.region.override.RegionOverrideService;
import org.tomdang.region.override.RegionOverrideState;
import org.tomdang.region.override.RegionOverrides;
import org.tomdang.region.position.BlockPosition;
import org.tomdang.region.registry.RegionRegistry;
import org.tomdang.region.shape.CompositeRegionShape;
import org.tomdang.region.shape.CuboidRegionShape;
import org.tomdang.region.shape.RegionShape;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class RegionVisualizationService {
	private final RegionRegistry regionRegistry;
	private final RegionOverrideService overrideService;
	private final RegionVisualizationSettings settings;

	public RegionVisualizationService(RegionRegistry regionRegistry, RegionOverrideService overrideService,
			RegionVisualizationSettings settings) {
		if (regionRegistry == null) throw new IllegalArgumentException("regionRegistry cannot be null");
		if (overrideService == null) throw new IllegalArgumentException("overrideService cannot be null");
		if (settings == null) throw new IllegalArgumentException("settings cannot be null");
		this.regionRegistry = regionRegistry;
		this.overrideService = overrideService;
		this.settings = settings;
	}

	public List<RegionVisualizationMarker> markers(String regionId, BlockPosition viewer) {
		if (viewer == null) throw new IllegalArgumentException("viewer cannot be null");
		RegionDefinition region = regionRegistry.require(regionId);
		List<RegionVisualizationMarker> output = new ArrayList<>();
		RegionOverrides runtime = overrideService.snapshot().getOrDefault(region.id(), RegionOverrides.empty());
		addOverrides(output, region, runtime, viewer);

		Set<BlockPosition> boundary = new LinkedHashSet<>();
		collectBoundary(region.shape(), viewer, boundary);
		boundary.stream()
				.sorted(Comparator.comparingLong(position -> distanceSquared(position, viewer)))
				.forEach(position -> addWithinLimit(output,
						new RegionVisualizationMarker(position, RegionVisualizationMarkerType.BOUNDARY)));
		return List.copyOf(output);
	}

	private void addOverrides(List<RegionVisualizationMarker> output, RegionDefinition region,
			RegionOverrides runtime, BlockPosition viewer) {
		Set<BlockPosition> candidates = new LinkedHashSet<>();
		candidates.addAll(region.overrides().inclusions());
		candidates.addAll(region.overrides().exclusions());
		candidates.addAll(runtime.inclusions());
		candidates.addAll(runtime.exclusions());
		candidates.stream()
				.filter(position -> inRange(position, viewer))
				.map(position -> markerForOverride(region, position))
				.sorted(Comparator.comparingLong(marker -> distanceSquared(marker.position(), viewer)))
				.forEach(marker -> addWithinLimit(output, marker));
	}

	private RegionVisualizationMarker markerForOverride(RegionDefinition region, BlockPosition position) {
		RegionOverrideState runtimeState = overrideService.state(region.id(), position);
		boolean inclusion = runtimeState == RegionOverrideState.INCLUSION
				|| (runtimeState == RegionOverrideState.NONE
				&& !region.overrides().exclusions().contains(position)
				&& region.overrides().inclusions().contains(position));
		return new RegionVisualizationMarker(position, inclusion
				? RegionVisualizationMarkerType.INCLUSION
				: RegionVisualizationMarkerType.EXCLUSION);
	}

	private void collectBoundary(RegionShape shape, BlockPosition viewer, Set<BlockPosition> output) {
		if (shape instanceof CuboidRegionShape cuboid) {
			collectCuboid(cuboid, viewer, output);
		} else if (shape instanceof CompositeRegionShape composite) {
			composite.shapes().forEach(child -> collectBoundary(child, viewer, output));
		}
	}

	private void collectCuboid(CuboidRegionShape cuboid, BlockPosition viewer, Set<BlockPosition> output) {
		BlockPosition minimum = cuboid.minimum();
		BlockPosition maximum = cuboid.maximum();
		if (!minimum.worldId().equals(viewer.worldId())) return;
		for (int y : distinct(minimum.y(), maximum.y())) {
			for (int z : distinct(minimum.z(), maximum.z())) addXEdge(output, viewer, minimum, maximum, y, z);
		}
		for (int x : distinct(minimum.x(), maximum.x())) {
			for (int z : distinct(minimum.z(), maximum.z())) addYEdge(output, viewer, minimum, maximum, x, z);
		}
		for (int x : distinct(minimum.x(), maximum.x())) {
			for (int y : distinct(minimum.y(), maximum.y())) addZEdge(output, viewer, minimum, maximum, x, y);
		}
	}

	private void addXEdge(Set<BlockPosition> output, BlockPosition viewer, BlockPosition minimum,
			BlockPosition maximum, int y, int z) {
		for (int x : samples(minimum.x(), maximum.x(), viewer.x())) addIfVisible(output, new BlockPosition(viewer.worldId(), x, y, z), viewer);
	}

	private void addYEdge(Set<BlockPosition> output, BlockPosition viewer, BlockPosition minimum,
			BlockPosition maximum, int x, int z) {
		for (int y : samples(minimum.y(), maximum.y(), viewer.y())) addIfVisible(output, new BlockPosition(viewer.worldId(), x, y, z), viewer);
	}

	private void addZEdge(Set<BlockPosition> output, BlockPosition viewer, BlockPosition minimum,
			BlockPosition maximum, int x, int y) {
		for (int z : samples(minimum.z(), maximum.z(), viewer.z())) addIfVisible(output, new BlockPosition(viewer.worldId(), x, y, z), viewer);
	}

	private List<Integer> samples(int minimum, int maximum, int viewerCoordinate) {
		int start = Math.max(minimum, viewerCoordinate - settings.radius());
		int end = Math.min(maximum, viewerCoordinate + settings.radius());
		if (start > end) return List.of();
		List<Integer> samples = new ArrayList<>();
		for (int value = start; value <= end; value += settings.boundarySpacing()) samples.add(value);
		if (samples.isEmpty() || samples.getLast() != end) samples.add(end);
		return samples;
	}

	private List<Integer> distinct(int first, int second) {
		return first == second ? List.of(first) : List.of(first, second);
	}

	private void addIfVisible(Set<BlockPosition> output, BlockPosition position, BlockPosition viewer) {
		if (inRange(position, viewer)) output.add(position);
	}

	private boolean inRange(BlockPosition position, BlockPosition viewer) {
		return position.worldId().equals(viewer.worldId())
				&& distanceSquared(position, viewer) <= (long) settings.radius() * settings.radius();
	}

	private long distanceSquared(BlockPosition first, BlockPosition second) {
		long x = (long) first.x() - second.x();
		long y = (long) first.y() - second.y();
		long z = (long) first.z() - second.z();
		return x * x + y * y + z * z;
	}

	private void addWithinLimit(List<RegionVisualizationMarker> output, RegionVisualizationMarker marker) {
		if (output.size() < settings.maximumMarkers()) output.add(marker);
	}
}
