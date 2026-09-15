package org.tomdang.combat.weapons.configuration;

import org.bukkit.Material;
import org.tomdang.customitemframework.Rarity;
import org.tomdang.customitemframework.stats.CustomItemStatModifiers;
import org.tomdang.player.stats.PlayerStatType;

import java.util.List;

public record WeaponDefinition(String id, Material material, String displayName, Rarity rarity,
		CustomItemStatModifiers statModifiers, List<String> abilityIDs) {
	public double damage() {
		return statModifiers.get(PlayerStatType.DAMAGE);
	}

	public double strength() {
		return statModifiers.get(PlayerStatType.STRENGTH);
	}
}
