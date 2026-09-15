package org.tomdang.player.stats.presentation;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.tomdang.player.stats.PlayerStatCategory;

import java.util.List;
import java.util.Objects;

public record PlayerStatsOverviewConfiguration(String title, int size,
		Material closeMaterial, String closeName, TextColor closeColor, int closeSlot,
		List<PlayerStatCategoryPresentation> categories) {
	public PlayerStatsOverviewConfiguration {
		if (title == null || title.isBlank()) throw new IllegalArgumentException("title cannot be blank");
		if (size != 54) throw new IllegalArgumentException("stats menus must contain 54 slots");
		if (closeMaterial == null) throw new IllegalArgumentException("closeMaterial cannot be null");
		if (closeName == null || closeName.isBlank()) throw new IllegalArgumentException("closeName cannot be blank");
		if (closeColor == null) throw new IllegalArgumentException("closeColor cannot be null");
		if (closeSlot < 0 || closeSlot >= size) throw new IllegalArgumentException("closeSlot is outside the inventory");
		if (categories == null || categories.stream().anyMatch(Objects::isNull)) throw new IllegalArgumentException("categories cannot be null or contain null");
		categories = List.copyOf(categories);
	}

	public PlayerStatCategoryPresentation category(PlayerStatCategory category) {
		if (category == null) throw new IllegalArgumentException("category cannot be null");
		return categories.stream().filter(entry -> entry.category() == category).findFirst()
				.orElseThrow(() -> new IllegalStateException("No presentation configured for " + category));
	}
}
