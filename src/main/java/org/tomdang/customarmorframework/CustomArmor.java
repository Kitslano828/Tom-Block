package org.tomdang.customarmorframework;

import lombok.Getter;
import org.bukkit.Color;
import org.bukkit.Material;
import org.tomdang.customitemframework.CustomItem;
import org.tomdang.customitemframework.ItemCategory;
import org.tomdang.customitemframework.Rarity;
import org.tomdang.customitemframework.stats.CustomItemStatModifiers;
import org.tomdang.player.stats.PlayerStatType;

import java.util.Map;

public class CustomArmor extends CustomItem {
	// Armor should have every stat as there will be armor for each skill and eventually one that is a jack of all trades
	@Getter
	private final ArmorSlot armorSlot;
	@Getter
	private final Color color;

	public CustomArmor(String id, Material material, String displayName, Rarity rarity,
					   ItemCategory itemCategory, ArmorSlot armorSlot, double health, double defense, Color color) {
		this(id, material, displayName, rarity, itemCategory, armorSlot, color,
				new CustomItemStatModifiers(Map.of(
						PlayerStatType.MAX_HEALTH, health,
						PlayerStatType.DEFENSE, defense
				)));
	}

	public CustomArmor(String id, Material material, String displayName, Rarity rarity,
	                   ItemCategory itemCategory, ArmorSlot armorSlot, Color color,
	                   CustomItemStatModifiers statModifiers) {
		super(id, material, displayName, rarity, itemCategory, statModifiers);
		if (armorSlot == null) throw new IllegalArgumentException("armorSlot cannot be null");
		if (color == null) throw new IllegalArgumentException("color cannot be null");
		this.armorSlot = armorSlot;
		this.color = color;
	}

	public double getHealth() {
		return getStatModifiers().get(PlayerStatType.MAX_HEALTH);
	}

	public double getDefense() {
		return getStatModifiers().get(PlayerStatType.DEFENSE);
	}
}
