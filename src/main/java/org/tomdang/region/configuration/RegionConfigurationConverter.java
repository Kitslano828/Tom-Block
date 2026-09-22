package org.tomdang.region.configuration;

import org.tomdang.region.definition.RegionDefinition;
import org.tomdang.region.override.RegionOverrides;
import org.tomdang.region.shape.CompositeRegionShape;
import org.tomdang.region.shape.CuboidRegionShape;
import org.tomdang.region.shape.PolygonPrismRegionShape;
import org.tomdang.region.shape.RegionShape;

import java.util.List;

public final class RegionConfigurationConverter {
	public RegionDefinition convert(RegionConfigurationDefinition configuration) {
		if (configuration == null) throw new IllegalArgumentException("configuration cannot be null");
		List<RegionShape> shapes = new java.util.ArrayList<>();
		configuration.cuboids().stream()
				.map(cuboid -> (RegionShape) new CuboidRegionShape(cuboid.minimum(), cuboid.maximum()))
				.forEach(shapes::add);
		configuration.polygons().stream()
				.map(polygon -> (RegionShape) new PolygonPrismRegionShape(
						polygon.worldId(), polygon.minimumY(), polygon.maximumY(), polygon.vertices()))
				.forEach(shapes::add);
		RegionShape shape = shapes.size() == 1 ? shapes.getFirst() : new CompositeRegionShape(shapes);
		return new RegionDefinition(
				configuration.id(), configuration.displayName(), configuration.parentId(),
				configuration.priority(), configuration.tags(), shape,
				new RegionOverrides(configuration.inclusions(), configuration.exclusions()));
	}
}
