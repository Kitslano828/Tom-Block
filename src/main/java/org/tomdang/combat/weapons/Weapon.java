package org.tomdang.combat.weapons;

import org.bukkit.Material;
import org.tomdang.customitemframework.CustomItem;
import org.tomdang.customitemframework.ItemCategory;
import org.tomdang.customitemframework.Rarity;
import org.tomdang.customitemframework.stats.CustomItemStatModifiers;
import org.tomdang.player.stats.PlayerStatType;

import java.util.Map;

public class Weapon extends CustomItem {

	// For Later:
	/*
		- Crit Chance
		- Double Hit Chance (for one hit to count as 2)
		- Attack Speed (reduce the cooldown before the player can attack again?)
	 */

	public Weapon(String id, Material material, String displayName, Rarity rarity, ItemCategory itemCategory,
				  double damage, double strength) {
		this(id, material, displayName, rarity, itemCategory, new CustomItemStatModifiers(Map.of(
				PlayerStatType.DAMAGE, damage,
				PlayerStatType.STRENGTH, strength
		)));
	}

	public Weapon(String id, Material material, String displayName, Rarity rarity, ItemCategory itemCategory,
				  CustomItemStatModifiers statModifiers) {
		super(id, material, displayName, rarity, itemCategory, statModifiers);
	}

	public double getDamage() {
		return getStatModifiers().get(PlayerStatType.DAMAGE);
	}

	public double getStrength() {
		return getStatModifiers().get(PlayerStatType.STRENGTH);
	}
}
