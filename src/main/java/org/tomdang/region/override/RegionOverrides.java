package org.tomdang.region.override;

import org.tomdang.region.position.BlockPosition;

import java.util.Set;

public final class RegionOverrides {
	private final Set<BlockPosition> inclusions;
	private final Set<BlockPosition> exclusions;

	public RegionOverrides(Set<BlockPosition> inclusions, Set<BlockPosition> exclusions) {
		if (inclusions == null) throw new IllegalArgumentException("inclusions cannot be null");
		if (exclusions == null) throw new IllegalArgumentException("exclusions cannot be null");
		if (inclusions.stream().anyMatch(java.util.Objects::isNull)) {
			throw new IllegalArgumentException("inclusions cannot contain null entries");
		}
		if (exclusions.stream().anyMatch(java.util.Objects::isNull)) {
			throw new IllegalArgumentException("exclusions cannot contain null entries");
		}
		this.inclusions = Set.copyOf(inclusions);
		this.exclusions = Set.copyOf(exclusions);
	}

	public static RegionOverrides empty() {
		return new RegionOverrides(Set.of(), Set.of());
	}

	public boolean resolve(BlockPosition position, boolean shapeMembership) {
		if (position == null) throw new IllegalArgumentException("position cannot be null");
		if (exclusions.contains(position)) return false;
		if (inclusions.contains(position)) return true;
		return shapeMembership;
	}

	public Set<BlockPosition> inclusions() {
		return inclusions;
	}

	public Set<BlockPosition> exclusions() {
		return exclusions;
	}
}
