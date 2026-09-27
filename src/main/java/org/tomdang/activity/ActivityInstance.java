package org.tomdang.activity;

import java.util.Set;
import java.util.UUID;

/** Runtime identity and authority boundary independent of any one gameplay skill. */
public record ActivityInstance(UUID instanceId, UUID ownerId, Set<UUID> participants, ActivityPolicy policy) {
	public ActivityInstance {
		if (instanceId == null || ownerId == null || participants == null || policy == null)
			throw new IllegalArgumentException("Activity instance is incomplete");
		participants = Set.copyOf(participants);
		if (!participants.contains(ownerId)) throw new IllegalArgumentException("Owner must be a participant");
	}
}
