package org.tomdang.region.configuration;

import org.tomdang.region.shape.RegionPolygonVertex;

import java.util.List;

public record RegionPolygonConfigurationDefinition(String worldId, int minimumY, int maximumY,
		List<RegionPolygonVertex> vertices) {
	public RegionPolygonConfigurationDefinition {
		if (worldId == null || worldId.isBlank()) throw new IllegalArgumentException("worldId cannot be blank");
		worldId = worldId.trim();
		if (minimumY > maximumY) throw new IllegalArgumentException("minimumY cannot exceed maximumY");
		if (vertices == null || vertices.size() < 3 || vertices.stream().anyMatch(java.util.Objects::isNull)) {
			throw new IllegalArgumentException("polygon needs at least three non-null vertices");
		}
		vertices = List.copyOf(vertices);
	}
}
