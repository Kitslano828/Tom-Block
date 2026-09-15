package org.tomdang.player.stats.presentation;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.tomdang.player.stats.PlayerStatType;

public record PlayerStatPresentation(
		PlayerStatType statType,
		String symbol,
		TextColor color,
		String description,
		Material menuMaterial,
		boolean visible
) {
	public PlayerStatPresentation {
		if (statType == null) throw new IllegalArgumentException("statType cannot be null");
		if (symbol == null || symbol.isBlank()) throw new IllegalArgumentException("symbol cannot be null or blank");
		if (color == null) throw new IllegalArgumentException("color cannot be null");
		if (description == null || description.isBlank()) {
			throw new IllegalArgumentException("description cannot be null or blank");
		}
		if (menuMaterial == null) throw new IllegalArgumentException("menuMaterial cannot be null");
		if (isAir(menuMaterial)) throw new IllegalArgumentException("menuMaterial cannot be air");
	}

	private static boolean isAir(Material material) {
		return material == Material.AIR || material == Material.CAVE_AIR || material == Material.VOID_AIR;
	}
}
