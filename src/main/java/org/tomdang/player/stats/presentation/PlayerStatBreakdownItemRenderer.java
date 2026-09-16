package org.tomdang.player.stats.presentation;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.tomdang.player.stats.PlayerStatValueFormatter;
import org.tomdang.player.stats.evaluation.PlayerStatBreakdown;
import org.tomdang.player.stats.evaluation.PlayerStatContribution;
import java.util.ArrayList;
import java.util.List;

public class PlayerStatBreakdownItemRenderer {
	public PlayerStatMenuItemDefinition renderSummary(PlayerStatPresentation presentation, PlayerStatBreakdown breakdown) {
		if (presentation == null || breakdown == null || presentation.statType() != breakdown.statType()) throw new IllegalArgumentException("summary inputs must describe the same stat");
		double additional = breakdown.rawValue() - breakdown.baseValue();
		List<Component> lore = new ArrayList<>();
		lore.add(line("Base: ", breakdown.baseValue()));
		lore.add(line("Contributions: ", additional));
		lore.add(Component.empty());
		if (breakdown.capped()) {
			lore.add(line("Uncapped: ", breakdown.rawValue()));
		}
		if (breakdown.cap().isPresent()) {
			if (breakdown.locationCap().isPresent()) {
				lore.add(Component.text("Location Cap (" + breakdown.locationCap().orElseThrow().locationId() + "): ", NamedTextColor.AQUA)
						.append(Component.text(PlayerStatValueFormatter.format(breakdown.cap().getAsDouble()), NamedTextColor.GOLD))
						.decoration(TextDecoration.ITALIC, false));
				lore.add(Component.text("Overrides other stat caps here.", NamedTextColor.GRAY)
						.decoration(TextDecoration.ITALIC, false));
			} else lore.add(capLine(breakdown.cap().getAsDouble()));
		}
		lore.add(effectiveLine(breakdown));
		return new PlayerStatMenuItemDefinition(presentation.menuMaterial(),
				Component.text(presentation.symbol()+" "+presentation.statType().getDisplayName()+" "+PlayerStatValueFormatter.format(breakdown.effectiveValue()), presentation.color()).decoration(TextDecoration.ITALIC,false),
				List.copyOf(lore));
	}

	public PlayerStatMenuItemDefinition renderContribution(PlayerStatContribution contribution, PlayerStatContributionPresentation presentation) {
		if (contribution == null || presentation == null || contribution.source() != presentation.source()) throw new IllegalArgumentException("contribution inputs must describe the same source");
		NamedTextColor amountColor = contribution.amount() >= 0 ? NamedTextColor.GREEN : NamedTextColor.RED;
		String amount = (contribution.amount() >= 0 ? "+" : "") + PlayerStatValueFormatter.format(contribution.amount());
		return new PlayerStatMenuItemDefinition(presentation.material(),
				Component.text(contribution.displayName(), presentation.color()).decoration(TextDecoration.ITALIC,false),
				List.of(Component.text("Source: ", NamedTextColor.GRAY).append(Component.text(presentation.displayName(), presentation.color())).decoration(TextDecoration.ITALIC,false),
						Component.text("Amount: ", NamedTextColor.GRAY).append(Component.text(amount, amountColor)).decoration(TextDecoration.ITALIC,false)));
	}

	private Component line(String label, double value) {
		NamedTextColor color = value < 0 ? NamedTextColor.RED : NamedTextColor.WHITE;
		String prefix = label.equals("Contributions: ") && value > 0 ? "+" : "";
		return Component.text(label, NamedTextColor.GRAY).append(Component.text(prefix + PlayerStatValueFormatter.format(value), color)).decoration(TextDecoration.ITALIC,false);
	}

	private Component capLine(double cap) {
		return Component.text("Stat Cap: ", NamedTextColor.GRAY)
				.append(Component.text(PlayerStatValueFormatter.format(cap), NamedTextColor.GOLD))
				.decoration(TextDecoration.ITALIC, false);
	}

	private Component effectiveLine(PlayerStatBreakdown breakdown) {
		Component line = Component.text("Effective: ", NamedTextColor.GRAY)
				.append(Component.text(PlayerStatValueFormatter.format(breakdown.effectiveValue()), NamedTextColor.WHITE));
		if (breakdown.capped()) {
			line = line.append(Component.text(" (CAPPED)", NamedTextColor.RED));
		}
		return line.decoration(TextDecoration.ITALIC, false);
	}
}
