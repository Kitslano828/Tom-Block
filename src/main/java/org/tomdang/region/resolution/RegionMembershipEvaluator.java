package org.tomdang.region.resolution;

import org.tomdang.region.definition.RegionDefinition;
import org.tomdang.region.override.RegionOverrideState;
import org.tomdang.region.override.RegionRuntimeOverrideProvider;
import org.tomdang.region.position.BlockPosition;

public final class RegionMembershipEvaluator {
	private final RegionRuntimeOverrideProvider runtimeOverrides;

	public RegionMembershipEvaluator(RegionRuntimeOverrideProvider runtimeOverrides) {
		if (runtimeOverrides == null) throw new IllegalArgumentException("runtimeOverrides cannot be null");
		this.runtimeOverrides = runtimeOverrides;
	}

	public RegionMembershipEvaluation evaluate(RegionDefinition region, BlockPosition position) {
		if (region == null) throw new IllegalArgumentException("region cannot be null");
		if (position == null) throw new IllegalArgumentException("position cannot be null");
		RegionOverrideState runtimeState = runtimeOverrides.state(region.id(), position);
		if (runtimeState == RegionOverrideState.EXCLUSION) {
			return new RegionMembershipEvaluation(false, RegionMembershipSource.RUNTIME_EXCLUSION);
		}
		if (runtimeState == RegionOverrideState.INCLUSION) {
			return new RegionMembershipEvaluation(true, RegionMembershipSource.RUNTIME_INCLUSION);
		}
		if (region.overrides().exclusions().contains(position)) {
			return new RegionMembershipEvaluation(false, RegionMembershipSource.CONFIGURED_EXCLUSION);
		}
		if (region.overrides().inclusions().contains(position)) {
			return new RegionMembershipEvaluation(true, RegionMembershipSource.CONFIGURED_INCLUSION);
		}
		boolean shapeMembership = region.shape().contains(position);
		return new RegionMembershipEvaluation(shapeMembership,
				shapeMembership ? RegionMembershipSource.SHAPE : RegionMembershipSource.OUTSIDE);
	}
}
