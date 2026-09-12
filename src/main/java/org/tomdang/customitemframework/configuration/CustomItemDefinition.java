package org.tomdang.customitemframework.configuration;

import org.bukkit.Material;
import org.tomdang.customitemframework.ItemCategory;
import org.tomdang.customitemframework.Rarity;

public record CustomItemDefinition(String id,
		Material material,
		String displayName,
		Rarity rarity,
		ItemCategory itemCategory) {
}
