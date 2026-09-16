package org.tomdang.region.edit;

import org.tomdang.region.override.RegionOverrideState;
import org.tomdang.region.position.BlockPosition;

public record RegionEditAction(
		String regionId,
		BlockPosition position,
		RegionOverrideState previousState,
		RegionOverrideState appliedState
) {
	public RegionEditAction {
		if (regionId == null || regionId.isBlank()) throw new IllegalArgumentException("regionId cannot be null or blank");
		if (position == null) throw new IllegalArgumentException("position cannot be null");
		if (previousState == null) throw new IllegalArgumentException("previousState cannot be null");
		if (appliedState == null) throw new IllegalArgumentException("appliedState cannot be null");
	}
}
