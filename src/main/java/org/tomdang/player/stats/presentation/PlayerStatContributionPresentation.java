package org.tomdang.player.stats.presentation;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.tomdang.player.stats.evaluation.PlayerStatContributionSource;

public record PlayerStatContributionPresentation(PlayerStatContributionSource source, String displayName,
		Material material, TextColor color) {
	public PlayerStatContributionPresentation {
		if (source == null || material == null || color == null) throw new IllegalArgumentException("contribution presentation fields cannot be null");
		if (displayName == null || displayName.isBlank()) throw new IllegalArgumentException("displayName cannot be blank");
	}
}
