package org.tomdang.player.stats.presentation;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import org.tomdang.player.stats.PlayerStatType;
import org.tomdang.player.stats.evaluation.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PlayerStatBreakdownItemRendererTest {
	private final PlayerStatBreakdownItemRenderer renderer = new PlayerStatBreakdownItemRenderer();

	@Test void rendersSummaryAndContribution() {
		PlayerStatPresentation stat = new PlayerStatPresentation(PlayerStatType.MAX_HEALTH, "❤", TextColor.color(0xFF5555), "Health", Material.RED_DYE, true);
		PlayerStatBreakdown breakdown = new PlayerStatBreakdown(PlayerStatType.MAX_HEALTH, 100, List.of(), 400);
		PlayerStatContribution contribution = new PlayerStatContribution(PlayerStatType.MAX_HEALTH, PlayerStatContributionSource.ARMOR, "rabbit-boots", "Rabbit Boots", 300);
		PlayerStatContributionPresentation source = new PlayerStatContributionPresentation(PlayerStatContributionSource.ARMOR, "Armor", Material.IRON_CHESTPLATE, TextColor.color(0x55FFFF));

		assertAll(
				() -> assertEquals(Material.RED_DYE, renderer.renderSummary(stat, breakdown).material()),
				() -> assertEquals(4, renderer.renderSummary(stat, breakdown).lore().size()),
				() -> assertEquals(Material.IRON_CHESTPLATE, renderer.renderContribution(contribution, source).material()),
				() -> assertEquals(2, renderer.renderContribution(contribution, source).lore().size())
		);
	}

	@Test void rejectsMismatchedInputs() {
		PlayerStatContribution contribution = new PlayerStatContribution(PlayerStatType.MAX_HEALTH, PlayerStatContributionSource.ARMOR, "id", "Armor", 1);
		PlayerStatContributionPresentation source = new PlayerStatContributionPresentation(PlayerStatContributionSource.HELD_ITEM, "Held", Material.STICK, TextColor.color(0xFFFFFF));
		assertThrows(IllegalArgumentException.class, () -> renderer.renderContribution(contribution, source));
	}
}
