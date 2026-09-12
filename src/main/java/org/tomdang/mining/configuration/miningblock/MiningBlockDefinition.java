package org.tomdang.mining.configuration.miningblock;

import org.bukkit.Material;

import java.util.List;

public record MiningBlockDefinition(
		Material material,
		int blockStrength,
		int breakingPower,
		int xp,
		long regenerationTimeSeconds,
		List<MiningDropDefinition> drops
) {
}
