package org.tomdang.customitemframework.configuration;

import org.bukkit.Material;
import org.tomdang.customitemframework.ItemCategory;
import org.tomdang.customitemframework.Rarity;
import org.tomdang.customitemframework.stats.CustomItemStatModifiers;
import org.tomdang.customitemframework.stats.CustomItemStatCapModifiers;

public record CustomItemDefinition(String id,
		Material material,
		String displayName,
		Rarity rarity,
		ItemCategory itemCategory,
		CustomItemStatModifiers statModifiers,
		CustomItemStatCapModifiers statCapModifiers) {
	public CustomItemDefinition(String id, Material material, String displayName, Rarity rarity,
	                            ItemCategory itemCategory, CustomItemStatModifiers statModifiers) {
		this(id, material, displayName, rarity, itemCategory, statModifiers, CustomItemStatCapModifiers.empty());
	}
}
