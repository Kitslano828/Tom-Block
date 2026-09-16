package org.tomdang.region.visualization;

public record RegionVisualizationSettings(
		long intervalTicks,
		int radius,
		int boundarySpacing,
		int maximumMarkers,
		int targetDistance
) {
	public RegionVisualizationSettings {
		if (intervalTicks < 1) throw new IllegalArgumentException("intervalTicks must be positive");
		if (radius < 1) throw new IllegalArgumentException("radius must be positive");
		if (boundarySpacing < 1) throw new IllegalArgumentException("boundarySpacing must be positive");
		if (maximumMarkers < 1) throw new IllegalArgumentException("maximumMarkers must be positive");
		if (targetDistance < 1) throw new IllegalArgumentException("targetDistance must be positive");
	}
}
