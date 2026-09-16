package org.tomdang.player.stats.presentation;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.tomdang.player.stats.PlayerStatValueFormatter;
import org.tomdang.player.stats.evaluation.PlayerStatBreakdown;

import java.util.ArrayList;
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

		List<Component> lore = new ArrayList<>();
		lore.add(Component.text(presentation.description(), NamedTextColor.GRAY)
				.decoration(TextDecoration.ITALIC, false));
		if (breakdown.cap().isPresent()) {
			lore.add(Component.empty());
			if (breakdown.locationCap().isPresent()) {
				lore.add(Component.text("Location Cap (" + breakdown.locationCap().orElseThrow().locationId() + "): ", NamedTextColor.AQUA)
						.append(Component.text(PlayerStatValueFormatter.format(breakdown.cap().getAsDouble()), NamedTextColor.GOLD))
						.decoration(TextDecoration.ITALIC, false));
				lore.add(Component.text("Overrides other stat caps here.", NamedTextColor.GRAY)
						.decoration(TextDecoration.ITALIC, false));
			} else lore.add(Component.text("Stat Cap: ", NamedTextColor.GRAY)
					.append(Component.text(
							PlayerStatValueFormatter.format(breakdown.cap().getAsDouble()),
							NamedTextColor.GOLD
					))
					.decoration(TextDecoration.ITALIC, false));
			if (breakdown.capped()) {
				lore.add(Component.text("Uncapped Value: ", NamedTextColor.GRAY)
						.append(Component.text(
								PlayerStatValueFormatter.format(breakdown.rawValue()),
								NamedTextColor.RED
						))
						.decoration(TextDecoration.ITALIC, false));
				lore.add(Component.text("CAPPED", NamedTextColor.RED)
						.decoration(TextDecoration.BOLD, true)
						.decoration(TextDecoration.ITALIC, false));
			}
		}
		lore.add(Component.empty());
		lore.add(Component.text("Click to view!", NamedTextColor.YELLOW)
				.decoration(TextDecoration.ITALIC, false));

		return new PlayerStatMenuItemDefinition(
				presentation.menuMaterial(),
				displayName,
				List.copyOf(lore)
		);
	}
}
