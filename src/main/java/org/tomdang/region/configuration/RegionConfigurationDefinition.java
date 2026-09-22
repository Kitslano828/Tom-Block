package org.tomdang.region.configuration;

import org.tomdang.region.position.BlockPosition;

import java.util.List;
import java.util.Optional;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public record RegionConfigurationDefinition(
		String id,
		String displayName,
		Optional<String> parentId,
		int priority,
		Set<String> tags,
		List<RegionCuboidConfigurationDefinition> cuboids,
		List<RegionPolygonConfigurationDefinition> polygons,
		Set<BlockPosition> inclusions,
		Set<BlockPosition> exclusions
) {
	public RegionConfigurationDefinition(String id, Optional<String> parentId, int priority, Set<String> tags,
			List<RegionCuboidConfigurationDefinition> cuboids, Set<BlockPosition> inclusions,
			Set<BlockPosition> exclusions) {
		this(id, id, parentId, priority, tags, cuboids, List.of(), inclusions, exclusions);
	}

	public RegionConfigurationDefinition(String id, String displayName, Optional<String> parentId, int priority,
			Set<String> tags, List<RegionCuboidConfigurationDefinition> cuboids,
			Set<BlockPosition> inclusions, Set<BlockPosition> exclusions) {
		this(id, displayName, parentId, priority, tags, cuboids, List.of(), inclusions, exclusions);
	}

	public RegionConfigurationDefinition {
		if (id == null || id.isBlank()) throw new IllegalArgumentException("id cannot be null or blank");
		id = id.trim();
		if (displayName == null || displayName.isBlank()) {
			throw new IllegalArgumentException("displayName cannot be null or blank");
		}
		displayName = displayName.trim();
		if (parentId == null) throw new IllegalArgumentException("parentId cannot be null");
		parentId = parentId.map(parent -> {
			if (parent.isBlank()) throw new IllegalArgumentException("parentId cannot be blank");
			return parent.trim();
		});
		if (tags == null) throw new IllegalArgumentException("tags cannot be null");
		if (cuboids == null || polygons == null || (cuboids.isEmpty() && polygons.isEmpty())) {
			throw new IllegalArgumentException("a region needs at least one cuboid or polygon");
		}
		if (inclusions == null) throw new IllegalArgumentException("inclusions cannot be null");
		if (exclusions == null) throw new IllegalArgumentException("exclusions cannot be null");
		if (tags.stream().anyMatch(tag -> tag == null || tag.isBlank())) {
			throw new IllegalArgumentException("tags cannot contain null or blank entries");
		}
		if (cuboids.stream().anyMatch(java.util.Objects::isNull)) {
			throw new IllegalArgumentException("cuboids cannot contain null entries");
		}
		if (polygons.stream().anyMatch(java.util.Objects::isNull)) {
			throw new IllegalArgumentException("polygons cannot contain null entries");
		}
		if (inclusions.stream().anyMatch(java.util.Objects::isNull)) {
			throw new IllegalArgumentException("inclusions cannot contain null entries");
		}
		if (exclusions.stream().anyMatch(java.util.Objects::isNull)) {
			throw new IllegalArgumentException("exclusions cannot contain null entries");
		}
		LinkedHashSet<String> normalizedTags = new LinkedHashSet<>();
		tags.stream().map(String::trim).forEach(normalizedTags::add);
		tags = Collections.unmodifiableSet(normalizedTags);
		cuboids = List.copyOf(cuboids);
		polygons = List.copyOf(polygons);
		inclusions = Set.copyOf(inclusions);
		exclusions = Set.copyOf(exclusions);
	}
}
