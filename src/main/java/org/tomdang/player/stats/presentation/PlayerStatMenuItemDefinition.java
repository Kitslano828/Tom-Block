package org.tomdang.player.stats.presentation;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;

import java.util.List;
import java.util.Objects;

public record PlayerStatMenuItemDefinition(
		Material material,
		Component displayName,
		List<Component> lore
) {
	public PlayerStatMenuItemDefinition {
		if (material == null) throw new IllegalArgumentException("material cannot be null");
		if (displayName == null) throw new IllegalArgumentException("displayName cannot be null");
		if (lore == null) throw new IllegalArgumentException("lore cannot be null");
		if (lore.stream().anyMatch(Objects::isNull)) {
			throw new IllegalArgumentException("lore cannot contain null elements");
		}
		lore = List.copyOf(lore);
	}
}
