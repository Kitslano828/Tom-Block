package org.tomdang.activity;

/** Reusable multiplayer contract for hunting, combat, fishing, and future activities. */
public record ActivityPolicy(ActivityVisibility visibility, ActivityAudience interaction,
		ActivityContributionPolicy contribution, ActivityAudience rewards) {
	public static final ActivityPolicy PRIVATE_SOLO = new ActivityPolicy(ActivityVisibility.OWNER,
			ActivityAudience.OWNER, ActivityContributionPolicy.BLOCK, ActivityAudience.OWNER);
	public static final ActivityPolicy PUBLIC_READ_ONLY = new ActivityPolicy(ActivityVisibility.NEARBY,
			ActivityAudience.OWNER, ActivityContributionPolicy.BLOCK, ActivityAudience.OWNER);
	public ActivityPolicy {
		if (visibility == null || interaction == null || contribution == null || rewards == null)
			throw new IllegalArgumentException("Activity policy is incomplete");
	}
}
