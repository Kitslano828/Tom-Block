package org.tomdang.collection;

public record CollectionMilestone(long amount, long skillXpReward) {
	public CollectionMilestone {
		if (amount <= 0 || skillXpReward < 0) throw new IllegalArgumentException("Invalid collection milestone");
	}
}
