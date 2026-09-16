package org.tomdang.region.tracking;

import java.util.List;

public record RegionMembershipTransition(RegionMembershipSnapshot previous, RegionMembershipSnapshot current,
		List<String> entered, List<String> left) {
	public RegionMembershipTransition {
		entered = List.copyOf(entered);
		left = List.copyOf(left);
	}

	public boolean primaryChanged() {
		return !previous.primary().equals(current.primary());
	}

	public boolean membershipChanged() {
		return !entered.isEmpty() || !left.isEmpty();
	}
}
