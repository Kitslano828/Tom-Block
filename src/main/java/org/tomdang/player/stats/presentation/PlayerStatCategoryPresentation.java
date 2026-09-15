package org.tomdang.player.stats.presentation;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.tomdang.player.stats.PlayerStatCategory;

public record PlayerStatCategoryPresentation(PlayerStatCategory category, String displayName,
		TextColor color, String description, Material material, Material borderMaterial, int slot, boolean visible) {
	public PlayerStatCategoryPresentation {
		if (category == null) throw new IllegalArgumentException("category cannot be null");
		if (displayName == null || displayName.isBlank()) throw new IllegalArgumentException("displayName cannot be blank");
		if (color == null) throw new IllegalArgumentException("color cannot be null");
		if (description == null || description.isBlank()) throw new IllegalArgumentException("description cannot be blank");
		if (material == null || borderMaterial == null) throw new IllegalArgumentException("materials cannot be null");
		if (slot < 0) throw new IllegalArgumentException("slot cannot be negative");
	}
}
