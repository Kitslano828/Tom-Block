package org.tomdang.region.definition;

import org.tomdang.region.override.RegionOverrides;
import org.tomdang.region.position.BlockPosition;
import org.tomdang.region.shape.RegionShape;

import java.util.Optional;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public record RegionDefinition(
		String id,
		String displayName,
		Optional<String> parentId,
		int priority,
		Set<String> tags,
		RegionShape shape,
		RegionOverrides overrides
) {
	public RegionDefinition(String id, Optional<String> parentId, int priority, Set<String> tags,
			RegionShape shape, RegionOverrides overrides) {
		this(id, id, parentId, priority, tags, shape, overrides);
	}

	public RegionDefinition {
		id = requireIdentifier(id, "id");
		displayName = requireIdentifier(displayName, "displayName");
		if (parentId == null) throw new IllegalArgumentException("parentId cannot be null");
		parentId = parentId.map(value -> requireIdentifier(value, "parentId"));
		if (tags == null) throw new IllegalArgumentException("tags cannot be null");
		if (tags.stream().anyMatch(tag -> tag == null || tag.isBlank())) {
			throw new IllegalArgumentException("tags cannot contain null or blank entries");
		}
		LinkedHashSet<String> normalizedTags = new LinkedHashSet<>();
		tags.stream().map(String::trim).forEach(normalizedTags::add);
		tags = Collections.unmodifiableSet(normalizedTags);
		if (shape == null) throw new IllegalArgumentException("shape cannot be null");
		if (overrides == null) throw new IllegalArgumentException("overrides cannot be null");
	}

	public boolean directlyContains(BlockPosition position) {
		if (position == null) throw new IllegalArgumentException("position cannot be null");
		return overrides.resolve(position, shape.contains(position));
	}

	private static String requireIdentifier(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " cannot be null or blank");
		}
		return value.trim();
	}
}
