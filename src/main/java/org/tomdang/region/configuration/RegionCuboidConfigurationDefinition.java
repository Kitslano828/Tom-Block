package org.tomdang.region.configuration;

import org.tomdang.region.position.BlockPosition;

public record RegionCuboidConfigurationDefinition(BlockPosition minimum, BlockPosition maximum) {
	public RegionCuboidConfigurationDefinition {
		if (minimum == null) throw new IllegalArgumentException("minimum cannot be null");
		if (maximum == null) throw new IllegalArgumentException("maximum cannot be null");
		if (!minimum.worldId().equals(maximum.worldId())) {
			throw new IllegalArgumentException("Cuboid corners must use the same world");
		}
	}
}
