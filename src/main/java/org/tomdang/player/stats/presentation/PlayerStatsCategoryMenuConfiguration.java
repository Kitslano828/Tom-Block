package org.tomdang.player.stats.presentation;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;

public record PlayerStatsCategoryMenuConfiguration(
		String titleFormat,
		int size,
		Material backMaterial,
		String backName,
		TextColor backColor,
		int backSlot,
		Material closeMaterial,
		String closeName,
		TextColor closeColor,
		int closeSlot
) {
	public PlayerStatsCategoryMenuConfiguration {
		if (titleFormat == null || titleFormat.isBlank()) throw new IllegalArgumentException("titleFormat cannot be blank");
		if (!titleFormat.contains("{category}")) throw new IllegalArgumentException("titleFormat must contain {category}");
		if (size != 54) throw new IllegalArgumentException("stats menus must contain 54 slots");
		if (backMaterial == null || closeMaterial == null) throw new IllegalArgumentException("navigation materials cannot be null");
		if (backName == null || backName.isBlank() || closeName == null || closeName.isBlank()) throw new IllegalArgumentException("navigation names cannot be blank");
		if (backColor == null || closeColor == null) throw new IllegalArgumentException("navigation colors cannot be null");
		if (backSlot < 0 || backSlot >= size || closeSlot < 0 || closeSlot >= size) throw new IllegalArgumentException("navigation slot is outside the inventory");
		if (backSlot == closeSlot) throw new IllegalArgumentException("back and close slots cannot overlap");
	}

	public String titleFor(String categoryName) {
		if (categoryName == null || categoryName.isBlank()) throw new IllegalArgumentException("categoryName cannot be blank");
		return titleFormat.replace("{category}", categoryName);
	}
}
