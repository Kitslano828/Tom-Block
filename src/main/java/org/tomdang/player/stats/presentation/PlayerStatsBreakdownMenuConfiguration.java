package org.tomdang.player.stats.presentation;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.tomdang.player.stats.evaluation.PlayerStatContributionSource;
import java.util.Map;

public record PlayerStatsBreakdownMenuConfiguration(String titleFormat, int size, int summarySlot,
		Material emptyMaterial, String emptyName, TextColor emptyColor,
		Material backMaterial, String backName, TextColor backColor, int backSlot,
		Material closeMaterial, String closeName, TextColor closeColor, int closeSlot,
		Map<PlayerStatContributionSource, PlayerStatContributionPresentation> sources) {
	public PlayerStatsBreakdownMenuConfiguration {
		if (titleFormat == null || !titleFormat.contains("{stat}")) throw new IllegalArgumentException("titleFormat must contain {stat}");
		if (size != 54) throw new IllegalArgumentException("stats menus must contain 54 slots");
		if (summarySlot < 0 || summarySlot >= size || backSlot < 0 || backSlot >= size || closeSlot < 0 || closeSlot >= size) throw new IllegalArgumentException("menu slot is outside the inventory");
		if (backSlot == closeSlot || summarySlot == backSlot || summarySlot == closeSlot) throw new IllegalArgumentException("fixed menu slots cannot overlap");
		if (emptyMaterial == null || emptyColor == null || backMaterial == null || backColor == null || closeMaterial == null || closeColor == null) throw new IllegalArgumentException("menu presentation cannot contain null fields");
		if (emptyName == null || emptyName.isBlank() || backName == null || backName.isBlank() || closeName == null || closeName.isBlank()) throw new IllegalArgumentException("menu names cannot be blank");
		if (sources == null || sources.size() != PlayerStatContributionSource.values().length) throw new IllegalArgumentException("every contribution source requires a presentation");
		sources = Map.copyOf(sources);
	}
	public String titleFor(String statName) { return titleFormat.replace("{stat}", statName); }
	public PlayerStatContributionPresentation source(PlayerStatContributionSource source) { return sources.get(source); }
}
