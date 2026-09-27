package org.tomdang.activity;

import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActivityAccessServiceTest {
    private final ActivityAccessService access = new ActivityAccessService();
    private final UUID owner = UUID.randomUUID();
    private final UUID participant = UUID.randomUUID();
    private final UUID observer = UUID.randomUUID();

    @Test void publicReadOnlyActivityIsVisibleButOnlyOwnerCanActOrEarnRewards() {
        var activity = new ActivityInstance(UUID.randomUUID(), owner, Set.of(owner, participant),
                ActivityPolicy.PUBLIC_READ_ONLY);

        assertTrue(access.canView(activity, observer));
        assertTrue(access.canInteract(activity, owner));
        assertFalse(access.canInteract(activity, participant));
        assertTrue(access.blocksUnauthorizedContribution(activity));
        assertEquals(Set.of(owner), access.rewardRecipients(activity));
    }

    @Test void participantPolicyCanSupportPartyActivitiesWithoutChangingTheRuntime() {
        var policy = new ActivityPolicy(ActivityVisibility.PARTICIPANTS, ActivityAudience.PARTICIPANTS,
                ActivityContributionPolicy.CREDIT, ActivityAudience.PARTICIPANTS);
        var activity = new ActivityInstance(UUID.randomUUID(), owner, Set.of(owner, participant), policy);

        assertTrue(access.canView(activity, participant));
        assertFalse(access.canView(activity, observer));
        assertTrue(access.canInteract(activity, participant));
        assertEquals(Set.of(owner, participant), access.rewardRecipients(activity));
    }
}
