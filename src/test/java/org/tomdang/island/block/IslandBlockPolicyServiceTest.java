package org.tomdang.island.block;

import org.junit.jupiter.api.Test;
import org.tomdang.island.preset.IslandAccessPolicy;
import org.tomdang.island.preset.IslandInteractionPolicy;
import org.tomdang.island.runtime.IslandRole;

import static org.junit.jupiter.api.Assertions.*;

class IslandBlockPolicyServiceTest {
	private final IslandBlockPolicyService service = new IslandBlockPolicyService();
	private final IslandInteractionPolicy policy = new IslandInteractionPolicy(
			IslandAccessPolicy.MEMBERS, IslandAccessPolicy.MEMBERS, IslandAccessPolicy.DENY,
			IslandAccessPolicy.EVERYONE, false, false, false);

	@Test void playerOriginTakesPriorityOverAResourceMaterial() {
		assertEquals(BlockInteractionAction.BREAK_PLAYER_PLACED, service.classifyBreak(true, true));
		assertFalse(service.allows(policy, IslandRole.VISITOR,
				service.classifyBreak(true, true)), "visitor must not obtain resource rewards from a placed ore or log");
	}

	@Test void registeredResourcesTakePriorityOverOrdinaryTerrain() {
		assertEquals(BlockInteractionAction.BREAK_REGISTERED_RESOURCE, service.classifyBreak(false, true));
		assertTrue(service.allows(policy, IslandRole.VISITOR, BlockInteractionAction.BREAK_REGISTERED_RESOURCE));
		assertFalse(service.allows(policy, IslandRole.VISITOR, BlockInteractionAction.BREAK_TERRAIN));
	}

	@Test void memberCanBuildAndRemoveTheirPlacedBlocks() {
		assertTrue(service.allows(policy, IslandRole.MEMBER, BlockInteractionAction.PLACE));
		assertTrue(service.allows(policy, IslandRole.MEMBER, BlockInteractionAction.BREAK_PLAYER_PLACED));
	}
}
