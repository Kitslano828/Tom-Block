package org.tomdang.activity;

import java.util.Set;
import java.util.UUID;

/** Single authority for activity visibility, interaction, contribution and rewards. */
public final class ActivityAccessService {
	public boolean canView(ActivityInstance activity, UUID playerId) {
		require(activity, playerId);
		return switch (activity.policy().visibility()) {
			case OWNER -> activity.ownerId().equals(playerId);
			case PARTICIPANTS -> activity.participants().contains(playerId);
			case NEARBY, WORLD -> true;
		};
	}
	public boolean canInteract(ActivityInstance activity, UUID playerId) {
		require(activity, playerId);
		return allowed(activity, playerId, activity.policy().interaction());
	}
	public boolean blocksUnauthorizedContribution(ActivityInstance activity) {
		return activity.policy().contribution() == ActivityContributionPolicy.BLOCK;
	}
	public Set<UUID> rewardRecipients(ActivityInstance activity) {
		return switch (activity.policy().rewards()) {
			case OWNER -> Set.of(activity.ownerId());
			case PARTICIPANTS, ANYONE -> activity.participants();
		};
	}
	private boolean allowed(ActivityInstance activity, UUID playerId, ActivityAudience audience) {
		return switch (audience) {
			case OWNER -> activity.ownerId().equals(playerId);
			case PARTICIPANTS -> activity.participants().contains(playerId);
			case ANYONE -> true;
		};
	}
	private void require(ActivityInstance activity, UUID playerId) {
		if (activity == null || playerId == null) throw new IllegalArgumentException("Activity and player are required");
	}
}
