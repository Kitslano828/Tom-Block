package org.tomdang.combat.weapons;

import lombok.Getter;
import org.bukkit.Material;
import org.tomdang.customabilityframework.customability.CustomAbility;
import org.tomdang.customitemframework.CustomItem;
import org.tomdang.customitemframework.ItemCategory;
import org.tomdang.customitemframework.Rarity;

import java.util.ArrayList;
import java.util.List;

public class Weapon extends CustomItem {

	@Getter
	private final double damage;
	@Getter
	private final double strength;

	// For Later:
	/*
		- Crit Chance
		- Double Hit Chance (for one hit to count as 2)
		- Attack Speed (reduce the cooldown before the player can attack again?)
	 */

	public Weapon(String id, Material material, String displayName, Rarity rarity, ItemCategory itemCategory,
				  double damage, double strength) {
		super(id, material, displayName, rarity, itemCategory);
		this.damage = damage;
		this.strength = strength;
	}
}
