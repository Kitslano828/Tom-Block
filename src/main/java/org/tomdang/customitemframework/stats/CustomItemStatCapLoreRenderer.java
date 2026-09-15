package org.tomdang.customitemframework.stats;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.tomdang.player.stats.PlayerStatType;
import org.tomdang.player.stats.PlayerStatValueFormatter;
import org.tomdang.player.stats.presentation.PlayerStatPresentationRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomItemStatCapLoreRenderer {
	private final PlayerStatPresentationRegistry presentations;

	public CustomItemStatCapLoreRenderer() {
		this.presentations = null;
	}

	public CustomItemStatCapLoreRenderer(PlayerStatPresentationRegistry presentations) {
		if (presentations == null) throw new IllegalArgumentException("presentations cannot be null");
		this.presentations = presentations;
	}

	public List<Component> render(CustomItemStatCapModifiers modifiers) {
		if (modifiers == null) throw new IllegalArgumentException("modifiers cannot be null");
		List<Component> lore = new ArrayList<>();
		for (Map.Entry<PlayerStatType, Double> entry : modifiers.asMap().entrySet()) {
			double amount = entry.getValue();
			String prefix = amount >= 0 ? "+" : "";
			lore.add(Component.text("Grants ", NamedTextColor.GRAY)
					.append(Component.text(prefix + PlayerStatValueFormatter.format(amount), valueColor(entry.getKey())))
					.append(Component.text(" " + entry.getKey().getDisplayName() + " Cap.", valueColor(entry.getKey())))
					.decoration(TextDecoration.ITALIC, false));
		}
		return List.copyOf(lore);
	}

	private net.kyori.adventure.text.format.TextColor valueColor(PlayerStatType statType) {
		if (presentations != null) return presentations.get(statType).color();
		return NamedTextColor.GREEN;
	}
}
