package org.tomdang.player.stats.presentation;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.tomdang.player.stats.PlayerStatValueFormatter;
import org.tomdang.player.stats.evaluation.PlayerStatEvaluation;
import java.util.ArrayList;
import java.util.List;

public class PlayerStatCategoryMenuItemRenderer {
	public PlayerStatMenuItemDefinition render(PlayerStatCategoryPresentation category,
			PlayerStatPresentationRegistry registry, PlayerStatEvaluation evaluation) {
		if (category == null || registry == null || evaluation == null) throw new IllegalArgumentException("renderer inputs cannot be null");
		List<Component> lore = new ArrayList<>();
		lore.add(Component.text(category.description(), NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
		lore.add(Component.empty());
		List<PlayerStatPresentation> stats = registry.getVisibleByCategory(category.category());
		if (stats.isEmpty()) lore.add(Component.text("No stats available yet.", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false));
		for (PlayerStatPresentation stat : stats) {
			double value = evaluation.getBreakdown(stat.statType()).effectiveValue();
			lore.add(Component.text(stat.symbol() + " " + stat.statType().getDisplayName() + ": ", stat.color())
					.append(Component.text(PlayerStatValueFormatter.format(value), NamedTextColor.WHITE)).decoration(TextDecoration.ITALIC, false));
		}
		lore.add(Component.empty());
		lore.add(Component.text("Click to view!", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
		return new PlayerStatMenuItemDefinition(category.material(),
				Component.text(category.displayName(), category.color()).decoration(TextDecoration.BOLD, true).decoration(TextDecoration.ITALIC, false), lore);
	}
}
