package org.tomdang.region.override;

import org.tomdang.region.position.BlockPosition;

public interface RegionRuntimeOverrideProvider {
	RegionOverrideState state(String regionId, BlockPosition position);

	default boolean resolve(String regionId, BlockPosition position, boolean configuredMembership) {
		return switch (state(regionId, position)) {
			case INCLUSION -> true;
			case EXCLUSION -> false;
			case NONE -> configuredMembership;
		};
	}

	static RegionRuntimeOverrideProvider empty() {
		return (regionId, position) -> RegionOverrideState.NONE;
	}
}
