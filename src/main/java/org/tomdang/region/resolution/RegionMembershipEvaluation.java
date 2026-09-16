package org.tomdang.region.resolution;

public record RegionMembershipEvaluation(boolean member, RegionMembershipSource source) {
	public RegionMembershipEvaluation {
		if (source == null) throw new IllegalArgumentException("source cannot be null");
	}
}
