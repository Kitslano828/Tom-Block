package org.tomdang.region.tracking;

import java.util.List;
import java.util.Optional;

/** Ordered region ids; resolved also includes semantic ancestors. */
public record RegionMembershipSnapshot(List<String> direct, List<String> resolved, Optional<String> primary) {
	public static final RegionMembershipSnapshot EMPTY = new RegionMembershipSnapshot(List.of(), List.of(), Optional.empty());

	public RegionMembershipSnapshot {
		direct = List.copyOf(direct);
		resolved = List.copyOf(resolved);
		primary = primary == null ? Optional.empty() : primary;
	}
}
