package org.tomdang.mining.configuration.miningtool;

import org.bukkit.Material;
import org.tomdang.customitemframework.Rarity;

import java.util.List;

public record MiningToolDefinition(String id, Material material, String displayName, Rarity rarity, int breakingPower, double miningSpeed, double fortune, List<String> abilityIDs) {
}
