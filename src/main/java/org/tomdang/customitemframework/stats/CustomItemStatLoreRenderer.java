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

public class CustomItemStatLoreRenderer {
	private final PlayerStatPresentationRegistry presentations;

	public CustomItemStatLoreRenderer() {
		this.presentations = null;
	}

	public CustomItemStatLoreRenderer(PlayerStatPresentationRegistry presentations) {
		if (presentations == null) throw new IllegalArgumentException("presentations cannot be null");
		this.presentations = presentations;
	}

	public List<Component> render(CustomItemStatModifiers statModifiers) {
		if (statModifiers == null) throw new IllegalArgumentException("statModifiers cannot be null");

		List<Component> lore = new ArrayList<>();
		for (Map.Entry<PlayerStatType, Double> entry : statModifiers.asMap().entrySet()) {
			Component line = Component.text(entry.getKey().getDisplayName() + ": ", NamedTextColor.GRAY)
					.append(Component.text(PlayerStatValueFormatter.format(entry.getValue()), valueColor(entry.getKey())))
					.decoration(TextDecoration.ITALIC, false);
			lore.add(line);
		}
		return List.copyOf(lore);
	}

	private net.kyori.adventure.text.format.TextColor valueColor(PlayerStatType statType) {
		return presentations == null ? NamedTextColor.GOLD : presentations.get(statType).color();
	}
}
