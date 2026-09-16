package org.tomdang.region.visualization;

import org.tomdang.region.position.BlockPosition;

public record RegionVisualizationMarker(BlockPosition position, RegionVisualizationMarkerType type) {
	public RegionVisualizationMarker {
		if (position == null) throw new IllegalArgumentException("position cannot be null");
		if (type == null) throw new IllegalArgumentException("type cannot be null");
	}
}
