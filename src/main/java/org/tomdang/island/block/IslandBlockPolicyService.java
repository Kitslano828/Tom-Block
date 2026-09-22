package org.tomdang.island.block;

import org.tomdang.island.preset.IslandAccessPolicy;
import org.tomdang.island.preset.IslandInteractionPolicy;
import org.tomdang.island.runtime.IslandRole;

public final class IslandBlockPolicyService {
	public BlockInteractionAction classifyBreak(boolean playerPlaced, boolean registeredResource) {
		if (playerPlaced) return BlockInteractionAction.BREAK_PLAYER_PLACED;
		if (registeredResource) return BlockInteractionAction.BREAK_REGISTERED_RESOURCE;
		return BlockInteractionAction.BREAK_TERRAIN;
	}

	public boolean allows(IslandInteractionPolicy policy, IslandRole role, BlockInteractionAction action) {
		IslandAccessPolicy access = switch (action) {
			case PLACE -> policy.placement();
			case BREAK_PLAYER_PLACED -> policy.playerPlacedBreaking();
			case BREAK_REGISTERED_RESOURCE -> policy.registeredResources();
			case BREAK_TERRAIN -> policy.terrainBreaking();
		};
		return access.allows(role);
	}
}
