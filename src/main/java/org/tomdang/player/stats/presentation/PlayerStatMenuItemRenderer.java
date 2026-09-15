package org.tomdang.player.stats.presentation;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.tomdang.player.stats.PlayerStatValueFormatter;
import org.tomdang.player.stats.evaluation.PlayerStatBreakdown;

import java.util.List;

public class PlayerStatMenuItemRenderer {

	public PlayerStatMenuItemDefinition render(
			PlayerStatPresentation presentation,
			PlayerStatBreakdown breakdown
	) {
		if (presentation == null) throw new IllegalArgumentException("presentation cannot be null");
		if (breakdown == null) throw new IllegalArgumentException("breakdown cannot be null");
		if (presentation.statType() != breakdown.statType()) {
			throw new IllegalArgumentException("presentation and breakdown must describe the same stat");
		}

		String formattedValue = PlayerStatValueFormatter.format(breakdown.effectiveValue());
		Component displayName = Component.text(
				presentation.symbol() + " " + presentation.statType().getDisplayName() + " " + formattedValue,
				presentation.color()
		).decoration(TextDecoration.ITALIC, false);

		List<Component> lore = List.of(
				Component.text(presentation.description(), NamedTextColor.GRAY)
						.decoration(TextDecoration.ITALIC, false),
				Component.empty(),
				Component.text("Click to view!", NamedTextColor.YELLOW)
						.decoration(TextDecoration.ITALIC, false)
		);

		return new PlayerStatMenuItemDefinition(
				presentation.menuMaterial(),
				displayName,
				lore
		);
	}
}
