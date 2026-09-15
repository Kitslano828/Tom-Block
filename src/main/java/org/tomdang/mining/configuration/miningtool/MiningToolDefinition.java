package org.tomdang.mining.configuration.miningtool;

import org.bukkit.Material;
import org.tomdang.customitemframework.Rarity;
import org.tomdang.customitemframework.stats.CustomItemStatModifiers;
import org.tomdang.customitemframework.stats.CustomItemStatCapModifiers;
import org.tomdang.player.stats.PlayerStatType;

import java.util.List;

public record MiningToolDefinition(String id, Material material, String displayName, Rarity rarity,
		int breakingPower, CustomItemStatModifiers statModifiers,
		CustomItemStatCapModifiers statCapModifiers, List<String> abilityIDs) {
	public MiningToolDefinition(String id, Material material, String displayName, Rarity rarity,
	                            int breakingPower, CustomItemStatModifiers statModifiers, List<String> abilityIDs) {
		this(id, material, displayName, rarity, breakingPower, statModifiers,
				CustomItemStatCapModifiers.empty(), abilityIDs);
	}
	public double miningSpeed() {
		return statModifiers.get(PlayerStatType.MINING_SPEED);
	}

	public double fortune() {
		return statModifiers.get(PlayerStatType.MINING_FORTUNE);
	}
}
